package edu.uci.ece.zen.orb.transport.serial;

import java.rmi.*;
import java.rmi.registry.*;
import java.io.*;
import java.net.*;

interface SerialPort
{
   void sendMessage(byte[] buffer) throws IOException;
   byte[] getMessage() throws IOException;
}

class SerialPortFactory
{
    static SerialPort createRMISerialPort() throws IOException
    {
        return new RMISerialPortImpl("rmi://dhcp-211219.mobile.uci.edu/SerialPort");
    }
/*
    static SerialPort createCommAPISerialPort() throws IOException
    {
    }
*/
}

class RMISerialPortImpl implements SerialPort
{
    private RMISerialPort rmiSerialPort;

    RMISerialPortImpl(String address) throws IOException
    {
        try
        {
            System.out.println("looking up " + address);
            rmiSerialPort = (RMISerialPort) Naming.lookup(address);
            System.out.println("got rmi address");
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
