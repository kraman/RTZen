/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

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
