package edu.uci.ece.zen.orb.transport.serial;

import java.rmi.*;
import java.rmi.registry.*;
import java.io.*;
import java.net.InetAddress;

public interface RMISerialPort extends Remote
{
    void sendMessage(byte[] buffer, String id) throws RemoteException;
    byte[] getMessage(String id) throws RemoteException;
}

class RMISerialPortClient implements SerialPort
{
    private RMISerialPort rmiSerialPort;

    RMISerialPortClient(String address) throws IOException
    {
        try
        {
            System.out.println("looking up " + address);
            rmiSerialPort = (RMISerialPort) Naming.lookup(address);
        }
        catch (NotBoundException e)
        {
            IOException ioex = new IOException();
            ioex.initCause(e);
            throw ioex;
        }
    }

    public void sendMessage(byte[] buffer) throws IOException
    {
        try
        {
            rmiSerialPort.sendMessage(buffer, InetAddress.getLocalHost().getHostAddress());
        }
        catch (Exception e)
        {
            IOException ex = new IOException();
            ex.initCause(e);
            throw ex;
        }
    }

    public byte[] getMessage() throws IOException
    {
        try
        {
            return rmiSerialPort.getMessage(InetAddress.getLocalHost().getHostAddress());
        }
        catch (Exception e)
        {
            IOException ex = new IOException();
            ex.initCause(e);
            throw ex;
        }
    }
}
