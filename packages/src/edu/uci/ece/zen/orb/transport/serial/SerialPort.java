package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

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

class SerialPortProtocol
{
    static final byte CONNECTION_REQUESTED = 1;
    static final byte CONNECTION_ACCEPTED = 2;
    static final byte CONNECTION_DENIED = 3;
    static final byte SOCKET_DATA = 4;

    private static byte[] encodeAddress(int messageHeaderLength, InetAddress inetAddress, int port)
    {
        byte[] address = inetAddress.getAddress();
        // Message format: message header + address length + address + port
        byte[] message = new byte[messageHeaderLength + 1 + address.length + 2];

        message[messageHeaderLength] = (byte) address.length;
        System.arraycopy(address, 0, message, messageHeaderLength + 1, address.length);
        message[message.length - 2] = (byte)(port >> 8);
        message[message.length - 1] = (byte)(port & 0xFF);

        return message;
    }

    static byte[] encodeConnectionRequested(SerialPortConnection connection)
    {
        byte[] message = encodeAddress(1, connection.getAddress(), connection.getPort());
        message[0] = CONNECTION_REQUESTED;

        return message;
    }

    static SerialPortConnection decodeConnectionRequested(byte[] message) throws UnknownHostException
    {
        byte[] address = new byte[message[1]];
        System.arraycopy(message, 2, address, 0, address.length);

        int port = (message[message.length - 2] << 8) + message[message.length - 1];

        return new SerialPortConnection(port, InetAddress.getByAddress(address));
    }

    static byte[] encodeConnectionAccepted(SerialPortConnection connection, Socket socket)
    {
        byte[] message = encodeAddress(2, connection.getAddress(), connection.getPort());
        message[0] = CONNECTION_ACCEPTED;
        message[1] = socket.getID();

        return message;
    }

    static byte decodeConnectionAccepted(byte[] message, SerialPortConnection connection) throws UnknownHostException
    {
        byte[] address = new byte[message[2]];
        System.arraycopy(message, 3, address, 0, address.length);

        connection.setPort( (message[message.length - 2] << 8) + message[message.length - 1] );
        connection.setAddress( InetAddress.getByAddress(address) );

        return message[1];
    }

    static byte[] encodeConnectionDenied(SerialPortConnection connection, Socket socket)
    {
        byte[] message = encodeAddress(2, connection.getAddress(), connection.getPort());
        message[0] = CONNECTION_DENIED;
        message[1] = socket.getID();

        return message;
    }

    static SerialPortConnection decodeConnectionDenied(byte[] message) throws UnknownHostException
    {
        byte[] address = new byte[message[2]];
        System.arraycopy(message, 3, address, 0, address.length);

        int port = (message[message.length - 2] << 8) + message[message.length - 1];

        return new SerialPortConnection(port, InetAddress.getByAddress(address));
    }

    static byte[] encodeSocketData(Socket socket, List outputBuffer)
    {
        byte[] message = new byte[2 + outputBuffer.size()];

        message[0] = SOCKET_DATA;
        message[1] = socket.getID();
        int offset = 2;

        for (Iterator i = outputBuffer.iterator(); i.hasNext(); )
        {
            Integer data = (Integer) i.next();
            message[offset++] = data.byteValue();
        }

        return message;
    }

    static void decodeSocketData(byte[] message, edu.oswego.cs.dl.util.concurrent.BoundedBuffer inputBuffer)
        throws InterruptedException
    {
        for (int i = 2; i < message.length; i++)
        {
            inputBuffer.put(new Integer(message[i]));
        }
    }

    static byte getSocketID(byte[] message)
    {
        return message[1];
    }

    static byte getMessageType(byte[] message)
    {
        return message[0];
    }
}
