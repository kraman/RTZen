package edu.uci.ece.zen.utils;

import java.util.Vector;
import javax.realtime.*;

public class WriteBuffer{
    private static Queue bufferCache;
    private static int BYTE=1;
    private static int SHORT=2;
    private static int LONG=4;
    private static int LONGLONG=8;

    static{
        try{
            bufferCache = (Queue) ImmortalMemory.instance().newInstance( Queue.class );
        }catch( Exception e ){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static WriteBuffer instance(){
        try{
            if( bufferCache.isEmpty() )
                return (WriteBuffer) ImmortalMemory.instance().newInstance( WriteBuffer.class );
            else
                return (WriteBuffer) bufferCache.dequeue();
        }catch( Exception e ){
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    private static void release( WriteBuffer self ){
        bufferCache.enqueue( self );
    }

    private Vector buffers;
    private boolean isLittleEndian;
    long position;
    long limit;
    long capacity;

    public WriteBuffer(){
        try{
            buffers = (Vector) ImmortalMemory.instance().newInstance( Vector.class );
        }catch( Exception e ){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void init(){
        position = limit = capacity = 0;
        buffers.removeAllElements();
    }

    public void free(){
        ByteArrayCache cache = ByteArrayCache.instance();
        for( int i=0;i<buffers.size();i++ )
            cache.returnByteArray( (byte[]) buffers.elementAt(i) );
        WriteBuffer.release( this );
    }

    public void setEndian( boolean isLittleEndian ){
        this.isLittleEndian = isLittleEndian;
    }

    private void ensureCapacity( int size ){
        if( size <= 0 )
            return;
        if( position + size > capacity ){
            byte[] byteArray = ByteArrayCache.instance().getByteArray();
            capacity += byteArray.length;
            buffers.addElement( byteArray );
        }
    }

    private void pad( int boundry ){
        int extraBytesUsed = (int)(position % boundry);

        if (extraBytesUsed != 0) {
            int incr = boundry - extraBytesUsed;
            ensureCapacity( incr );
            position += incr;
            if( limit < position )
                limit = position;
        }
    }

    public long getPosition(){
        return position;
    }

    public void setPosition( long position ){
        this.position = position;
        ensureCapacity( (int)(capacity-position) );
        if( position > limit )
            limit = position;
    }

    public long getLimit(){
        return limit;
    }

    public void setLimit( long limit ){
        this.limit = limit;
        ensureCapacity( (int)(capacity-limit) );
    }

    public void writeByte( byte v ){
        ensureCapacity(1);
        byte[] buffer = (byte[]) buffers.elementAt((int)(position/1024));
        buffer[(int)(position%1024)] = v;
        ++position;
        if( position > limit )
            limit = position;
    }

    public void writeByteArray( byte[] v , int offset , int length ){
        ensureCapacity(length);
        byte[] buffer = (byte[]) buffers.elementAt((int)(position/1024));
        while( length > 0 ){
            int curBufPos = (int)(position%1024);
            int copyLength = 1024-curBufPos;
            if( copyLength > length )
                copyLength = length;
            System.arraycopy( v , offset , buffer , curBufPos , copyLength );
            offset += copyLength;
            length -= copyLength;
            position += copyLength;
        }
        if( position > limit )
            limit = position;
    }

    public void dumpBuffer( WriteBuffer out ){
        for( int i=0;i<limit/1024-1;i++ ){
            out.writeByteArray( (byte[])buffers.elementAt(i) , 0 , 1024 );
        }
        out.writeByteArray( (byte[])buffers.elementAt(((int)(limit/1024))) , 0 , (int)(limit%1024) );
    }

    private void dumpByteArray( byte[] arr , int off , int len , java.io.OutputStream out ) throws java.io.IOException{
        out.write( arr , off , len );
        //System.err.write( arr , off , len );
    }

    public void dumpBuffer( java.io.OutputStream out ) throws java.io.IOException{
        //System.err.println( "----BEGIN GIOP MESSAGE----" );
        for( int i=0;i<limit/1024-1;i++ ){
            dumpByteArray( (byte[])buffers.elementAt(i) , 0 , 1024 , out );
        }
        dumpByteArray( (byte[])buffers.elementAt(((int)(limit/1024))) , 0 , (int)(limit%1024) , out );
        //System.err.println( "\n----END GIOP MESSAGE----" );
    }

    public void readByteArray( byte[] v , int offset , int length ){
        while( length > 0 ){
            byte[] curBuf = (byte[])buffers.elementAt((int)(position/1024));
            int curBufPos = (int)position%1024;
            int bytesLeft = 1024 - curBufPos;
            if( bytesLeft > length )
                bytesLeft = length;
            System.arraycopy( curBuf , curBufPos , v , offset , bytesLeft );

            length -= bytesLeft;
            position += bytesLeft;
        }
    }

    public ReadBuffer readBuffer( int length ){
        ReadBuffer out = ReadBuffer.instance();
        byte[] tmpBuf = ByteArrayCache.instance().getByteArray();

        while( length > 0 ){
            int copyLen = length > tmpBuf.length ? tmpBuf.length : length;
            readByteArray( tmpBuf , 0 , copyLen );
            out.writeByteArray( tmpBuf , 0 , copyLen );
            length -= copyLen;
        }
        ByteArrayCache.instance().returnByteArray( tmpBuf );

        return out;
    }

    public ReadBuffer getReadBuffer(){
        ReadBuffer out = ReadBuffer.instance();
        for( int i=0;i<position/1024-1;i++ ){
            out.writeByteArray( (byte[])buffers.elementAt(i) , 0 , 1024 );
        }
        out.writeByteArray( (byte[])buffers.elementAt(((int)(position/1024))) , 0 , (int)(position%1024) );
        WriteBuffer.release( this );
        return out;
    }

    //public ReadBuffer getReadBufferAndFree(){
    //    ReadBuffer out = ReadBuffer.instance();
    //    out.init( buffers , position , capacity );
    //    return out;
    //}

    public void writeShort( short v ){
        pad(WriteBuffer.SHORT);
        byte b1 = (byte) ((v >>> 8) & 0xFF); 
        byte b2 = (byte) (v & 0xFF);
        if( isLittleEndian ){
            writeByte( b2 );
            writeByte( b1 );
        }else{
            writeByte( b1 );
            writeByte( b2 );
        }
    }

    public void writeLong( int v ){
        pad(WriteBuffer.LONG);
        byte b1 = (byte) ((v >>> 24) & 0xFF);
        byte b2 = (byte) ((v >>> 16) & 0xFF);
        byte b3 = (byte) ((v >>> 8) & 0xFF);
        byte b4 = (byte) (v & 0xFF);
        if( isLittleEndian ){
            writeByte( b4 );
            writeByte( b3 );
            writeByte( b2 );
            writeByte( b1 );
        }else{
            writeByte( b1 );
            writeByte( b2 );
            writeByte( b3 );
            writeByte( b4 );
        }
    }
    
    public void writeLongLong( long v ){
        pad(WriteBuffer.LONGLONG);
        byte b1 = (byte) ((v >> 56) & 0xFF);
        byte b2 = (byte) ((v >> 48) & 0xFF);
        byte b3 = (byte) ((v >> 40) & 0xFF);
        byte b4 = (byte) ((v >> 32) & 0xFF);
        byte b5 = (byte) ((v >> 24) & 0xFF);
        byte b6 = (byte) ((v >> 16) & 0xFF);
        byte b7 = (byte) ((v >> 8) & 0xFF);
        byte b8 = (byte) (v & 0xFF);
        if( isLittleEndian ){
            writeByte( b8 );
            writeByte( b7 );
            writeByte( b6 );
            writeByte( b5 );
            writeByte( b4 );
            writeByte( b3 );
            writeByte( b2 );
            writeByte( b1 );
        }else{
            writeByte( b1 );
            writeByte( b2 );
            writeByte( b3 );
            writeByte( b4 );
            writeByte( b5 );
            writeByte( b6 );
            writeByte( b7 );
            writeByte( b8 );
        }
    }

    public boolean equals( WriteBuffer rhs ){
        try{
            int lastIdx = (int)(position/1024);
            for( int i=0;i<lastIdx;i++ )
                if( !java.util.Arrays.equals( (byte[]) buffers.elementAt(i) , (byte[]) rhs.buffers.elementAt(i) ) )
                    return false;
            byte[] lhs_lastbuf = (byte[]) buffers.elementAt( lastIdx );
            byte[] rhs_lastbuf = (byte[]) rhs.buffers.elementAt( lastIdx );
            int posInBuf = (int) (position%1024);
            for( int i=0;i<posInBuf;i++ )
                if( lhs_lastbuf[i] != rhs_lastbuf[i] )
                    return false;
            return true;
        }catch( ArrayIndexOutOfBoundsException  ex ){
            return false;
        }
    }

    private long mementoPosition;
    public void setLocationMemento(){
        pad( WriteBuffer.LONG );
        mementoPosition = position;
    }

    public void writeLongAtLocationMemento( int val ){
        long tmp = position;
        position = mementoPosition;
        writeLong( val );
        position = tmp;
    }
}
