package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerSocket
{
    static List connections = new LinkedList();

    Connection connection;
    Socket socket;

    private ServerSocket() {}

    public ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
        connection = new Connection(port, bindAddr, this);
    }

    public Socket accept() throws IOException {
        try
        {
            synchronized (connections)
            {
                connections.add(connection);
            }

            System.out.println("ServerSocket.accept (port=" + connection.port + ", addr=" + connection.address + ")");
            System.out.println("waiting for incoming connections....");
            connection.waitForConnection();

            System.out.println("ServerSocket.accept GOT CONNECTION!");

            return socket;
        }
        catch (InterruptedException e)
        {
            IOException ioex = new IOException();
            ioex.initCause(e);
            throw ioex;
        }
    }

    public int getLocalPort() {
        return connection.port;
    }

    public InetAddress getInetAddress() {
        return connection.address;
    }
}

class Connection
{
    int port;
    InetAddress address;
    ServerSocket serverSocket;

    Connection(int port, InetAddress address, ServerSocket serverSocket)
    {
        this.port = port;
        this.address = address;
        this.serverSocket = serverSocket;
    }

    Connection(int port, String host) throws UnknownHostException
    {
        this.port = port;
        address = InetAddress.getByName(host);
    }

    synchronized void connect(Socket socket)
    {
        serverSocket.socket = socket;
        notify();
    }

    synchronized void waitForConnection() throws InterruptedException
    {
        wait();
    }

    public boolean equals(Object o)
    {
        if (o instanceof Connection)
        {
            Connection c = (Connection) o;
            return c.port == port && c.address.equals(address);
        }

        return false;
    }

    public String toString()
    {
        return address + ":" + port;
    }
}
