package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

import edu.uci.ece.zen.utils.ZenProperties;

public class Socket
{
    private String serialPort;
    private SerialStream serialStream;

    public Socket(String host, int port) throws UnknownHostException, IOException {
        serialPort = ZenProperties.getGlobalProperty("serial.port", "/dev/ttyS0");

        synchronized (ServerSocket.connections)
        {
            // Search for a waiting connection for the requested host and port
            Connection requestedConnection = new Connection(port, host);
            for (Iterator i = ServerSocket.connections.iterator(); i.hasNext(); )
            {
                Connection waitingConnection = (Connection) i.next();

                if (waitingConnection.equals(requestedConnection))
                {
                    // If the waiting connection and the requested connection are on the same host...
                    if (InetAddress.getLocalHost().equals(requestedConnection.address))
                    {
                        System.out.println("requested socket was a LOCAL host");
                        serialStream = new LocalSerialStream();
                        waitingConnection.connect(this);
                    }
                    else
                    {
                        System.out.println("requested socket was a REMOTE host");
                        serialStream = new RemoteSerialStream();
                    }

                    return;
                }
            }

            throw new IOException("Could not connect to the host at " + requestedConnection);
        }
    }

    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        // Dummy replacement; no implementation
    }

    public synchronized void setSendBufferSize(int size) throws SocketException {
        // Dummy replacement; no implementation
    }

    public void setTcpNoDelay(boolean on) throws SocketException {
        // Dummy replacement; no implementation
    }

    public void setKeepAlive(boolean on) throws SocketException {
        // Dummy replacement; no implementation
    }

    public InputStream getInputStream() throws IOException {
        return serialStream.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return serialStream.getOutputStream();
    }
}

abstract class SerialStream
{
    abstract InputStream getInputStream();
    abstract OutputStream getOutputStream();
}

class LocalSerialStream extends SerialStream
{
    private List queue;

    LocalSerialStream()
    {
        queue = new LinkedList();
    }

    InputStream getInputStream()
    {
        return new LocalSerialStream.IS();
    }

    OutputStream getOutputStream()
    {
        return new LocalSerialStream.OS();
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
                IOException ioex = new IOException();
                ioex.initCause(e);
                throw ioex;
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
