package edu.uci.ece.zen.orb.transport.serial;

import java.util.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
Manages the serial port by providing utility methods for getting and receiving
messages through the serial port. Sends out connection requests and listens for
responses to them.
*/
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

    private byte[] buffer = new byte[SerialPort.MAX_MESSAGE_LENGTH];

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

        // Synchronize on the connection list so that it doesn't get modified while we are
        // iterating over it
        synchronized (listeningConnections)
        {
            System.out.println("SerialPortManager: connect: trying to find a listening local connection to " + host);
            // Search for a listening local connection...
            for (Iterator i = listeningConnections.iterator(); i.hasNext(); )
            {
                SerialPortConnection listeningConnection = (SerialPortConnection) i.next();

                // If the listening connection and the requested connection match...
                if (listeningConnection.equals(requestedConnection))
                {
                    i.remove();
                    connections.add(listeningConnection);
                    System.out.println("SerialPortManager: connect: found local connection! binding...");

                    listeningConnection.connect(socket, fromLocalHost, serialPort);

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
                // Tell the connection what socket it's for so that the serial port listener knows about it
                requestedConnection.setSocket(socket);

                requestedConnections.add(requestedConnection);

                synchronized (buffer)
                {
                    int messageLength = SerialPortProtocol.encodeConnectionRequested(requestedConnection, buffer);
                    System.out.println("SerialPortManager: connect: couldn't find local listener, sending request to remote host...");
                    serialPort.sendMessage(buffer, messageLength);
                }

                System.out.println("SerialPortManager: connect: waiting for response from remote host...");
                requestedConnection.waitForConnection();
                System.out.println("SerialPortManager: connect: response from remote host received!");

                return requestedConnection;
            }
            catch (InterruptedException e)
            {
                throw new IOException("The local SerialPortManager was interrupted while waiting for a response from the remote SerialPortManager");
            }
        }

        System.out.println("SerialPortManager: connect: no connections found, returning...");

        // The requested host could not be found, or it had no sockets open for connections.
        return null;
    }

    private class SerialPortListener extends Thread
    {
        private byte[] message = new byte[SerialPort.MAX_MESSAGE_LENGTH];

        public void run()
        {
            while (true)
            {
                // Check for incoming connections...

                try
                {
                    // Block until a message is available
System.out.println("SerialPortListener: run: calling serialPort.getMessage...");
                    int messageLength = serialPort.getMessage(message);
System.out.println("SerialPortListener: run: got a message! " + messageLength + " bytes long");

                    if (message != null)
                    {
                        Socket socket;
                        SerialPortConnection connection, requestedConnection;

                        switch (SerialPortProtocol.getMessageType(message))
                        {
                            case SerialPortProtocol.CONNECTION_REQUESTED:
                                System.out.println("SerialPortListener: run: it's a connection request");
                                requestedConnection = SerialPortProtocol.decodeConnectionRequested(message, messageLength);

                                socket = new Socket();
                                if (connect(requestedConnection.getPort(),
                                            requestedConnection.getAddress().getHostName(),
                                            socket,
                                            false) != null)
                                {
                                    messageLength = SerialPortProtocol.encodeConnectionAccepted(
                                        requestedConnection, socket, buffer);
                                    System.out.println("SerialPortListener: run: sending connection accept message back to caller");
                                    serialPort.sendMessage(buffer, messageLength);
                                }
                                else
                                {
                                    messageLength = SerialPortProtocol.encodeConnectionDenied(
                                        requestedConnection, socket, buffer);
                                    System.out.println("SerialPortListener: run: sending connection denied message back to caller");
                                    serialPort.sendMessage(buffer, messageLength);
                                }

                                break;

                            case SerialPortProtocol.CONNECTION_ACCEPTED:
                                System.out.println("SerialPortListener: run: it's a connection accept");
                                SerialPortConnection acceptedConnection = new SerialPortConnection();
                                byte socketID = SerialPortProtocol.decodeConnectionAccepted(message, messageLength, acceptedConnection);
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

                                        System.out.println("SerialPortListener: run: found socket awaiting acceptance, connecting...");
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
                                System.out.println("SerialPortListener: run: it's a connection denied");
                                connection = SerialPortProtocol.decodeConnectionDenied(message, messageLength);
                                // FIXME: We should interrupt the waiting connection here. Otherwise, it will
                                // block forever, waiting for waitForConnection to return.
                                edu.uci.ece.zen.utils.ZenProperties.logger.log(
                                    edu.uci.ece.zen.utils.Logger.WARN,
                                    getClass(), "run",
                                    "Received response from remote host to connection request. Request was denied because host " + connection + " is unknown.");
                                break;

                            case SerialPortProtocol.SOCKET_DATA:
                                System.out.println("SerialPortListener: run: it's socket data");
                                byte id = SerialPortProtocol.getSocketID(message);
                                boolean foundSocket = false;

                                // Find the connection that matches the socket ID we received
                                for (Iterator i = connections.iterator(); i.hasNext(); )
                                {
                                    connection = (SerialPortConnection) i.next();
                                    if (connection.getSocket().getID() == id)
                                    {
                                        foundSocket = true;

                                        SerialPortProtocol.decodeSocketData(
                                            message, messageLength, (RemoteSerialPortStream)connection.getStream());
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

                            default:
                                System.out.println("SerialPortListener: run: unknown message! message="+Integer.toHexString(SerialPortProtocol.getMessageType(message)));
                                for (int i = 0; i < messageLength; i++)
                                {
                                    System.out.print(message[i] + " ");
                                }
                                System.out.println();
                                edu.uci.ece.zen.utils.ZenProperties.logger.log(
                                    edu.uci.ece.zen.utils.Logger.WARN,
                                    getClass(), "run",
                                    "Received socket data from remote host, but message type is unknown.");
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
