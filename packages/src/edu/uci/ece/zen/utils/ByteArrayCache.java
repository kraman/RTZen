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
            if( byteBuffers.isEmpty() )
                return (byte[]) imm.newArray( byte.class , 1024 );
            else
                return (byte[]) byteBuffers.dequeue();
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
