package edu.uci.ece.zen.orb.transport.serial;

import java.io.*;
import java.util.*;
import edu.oswego.cs.dl.util.concurrent.Mutex;

import javax.realtime.*;

import edu.uci.ece.zen.utils.ZenProperties;
/**
Provides a simplified, universal serial port interface.
*/
public abstract class SerialPort
{
    static final int MAX_MESSAGE_LENGTH = 512;
    public Mutex lock = new Mutex();
    public Transport myTransport = null;

    byte[] tmpBuffer;
    public InputStream istream = new SerialPortInputStream();
    public OutputStream ostream = new SerialPortOutputStream();
    public SerialPort(){
        tmpBuffer = new byte[SerialPort.MAX_MESSAGE_LENGTH];
    }

    /**
    Sends the buffer through the serial port and blocks until all bytes
    in the buffer have been sent.
    */
    public abstract void setMessage(byte[] buffer, int messageLength) throws IOException;

    /**
    Retrieves a message from the serial port, blocking until one is available.
    It will try to read all bytes that are available up to the length of the
    buffer, possibly fewer if no more bytes are available. The caller must
    ensure that the buffer is at least as large as any message sent through
    the serial port.
    @return The number of bytes that were read into the buffer.
    */
    public abstract int getMessage(byte[] buffer) throws IOException;

    public synchronized SerialPort accept(){
        try{
            ZenProperties.logger.log( "++++++++++++Serial port: accept called() " );
            while( true ){
                int size = getMessage( tmpBuffer );
                if (ZenProperties.dbg) {
                    for( int i=0;i<SerialPort.MAX_MESSAGE_LENGTH;i++ )
                        System.out.print( tmpBuffer[i] + "," );
                    System.out.println("");
                }
                if( tmpBuffer[0] == 2 && tmpBuffer[1] == 1 && tmpBuffer[2] == 7 && tmpBuffer[3] == 7 ){
                    ZenProperties.logger.log( "Serial port: accept called(); magic recieved" );
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
}



class SerialPortInputStream extends InputStream{
    byte tmpBuffer[] = new byte[SerialPort.MAX_MESSAGE_LENGTH];
    int pos;
    int limit;
    static final int INIT_POS = 3;

    public int available() throws java.io.IOException{
        return limit-pos;
    }

    void checkAvail(){
        //System.out.println( "Avail: " + limit + " pos: " + pos );
        try{
            if( (limit-pos) == 0 ){
                int tmp=0;
                //System.out.println( "get message from port" );
                while( (tmp = SerialPortFactory.instance().getMessage( tmpBuffer )) <= 0 )
                {
                    //System.out.println( "...." );
                }
                //System.out.println( "tmp _" + tmp );
                pos = INIT_POS;
                //limit = tmpBuffer[1];
                
                limit = 0;
                limit |= tmpBuffer[1] & 0xFF;
                limit <<= 8;
                limit |= tmpBuffer[2] & 0xFF;      
                
                //System.out.println("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRREAD LIMIT: " + limit);
                
            }
        }catch( IOException ioex ){
            ioex.printStackTrace();
        }
    }

    public int read() throws java.io.IOException{
        //ZenProperties.logger.log( "-------------------------Serial port: read 1 " );
        checkAvail();
        return tmpBuffer[pos++];
    }

    public void close() throws java.io.IOException{
        limit=0;
        pos=INIT_POS;
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
    byte[] buffer = new byte[SerialPort.MAX_MESSAGE_LENGTH];
    int pos=SerialPortInputStream.INIT_POS;

    public SerialPortOutputStream(){
        buffer[0]=2;
    }

    public void close() throws java.io.IOException{ pos=SerialPortInputStream.INIT_POS; }
    public void flush() throws java.io.IOException{
        //buffer[1] = (byte) pos;
        buffer[1] = (byte) ((pos >>> 8) & 0xFF);
        buffer[2] = (byte) (pos & 0xFF);        
        //System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWrite POS: " + pos);
        if( pos > SerialPort.MAX_MESSAGE_LENGTH ){
            System.err.println( "Message is WAY TOO BIG. msg size: " + pos + " (Max supported:" + SerialPort.MAX_MESSAGE_LENGTH +")" );
            System.exit(-1);
        }
        SerialPortFactory.instance().setMessage( buffer , SerialPort.MAX_MESSAGE_LENGTH );
        pos=SerialPortInputStream.INIT_POS;
    }

    public void write(int i) throws java.io.IOException{
        buffer[pos++] = (byte)i;
    }

    public void write(byte[] buf) throws java.io.IOException{
        //ZenProperties.logger.log("-------------------------Serial port: write 1 " );
        
        System.arraycopy( buf , 0 , buffer , pos , buf.length );
        pos += buf.length;
    }

    public void write(byte[] buf,int start,int len) throws java.io.IOException{
        //ZenProperties.logger.log("-------------------------Serial port: write 2 " );
        //System.out.println(buf.length + " " + start + " " + buffer.length + " " + len);
        System.arraycopy( buf , start , buffer , pos , len );
        pos += len;
    }
}
