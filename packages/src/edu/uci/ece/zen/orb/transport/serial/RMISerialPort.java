package edu.uci.ece.zen.orb.transport.serial;

import java.rmi.*;

public interface RMISerialPort extends Remote
{
    void sendMessage(byte[] buffer, String id) throws RemoteException;
    byte[] getMessage(String id) throws RemoteException;
}
