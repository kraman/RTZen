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

    private static Object [] vectorArgTypes;
    private static java.lang.reflect.Constructor vectorConstructor;
    private static int maxCap = 10;

    static {
        try {
            vectorConstructor = Vector.class
                    .getConstructor(new Class [] {int.class});
            vectorArgTypes = new Object[1];
            maxCap = Integer.parseInt(ZenProperties
                .getGlobalProperty( "writebuffer.size" , "10" ));
            vectorArgTypes[0] = new Integer(maxCap);
            bufferCache = (Queue) ImmortalMemory.instance().newInstance(
                    Queue.class);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, WriteBuffer.class, "static <init>", e);
            System.exit(-1);
        }
    }
    private static int numFree = 0;
    static int idgen = -1;
    int id;
    boolean inUse = false;
    public static WriteBuffer instance() {
        try {

            //Thread.dumpStack();
            idgen++;
            numFree--;
            if (bufferCache.isEmpty()){
                WriteBuffer wb =
                (WriteBuffer) ImmortalMemory
                    .instance().newInstance(WriteBuffer.class);
                //System.out.println("WWINST:" + idgen);
                wb.id = idgen;
                wb.inUse = true;
                return wb;
            } else {
                WriteBuffer wb = (WriteBuffer) bufferCache.dequeue();
                //System.out.println("WWINST:" + idgen);
                wb.id = idgen;
                wb.inUse = true;
                return wb;
            }
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, WriteBuffer.class, "instance", e);
            System.exit(-1);
        }
        return null;
    }

    private static void release(WriteBuffer self) {
        bufferCache.enqueue(self);
    }

    private Vector buffers;

    private boolean isLittleEndian = false;

    long position;

    long limit;

    long capacity;

    public WriteBuffer() {
        try {
            buffers = (Vector) ImmortalMemory.instance().newInstance(
                    vectorConstructor, vectorArgTypes);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, getClass(), "<init>", e);
            System.exit(-1);
        }
    }
/*
    public void printBuffer() {

        ZenProperties.logger.log("Here is in the printBuffer of WriteBuffer...");
        int i, j;
        for (i = 0; i < buffers.size(); i++) {
            byte[] buffer = (byte[]) buffers.elementAt(i);
            for (j = 0; j < buffer.length; j++) {
                ZenProperties.logger.log(buffer[j]);
            }
        }

    }
*/
    public void init() {
        position = limit = capacity = 0;
        buffers.removeAllElements();
    }

    public void free() {

        if(!inUse){
            ZenProperties.logger.log(Logger.WARN, WriteBuffer.class,
                "free",
                "Buffer already freed.");
                //System.exit(-1);
                //still deciding what to do here
            return;
        }
        //Thread.dumpStack();
        //System.out.println("WWFREE:" + id);

        ByteArrayCache cache = ByteArrayCache.instance();
        for (int i = 0; i < buffers.size(); i++){
            cache.returnByteArray((byte[]) buffers.elementAt(i));
            ba--;
        }

         if(ZenProperties.memDbg1) System.out.write('b');
         if(ZenProperties.memDbg1) System.out.write('w');
        if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(ba);
        if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(buffers.size());
        if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(bs);

        buffers.removeAllElements();
        init();
        WriteBuffer.release(this);
        numFree++;

        if(ZenProperties.memDbg1) System.out.write('w');
        if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(numFree);

       inUse = false;
    }

    public String toString(){

        byte [] newarr = new byte[(int)position];
        byte [] buf = (byte[]) buffers.elementAt((int) (position / 1024));

        for(int i = 0; i < position; ++i)
            newarr[i] = buf[i];

        return FString.byteArrayToString(newarr) + "\n\nposition: " + position;
    }

    public void setEndian(boolean isLittleEndian) {
        this.isLittleEndian = isLittleEndian;
    }
    int ba = 0;
    int bs = 0;
    private void ensureCapacity(int size) {
        if (size <= 0) return;
        while (position + size > capacity) {
//         edu.uci.ece.zen.utils.Logger.write(position);
//         edu.uci.ece.zen.utils.Logger.write(size);
//         edu.uci.ece.zen.utils.Logger.write(capacity);

           //Thread.dumpStack();
           edu.uci.ece.zen.utils.Logger.printMemStatsImm(511);
            byte[] byteArray = ByteArrayCache.instance().getByteArray();
            edu.uci.ece.zen.utils.Logger.printMemStatsImm(512);
            capacity += byteArray.length;
            if(buffers.size()+1 >= maxCap){
                ZenProperties.logger.log(Logger.FATAL, WriteBuffer.class,
                    "ensureCapacity",
                    "Reached maximum buffer capacity. Try adjusting " +
                    "writebuffer.size property. Current value is: " + maxCap);
                    //still deciding what to do here
            }
            buffers.addElement(byteArray);
            ba++;
        }
        bs = buffers.size();
    }

    private void pad(int boundry) {
        /*
        int extraBytesUsed = (int) (position % boundry);

        if (extraBytesUsed != 0) {
            int incr = boundry - extraBytesUsed;
            ensureCapacity(incr);
            position += incr;
            if (limit < position) limit = position;
        }
        */
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
        ensureCapacity((int) (capacity - position));
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
        ensureCapacity(1);
        byte[] buffer = (byte[]) buffers.elementAt((int) (position / 1024));
        buffer[(int) (position % 1024)] = v;
        ++position;
        if (position > limit) limit = position;
    }

    public void writeByteArray(byte[] v, int offset, int length) {
        edu.uci.ece.zen.utils.Logger.printMemStatsImm(505);
        ensureCapacity(length);
        edu.uci.ece.zen.utils.Logger.printMemStatsImm(506);
        while (length > 0) {
            byte[] buffer = (byte[]) buffers.elementAt((int) (position / 1024));
            int curBufPos = (int) (position % 1024);
            int copyLength = 1024 - curBufPos;
            if (copyLength > length) copyLength = length;
//        edu.uci.ece.zen.utils.Logger.printMemStatsImm(507);
            System.arraycopy(v, offset, buffer, curBufPos, copyLength);
            offset += copyLength;
            length -= copyLength;
            position += copyLength;
       }
        if (position > limit) limit = position;
    }

    public void dumpBuffer(WriteBuffer out) {
        //System.out.println( "----GIOP MSG START---" );
        for (int i = 0; i < position / 1024; i++) {
            out.writeByteArray((byte[]) buffers.elementAt(i), 0, 1024);
            //System.out.write( (byte[])buffers.elementAt(i) , 0 , 1024 );
        }
        out.writeByteArray((byte[]) buffers
                .elementAt(((int) (position / 1024))), 0,
                (int) (position % 1024));
        //System.out.write( (byte[])buffers.elementAt(((int)(position/1024))) ,
        // 0 , (int)(position%1024) );
        //System.out.flush();
        //System.out.println( "\n----GIOP MSG END---" );
    }

    private void dumpByteArray(byte[] arr, int off, int len,
            java.io.OutputStream out) throws java.io.IOException {
        out.write(arr, off, len);
        //System.err.write( arr , off , len );
    }

    public void dumpBuffer(java.io.OutputStream out) throws java.io.IOException {
        //System.err.println( "----BEGIN GIOP MESSAGE----" + position );
        for (int i = 0; i < position / 1024; i++) {
            //System.out.println( "Writing buffer " + i + " from 0 to 1024" );
            dumpByteArray((byte[]) buffers.elementAt(i), 0, 1024, out);
            //System.out.write( (byte[])buffers.elementAt(i) , 0 , 1024 );
        }
        //System.out.println( "Writing buffer " + (position/1024) + " from 0 to
        // " +(position%1024) );
        dumpByteArray((byte[]) buffers.elementAt(((int) (position / 1024))), 0,
                (int) (position % 1024), out);
        //System.out.write( (byte[])buffers.elementAt(((int)(position/1024))) ,
        // 0 , (int)(position%1024) );
        //System.err.println( "\n----END GIOP MESSAGE----" );
    }

    public void readByteArray(byte[] v, int offset, int length) {
        while (length > 0) {
            byte[] curBuf = (byte[]) buffers.elementAt((int) (position / 1024));
            int curBufPos = (int) position % 1024;
            int bytesLeft = 1024 - curBufPos;
            if (bytesLeft > length) bytesLeft = length;
            System.arraycopy(curBuf, curBufPos, v, offset, bytesLeft);

            length -= bytesLeft;
            position += bytesLeft;
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
            out.writeByteArray((byte[]) buffers.elementAt(i), 0, 1024);
        }
        out.writeByteArray((byte[]) buffers
                .elementAt(((int) (position / 1024))), 0,
                (int) (position % 1024));
        return out;
    }

    public ReadBuffer getReadBufferAndFree() {
        ReadBuffer ret = getReadBuffer();
        free();
        return ret;
    }

    public void writeShort(short v) {
        pad(WriteBuffer.SHORT);

        byte b1 = (byte) ((v >>> 8) & 0xFF);
        byte b2 = (byte) (v & 0xFF);

        if (isLittleEndian) {
            writeByte(b2);
            writeByte(b1);
        } else {
            writeByte(b1);
            writeByte(b2);
        }
    }

    public void writeLong(int v) {
        pad(WriteBuffer.LONG);
        byte b1 = (byte) ((v >>> 24) & 0xFF);
        byte b2 = (byte) ((v >>> 16) & 0xFF);
        byte b3 = (byte) ((v >>> 8) & 0xFF);
        byte b4 = (byte) (v & 0xFF);
        //System.out.println( "" + ((int)b1) + " " + ((int)b2) + " " +
        // ((int)b3) + " " + ((int)b4) );
        if (isLittleEndian) {
            writeByte(b4);
            writeByte(b3);
            writeByte(b2);
            writeByte(b1);
        } else {
            writeByte(b1);
            writeByte(b2);
            writeByte(b3);
            writeByte(b4);
        }
    }

    public void writeLongLong(long v) {
        //  System.out.println("If translated to double, it's
        // "+Double.longBitsToDouble(v));

        pad(WriteBuffer.LONGLONG);
        byte b1 = (byte) ((v >>> 56) & 0xFF);
        if (ZenProperties.dbg) ZenProperties.logger.log(b1 + "");
        byte b2 = (byte) ((v >>> 48) & 0xFF);
        byte b3 = (byte) ((v >>> 40) & 0xFF);
        byte b4 = (byte) ((v >>> 32) & 0xFF);
        byte b5 = (byte) ((v >>> 24) & 0xFF);
        byte b6 = (byte) ((v >>> 16) & 0xFF);
        byte b7 = (byte) ((v >>> 8) & 0xFF);
        byte b8 = (byte) (v & 0xFF);

        if (isLittleEndian) {
            writeByte(b8);
            writeByte(b7);
            writeByte(b6);
            writeByte(b5);
            writeByte(b4);
            writeByte(b3);
            writeByte(b2);
            writeByte(b1);
        } else {
            writeByte(b1);
            writeByte(b2);
            writeByte(b3);
            writeByte(b4);
            writeByte(b5);
            writeByte(b6);
            writeByte(b7);
            writeByte(b8);
        }
    }

    public boolean equals(WriteBuffer rhs) {
        try {
            int lastIdx = (int) (position / 1024);
            for (int i = 0; i < lastIdx; i++)
                if (!java.util.Arrays.equals((byte[]) buffers.elementAt(i),
                        (byte[]) rhs.buffers.elementAt(i))) return false;
            byte[] lhs_lastbuf = (byte[]) buffers.elementAt(lastIdx);
            byte[] rhs_lastbuf = (byte[]) rhs.buffers.elementAt(lastIdx);
            int posInBuf = (int) (position % 1024);
            for (int i = 0; i < posInBuf; i++)
                if (lhs_lastbuf[i] != rhs_lastbuf[i]) return false;
            return true;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    private long mementoPosition;

    public void setLocationMemento() {
        mementoPosition = position;
        pad(WriteBuffer.BYTE);
        pad(WriteBuffer.BYTE);
    }

    public void writeLongAtLocationMemento(int val) {
        long tmp = position;
        position = mementoPosition;
        //System.out.println( "\n\nlen to write: " + val );
        //writeByte((byte) (val & 0xff));

        byte b1 = (byte) ((val >>> 8) & 0xFF);
        byte b2 = (byte) (val & 0xFF);

        writeByte(b1);
        writeByte(b2);

        position = tmp;
    }
}
