package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.comm.*;

/**
Provides an abstraction of the serial port protocol to ease the task of
packing and unpacking messages for socket emulation.
*/
class SerialPortSocketProtocol
{
    static final byte CONNECTION_REQUESTED = 1;
    static final byte CONNECTION_ACCEPTED = 2;
    static final byte CONNECTION_DENIED = 3;
    static final byte SOCKET_DATA = 4;

    static final int SOCKET_DATA_HEADER_LENGTH = 2;

    // Format: address length (1 byte), address (address.length bytes), port (2 bytes)
    // FIXME: This still has memory leaks in RTSJ
    private static int encodeAddress(byte[] buffer, int bufferOffset, InetAddress inetAddress, int port)
    {
        byte[] address = inetAddress.getAddress();

        buffer[bufferOffset] = (byte) address.length;  // Assume the address is less than 128 bytes long
        System.arraycopy(address, 0, buffer, bufferOffset + 1, address.length);
        buffer[bufferOffset + 1 + address.length] = (byte)(port >> 8);
        buffer[bufferOffset + 1 + address.length + 1] = (byte)(port & 0xFF);

        return bufferOffset + 1 + address.length + 1 + 1;
    }

    static int encodeConnectionRequested(SerialPortConnection connection, byte[] buffer)
    {
        int messageLength = encodeAddress(buffer, 1, connection.getAddress(), connection.getPort());
        buffer[0] = CONNECTION_REQUESTED;

        return messageLength;
    }

    // FIXME: This still has memory leaks in RTSJ
    static SerialPortConnection decodeConnectionRequested(byte[] buffer, int messageLength) throws UnknownHostException
    {
        byte[] address = new byte[buffer[1]];
        System.arraycopy(buffer, 2, address, 0, buffer[1]);

        int port = (buffer[messageLength - 2] << 8) + buffer[messageLength - 1];

        return new SerialPortConnection(port, InetAddress.getByAddress(address));
    }

    static int encodeConnectionAccepted(SerialPortConnection connection, Socket socket, byte[] buffer)
    {
        int messageLength = encodeAddress(buffer, 2, connection.getAddress(), connection.getPort());
        buffer[0] = CONNECTION_ACCEPTED;
        buffer[1] = socket.getID();

        return messageLength;
    }

    // FIXME: This still has memory leaks in RTSJ
    static byte decodeConnectionAccepted(byte[] buffer, int messageLength, SerialPortConnection connection)
        throws UnknownHostException
    {
        byte[] address = new byte[buffer[2]];
        System.arraycopy(buffer, 3, address, 0, address.length);

        connection.setPort( (buffer[messageLength - 2] << 8) + buffer[messageLength - 1] );
        connection.setAddress( InetAddress.getByAddress(address) );

        return buffer[1];
    }

    static int encodeConnectionDenied(SerialPortConnection connection, Socket socket, byte[] buffer)
    {
        int messageLength = encodeAddress(buffer, 2, connection.getAddress(), connection.getPort());
        buffer[0] = CONNECTION_DENIED;
        buffer[1] = socket.getID();

        return messageLength;
    }

    // FIXME: This still has memory leaks in RTSJ
    static SerialPortConnection decodeConnectionDenied(byte[] buffer, int messageLength) throws UnknownHostException
    {
        byte[] address = new byte[buffer[2]];
        System.arraycopy(buffer, 3, address, 0, address.length);

        int port = (buffer[messageLength - 2] << 8) + buffer[messageLength - 1];

        return new SerialPortConnection(port, InetAddress.getByAddress(address));
    }

    // IMPORTANT: Buffer must have empty space of size SOCKET_DATA_HEADER_LENGTH at the front of the buffer!
    // This function will fill this space with header data.
    static void encodeSocketData(Socket socket, byte[] buffer)
    {
        buffer[0] = SOCKET_DATA;
        buffer[1] = socket.getID();
    }

    // Buffer must be at least as long as the largest message
    static void decodeSocketData(byte[] buffer, int messageLength, RemoteSerialPortStream stream)
        throws InterruptedException, IOException
    {
        for (int i = 2; i < messageLength; i++)
        {
            // System.out.println("SerialPortSocketProtocol: decodeSocketData: adding byte to serial port stream: " + Integer.toHexString(buffer[i]));
            stream.addToInputStream(buffer[i]);
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
