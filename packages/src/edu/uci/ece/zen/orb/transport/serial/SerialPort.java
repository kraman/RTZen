package edu.uci.ece.zen.orb.transport.serial;

import java.rmi.*;
import java.rmi.registry.*;
import java.io.*;
import java.net.*;

class SerialPort
{
    static RMISerialPort rmiSerialPort;

    static
    {
        try
        {
            rmiSerialPort =
                (RMISerialPort) Naming.lookup("rmi://dhcp-211219.mobile.uci.edu/SerialPort");
        }
        catch (Exception e)
        {
            System.err.println("ERROR: Cannot connect to serial port server");
            e.printStackTrace();
        }
    }

    static void SendMessage(byte[] buffer) throws IOException
    {
        try
        {
            rmiSerialPort.SendMessage(buffer, InetAddress.getLocalHost().getHostAddress());
        }
        catch (Exception e)
        {
            IOException ex = new IOException();
            ex.initCause(e);
            throw ex;
        }
    }

    static byte[] GetMessage() throws IOException
    {
        try
        {
            return rmiSerialPort.GetMessage(InetAddress.getLocalHost().getHostAddress());
        }
        catch (Exception e)
        {
            IOException ex = new IOException();
            ex.initCause(e);
            throw ex;
        }
    }
}
