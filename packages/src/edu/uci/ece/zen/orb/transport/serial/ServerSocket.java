package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;

public class ServerSocket
{
    public ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
        // Dummy replacement; no implementation
    }

    public Socket accept() throws IOException {
        try
        {
            System.out.println("Waiting for new client connections...");
            Thread.currentThread().sleep(5000);
            System.out.println("Got new client connection");
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        System.out.println("socket accepted by server socket, creating new one");
        return new Socket(null, -1);
    }

    public int getLocalPort() {
        // Dummy replacement; no implementation
        return -1;
    }

    public InetAddress getInetAddress() {
        // Dummy replacement; no implementation
        return new InetAddress();
    }
}
