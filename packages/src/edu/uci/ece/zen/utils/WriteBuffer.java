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
        init();
        WriteBuffer.release( this );
    }

    public void setEndian( boolean isLittleEndian ){
        this.isLittleEndian = isLittleEndian;
    }

    private void ensureCapacity( int size ){
        if( size <= 0 )
            return;
        while( position + size > capacity ){
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
        while( length > 0 ){
            byte[] buffer = (byte[]) buffers.elementAt((int)(position/1024));
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
        //System.out.println( "----GIOP MSG START---" );
        for( int i=0;i<position/1024;i++ ){
            out.writeByteArray( (byte[])buffers.elementAt(i) , 0 , 1024 );
            //System.out.write( (byte[])buffers.elementAt(i) , 0 , 1024 );
        }
        out.writeByteArray( (byte[])buffers.elementAt(((int)(position/1024))) , 0 , (int)(position%1024) );
        //System.out.write( (byte[])buffers.elementAt(((int)(position/1024))) , 0 , (int)(position%1024) );
        //System.out.flush();
        //System.out.println( "\n----GIOP MSG END---" );
    }

    private void dumpByteArray( byte[] arr , int off , int len , java.io.OutputStream out ) throws java.io.IOException{
        out.write( arr , off , len );
        //System.err.write( arr , off , len );
    }

    public void dumpBuffer( java.io.OutputStream out ) throws java.io.IOException{
        //System.err.println( "----BEGIN GIOP MESSAGE----" + position );
        for( int i=0;i<position/1024;i++ ){
            //System.out.println( "Writing buffer " + i + " from 0 to 1024" );
            dumpByteArray( (byte[])buffers.elementAt(i) , 0 , 1024 , out );
            //System.out.write( (byte[])buffers.elementAt(i) , 0 , 1024 );
        }
        //System.out.println( "Writing buffer " + (position/1024) + " from 0 to " +(position%1024) );
        dumpByteArray( (byte[])buffers.elementAt(((int)(position/1024))) , 0 , (int)(position%1024) , out );
        //System.out.write( (byte[])buffers.elementAt(((int)(position/1024))) , 0 , (int)(position%1024) );
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
        out.init();
        for( int i=0;i<position/1024-1;i++ ){
            out.writeByteArray( (byte[])buffers.elementAt(i) , 0 , 1024 );
        }
        out.writeByteArray( (byte[])buffers.elementAt(((int)(position/1024))) , 0 , (int)(position%1024) );
        return out;
    }

    public ReadBuffer getReadBufferAndFree(){
        ReadBuffer ret = getReadBuffer();
        free();
        return ret;
    }

    public void writeShort( short v ){
        pad(WriteBuffer.SHORT);
         System.out.println("In writeShort(), After pad, the position, limit and capacity are "+position+" "+limit+" "+capacity);

        byte b1 = (byte) ((v >>> 8) & 0xFF); 
        byte b2 = (byte) (v & 0xFF);
        System.out.println("In writeShort,b1, b2");
         System.out.println(b1);
        System.out.println(b2);

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
        //System.out.println( "" + ((int)b1) + " " + ((int)b2) + " " + ((int)b3) + " " + ((int)b4) );
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
        System.out.println("This is in writeLongLong");
        System.out.println("The input LongLong value is "+v);
      //  System.out.println("If translated to double, it's "+Double.longBitsToDouble(v));
        System.out.println("Before pad, the position, limit and capacity are "+position+" "+limit+" "+capacity);

        pad(WriteBuffer.LONGLONG);
      System.out.println("After pad, the position, limit and capacity are "+position+" "+limit+" "+capacity);
       System.out.println("Here begin the print of highest write byptes from lowest to highest");
        byte b1 = (byte) ((v >>> 56) & 0xFF);
        System.out.println(b1);
        byte b2 = (byte) ((v >>> 48) & 0xFF);
        byte b3 = (byte) ((v >>> 40) & 0xFF);
        byte b4 = (byte) ((v >>> 32) & 0xFF);
        byte b5 = (byte) ((v >>> 24) & 0xFF);
        byte b6 = (byte) ((v >>> 16) & 0xFF);
	byte b7 = (byte) ((v >>> 8) & 0xFF);
	byte b8 = (byte) (v & 0xFF);
	System.out.println(b1);
	System.out.println(b2);
	System.out.println(b3);
	System.out.println(b4);
	System.out.println(b5);
	System.out.println(b6);
	System.out.println(b7);
	System.out.println(b8);

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
