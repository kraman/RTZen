package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import java.net.InetAddress;
import javax.comm.*;
import java.util.*;
import edu.oswego.cs.dl.util.concurrent.*;

// Note: This implementation adds an extra byte (used for framing) to every serial port message.
class CommAPISerialPort implements SerialPort, SerialPortEventListener
{
    private javax.comm.SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] inputBuffer = new byte[SerialPort.MAX_MESSAGE_LENGTH];
    private int bytesRead = -1;
    private int messageLength = 0;
    private Semaphore messageAvailable = new Semaphore(0);
    private Semaphore messageDelivered = new Semaphore(1);

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
                    // System.out.println("CommAPISerialPort: <init>: found port: " + portName);
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
                    // e.printStackTrace();
                    IOException ioex = new IOException();
                    ioex.initCause(e);
                    throw ioex;
                }
                catch (UnsupportedCommOperationException e)
                {
                    // e.printStackTrace();
                    IOException ioex = new IOException();
                    ioex.initCause(e);
                    throw ioex;
                }
                catch (PortInUseException e)
                {
                    // e.printStackTrace();
                    IOException ioex = new IOException();
                    ioex.initCause(e);
                    throw ioex;
                }

                // System.out.println("CommAPISerialPort: <init>: done opening port, returning...");
                return;
            }
        }
        // System.out.println("CommAPISerialPort: <init>: no serial port found!");

        throw new IOException("No serial port was found at " + portName);
    }

    public void serialEvent(SerialPortEvent event)
    {
        // System.out.println("CommAPISerialPort: serialEvent: got event");
        try
        {
            if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE)
            {
                // System.out.println("CommAPISerialPort: serialEvent: it's a data available event.");

                // If getMessage hasn't yet gotten the message...
                if (bytesRead == messageLength)
                {
                    // ...wait until it does.
                    // System.out.println("CommAPISerialPort: serialEvent: waiting until prior message is delivered...");
                    messageDelivered.acquire();
                    // System.out.println("CommAPISerialPort: serialEvent: ok, message was delivered. continuing...");
                    bytesRead = -1;
                    messageLength = 0;
                }

                if (messageLength == 0)
                {
                    messageLength = inputStream.read();
                    // System.out.println("CommAPISerialPort: serialEvent: this is the first chunk of a message. Size: " + messageLength);
                    bytesRead = 0;
                }

                int bytesAvailable = inputStream.available();

                if (bytesAvailable > 0)
                {
                    if (bytesRead + bytesAvailable > inputBuffer.length)
                    {
                        throw new IOException("Buffer is full");
                    }

                    bytesRead += inputStream.read(
                        inputBuffer, bytesRead, Math.min(bytesAvailable, messageLength-bytesRead));

                    // System.out.println("CommAPISerialPort: serialEvent: done reading chunk, " + bytesAvailable + " bytes total have been read for the current message");

                    // If an entire message has been read, tell getMessage that a message is available
                    if (bytesRead == messageLength)
                    {
                        // System.out.println("CommAPISerialPort: serialEvent: done reading message of " + messageLength + " bytes, notifying getMessage");
                        messageAvailable.release();
                    }
                }
            }
        }
        catch (InterruptedException e)
        {
            edu.uci.ece.zen.utils.ZenProperties.logger.log(
                edu.uci.ece.zen.utils.Logger.WARN, getClass(), "serialEvent", "Interrupted while trying to acquire mutex on serial port input buffer", e);
        }
        catch (IOException e)
        {
e.printStackTrace();
            edu.uci.ece.zen.utils.ZenProperties.logger.log(
                edu.uci.ece.zen.utils.Logger.WARN, getClass(), "serialEvent", "Could not read from serial port", e);
        }
    }

    public int getMessage(byte[] buffer) throws IOException
    {
        try
        {
            // System.out.println("CommAPISerialPort: getMessage: blocking until input buffer has data...");

            messageAvailable.acquire();

            // System.out.println("CommAPISerialPort: getMessage: input buffer got unblocked! moving " + messageLength + " bytes into buffer...");

            System.arraycopy(inputBuffer, 0, buffer, 0, messageLength);

            int retVal = messageLength;
            messageDelivered.release();
            return retVal;
        }
        catch (InterruptedException e)
        {
            IOException ioex = new IOException();
            ioex.initCause(e);
            throw ioex;
        }
    }

    public void sendMessage(byte[] buffer, int bufferSize) throws IOException
    {
        // System.out.println("CommAPISerialPort: sendMessage: sending " + bufferSize + " bytes through serial port...");
        outputStream.write(bufferSize);
        outputStream.write(buffer, 0, bufferSize);
        // System.out.println("CommAPISerialPort: sendMessage: sent " + bufferSize + " bytes through serial port");
    }
}
