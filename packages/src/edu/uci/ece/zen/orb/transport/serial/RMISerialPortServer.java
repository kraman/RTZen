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

    public void sendMessage(byte[] buffer, String id) throws RemoteException
    {
        List messageQueue = (List) messageTable.get(id);

        if (messageQueue == null)
        {
            messageQueue = new ArrayList(10);
            messageTable.put(id, messageQueue);
        }

        messageQueue.add(buffer);

        // For debugging:
        String message = "";
        for (int i = 0; i < buffer.length; i++)
        {
            message += Integer.toHexString(buffer[i]) + " ";
        }
        System.out.println("Received message from " + id + ": " + message);
    }

    public byte[] getMessage(String id) throws RemoteException
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
                    byte[] buffer = (byte[]) messageQueue.remove(0);

                    // For debugging:
                    String message = "";
                    for (int i = 0; i < buffer.length; i++)
                    {
                        message += Integer.toHexString(buffer[i]) + " ";
                    }
                    System.out.println("Delivering message to " + id + ": " + message);

                    return buffer;
                }

                break;
            }
        }

        return null;
    }

    public static void main(String args[])
    {
        try
        {
//            System.setSecurityManager(new RMISecurityManager());
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
