package edu.uci.ece.zen.orb.transport.serial;

import java.rmi.*;
import java.rmi.registry.*;
import java.io.*;
import java.net.InetAddress;

interface RMISerialPort extends Remote
{
    void sendMessage(byte[] message, int bufferLength, String id) throws RemoteException;
    byte[] getMessage(String id) throws RemoteException;
}

class RMISerialPortClient implements SerialPort
{
    private RMISerialPort rmiSerialPort;

    RMISerialPortClient(String address) throws IOException
    {
        try
        {
            System.out.println("Looking up " + address);
            rmiSerialPort = (RMISerialPort) Naming.lookup(address);
        }
        catch (NotBoundException e)
        {
            IOException ioex = new IOException();
            ioex.initCause(e);
            throw ioex;
        }
    }

    public void sendMessage(byte[] buffer, int bufferLength) throws IOException
    {
        try
        {
            /*
String msg = "";
for (int i = 0; i < bufferLength; i++)
    msg += Integer.toHexString(buffer[i]) + " ";

            System.out.println("RMISerialPortClient: sendMessage: sending message: " + msg);*/
            rmiSerialPort.sendMessage(buffer, bufferLength, InetAddress.getLocalHost().getHostAddress());
        }
        catch (Exception e)
        {
            IOException ex = new IOException();
            ex.initCause(e);
            throw ex;
        }
    }

    public int getMessage(byte[] buffer) throws IOException
    {
        try
        {
            /*
            System.out.println("RMISerialPortClient: getMessage: waiting for message from server...");
            */
            byte[] message = rmiSerialPort.getMessage(InetAddress.getLocalHost().getHostAddress());
            System.arraycopy(message, 0, buffer, 0, message.length);
            /*
String msg = "";
for (int i = 0; i < message.length; i++)
    msg += Integer.toHexString(message[i]) + " ";
            System.out.println("RMISerialPortClient: getMessage: got a message: " + msg);*/
            return message.length;
        }
        catch (Exception e)
        {
            IOException ex = new IOException();
            ex.initCause(e);
            throw ex;
        }
    }
}
