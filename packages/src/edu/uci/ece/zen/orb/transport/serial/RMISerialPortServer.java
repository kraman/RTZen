package edu.uci.ece.zen.orb.transport.serial;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;

public class RMISerialPortServer extends UnicastRemoteObject implements RMISerialPort
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
