package edu.uci.ece.zen.orb.transport.serial;

import java.util.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SerialPortManager
{
    private static final SerialPortManager instance = new SerialPortManager();

    // Connections that have been established
    private List connections = new LinkedList();

    // Connections that are waiting for a host to contact them
    private List listeningConnections = new LinkedList();

    // Connections that are trying to connect to a remote host
    private List requestedConnections = new LinkedList();

    private SerialPort serialPort;
    private SerialPortListener serialPortListener;

    private SerialPortManager()
    {
        try
        {
            serialPort = SerialPortFactory.createRMISerialPort();
        }
        catch (IOException e)
        {
            edu.uci.ece.zen.utils.ZenProperties.logger.log(
                edu.uci.ece.zen.utils.Logger.WARN, getClass(), "<init>", e);
        }

        serialPortListener = new SerialPortListener();
        serialPortListener.start();
    }

    public static SerialPortManager instance()
    {
        return instance;
    }

    void addListeningConnection(SerialPortConnection connection)
    {
        // Synchronize on the connection list so that we don't modify it while we are
        // iterating over it
        synchronized (listeningConnections)
        {
            listeningConnections.add(connection);
        }
    }

    // Tries to connect to local socket if it exists, otherwise tries to connect to remote socket
    // fromLocalHost is true if the connect request is coming from a socket on the local host, false
    //   if the request came from a socket elsewhere.
    SerialPortConnection connect(int port, String host, Socket socket, boolean fromLocalHost)
        throws UnknownHostException, IOException
    {
        SerialPortConnection requestedConnection = new SerialPortConnection(port, host);
System.out.println("SerialPortManager: connect: trying to connect socket to " + host + ":" + port);
        // Synchronize on the connection list so that it doesn't get modified while we are
        // iterating over it
        synchronized (listeningConnections)
        {
            // Search for a listening local connection...
            for (Iterator i = listeningConnections.iterator(); i.hasNext(); )
            {
                SerialPortConnection listeningConnection = (SerialPortConnection) i.next();

                // If the listening connection and the requested connection match...
                if (listeningConnection.equals(requestedConnection))
                {
                    i.remove();
                    connections.add(listeningConnection);
System.out.println("SerialPortManager: connect: found listener for " + host + ":" + port + ", connecting to socket...");
                    listeningConnection.connect(socket, fromLocalHost, serialPort);

                    return listeningConnection;
                }
            }
        }
System.out.println("SerialPortManager: connect: trying to connect socket to " + host + ":" + port + " failed - NO LISTENERS WERE FOUND");

        // We didn't find any local sockets to connect to, so if this is a local socket requesting
        // a connection, ask the other end if it has any listening sockets...
        if (fromLocalHost)
        {
            try
            {
System.out.println("SerialPortManager: connect: trying to satisfy connection request by contacting other host...");
                // Tell the connection what socket it's for so that the serial port listener knows about it
                requestedConnection.setSocket(socket);

                requestedConnections.add(requestedConnection);

                serialPort.sendMessage(SerialPortProtocol.encodeConnectionRequested(requestedConnection));

System.out.println("SerialPortManager: connect: connection request sent to other host, waiting for reply...");

                requestedConnection.waitForConnection();

                return requestedConnection;
            }
            catch (InterruptedException e)
            {
                throw new IOException("The local SerialPortManager was interrupted while waiting for a response from the remote SerialPortManager");
            }
        }

        throw new UnknownHostException("The requested host (" + requestedConnection + ") could not be found, or it had no sockets open for connections.");
    }

    private class SerialPortListener extends Thread
    {
        private static final int LISTEN_INTERVAL = 5000;

        public void run()
        {
            while (true)
            {
                // Check for incoming connections...

                try
                {
                    sleep(LISTEN_INTERVAL);

                    byte[] message = serialPort.getMessage();

                    if (message != null)
                    {
                        Socket socket;
                        SerialPortConnection connection, requestedConnection;

                        switch (SerialPortProtocol.getMessageType(message))
                        {
                            case SerialPortProtocol.CONNECTION_REQUESTED:
                                requestedConnection = SerialPortProtocol.decodeConnectionRequested(message);

                                socket = new Socket();
                                if (connect(requestedConnection.getPort(),
                                            requestedConnection.getAddress().getHostName(),
                                            socket,
                                            false) != null)
                                {
                                    serialPort.sendMessage(
                                        SerialPortProtocol.encodeConnectionAccepted(requestedConnection, socket));
                                }
                                else
                                {
                                    serialPort.sendMessage(
                                        SerialPortProtocol.encodeConnectionDenied(requestedConnection, socket));
                                }

                                break;

                            case SerialPortProtocol.CONNECTION_ACCEPTED:
                                SerialPortConnection acceptedConnection = new SerialPortConnection();
                                byte socketID = SerialPortProtocol.decodeConnectionAccepted(message, acceptedConnection);
                                boolean connectionFound = false;

                                // Walk through the list of requested connections and find the one that was accepted
                                for (Iterator i = requestedConnections.iterator(); i.hasNext(); )
                                {
                                    requestedConnection = (SerialPortConnection) i.next();
                                    if (requestedConnection.equals(acceptedConnection))
                                    {
                                        // The connection has been accepted, so move it to the connected list
                                        i.remove();
                                        connections.add(requestedConnection);

                                        requestedConnection.getSocket().setID(socketID);

                                        requestedConnection.connect(requestedConnection.getSocket(), false, serialPort);

                                        connectionFound = true;
                                        break;
                                    }
                                }

                                if (!connectionFound)
                                {
                                    edu.uci.ece.zen.utils.ZenProperties.logger.log(
                                        edu.uci.ece.zen.utils.Logger.WARN,
                                        getClass(), "run",
                                        "Received response from remote host to connection request. Request was accepted, but the host address in the reply (" + acceptedConnection + ") is unknown.");
                                }
                                break;

                            case SerialPortProtocol.CONNECTION_DENIED:
                                connection = SerialPortProtocol.decodeConnectionDenied(message);
                                edu.uci.ece.zen.utils.ZenProperties.logger.log(
                                    edu.uci.ece.zen.utils.Logger.WARN,
                                    getClass(), "run",
                                    "Received response from remote host to connection request. Request was denied because host " + connection + " is unknown.");
                                break;

                            case SerialPortProtocol.SOCKET_DATA:
                                byte id = SerialPortProtocol.getSocketID(message);
                                boolean foundSocket = false;

                                // Find the connection that matches the socket ID we received
                                for (Iterator i = connections.iterator(); i.hasNext(); )
                                {
                                    connection = (SerialPortConnection) i.next();
                                    if (connection.getSocket().getID() == id)
                                    {
                                        foundSocket = true;

                                        RemoteSerialPortStream remoteStream =
                                            (RemoteSerialPortStream) connection.getStream();
                                        SerialPortProtocol.decodeSocketData(message, remoteStream.inputBuffer);
                                        break;
                                    }
                                }

                                if (!foundSocket)
                                {
                                    edu.uci.ece.zen.utils.ZenProperties.logger.log(
                                        edu.uci.ece.zen.utils.Logger.WARN,
                                        getClass(), "run",
                                        "Received socket data from remote host, but the socket ID (" + id + ") does not match any existing socket connection.");
                                }

                                break;
                        }
                    }
                }
                catch (UnknownHostException e)
                {
                    edu.uci.ece.zen.utils.ZenProperties.logger.log(
                        edu.uci.ece.zen.utils.Logger.WARN,
                        getClass(), "run", "Received connection request from serial port, but host is unknown", e);
                }
                catch (IOException e)
                {
                    edu.uci.ece.zen.utils.ZenProperties.logger.log(
                        edu.uci.ece.zen.utils.Logger.WARN,
                        getClass(), "run", "Could not read from serial port, trying again", e);
                }
                catch (InterruptedException e)
                {
                    edu.uci.ece.zen.utils.ZenProperties.logger.log(
                        edu.uci.ece.zen.utils.Logger.WARN,
                        getClass(), "run", "SerialPortListener was interrupted while sleeping or while waiting for data to become available", e);
                }
            }
        }
    }
}


class SerialPortConnection
{
    private int port;
    private InetAddress address;
    private SerialPortStream stream;
    private Socket socket;

    SerialPortConnection()
    {
        // No implementation; use empty values
    }

    SerialPortConnection(int port, InetAddress address)
    {
        this.port = port;
        this.address = address;
    }

    SerialPortConnection(int port, String host) throws UnknownHostException
    {
        this.port = port;
        address = InetAddress.getByName(host);
    }

    // This function needs to be synchronized for wait/notify
    synchronized void connect(Socket socket, boolean local, SerialPort serialPort)
    {
        this.socket = socket;
        socket.setConnection(this);

        if (socket.getID() == 0)
        {
            socket.setID(Socket.NEXT_AVAILABLE_ID);
        }
System.out.println("SerialPortManager: making new " + (local?"local":"remote") + " connection with " + this + ", socketID=" + socket.getID());
        if (local)
        {
            stream = new LocalSerialPortStream();
        }
        else
        {
            stream = new RemoteSerialPortStream(socket, serialPort);
        }

        notify();
    }

    // This function needs to be synchronized for wait/notify
    synchronized Socket waitForConnection() throws InterruptedException
    {
        SerialPortManager.instance().addListeningConnection(this);

        wait();

        return socket;
    }

    int getPort()
    {
        return port;
    }

    void setPort(int port)
    {
        this.port = port;
    }

    InetAddress getAddress()
    {
        return address;
    }

    void setAddress(InetAddress address)
    {
        this.address = address;
    }

    SerialPortStream getStream()
    {
        return stream;
    }

    void setStream(SerialPortStream stream)
    {
        this.stream = stream;
    }

    Socket getSocket()
    {
        return socket;
    }

    void setSocket(Socket socket)
    {
        this.socket = socket;
    }

    public boolean equals(Object o)
    {
        if (o instanceof SerialPortConnection)
        {
            SerialPortConnection c = (SerialPortConnection) o;
            return c.port == port && c.address.equals(address);
        }

        return false;
    }

    public String toString()
    {
        return address + ":" + port;
    }
}

interface SerialPortStream
{
    InputStream getInputStream();
    OutputStream getOutputStream();
}

class LocalSerialPortStream implements SerialPortStream
{
    private edu.oswego.cs.dl.util.concurrent.BoundedBuffer queue;
    private IS inputStream;
    private OS outputStream;

    LocalSerialPortStream()
    {
        // FIXME: How do we choose a good capacity?
        queue = new edu.oswego.cs.dl.util.concurrent.BoundedBuffer(1024);
        inputStream = new IS();
        outputStream = new OS();
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    class IS extends InputStream
    {
        // FIXME: When (if ever) must we return -1 here?
        public int read() throws IOException
        {
            try
            {
System.out.println("Local input stream: trying to remove byte from queue (may block)");
                Integer n = (Integer) queue.take();
System.out.println("Local input stream: removed byte from queue: " + Integer.toHexString(n.intValue() & 0xFF) + ", available=" + available());
                return n.intValue() & 0xFF;
            }
            catch (InterruptedException e)
            {
                IOException ioex = new IOException();
                ioex.initCause(e);
                throw ioex;
            }
        }

        public int available() throws IOException
        {
            return queue.size();
        }
    }

    class OS extends OutputStream
    {
        public void write(int b) throws IOException
        {
            try
            {
System.out.println("Local output stream: writing " + Integer.toHexString(b & 0xFF) + ", buffer size = " + queue.size());
                queue.put(new Integer(b));
            }
            catch (InterruptedException e)
            {
                IOException ioex = new IOException();
                ioex.initCause(e);
                throw ioex;
            }
        }
    }
}

class RemoteSerialPortStream implements SerialPortStream
{
    private IS inputStream;
    private OS outputStream;
    private SerialPort serialPort;
    private Socket socket;

    edu.oswego.cs.dl.util.concurrent.BoundedBuffer inputBuffer; // Not private so the serial port listener can access it

    RemoteSerialPortStream(Socket socket, SerialPort serialPort)
    {
        this.socket = socket;
        this.serialPort = serialPort;

        inputBuffer = new edu.oswego.cs.dl.util.concurrent.BoundedBuffer(1024);

        inputStream = new IS();
        outputStream = new OS();
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    class IS extends InputStream
    {
        // FIXME: When (if ever) must we return -1 here?
        public int read() throws IOException
        {
            try
            {
System.out.println("Remote input stream: trying to remove byte from queue (may block)");
                Integer n = (Integer) inputBuffer.take();
System.out.println("Remote input stream: removed byte from queue: " + Integer.toHexString(n.intValue() & 0xFF) + ", available=" + available());
                return n.intValue() & 0xFF;
            }
            catch (InterruptedException e)
            {
                IOException ioex = new IOException();
                ioex.initCause(e);
                throw ioex;
            }
        }

        public int read(byte[] b, int off, int len) throws IOException
        {
            int bytesRead = super.read(b, off, len);
            return bytesRead;
        }

        public int available() throws IOException
        {
            return inputBuffer.size();
        }
    }

    class OS extends OutputStream
    {
        private List outputBuffer;

        OS()
        {
            outputBuffer = new LinkedList();
        }

        public void write(int b) throws IOException
        {
System.out.println("Remote output stream: writing " + Integer.toHexString(b&0xFF) + ", buffer size = " + outputBuffer.size());

            outputBuffer.add(new Integer(b & 0xFF));
        }

        public void write(byte b[], int off, int len) throws IOException
        {
            super.write(b, off, len);
System.out.println("Remote output stream: flushing " + outputBuffer.size() + " bytes");
            flush();
        }

        public void flush() throws IOException
        {
            serialPort.sendMessage(SerialPortProtocol.encodeSocketData(socket, outputBuffer));
            outputBuffer.clear();
        }
    }
}
