package edu.uci.ece.zen.utils;

import java.util.Vector;

import javax.realtime.ImmortalMemory;

public class ReadBuffer {

    private static Queue bufferCache;

    private static int BYTE = 1;
    private static int SHORT = 2;
    private static int LONG = 4;
    private static int LONGLONG = 8;
    private static int numFree = 0; //just to debug the number of free buffers
    private boolean enableAlignment = true;

    private static Object [] vectorArgTypes;
    private static java.lang.reflect.Constructor vectorConstructor;
    private static int maxCap = 10;

    public void setAlignment( boolean align ){
        enableAlignment = align;
    }

    static {
        try {
            vectorConstructor = Vector.class
                    .getConstructor(new Class [] {int.class});
            vectorArgTypes = new Object[1];
            maxCap = Integer.parseInt(ZenProperties
                .getGlobalProperty( "readbuffer.size" , "20" ));
            vectorArgTypes[0] = new Integer(maxCap);
            bufferCache = (Queue) ImmortalMemory.instance().newInstance(
                    Queue.class);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, ReadBuffer.class, "static <init>", e);
            System.exit(-1);
        }
    }
    static int idgen = 0;
    int id;

    boolean inUse = false;
    public static ReadBuffer instance() {
        try {
            //Thread.dumpStack();
                numFree--;
            if (bufferCache.isEmpty()) {
                ReadBuffer rb = (ReadBuffer) ImmortalMemory.instance().newInstance(ReadBuffer.class);
                //rb.id = idgen++;

                rb.inUse = true;
                return rb;
            }else {
                //Thread.dumpStack();
                ReadBuffer ret = (ReadBuffer) bufferCache.dequeue();
                if(ret.inUse)
                    ZenProperties.logger.log(Logger.FATAL, ReadBuffer.class,
                        "free",
                        "Buffer already in use.");
                //ret.previd = ret.id;
                //ret.id = idgen++;
                ret.inUse = true;
                ret.init();
                return ret;
            }
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, ReadBuffer.class, "instance", e);
            System.exit(-1);
        }
        return null;
    }

    private static void release(ReadBuffer self) {
        bufferCache.enqueue(self);
    }

    private Vector buffers;

    private boolean isLittleEndian;

    long position;

    long limit;

    long capacity;

    long nextGIOPInterrupt;

    public ReadBuffer() {
        try {
            buffers = (Vector) ImmortalMemory.instance().newInstance(
                    vectorConstructor, vectorArgTypes);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, getClass(), "<init>", e);
            System.exit(-1);
        }
    }
    public String toString(){

        byte [] newarr = new byte[(int)limit];
        byte [] buf = (byte[]) buffers.elementAt((int) (limit / 1024));

        for(int i = 0; i < limit; ++i)
            newarr[i] = buf[i];

        return FString.byteArrayToString(newarr) + "\n\nlimit: " + limit;
    }
    public void init() {
        position = capacity = limit = 0;
        peekString = null;
        peekStringPos = -1;
        buffers.removeAllElements();
        enableAlignment = true;
    }

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
    }

    public void resetMessagePosition() {
        peekString = null;
        peekStringPos = -1;
        position = 0;
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

    public void appendFromStream(java.io.InputStream stream, int numBytes) {


        try {

            ensureCapacity(numBytes);
            while (numBytes > 0) {
                //System.err.println( "Still need to read " + numBytes + "
                // bytes" );
                int readBytes = numBytes;
                if (readBytes > 1024 - (int) (limit % 1024)) readBytes = 1024 - (int) (limit % 1024);
                //System.err.println( "Going to read " + readBytes + " to fill
                // buffer" );
                numBytes -= readBytes;

                while (readBytes > 0) {
                    int read = stream.read((byte[]) buffers
                            .elementAt((int) (limit / 1024)),
                            ((int) limit % 1024), readBytes);
                    //System.err.println( "Read " + read + " ... " );
                    readBytes -= read;
                    limit += read;
                }

            }

            //System.err.println( "---BEGIN Incomming GIOP message---" );
            //for( int i=0;i<limit/1024-1;i++ ){
            //    System.err.write( (byte[])buffers.elementAt(i) , 0 , 1024 );
            // }
            //System.err.write( (byte[])buffers.elementAt(((int)(limit/1024)))
            // , 0 , (int)(limit%1024) );
            //System.err.println( "\n---END Incomming GIOP message---" );
            //System.out.println( "Limit is " + limit );

        } catch (java.io.IOException ioex) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "appendFromStream", ioex);
        }
    }

    public void writeByteArray(byte[] v, int offset, int length) {
        ensureCapacity(length);
        while (length > 0) {
            byte[] buffer = (byte[]) buffers.elementAt((int) (limit / 1024));
            int curBufPos = (int) (limit % 1024);
            int copyLength = 1024 - curBufPos;
            if (copyLength > length) copyLength = length;
            System.arraycopy(v, offset, buffer, curBufPos, copyLength);
            offset += copyLength;
            length -= copyLength;
            limit += copyLength;
        }
    }

    public void free() {
        /*
        synchronized(ReadBuffer.class){
            System.out.println("freeing id" + id + "prev id" + previd);

            Thread.dumpStack();
        }

        if(position < limit) {
            synchronized(ReadBuffer.class){
                System.out.println("pos: " + position);
                System.out.println("lim: " + limit);
            }
            return;

        }*/

        if(!inUse){
            ZenProperties.logger.log(Logger.WARN, ReadBuffer.class,
                "free",
                "Buffer already freed.");
                //System.exit(-1);
                //still deciding what to do here
            return;
        }

                /*
        System.out.write('f');
        System.out.write('\n');
        System.out.flush();
        edu.uci.ece.zen.utils.Logger.writeln(id);*/

        edu.uci.ece.zen.utils.Logger.printMemStatsImm(635);
        ByteArrayCache cache = ByteArrayCache.instance();
        edu.uci.ece.zen.utils.Logger.printMemStatsImm(636);
        for (int i = 0; i < buffers.size(); i++){
            cache.returnByteArray((byte[]) buffers.elementAt(i));
            ba--;
        }


         if(ZenProperties.memDbg1) System.out.write('b');
         if(ZenProperties.memDbg1) System.out.write('r');
        if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(ba);
        if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(buffers.size());
        if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(bs);


       edu.uci.ece.zen.utils.Logger.printMemStatsImm(637);
        //buffers.removeAllElements();
        init();
        edu.uci.ece.zen.utils.Logger.printMemStatsImm(638);
        ReadBuffer.release(this);
        edu.uci.ece.zen.utils.Logger.printMemStatsImm(639);
        numFree++;
        if(ZenProperties.memDbg1) System.out.write('r');
        if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(numFree);
       // edu.uci.ece.zen.utils.Logger.writeln(bufferCache.size());
       inUse = false;
    }

    public void freeWithoutBufferRelease() {
        ReadBuffer.release(this);
    }

    /*
     * public void writeUnAligned( WriteBuffer wb , int bufOffset , int numBufs ){
     * for( int i=0;i <numBufs;i++ ){ byte[] buf = (byte[]) buffers.elementAt(
     * bufOffset+i ); wb.writeUnAligned( buf , 1024 , 1024 ); } }
     */

    public void setEndian(boolean isLittleEndian) {
        this.isLittleEndian = isLittleEndian;
    }
    int ba = 0;
    int bs = 0;
    private void ensureCapacity(int size) {
        if (size <= 0) return;
        while (limit + size > capacity) {
            edu.uci.ece.zen.utils.Logger.printMemStatsImm(711);
            byte[] byteArray = ByteArrayCache.instance().getByteArray();
            edu.uci.ece.zen.utils.Logger.printMemStatsImm(712);
            capacity += byteArray.length;
            if(buffers.size()+1 >= maxCap){
                ZenProperties.logger.log(Logger.FATAL, ReadBuffer.class,
                    "ensureCapacity",
                    "Reached maximum buffer capacity. Try adjusting " +
                    "readbuffer.size property. Current value is: " + maxCap);
                    //System.exit(-1);
                    //still deciding what to do here
            }
            buffers.addElement(byteArray);
            ba++;
       }
        bs = buffers.size();
    }

    public long getPosition() {
        return position;
    }

    /*
     * public void setPosition(int pos){ position = pos; }
     */

    public long getLimit() {
        return limit;
    }

    private void checkReadPositionLimit(int dlen) {
        //TODO: See what to do here
    }

    private void pad(int boundry) {
        if( !enableAlignment )
            return;

        int extraBytesUsed = (int) ((position + 12) % boundry);
        // The CDR alignment should count from the beginning of GIOP header.  But the GIOP header is excluded in CDRInputStream position. So 12 must be added. Yue Zhang on 09.22pm, 08/01/2004
        if (extraBytesUsed != 0) {
            int incr = boundry - extraBytesUsed;
            checkReadPositionLimit(incr);
            position += incr;
        }
    }

    public byte readByte() {
        checkReadPositionLimit(1);
        //if (!inUse) System.out.println("IN USE:" + inUse);
        byte[] curBuf = (byte[]) buffers.elementAt((int) (position / 1024));
        byte ret = curBuf[(int) (position % 1024)];
        position++;
        return ret;
    }

    public void readByteArray(byte[] v, int offset, int length) {
        checkReadPositionLimit(length);
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

    public short readShort() {
        pad(ReadBuffer.SHORT);

        checkReadPositionLimit(ReadBuffer.SHORT);
        byte b1 = readByte();
        byte b2 = readByte();

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
        checkReadPositionLimit(ReadBuffer.LONG);
        byte b1 = readByte();
        byte b2 = readByte();
        byte b3 = readByte();
        byte b4 = readByte();
        //System.out.println( "" + ((int)b1) + " " + ((int)b2) + " " +
         //((int)b3) + " " + ((int)b4) );
        int ret = 0;
        if (isLittleEndian) {
            ret |= b4 & 0xFF;
            ret <<= 8;
            ret |= b3 & 0xFF;
            ret <<= 8;
            ret |= b2 & 0xFF;
            ret <<= 8;
            ret |= b1 & 0xFF;
            //System.err.println( "Crap ...little endian...Long is " + ret );
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
        checkReadPositionLimit(ReadBuffer.LONGLONG);
        byte b1 = readByte();
        byte b2 = readByte();
        byte b3 = readByte();
        byte b4 = readByte();
        byte b5 = readByte();
        byte b6 = readByte();
        byte b7 = readByte();
        byte b8 = readByte();

        long ret = 0;
        if (isLittleEndian) {
            //    ret |= ((short)(b8 & 0xFF)) << 56;
            //    ret |= ((short)(b7 & 0xFF)) << 48;
            //    ret |= ((short)(b6 & 0xFF)) << 40;
            //    ret |= ((short)(b5 & 0xFF)) << 32;
            //    ret |= ((short)(b4 & 0xFF)) << 24;
            //    ret |= ((short)(b3 & 0xFF)) << 16;
            //    ret |= ((short)(b2 & 0xFF)) << 8;
            //    ret |= ((short)(b1 & 0xFF)) << 0;
            //Change back to the way in Zen
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
            /*
             * ret |= ((short)(b1 & 0xFF)) < < 56; ret |= ((short)(b2 & 0xFF)) < <
             * 48; ret |= ((short)(b3 & 0xFF)) < < 40; ret |= ((short)(b4 &
             * 0xFF)) < < 32; ret |= ((short)(b5 & 0xFF)) < < 24; ret |=
             * ((short)(b6 & 0xFF)) < < 16; ret |= ((short)(b7 & 0xFF)) < < 8;
             * ret |= ((short)(b8 & 0xFF)) < < 0;
             */
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
        if (ZenProperties.dbg) ZenProperties.logger.log("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$FString len" + len);
        if( isString )
            len--;
        FString fs = FString.instance();
        for(int i = 0; i < len; ++i)
            fs.append(readByte());

        if (isString) readByte(); //eat an xtra byte if not just byte array
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
        //System.err.println( "Long is " + new String( buf ) );
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
            out.writeByteArray((byte[]) buffers.elementAt(i), 0, 1024);
        }
        out.writeByteArray((byte[]) buffers.elementAt(((int) (limit / 1024))),
                0, (int) (limit % 1024));
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
}
