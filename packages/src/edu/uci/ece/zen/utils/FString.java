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

    public void init( int maxSize ){// throws InstantiationException,IllegalAccessException,InaccessibleAreaException{
        this.maxSize = maxSize;
        this.currentSize = 0;
        try{
           // this.data = (byte[]) MemoryArea.getMemoryArea(this).newArray( byte.class , maxSize );
           byte[] dataTemp =  (byte[]) MemoryArea.getMemoryArea(this).newArray( byte.class , maxSize );
           this.data = dataTemp;
        }catch( Exception e ){
            e.printStackTrace();
        }
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

    public void append( long value ){
        data[currentSize++] = (byte) ((value >>> 56) & 0xFF);
        data[currentSize++] = (byte) ((value >>> 48) & 0xFF);
        data[currentSize++] = (byte) ((value >>> 40) & 0xFF);
        data[currentSize++] = (byte) ((value >>> 32) & 0xFF);
        data[currentSize++] = (byte) ((value >>> 24) & 0xFF);
        data[currentSize++] = (byte) ((value >>> 16) & 0xFF);
        data[currentSize++] = (byte) ((value >>> 8) & 0xFF);
        data[currentSize++] = (byte) (value & 0xFF);
    }

    public void append( FString str ){
        append( str.getData() , 0 , str.length() );
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
        }catch( Exception e2 ){
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

    public boolean equals( Object obj ){
        if( obj instanceof FString ){
            FString fobj = (FString) obj;
            if( fobj.length() != length() )
                return false;
            boolean retVal = true;
            for( int i=0;i<length()&&retVal;i++ )
                retVal = retVal && getData()[i] == fobj.getData()[i];
            return retVal;
        }
        return false;
    }
}
