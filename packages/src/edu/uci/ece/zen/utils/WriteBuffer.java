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
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

public class WriteBuffer {
    private static Queue bufferCache;

    private static int BYTE = 1;
    private static int SHORT = 2;
    private static int LONG = 4;
    private static int LONGLONG = 8;

    private static int maxCap = 10;

    static {
        try {
            maxCap = Integer.parseInt(ZenProperties.getGlobalProperty( "writebuffer.size" , "20" ));
        bufferCache = (Queue) ImmortalMemory.instance().newInstance(Queue.class);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, WriteBuffer.class, "static <init>", e);
            System.exit(-1);
        }
    }

    public static WriteBuffer instance() {
        WriteBuffer rb = (WriteBuffer) Queue.getQueuedInstance( WriteBuffer.class , bufferCache );
        return rb;
    }

    private static void release(WriteBuffer self) {
        bufferCache.enqueue(self);
    }

    private byte buffers[][];
    int numBuffers;
    private boolean isLittleEndian = false;
    long position;
    long limit;
    long capacity;

    int curBuffer;
    int curBufferPos;

    public WriteBuffer() {
        try {
            buffers = new byte[WriteBuffer.maxCap][];
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, getClass(), "<init>", e);
            System.exit(-1);
        }
    }

    public void init() {
        position = limit = capacity = 0;
        numBuffers = 0;
        profileLength = 0;
        curBuffer=0;
        curBufferPos=0;
    }

    public void free() {
        ByteArrayCache cache = ByteArrayCache.instance();
        for (int i = 0; i < numBuffers; i++){
            cache.returnByteArray(buffers[i]);
        }
        numBuffers = 0;
        WriteBuffer.release(this);
    }

    public String toString(){
        byte [] newarr = new byte[(int)limit];
        for(int i = 0; i < limit; ++i)
            newarr[i] = buffers[(int) (i / 1024)][i%1024];
        return FString.byteArrayToString(newarr) + "\n\nposition: " + position;
    }

    public void setEndian(boolean isLittleEndian) {
        this.isLittleEndian = isLittleEndian;
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

    private void pad(int boundry) {
        ensureCapacity(WriteBuffer.LONGLONG);
        int extraBytesUsed = (int) ((position) % boundry);
        
        if (extraBytesUsed != 0) {
            int incr = boundry - extraBytesUsed;
            position += incr;
        }

        curBuffer = (int)position/1024;
        curBufferPos = (int)position%1024;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
        curBuffer = (int) position/1024;
        curBufferPos = (int) position%1024;
        ensureCapacity((int) (position-limit));
        if (position > limit) limit = position;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
        ensureCapacity((int) (capacity - limit));
    }

    public void writeByte(byte v) {
        ensureCapacity(WriteBuffer.BYTE);
        pad(WriteBuffer.BYTE);
        buffers[curBuffer][curBufferPos] = v;
        curBufferPos++;
        position++;
        if (position > limit) limit = position;
    }

    public void writeByteArray(byte[] v, int offset, int length) {
        ensureCapacity(length);
        while (length > 0) {
            int copyLength = 1024 - curBufferPos;
            if (copyLength > length) copyLength = length;

            System.arraycopy( v, offset, buffers[curBuffer], curBufferPos, copyLength);
            offset += copyLength;
            length -= copyLength;
            position += copyLength;

            if( curBufferPos >= 1024 ){
                curBuffer++;
                curBufferPos=0;
            }
       }
       if (position > limit) limit = position;
    }

    public void dumpBuffer(WriteBuffer out) {
        for (int i = 0; i < position / 1024; i++) {
            out.writeByteArray((byte[]) buffers[i], 0, 1024);
        }
        if( position % 1024 != 0 )
            out.writeByteArray((byte[]) buffers[(int) (position / 1024)], 0, (int) (position % 1024));
    }

    private void dumpByteArray(byte[] arr, int off, int len, java.io.OutputStream out) throws java.io.IOException {
        out.write(arr, off, len);
    }

    public void dumpBuffer(java.io.OutputStream out) throws java.io.IOException {
        for (int i = 0; i < position / 1024; i++) {
            dumpByteArray((byte[]) buffers[i], 0, 1024, out);
        }
        if( position % 1024 != 0 )
            dumpByteArray((byte[]) buffers[(int) (position / 1024)], 0, (int) (position % 1024), out);
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

    public ReadBuffer getReadBuffer() {
        ReadBuffer out = ReadBuffer.instance();
        out.init();
        for (int i = 0; i < position / 1024 - 1; i++) {
            out.writeByteArray((byte[]) buffers[i], 0, 1024);
        }
        if( position % 1024 != 0 )
            out.writeByteArray((byte[]) buffers[(int) (position / 1024)] , 0, (int) (position % 1024));
        return out;
    }

    public ReadBuffer getReadBufferAndFree() {
        ReadBuffer ret = getReadBuffer();
        free();
        return ret;
    }

    public void writeShort(short v) {
        ensureCapacity(WriteBuffer.SHORT);
        pad(WriteBuffer.SHORT);

        byte b1 = (byte) ((v >>> 8) & 0xFF);
        byte b2 = (byte) (v & 0xFF);

        if (isLittleEndian) {
            buffers[curBuffer][curBufferPos++] = b2;
            buffers[curBuffer][curBufferPos++] = b1;
        } else {
            buffers[curBuffer][curBufferPos++] = b1;
            buffers[curBuffer][curBufferPos++] = b2;
        }
        position+=2;
        if (position > limit) limit = position;
    }

    public void writeLong(int v) {
        ensureCapacity(WriteBuffer.LONG);
        pad(WriteBuffer.LONG);
        
        byte b1 = (byte) ((v >>> 24) & 0xFF);
        byte b2 = (byte) ((v >>> 16) & 0xFF);
        byte b3 = (byte) ((v >>> 8) & 0xFF);
        byte b4 = (byte) (v & 0xFF);
        
        if (isLittleEndian) {
            buffers[curBuffer][curBufferPos++] = b4;
            buffers[curBuffer][curBufferPos++] = b3;
            buffers[curBuffer][curBufferPos++] = b2;
            buffers[curBuffer][curBufferPos++] = b1;
        } else {
            buffers[curBuffer][curBufferPos++] = b1;
            buffers[curBuffer][curBufferPos++] = b2;
            buffers[curBuffer][curBufferPos++] = b3;
            buffers[curBuffer][curBufferPos++] = b4;
        }
        position+=4;
        if (position > limit) limit = position;
    }

    public void writeLongLong(long v) {
        ensureCapacity(WriteBuffer.LONGLONG);
        pad(WriteBuffer.LONGLONG);

        byte b1 = (byte) ((v >>> 56) & 0xFF);
        byte b2 = (byte) ((v >>> 48) & 0xFF);
        byte b3 = (byte) ((v >>> 40) & 0xFF);
        byte b4 = (byte) ((v >>> 32) & 0xFF);
        byte b5 = (byte) ((v >>> 24) & 0xFF);
        byte b6 = (byte) ((v >>> 16) & 0xFF);
        byte b7 = (byte) ((v >>> 8) & 0xFF);
        byte b8 = (byte) (v & 0xFF);

        if (isLittleEndian) {
            buffers[curBuffer][curBufferPos++] = b8;
            buffers[curBuffer][curBufferPos++] = b7;
            buffers[curBuffer][curBufferPos++] = b6;
            buffers[curBuffer][curBufferPos++] = b5;
            buffers[curBuffer][curBufferPos++] = b4;
            buffers[curBuffer][curBufferPos++] = b3;
            buffers[curBuffer][curBufferPos++] = b2;
            buffers[curBuffer][curBufferPos++] = b1;
        } else {
            buffers[curBuffer][curBufferPos++] = b1;
            buffers[curBuffer][curBufferPos++] = b2;
            buffers[curBuffer][curBufferPos++] = b3;
            buffers[curBuffer][curBufferPos++] = b4;
            buffers[curBuffer][curBufferPos++] = b5;
            buffers[curBuffer][curBufferPos++] = b6;
            buffers[curBuffer][curBufferPos++] = b7;
            buffers[curBuffer][curBufferPos++] = b8;
        }
        position+=8;
        if (position > limit) limit = position;
    }

    public boolean equals(WriteBuffer rhs) {
        try {
            int lastIdx = (int) (position / 1024);
            for (int i = 0; i < lastIdx; i++)
                if (!java.util.Arrays.equals((byte[]) buffers[i], (byte[]) rhs.buffers[i])) return false;
            if( position % 1024 != 0 ){
                byte[] lhs_lastbuf = (byte[]) buffers[lastIdx];
                byte[] rhs_lastbuf = (byte[]) rhs.buffers[lastIdx];
                int posInBuf = (int) (position % 1024);
                for (int i = 0; i < posInBuf; i++)
                    if (lhs_lastbuf[i] != rhs_lastbuf[i]) return false;
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    private long mementoPosition;

    public void setLocationMemento() {
        pad(WriteBuffer.LONG);
        mementoPosition = position;
    }

    public void writeLongAtLocationMemento(int val) {
        long tmp = position;
        setPosition( mementoPosition );
        writeLong(val);
        setPosition( tmp );
    }

    private long profileLengthPosition;
    private int profileLength;

    public void setProfileLengthMemento() {
        pad(WriteBuffer.LONG);
        profileLengthPosition = position;
        writeLong(0);
    }

    public void writeLongAtProfileLengthMemento(int val) {
        profileLength += val;
        long tmp = position;
        setPosition( profileLengthPosition );
        writeLong(profileLength);
        setPosition( tmp );
    }
}
