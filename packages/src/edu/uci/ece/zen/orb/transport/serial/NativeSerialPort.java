package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import javax.realtime.*;
import edu.oswego.cs.dl.util.concurrent.Mutex;

class NativeSerialPort
{
    public static NativeSerialPort _instance;

    static
    {
        System.out.println("Loading libNativeSerialPort.so...");
        System.load("/home/kraman/javaperf-repository/prismj/RTZen-serial/SerialDriver/libNativeSerialPort.so");
    }

    public synchronized static NativeSerialPort instance(){
        
        if (_instance == null)
        {
            try{
                _instance = (NativeSerialPort) javax.realtime.ImmortalMemory.instance().newInstance( NativeSerialPort.class );
            }catch(java.lang.IllegalAccessException e){
                //for some reason, non-rt jvm comes here
                _instance = new NativeSerialPort();
            }catch(java.lang.InstantiationException e){
                e.printStackTrace();
            }
        }

        return _instance;
    }

    byte[] tmpBuffer;
    public Mutex lock;
    private NativeSerialPort(){
        tmpBuffer = new byte[4];
    }

    public synchronized NativeSerialPort accept(){
        try{
            lock.acquire();
            while( true ){
                getMessage( tmpBuffer );
                if( tmpBuffer[0] == 0 && tmpBuffer[1] == 1 && tmpBuffer[2] == 7 && tmpBuffer[3] == 7 ){
                    return this;
                }else{
                    System.out.println( "Synchronization lost. port reset" );
                }
            }
        }catch( Exception e ){
            e.printStackTrace();
        }
        return null;
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

    public int read() throws java.io.IOException{
        while( NativeSerialPort.instance().getMessage( tmpBuffer ) != 1 ){}
        return tmpBuffer[0];
    }

    public void close() throws java.io.IOException{ }
    public synchronized void reset() throws java.io.IOException{ }
    public boolean markSupported(){ return false; }
    public synchronized void mark(int m){ }

    public long skip(long l) throws java.io.IOException{
        long ls = l;
        while( l > 0 ){
            read();
            l--;
        }
        return ls;
    }

    public int read(byte[] buf) throws java.io.IOException{
        return NativeSerialPort.instance().getMessage( buf );
    }

    public int read(byte[] buf,int start,int len) throws java.io.IOException{
        for( int i=start;i<start+len;i++ )
            buf[i] = (byte)read();
        return len;
    }

}

class SerialPortOutputStream extends OutputStream{
    public void close() throws java.io.IOException{ }
    public void flush() throws java.io.IOException{ }

    byte tmpBuffer[] = new byte[1];
    public void write(int i) throws java.io.IOException{
        tmpBuffer[0] = (byte)i;
        NativeSerialPort.instance().setMessage( tmpBuffer , 1 );
    }

    public void write(byte[] buf) throws java.io.IOException{
        NativeSerialPort.instance().setMessage( buf , buf.length );
    }

    public void write(byte[] buf,int start,int len) throws java.io.IOException{
        for( int i=start;i<start+len;i++ )
            write( buf[i] );
    }
}
