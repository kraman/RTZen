package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;

class NativeSerialPort implements SerialPort
{
    public static NativeSerialPort _instance;

    static
    {
        System.out.println("Loading libNativeSerialPort.so...");
        System.load("/home/kraman/javaperf-repository/prismj/RTZen-serial/SerialDriver/libNativeSerialPort.so");
    }

    public synchronized static NativeSerialPort instance(){
        if( _instance == null )
            _instance = ImmortalMemory.newInstance( NativeSerialPort.class );
        return _instance;
    }

    byte[] tmpBuffer;
    private NativeSerialPort(){
        tmpBuffer = byte[4];
    }

    public synchronized NativeSerialPort accept(){
        while( true ){
            getMessage( tmpBuffer );
            if( tmpBuffer[0] == 0 && tmpBuffer[1] = 1 && tmpBuffer[2] == 7 && tmpBuffer[3] == 7 ){
                return this;
            }else{
                System.out.println( "Synchronization lost. port reset" );
            }
        }
    }

    public InputStream getInputStream(){ return null; }
    public OutputStream getOutputStream(){ return null; }

    public native int getMessage(byte[] buffer) throws IOException;
    public native void setMessage(byte[] buffer, int messageLength) throws IOException;
}

class SerialPortInputStream extends InputStream{
    byte tmpBuffer[] = new byte[1];

    public int available() throws java.io.IOException{
        return 0;
    }

    public abstract int read() throws java.io.IOException{
        NativeSerialPort.instance().getMessage( tmpBuffer );
        return tmpBuffer[0];
    }

    public void close() throws java.io.IOException{
    }

    public synchronized void reset() throws java.io.IOException{
    }

    public boolean markSupported(){
        return false;
    }

    public synchronized void mark(int){
    }

    public long skip(long l) throws java.io.IOException{
        while( l > 0 ){
            read();
            l--;
        }
    }

    public int read(byte[] buf) throws java.io.IOException{
        return NativeSerialPort.instance().getMessage( buf );
    }

    public int read(byte[],int,int) throws java.io.IOException{

    }

}

class SerialPortOutputStream extends OutputStream{
}
