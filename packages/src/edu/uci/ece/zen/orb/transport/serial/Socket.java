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

    private Socket() {}

    public Socket(String host, int port) throws UnknownHostException, IOException
    {
        System.out.println("Socket(): host=" + host + ":" + port);
        connection = SerialPortManager.instance().connect(port, host, this);
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
        return connection.stream.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return connection.stream.getOutputStream();
    }
}
