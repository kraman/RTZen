package edu.uci.ece.zen.utils;

import java.util.Vector;
import javax.realtime.*;

public class ByteArrayCache{
    private ImmortalMemory imm;
    private Queue byteBuffers;

    private static ByteArrayCache _instance = null;
    public static ByteArrayCache instance(){
        if( _instance == null )
            try{
                _instance = (ByteArrayCache) ImmortalMemory.instance().newInstance( ByteArrayCache.class );
            }catch( Exception e ){
                e.printStackTrace();
                System.exit( -1 );
            }
        return _instance;
    }
    
    private ByteArrayCreator bac = new ByteArrayCreator();
    public ByteArrayCache(){
        try{
            imm = ImmortalMemory.instance();
            byteBuffers = (Queue) imm.newInstance( Queue.class );
        }catch( Exception e ){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public byte[] getByteArray(){
        try{
            byte[] ret = (byte[]) byteBuffers.dequeue();
            if( ret == null ){
                return (byte[]) imm.newArray( byte.class , 1024 );
            }else{
                return ret;
            }
        }catch( Exception e ){
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    public void returnByteArray( byte[] buf ){
        byteBuffers.enqueue( buf );
    }
}

class ByteArrayCreator implements Runnable{
    byte[] retval;

    public void run(){
        retval = new byte[1024];
    }
    
    byte[] getByteArray(){
        return retval;
    }
}
