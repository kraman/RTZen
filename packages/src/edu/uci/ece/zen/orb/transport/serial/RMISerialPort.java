package edu.uci.ece.zen.orb.transport.serial;

import java.rmi.*;

public interface RMISerialPort extends Remote
{
    public void SendMessage(byte[] buffer, String id) throws RemoteException;
    public byte[] GetMessage(String id) throws RemoteException;
}
