package edu.uci.ece.zen.orb.transport.serial;

import java.util.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SerialPortManager
{
    private static final SerialPortManager instance = new SerialPortManager();

    private List connections = new LinkedList();
    private List waitingConnections = new LinkedList();

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

    void addWaitingConnection(SerialPortConnection connection)
    {
        // Synchronize on the connection list so that we don't modify it while we are
        // iterating over it
        synchronized (waitingConnections)
        {
            waitingConnections.add(connection);
        }
    }

    SerialPortConnection connect(int port, String host, Socket socket) throws UnknownHostException, IOException
    {
        System.out.println("creating connection holder...");
        SerialPortConnection requestedConnection = new SerialPortConnection(port, host);

        // Synchronize on the connection list so that it doesn't get modified while we are
        // iterating over it
        System.out.println("getting connection mutex...");
        synchronized (waitingConnections)
        {
            System.out.println("searching for existing connections...");
            // Search for a waiting connection for the requested host and port
            for (Iterator i = waitingConnections.iterator(); i.hasNext(); )
            {
                SerialPortConnection waitingConnection = (SerialPortConnection) i.next();

                if (waitingConnection.equals(requestedConnection))
                {
                    System.out.println("found one!");
                    // If the waiting connection and the requested connection are on the same host...
                    if (InetAddress.getLocalHost().equals(requestedConnection.getAddress()))
                    {
                        System.out.println("and it's local!");
                        i.remove();
                        connections.add(waitingConnection);

                        waitingConnection.connectLocally(socket);
                        System.out.println("requested socket was a LOCAL host");

                        return waitingConnection;
                    }
                    else
                    {
                        System.out.println("requested socket was a REMOTE host");
//                        serialStream = new RemoteSerialStream();
                        return waitingConnection;
                    }
                }
            }

            throw new IOException("Could not connect to the host at " + requestedConnection);
        }
    }

    private class SerialPortListener extends Thread
    {
        public void run()
        {
            while (true)
            {
                // Check for incoming connections...
                System.out.println("SerialPortListener: checking for incoming message...");

                try
                {
                    sleep(5000);
                }
                catch (InterruptedException e)
                {
                    // No problem, ignore
                }
            }
        }
    }
}


class SerialPortConnection
{
    private int port;
    private InetAddress address;
    private boolean connected;

    SerialPortStream stream;
    Socket socket;

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
    synchronized void connectLocally(Socket socket)
    {
        this.socket = socket;
        stream = new LocalSerialPortStream();
        connected = true;

        notify();
    }

    // This function needs to be synchronized for wait/notify
    synchronized void waitForConnection() throws InterruptedException
    {
        SerialPortManager.instance().addWaitingConnection(this);
        wait();
    }

    boolean isConnected()
    {
        return connected;
    }

    int getPort()
    {
        return port;
    }

    InetAddress getAddress()
    {
        return address;
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
                System.out.println("Local connection reading " + n.toHexString(n.intValue()));
                return n.intValue();
            }
            catch (IndexOutOfBoundsException e)
            {
                throw new IOException("Input stream cannot be read; no data available");
            }
        }
    }

    class OS extends OutputStream
    {
        public void write(int b) throws IOException
        {
            System.out.println("Local connection writing " + Integer.toHexString(b));
            queue.add(new Integer(b));
        }
    }
}
