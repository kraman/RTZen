package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;

class NativeSerialPort implements SerialPort
{
    static
    {
//        System.out.println("Loading /home/trevor/RTZEN-serial/SerialDriver/libNativeSerialPort.so...");
        System.load("/home/trevor/RTZEN-serial/SerialDriver/libNativeSerialPort.so");
    }
    
    public native int getMessage(byte[] buffer) throws IOException;
    public native void setMessage(byte[] buffer, int messageLength) throws IOException;
}
