package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import javax.realtime.*;
import edu.oswego.cs.dl.util.concurrent.Mutex;
import edu.uci.ece.zen.utils.ZenProperties;

class NativeSerialPort
{
    public static NativeSerialPort _instance;

    static
    {
        ZenProperties.logger.log("Loading libNativeSerialPort.so...");
        //System.load("/home/mpanahi/RTZen/packages/src/edu/uci/ece/zen/orb/transport/serial/jni/libNativeSerialPort.so");
        System.load(ZenProperties.getGlobalProperty( "serial.library.path" , "" ));
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
    public Mutex lock = new Mutex();
    public InputStream istream = new SerialPortInputStream();
    public OutputStream ostream = new SerialPortOutputStream();
    private NativeSerialPort(){
        tmpBuffer = new byte[89];
    }

    public synchronized NativeSerialPort accept(){
        try{
            ZenProperties.logger.log( "++++++++++++Serial port: accept called() " );
            lock.acquire();
            ZenProperties.logger.log( "++++++++++++Serial port: accept called(); lock acquired" );
            while( true ){
                int size = getMessage( tmpBuffer );
		for( int i=0;i<89;i++ )
			System.out.print( tmpBuffer[i] + " " );
		System.out.println("");
                if( tmpBuffer[0] == 2 && tmpBuffer[1] == 1 && tmpBuffer[2] == 7 && tmpBuffer[3] == 7 ){
                    ZenProperties.logger.log( "Serial port: accept called(); lock acquired; magic recieved" );
                    return this;
                }else{
                    ZenProperties.logger.log("Synchronization lost. port reset" );
                    if (ZenProperties.dbg) 
                        ZenProperties.logger.log( "data: " + tmpBuffer[0] + " " +tmpBuffer[1] + " " +tmpBuffer[2] + " "+  tmpBuffer[3] + " " );
                    if (ZenProperties.dbg) ZenProperties.logger.log("size: " + size);
                }
            }
        }catch( Exception e ){
            e.printStackTrace();
        }
        return null;
    }

    public InputStream getInputStream(){ return istream; }
    public OutputStream getOutputStream(){ return ostream; }

    public native int getMessage(byte[] buffer) throws IOException;
    public native void setMessage(byte[] buffer, int messageLength) throws IOException;
}

class SerialPortInputStream extends InputStream{
    byte tmpBuffer[] = new byte[89];
    int pos;
    int limit;

    public int available() throws java.io.IOException{
        return limit-pos;
    }

    void checkAvail(){
	System.out.println( "Avail: " + limit + " pos: " + pos );
	try{
            if( (limit-pos) == 0 ){
		int tmp=0;
		System.out.println( "get message from port" );
		while( (tmp = NativeSerialPort.instance().getMessage( tmpBuffer )) <= 0 )
		{ System.out.println( "...." ); }
		System.out.println( "tmp _" + tmp );
	        pos = 2;
	        limit = tmpBuffer[1];
            }
	}catch( IOException ioex ){
	    ioex.printStackTrace();
        }
    }

    public int read() throws java.io.IOException{
        ZenProperties.logger.log( "-------------------------Serial port: read 1 " );
        checkAvail();
        return tmpBuffer[pos++];
    }

    public void close() throws java.io.IOException{
        limit=0;
        pos=2;
    }

    public synchronized void reset() throws java.io.IOException{ }
    public boolean markSupported(){ return false; }
    public synchronized void mark(int m){ }

    public long skip(long l) throws java.io.IOException{
        checkAvail();
        pos += (int)l;
        return l;
    }

    public int read(byte[] buf) throws java.io.IOException{
        ZenProperties.logger.log( "-------------------------Serial port: read 2 " );
        checkAvail();
        System.arraycopy( tmpBuffer , pos , buf , 0 , buf.length );
        return buf.length;
    }

    public int read(byte[] buf,int start,int len) throws java.io.IOException{
        ZenProperties.logger.log( "-------------------------Serial port: read 3 " );
        for( int i=start;i<start+len;i++ )
            buf[i] = (byte)read();
        return len;
    }

}

class SerialPortOutputStream extends OutputStream{
    byte[] buffer = new byte[89];
    int pos=2;

    public SerialPortOutputStream(){
        buffer[0]=2;
    }

    public void close() throws java.io.IOException{ pos=2; }
    public void flush() throws java.io.IOException{ 
        buffer[1] = (byte) pos;
	if( pos > 89 ){
		System.out.println( "Message is WAY TOO BIG. msg size: " + pos + " (Max supported:89)" );
		System.exit(-1);
	}
        NativeSerialPort.instance().setMessage( buffer , 89 );
        pos=2;
    }

    public void write(int i) throws java.io.IOException{
        buffer[pos++] = (byte)i;
    }

    public void write(byte[] buf) throws java.io.IOException{
        ZenProperties.logger.log("-------------------------Serial port: write 1 " );
        System.arraycopy( buf , 0 , buffer , pos , buf.length );
        pos += buf.length;
    }

    public void write(byte[] buf,int start,int len) throws java.io.IOException{
        ZenProperties.logger.log("-------------------------Serial port: write 2 " );
        System.arraycopy( buf , start , buffer , pos , len );
        pos += len;
    }
}
