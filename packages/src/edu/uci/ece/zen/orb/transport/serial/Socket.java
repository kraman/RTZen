package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

import edu.uci.ece.zen.utils.ZenProperties;

public class Socket
{
    private SerialPortConnection connection;
    private byte id;

    private static byte lastUsedID;

    static final byte NEXT_AVAILABLE_ID = -1;

    public Socket()
    {
        // No initialization
    }

    public Socket(String host, int port) throws UnknownHostException, IOException
    {
        System.out.println("Socket: <init>: trying to connect to " + host + ":" + port);
        SerialPortManager.instance().connect(port, host, this, true);
        System.out.println("Socket: <init>: got connection to " + host + ":" + port + "!");
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
        return connection.getStream().getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return connection.getStream().getOutputStream();
    }

    void setConnection(SerialPortConnection connection)
    {
        this.connection = connection;
    }

    byte getID()
    {
        return id;
    }

    void setID(byte id)
    {
        if (id == NEXT_AVAILABLE_ID)
        {
            lastUsedID++;
            this.id = lastUsedID;
        }
        else
        {
            this.id = id;
        }
    }
}
