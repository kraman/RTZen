package edu.uci.ece.zen.orb.transport.serial;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
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

class RMISerialPortServer extends UnicastRemoteObject implements RMISerialPort
{
    private Hashtable messageTable;

    public RMISerialPortServer() throws RemoteException
    {
        messageTable = new Hashtable();
    }

    public void sendMessage(byte[] message, int messageLength, String id) throws RemoteException
    {
        synchronized (messageTable)
        {
            List messageQueue = (List) messageTable.get(id);

            if (messageQueue == null)
            {
                messageQueue = new ArrayList(10000);
                messageTable.put(id, messageQueue);
            }

            byte[] newMessage = new byte[messageLength];
            System.arraycopy(message, 0, newMessage, 0, messageLength);
            messageQueue.add(newMessage);

            // For debugging:
            String messageString = "";
            for (int i = 0; i < newMessage.length; i++)
            {
                messageString += Integer.toHexString(newMessage[i] & 0xFF) + " ";
            }
            System.out.println("Received message from " + id + ": " + messageString);
        }
    }

    public byte[] getMessage(String id) throws RemoteException
    {
        // This is a very lazy way of blocking until a message has been received
        while (true)
        {
            try
            {
                Thread.currentThread().sleep(3000);
            }
            catch (InterruptedException e)
            {
                RemoteException re = new RemoteException();
                re.initCause(e);
                throw re;
            }

            synchronized (messageTable)
            {
                // Walk the table of messages and find the one that the OTHER guy sent
                for (Enumeration e = messageTable.keys(); e.hasMoreElements(); )
                {
                    String otherID = (String) e.nextElement();
                    if (!otherID.equals(id))
                    {
                        List messageQueue = (List) messageTable.get(otherID);
                        if (messageQueue.size() > 0)
                        {
                            byte[] message = (byte[]) messageQueue.remove(0);

                            // For debugging:
                            String messageString = "";
                            for (int i = 0; i < message.length; i++)
                            {
                                messageString += Integer.toHexString(message[i] & 0xFF) + " ";
                            }
                            System.out.println("Delivering message to " + id + ": " + messageString);

                            return message;
                        }
                        else
                        {
                            // System.out.println("No messages are waiting for " + id);
                        }

                        break;
                    }
                }

//                System.out.println("No entries for " + id + " were found");
            }
        }
    }

    public static void main(String args[])
    {
        try
        {
            RMISerialPortServer server = new RMISerialPortServer();
            LocateRegistry.createRegistry(1099).rebind("SerialPort", server);
            System.out.println("The server is listening...");
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
