package edu.uci.ece.zen.orb.transport.serial;

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
        return new RMISerialPortClient(
            edu.uci.ece.zen.utils.ZenProperties.getGlobalProperty(
                "serial-transport.rmi-address",
                "rmi://doc.ece.uci.edu/SerialPort"));
    }
/*
    static SerialPort createCommAPISerialPort() throws IOException
    {
    }
*/
}
