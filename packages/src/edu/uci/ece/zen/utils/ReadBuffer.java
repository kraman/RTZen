/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 *
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.utils;

import java.util.Vector;
import javax.realtime.ImmortalMemory;

public class ReadBuffer {
    /**Immortal memory cache of ReadBuffer objects*/
    private static Queue bufferCache;

    /**Length of BYTE datatype=1*/
    private static final int BYTE = 1;
    /**Length of SHORT datatype=2*/
    private static final int SHORT = 2;
    /**Length of LONG datatype=4*/
    private static final int LONG = 4;
    /**Length of LONGLONG datatype=8*/
    private static final int LONGLONG = 8;

    /**Maximum number of byte[] that can be stored in a ReadBuffer*/
    private static int maxCap = 10;
    /**Number of ReadBuffer objects to preallocate*/
    private static int preAlloc = 10;
    /**Array to maintain all byte[]*/
    private byte[][] buffers;
    /**Number of byte[] currently in use by this ReadBuffer*/
    private int numBuffers;
    /**True if this ReadBuffer deals with little endian data*/
    private boolean isLittleEndian;
    /**Holds the current byte[] idx*/
    private int curBuffer;
    /**Holds the current position of read pointer within curBuffer*/
    private int curBufferPos;
    
    /**Overall position of read pointer within whole ReadBuffer*/
    long position;
    /**Overall amount of data stored within this ReadBuffer*/
    long limit;
    /**Overall capacity in this ReadBuffer. capacity = numBuffers*bufferSize*/
    long capacity;

    static {
        try {
            maxCap = Integer.parseInt(ZenProperties.getGlobalProperty( "readbuffer.size" , "20" ));
            preAlloc = Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.orb.readBuffer.preAllocate" , "20" ) );
            bufferCache = (Queue) ImmortalMemory.instance().newInstance( Queue.class);
            ReadBuffer.preAllocate( preAlloc );
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, ReadBuffer.class, "static <init>", e);
            System.exit(-1);
        }
    }

    /**Private method to preallocate a used defined number of ReadBuffer objects*/
    private static void preAllocate( int num ){
        try{
            for( int i=0;i<num;i++ ){
                ZenProperties.logger.log(Logger.INFO, ReadBuffer.class, "preAllocate", "Creating new instance.");
                ReadBuffer rb = (ReadBuffer) ImmortalMemory.instance().newInstance(ReadBuffer.class);
                bufferCache.enqueue( rb );
            }
        }catch( Exception e ){
            ZenProperties.logger.log(Logger.WARN, ReadBuffer.class, "preAllocate", "Unable to pre-allocate");
        }
    }

    /**Returns a cached instance of the ReadBuffer object or creates a new one if no cached object is available*/
    public static ReadBuffer instance() {
        ReadBuffer rb = (ReadBuffer) Queue.getQueuedInstance( ReadBuffer.class , bufferCache );
        rb.init();
        return rb;
    }

    /**Release an instance of the ReadBuffer and add it back into the cache*/
    private static void release(ReadBuffer self) {
        bufferCache.enqueue(self);
    }

    /**Default constructor of the ReadBuffer*/
    public ReadBuffer() {
        try {
            buffers = new byte[maxCap][];
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, getClass(), "<init>", e);
            System.exit(-1);
        }
    }

    /**Default initializer of ReadBuffer*/
    public void init() {
        position = capacity = limit = 0;
        peekString = null;
        peekStringPos = -1;
        ensureCapacity(1);

        curBuffer = 0;
        curBufferPos = 0;
    }

    public void free() {
        ByteArrayCache cache = ByteArrayCache.instance();
        for (int i = 0; i < numBuffers; i++){
            cache.returnByteArray( buffers[i] );
        }
        numBuffers = 0;
        ReadBuffer.release(this);
    }

    public void freeWithoutBufferRelease() {
        ReadBuffer.release(this);
    }

    public void resetMessagePosition() {
        peekString = null;
        peekStringPos = -1;
        position = 0;
        curBufferPos = 0;
        curBuffer = 0;
    }

    private void ensureCapacity(int size) {
        if (size <= 0) return;
        while (limit+size > capacity) {
            if( numBuffers+1 >= maxCap ){
                ZenProperties.logger.log(Logger.FATAL, ReadBuffer.class, "ensureCapacity", "Reached maximum buffer capacity. Try adjusting " +
                        "readbuffer.size property. Current value is: " + maxCap);
                System.exit(-1);
            }

            byte[] byteArray = ByteArrayCache.instance().getByteArray();
            buffers[numBuffers++] = byteArray;
            capacity += byteArray.length;
       }
    }

    public long getPosition() {
        return position;
    }

    public long getLimit() {
        return limit;
    }

    private void pad(int boundry) {
        // The CDR alignment should count from the beginning of GIOP header.
        // But the GIOP header is excluded in CDRInputStream position. So 12
        // must be added. Yue Zhang on 09.22pm, 08/01/2004
        int extraBytesUsed = (int) ((position+12) % boundry);
        
        if (extraBytesUsed != 0) {
            int incr = boundry - extraBytesUsed;
            position += incr;
        }

        curBuffer = (int)position/1024;
        curBufferPos = (int)position%1024;
    }

    public void appendFromStream(java.io.InputStream stream, int numBytes) {
        try {
            ensureCapacity(numBytes);
            while (numBytes > 0) {
                int readBytes = numBytes;
                if (readBytes > 1024 - (int) (limit % 1024))
                    readBytes = 1024 - (int) (limit % 1024);
                numBytes -= readBytes;

                while (readBytes > 0) {
                    int read = stream.read(buffers[(int)limit/1024], (int)limit%1024, readBytes);
                    readBytes -= read;
                    limit += read;
                }
            }
        } catch (java.io.IOException ioex) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "appendFromStream", ioex);
        }
    }

    public void writeByteArray(byte[] v, int offset, int length) {
        ensureCapacity(length);
        while (length > 0) {
            byte[] buffer = buffers [(int)limit/1024];
            int curBufPos = (int) (limit%1024);
            int copyLength = 1024 - curBufPos;
            if (copyLength > length) copyLength = length;
            System.arraycopy(v, offset, buffer, curBufPos, copyLength);
            offset += copyLength;
            length -= copyLength;
            limit += copyLength;
        }
    }

    public void setEndian(boolean isLittleEndian) {
        this.isLittleEndian = isLittleEndian;
    }
    
    public byte readByte() {
        pad(ReadBuffer.BYTE);
        
        byte ret = buffers[curBuffer][curBufferPos];
        ++position;
        ++curBufferPos;
        return ret;
    }

    public void readByteArray(byte[] v, int offset, int length) {
        while(length>0){
            int bytesLeft = 1024 - curBufferPos;
            int bytesToCopy = bytesLeft > length ? length : bytesLeft;
            System.arraycopy( buffers[curBuffer] , curBufferPos , v , offset, bytesToCopy );
            
            length -= bytesToCopy;
            position += bytesToCopy;
            offset += bytesToCopy;
            curBufferPos += bytesToCopy;

            if( curBufferPos >= 1024 ){
                curBuffer++;
                curBufferPos = 0;
            }
        }
    }

    public short readShort() {
        pad(ReadBuffer.SHORT);

        byte b1 = buffers[curBuffer][curBufferPos++];
        byte b2 = buffers[curBuffer][curBufferPos++];
        position+=2;

        short ret = 0;
        if (isLittleEndian) {
            ret |= b2 & 0xFF;
            ret <<= 8;
            ret |= b1 & 0xFF;
            return ret;
        } else {
            ret |= b1 & 0xFF;
            ret <<= 8;
            ret |= b2 & 0xFF;
            return ret;
        }
    }

    public int readLong() {
        pad(ReadBuffer.LONG);
        byte b1 = buffers[curBuffer][curBufferPos++];
        byte b2 = buffers[curBuffer][curBufferPos++];
        byte b3 = buffers[curBuffer][curBufferPos++];
        byte b4 = buffers[curBuffer][curBufferPos++];
        position+=4;

        int ret = 0;
        if (isLittleEndian) {
            ret |= b4 & 0xFF;
            ret <<= 8;
            ret |= b3 & 0xFF;
            ret <<= 8;
            ret |= b2 & 0xFF;
            ret <<= 8;
            ret |= b1 & 0xFF;
            return ret;
        } else {
            ret |= b1 & 0xFF;
            ret <<= 8;
            ret |= b2 & 0xFF;
            ret <<= 8;
            ret |= b3 & 0xFF;
            ret <<= 8;
            ret |= b4 & 0xFF;
            //System.err.println( "Long is " + ret );
            return ret;
        }
    }

    public long readLongLong() {
        pad(ReadBuffer.LONGLONG);
        byte b1 = buffers[curBuffer][curBufferPos++];
        byte b2 = buffers[curBuffer][curBufferPos++];
        byte b3 = buffers[curBuffer][curBufferPos++];
        byte b4 = buffers[curBuffer][curBufferPos++];
        byte b5 = buffers[curBuffer][curBufferPos++];
        byte b6 = buffers[curBuffer][curBufferPos++];
        byte b7 = buffers[curBuffer][curBufferPos++];
        byte b8 = buffers[curBuffer][curBufferPos++];
        position+=4;

        long ret = 0;
        if (isLittleEndian) {
            ret += ((long) (b8 & 0xFF)) << 56;
            ret += ((long) (b7 & 0xFF)) << 48;
            ret += ((long) (b6 & 0xFF)) << 40;
            ret += ((long) (b5 & 0xFF)) << 32;
            ret += ((long) (b4 & 0xFF)) << 24;
            ret += ((long) (b3 & 0xFF)) << 16;
            ret += ((long) (b2 & 0xFF)) << 8;
            ret += ((long) (b1 & 0xFF)) << 0;
            return ret;
        } else {
            ret += ((long) (b1 & 0xFF)) << 56;
            ret += ((long) (b2 & 0xFF)) << 48;
            ret += ((long) (b3 & 0xFF)) << 40;
            ret += ((long) (b4 & 0xFF)) << 32;
            ret += ((long) (b5 & 0xFF)) << 24;
            ret += ((long) (b6 & 0xFF)) << 16;
            ret += ((long) (b7 & 0xFF)) << 8;
            ret += ((long) (b8 & 0xFF)) << 0;

            return ret;
        }
    }

    public FString readFString(boolean isString) {
        int len = readLong();
        if( isString )
            len--;
        FString fs = FString.instance();
        byte[] tmp = ByteArrayCache.instance().getByteArray();
        readByteArray( tmp , 0 , len );
        fs.append( tmp , 0 , len );
        if (isString) readByte(); //eat an xtra byte if not just byte array
        ByteArrayCache.instance().returnByteArray( tmp );
        return fs;
    }

    public String readString() {
        if (peekString != null && peekStringPos == getPosition()) {
            String tmp = peekString;
            peekString = null;
            peekStringPos = -1;
            return tmp;
        }
        int len = readLong();
        len--;
        byte buf[] = new byte[len];
        readByteArray(buf, 0, len);
        readByte();
        StringBuffer strbuf = new StringBuffer();
        for (int i = 0; i < len; i++)
            strbuf.append((char) buf[i]);
        return strbuf.toString();
    }

    private String peekString;

    private long peekStringPos;

    public String peekString() {
        if (peekString != null && peekStringPos == getPosition()) { return peekString; }
        int len = readLong();
        len--;
        byte buf[] = new byte[len];
        readByteArray(buf, 0, len);
        readByte();
        peekString = new String(buf);
        peekStringPos = getPosition();
        return peekString;
    }

    public void dumpBuffer(WriteBuffer out) {
        for (int i = 0; i < limit / 1024 - 1; i++) {
            out.writeByteArray(buffers[i], 0, 1024);
        }
        out.writeByteArray(buffers[ (int)limit/1024], 0, (int) (limit % 1024));
    }

    /**
     * Points to next Read buffer in linked list of ReadBuffers. Used by GIOP
     * v1.1 fragments.
     */
    private ReadBuffer nextBuffer = null;

    /**
     * Set the buffer that is read from after this buffer is used up. Used by
     * GIOP v1.1 fragments.
     */
    public void setNextBuffer(ReadBuffer aNextBuffer) {
        nextBuffer = aNextBuffer;
    }

    /**
     * Get the buffer that has been set to be read from after this buffer is
     * used up. Used by GIOP v1.1 fragments.
     *
     * @return the next buffer to be read from or null if no next buffer.
     */
    public ReadBuffer getNextBuffer() {
        return nextBuffer;
    }

    public String toString(){
        byte [] newarr = new byte[(int)limit];
        for(int i = 0; i < limit; ++i)
            newarr[i] = buffers[i/1024][i%1024];
        return FString.byteArrayToString(newarr) + "\n\nlimit: " + limit;
    }

    /*
    public void init(Vector buffers, int limit, int capacity) {
        peekString = null;
        peekStringPos = -1;
        this.buffers.removeAllElements();
        for (int i = 0; i < buffers.size(); i++)
            this.buffers.addElement(buffers.elementAt(i));
        position = 0;
        this.capacity = capacity;
        this.limit = limit;
        enableAlignment = true;
    }*/

    public ReadBuffer readBuffer(int length) {
        ReadBuffer out = ReadBuffer.instance();
        byte[] tmpBuf = ByteArrayCache.instance().getByteArray();

        while (length > 0) {
            int copyLen = length > tmpBuf.length ? tmpBuf.length : length;
            readByteArray(tmpBuf, 0, copyLen);
            out.writeByteArray(tmpBuf, 0, copyLen);
            length -= copyLen;
        }
        ByteArrayCache.instance().returnByteArray(tmpBuf);

        return out;
    }
}
