package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
//import javax.comm.*;

/**
Provides a simplified, universal serial port interface.
*/
interface SerialPort
{
    static final int MAX_MESSAGE_LENGTH = 1024;

    /**
    Sends the buffer through the serial port and blocks until all bytes
    in the buffer have been sent.
    */
    void setMessage(byte[] buffer, int messageLength) throws IOException;

    /**
    Retrieves a message from the serial port, blocking until one is available.
    It will try to read all bytes that are available up to the length of the
    buffer, possibly fewer if no more bytes are available. The caller must
    ensure that the buffer is at least as large as any message sent through
    the serial port.
    @return The number of bytes that were read into the buffer.
    */
    int getMessage(byte[] buffer) throws IOException;
}

/**
Factory methods for simplifying the creation of serial port drivers.
*/
class SerialPortFactory
{
    private static SerialPort serialPort;
    
    static SerialPort createNativeSerialPort() throws IOException
    {
        if (serialPort == null)
        {
            serialPort = new NativeSerialPort();
        }

        return serialPort;
    }
    
    /*
    static SerialPort createRMISerialPort() throws IOException
    {
        return new RMISerialPortClient(
            edu.uci.ece.zen.utils.ZenProperties.getGlobalProperty(
                "serial-transport.rmi-address",
                "rmi://doc.ece.uci.edu/SerialPort"));
    }

    static SerialPort createCommAPISerialPort() throws IOException
    {
        return new CommAPISerialPort(
            edu.uci.ece.zen.utils.ZenProperties.getGlobalProperty(
                "serial-transport.port",
                "/dev/ttyS0"));
    }
    */
}
