package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import javax.realtime.*;

import edu.uci.ece.zen.utils.ZenProperties;

class NativeSerialPort extends SerialPort
{

    static
    {
        ZenProperties.logger.log("Loading libNativeSerialPort.so...");
        //System.load("/home/mpanahi/RTZen/packages/src/edu/uci/ece/zen/orb/transport/serial/jni/libNativeSerialPort.so");
        System.load(ZenProperties.getGlobalProperty( "serial.library.path" , "" ));
    }

    public native int getMessage(byte[] buffer) throws IOException;
    public native void setMessage(byte[] buffer, int messageLength) throws IOException;
}
