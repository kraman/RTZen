package edu.uci.ece.zen.orb.transport.serial;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.*;
import java.util.*;

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
        SerialPortManager.instance().addListeningConnection(this);

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
