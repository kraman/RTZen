package edu.uci.ece.zen.utils;

import javax.realtime.*;

public class FString{
    public FString(){}
    public FString( int maxSize ){
        this.maxSize = maxSize;
        this.currentSize = 0;
        this.data = new byte[maxSize];
    }

    int maxSize;
    int currentSize;
    byte[] data;

    public void init( int maxSize ) throws InstantiationException,IllegalAccessException{
        this.maxSize = maxSize;
        this.currentSize = 0;
        this.data = (byte[]) MemoryArea.getMemoryArea(this).newArray( byte.class , maxSize );
    }

    public void append( byte[] data ){
        append( data , 0 , data.length );
    }

    public void append( byte[] data , int offset , int length ){
        if( currentSize + length < maxSize ){
            //KLUDGE: ERROR here
        }
        System.arraycopy( data , offset , this.data , currentSize , length );
        currentSize += length;
    }

    public void append( String str ){
        append( str.getBytes() , 0 , str.length() );
    }

    public void append( byte b ){
        data[currentSize++] = b;
    }

    public void append( char c ){
        data[currentSize++] = (byte) c;
    }


    public byte[] getData(){
        return this.data;
    }

    public int length(){
        return currentSize;
    }

    public byte[] getTrimData(){
        byte[] ret = new byte[currentSize];
        System.arraycopy( data , 0 , ret , 0 , currentSize );
        return ret;
    }

    public byte[] getTrimData( MemoryArea mem ){
        try{
            byte[] ret = (byte[]) mem.newArray( byte.class , currentSize );
            System.arraycopy( data , 0 , ret , 0 , currentSize );
            return ret;
        }catch( InstantiationException e1 ){
             ZenProperties.logger.log(
                Logger.SEVERE,
                "edu.uci.ece.zen.utils.FString",
                "getTrimdata",
                "Could not initialize String due to exception: " + e1.toString()
                );
        }catch( IllegalAccessException e2 ){
             ZenProperties.logger.log(
                Logger.SEVERE,
                "edu.uci.ece.zen.utils.FString",
                "getTrimdata",
                "Could not initialize String due to exception: " + e2.toString()
                );
        }
        return null;
    }

    public void reset(){
        currentSize = 0;
    }
}
