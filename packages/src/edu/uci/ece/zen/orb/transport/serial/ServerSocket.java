package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerSocket
{
    private SerialPortConnection connection;

    private ServerSocket() {}

    public ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException
    {
        connection = new SerialPortConnection(port, bindAddr);
    }

    public Socket accept() throws IOException
    {
        try
        {
            System.out.println("ServerSocket.accept (port=" + connection.getPort() + ", addr=" + connection.getAddress() + "), waiting for incoming connections....");

            connection.waitForConnection();

            System.out.println("ServerSocket.accept got connection");

            return connection.socket;
        }
        catch (InterruptedException e)
        {
            IOException ioex = new IOException("ServerSocket was interrupted while waiting for a connection");
            ioex.initCause(e);
            throw ioex;
        }
    }

    public int getLocalPort() {
        return connection.getPort();
    }

    public InetAddress getInetAddress() {
        return connection.getAddress();
    }
}
