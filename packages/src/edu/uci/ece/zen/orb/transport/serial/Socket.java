package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;

import edu.uci.ece.zen.utils.ZenProperties;

public class Socket
{
    private String serialPort;

    public Socket(String host, int port) throws UnknownHostException, IOException {
        // Ignore host and port. Use serial port device in zen.properties instead.
System.out.println("socket: creating new socket");
        serialPort = ZenProperties.getGlobalProperty("serial.port", "/dev/ttyS0");

        System.err.println("zen port = " + serialPort);
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
        return new SerialInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return new SerialOutputStream();
    }
}

class SerialInputStream extends InputStream
{
    public int read() throws IOException
    {
        try
        {
            System.out.println("read request for serialinputstream, waiting 5 seconds...");
            Thread.currentThread().sleep(5000);
            System.out.println("returning from read");
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return 0;
    }
}

class SerialOutputStream extends OutputStream
{
    public void write(int b) throws IOException
    {
        System.out.println("write request for serial input stream, data=" + Integer.toHexString(b));
    }
}
