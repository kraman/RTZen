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
        System.out.println("SerialPortManager: adding listening connection...");
        synchronized (listeningConnections)
        {
            listeningConnections.add(connection);
        }
        System.out.println("SerialPortManager: adding listening connection. waiting=" + listeningConnections.size());
    }

    // Tries to connect to local socket if it exists, otherwise tries to connect to remote socket
    // fromLocalHost is true if the connect request is coming from a socket on the local host, false
    //   if the request came from a socket elsewhere.
    SerialPortConnection connect(int port, String host, Socket socket, boolean fromLocalHost)
        throws UnknownHostException, IOException
    {
        SerialPortConnection requestedConnection = new SerialPortConnection(port, host);

        // Synchronize on the connection list so that it doesn't get modified while we are
        // iterating over it
        synchronized (listeningConnections)
        {
            System.out.println("SerialPortManager: searching for local listening connections, listening count=" + listeningConnections.size() + "...");
            // Search for a listening local connection...
            for (Iterator i = listeningConnections.iterator(); i.hasNext(); )
            {
                SerialPortConnection listeningConnection = (SerialPortConnection) i.next();
System.out.println("SerialPortManager: checking listening connection: " + listeningConnection);
                // If the listening connection and the requested connection match...
                if (listeningConnection.equals(requestedConnection))
                {
                    System.out.println("SerialPortManager: found listening connection match with requested connection!");

                    i.remove();
                    connections.add(listeningConnection);

                    listeningConnection.connect(socket, fromLocalHost, serialPort);
                    System.out.println("SerialPortManager: returning connection to socket, connection=" + listeningConnection);
                    return listeningConnection;
                }
            }
        }

        // We didn't find any local sockets to connect to, so if this is a local socket requesting
        // a connection, ask the other end if it has any listening sockets...
        if (fromLocalHost)
        {
            try
            {
                System.out.println("SerialPortManager: Didn't find a local connection in the waiting list, sending remote connection request for " + requestedConnection);

                // Tell the connection what socket it's for so that the serial port listener knows about it
                requestedConnection.setSocket(socket);

                requestedConnections.add(requestedConnection);

                serialPort.sendMessage(SerialPortProtocol.encodeConnectionRequested(requestedConnection));

                System.out.println("SerialPortManager: Sent remote connection request, now waiting for response");
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
        private static final int LISTEN_INTERVAL = 500;

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
                        System.out.println("SerialPortListener: got a message!");
                        Socket socket;
                        SerialPortConnection connection, requestedConnection;

                        switch (SerialPortProtocol.getMessageType(message))
                        {
                            case SerialPortProtocol.CONNECTION_REQUESTED:
                                System.out.println("SerialPortListener: it's a connection request");

                                requestedConnection = SerialPortProtocol.decodeConnectionRequested(message);

                                System.out.println("SerialPortListener: decoded connection is " + requestedConnection + ", looking for match...");

                                socket = new Socket();
                                if (connect(requestedConnection.getPort(),
                                            requestedConnection.getAddress().getHostName(),
                                            socket,
                                            false) != null)
                                {
                                    System.out.println("SerialPortListener: connection request was satisfied; a listening connection was found. Now returning an accept response with socket id=" + socket.getID());
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

                                System.out.println("SerialPortListener: got accept message from remote host!");

                                // Walk through the list of requested connections and find the one that was accepted
                                for (Iterator i = requestedConnections.iterator(); i.hasNext(); )
                                {
                                    requestedConnection = (SerialPortConnection) i.next();
                                    if (requestedConnection.equals(acceptedConnection))
                                    {
                                        // The connection has been accepted, so move it to the connected list
                                        i.remove();
                                        connections.add(requestedConnection);
System.out.println("SerialPortListener: CONNECTION_ACCEPTED: found match for connection reply, setting socket to server socket id " + socketID);
                                        requestedConnection.getSocket().setID(socketID);

                                        requestedConnection.connect(requestedConnection.getSocket(), false, serialPort);
//System.out.println("SerialPortListener: CONNECTION_ACCEPTED: completed connection for socket id " + socketID);

                                        connectionFound = true;
                                        break;
                                    }
                                }

                                if (!connectionFound)
                                {
System.out.println("SerialPortListener: received response from remote host to connection request, but the host address in the reply (" + acceptedConnection + ") is unknown");
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
                                System.out.println("SerialPortListener: it's socket data from socket #" + id);

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
System.out.println("SerialPortListener: found connection for socket data and decoded data into it.");
System.out.println("SerialPortListener: available data for socket " + id + " is now " + connection.getStream().getInputStream().available());
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
/*                    else
                    {
System.out.println("SerialPortListener: no message in buffer, will try again");
                    }
*/                }
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
                    // Should not happen. Ignore and continue.
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
        System.out.println("SerialPortConnection: connect called for " + (local ? "local" : "remote"));

        this.socket = socket;
        socket.setConnection(this);

        if (socket.getID() == 0)
        {
            socket.setID(Socket.NEXT_AVAILABLE_ID);
        }

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
        System.out.println("SerialPortConnection: waitForConnection called");
        SerialPortManager.instance().addListeningConnection(this);
        System.out.println("SerialPortConnection: waitForConnection added listening connection");
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
        System.out.println("SerialPortConnection: setting stream");
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
    private List queue;
    private IS inputStream;
    private OS outputStream;

    LocalSerialPortStream()
    {
        queue = new LinkedList();
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
        public int read() throws IOException
        {
            try
            {
                Integer n = (Integer) queue.remove(0);
                System.out.println("SerialPortStream: local stream reading " + n.toHexString(n.intValue()));
                return n.intValue();
            }
            catch (IndexOutOfBoundsException e)
            {
                throw new IOException("Input stream cannot be read; no data available");
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
            System.out.println("SerialPortStream: local stream writing " + Integer.toHexString(b));
            queue.add(new Integer(b));
        }
    }
}

class RemoteSerialPortStream implements SerialPortStream
{
    private IS inputStream;
    private OS outputStream;
    private SerialPort serialPort;
    private Socket socket;

    List inputBuffer;  // Not private so that the serial port listener can access it
    private List outputBuffer;

    RemoteSerialPortStream(Socket socket, SerialPort serialPort)
    {
        this.socket = socket;
        this.serialPort = serialPort;

        inputBuffer = new LinkedList();
        outputBuffer = new LinkedList();

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
        public int read() throws IOException
        {
            if (available() == 0)
            {
                return -1;
            }
            else
            {
                Integer n = (Integer) inputBuffer.remove(0);
                System.out.println("SerialPortStream: remote stream reading " + n.toHexString(n.intValue()) + ", " + available() + " bytes left in buffer");
                return n.intValue() & 0xFF;
            }
        }

        public int read(byte[] b, int off, int len) throws IOException
        {
            int bytesRead = super.read(b, off, len);
            System.out.println("SerialPortInputStream: read byte array, length=" + b.length + " off=" + off + " len=" + len + ", available()=" + available() + ", returning " + bytesRead);
            return bytesRead;
        }

        public int available() throws IOException
        {
            return inputBuffer.size();
        }
    }

    class OS extends OutputStream
    {
        public void write(int b) throws IOException
        {
            System.out.println("SerialPortStream: remote stream writing " + Integer.toHexString(b & 0xFF));
            outputBuffer.add(new Integer(b & 0xFF));
        }

        public void write(byte b[], int off, int len) throws IOException
        {
            super.write(b, off, len);
            flush();
        }

        public void flush() throws IOException
        {
            System.out.println("SerialPortStream: remote stream flushing " + outputBuffer.size() + " bytes!");
            serialPort.sendMessage(SerialPortProtocol.encodeSocketData(socket, outputBuffer));
            outputBuffer.clear();
        }
    }
}
