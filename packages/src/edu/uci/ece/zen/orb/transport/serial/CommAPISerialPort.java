package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import java.net.InetAddress;
import javax.comm.*;
import java.util.*;

/**
Assumes that no serial port message will be larger than MAX_MESSAGE_SIZE.
*/
class CommAPISerialPort implements SerialPort, SerialPortEventListener
{
    private javax.comm.SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;

    CommAPISerialPort(String portName) throws IOException
    {
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier portID;

        while (portList.hasMoreElements())
        {
            portID = (CommPortIdentifier) portList.nextElement();

            if (portID.getPortType() == CommPortIdentifier.PORT_SERIAL &&
                portID.getName().equals(portName))
            {
                try
                {
                    System.out.println("CommAPISerialPort: <init>: found port: " + portName);
                    serialPort = (javax.comm.SerialPort) portID.open("CommAPISerialPort", 0);

                    inputStream = serialPort.getInputStream();
                    outputStream = serialPort.getOutputStream();

                    serialPort.addEventListener(this);
                    serialPort.notifyOnDataAvailable(true);

                    serialPort.setSerialPortParams(9600,
                        javax.comm.SerialPort.DATABITS_8,
                        javax.comm.SerialPort.STOPBITS_1,
                        javax.comm.SerialPort.PARITY_NONE);
                }
                catch (TooManyListenersException e)
                {
                    IOException ioex = new IOException();
                    ioex.initCause(e);
                    throw ioex;
                }
                catch (UnsupportedCommOperationException e)
                {
                    IOException ioex = new IOException();
                    ioex.initCause(e);
                    throw ioex;
                }
                catch (PortInUseException e)
                {
                    IOException ioex = new IOException();
                    ioex.initCause(e);
                    throw ioex;
                }

                System.out.println("CommAPISerialPort: <init>: done opening port, returning...");
                return;
            }
        }

        throw new IOException("No serial port was found at " + portName);
    }

    public void serialEvent(SerialPortEvent event)
    {
        System.out.println("CommAPISerialPort: serialEvent: got event");
        try
        {
            if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE)
            {
                System.out.println("CommAPISerialPort: serialEvent: it's a data available event");
                synchronized (inputStream)
                {
                    while (inputStream.available() > 0)
                    {
                        System.out.println("CommAPISerialPort: serialEvent: read byte: " + inputStream.read());
                    }

                    inputStream.notify();
                }
            }
        }
        catch (IOException e)
        {
            edu.uci.ece.zen.utils.ZenProperties.logger.log(
                edu.uci.ece.zen.utils.Logger.WARN, getClass(), "serialEvent", "Could not read from serial port", e);
        }
    }

    public void sendMessage(byte[] buffer, int bufferSize) throws IOException
    {
        System.out.println("CommAPISerialPort: sendMessage: sending " + bufferSize + " bytes through serial port...");
        outputStream.write(buffer, 0, bufferSize);
    }

    public int getMessage(byte[] buffer) throws IOException
    {
        try
        {
            synchronized (inputStream)
            {
                System.out.println("CommAPISerialPort: getMessage: blocking until input buffer has data...");
                inputStream.wait();
                System.out.println("CommAPISerialPort: getMessage: input buffer got unblocked! reading data...");
                System.out.println("FIXME: read from serial port and return data here");
                return 0; //FIXME
            }
        }
        catch (InterruptedException e)
        {
            IOException ioex = new IOException();
            ioex.initCause(e);
            throw ioex;
        }
    }
}
