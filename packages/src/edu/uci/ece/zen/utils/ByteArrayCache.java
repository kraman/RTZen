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
        System.out.println( "byteArrayCache.getByteArray 1" );
        try{
        System.out.println( "byteArrayCache.getByteArray 2");
            if( byteBuffers.isEmpty() ){
        System.out.println( "byteArrayCache.getByteArray 3");
                //imm.executeInArea( bac );
                return (byte[]) imm.newArray( byte.class , 1024 );
                //return bac.getByteArray();
            }else{
        System.out.println( "byteArrayCache.getByteArray 4");
                return (byte[]) byteBuffers.dequeue();
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
