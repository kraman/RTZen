package edu.uci.ece.zen.utils;

import javax.realtime.ImmortalMemory;

/**
 * This class holds a cache of byte arrays. It is responsible for the reuse of
 * byte buffers.
 * 
 * @author Krishna Raman
 */
public class ByteArrayCache {
    /**
     * Byte arrays are created from immortal memory. This variable keeps a
     * reference to the immortal memory object.
     */
    private ImmortalMemory imm;

    /**
     * A reference to the queue object used to store the list of currently
     * unused byte arrays.
     */
    private Queue byteBuffers;

    /**
     * Stores a static reference to the ByteArrayCache object.
     */
    private static ByteArrayCache _instance = null;

    /**
     * Returns an instance of the ByteArrayCache. It creates a new one from
     * ImmortalMemory if necessary.
     * 
     * @return An instance of the ByteArrayCache.
     */
    public static ByteArrayCache instance() {
        if (_instance == null) try {
            _instance = (ByteArrayCache) ImmortalMemory.instance().newInstance(
                    ByteArrayCache.class);
            System.out.println( "ByteArrayCache creatd" );
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, ByteArrayCache.class, "instance", e);
            System.exit(-1);
        }
        return _instance;
    }

    /**
     * A constructor to the ByteArrayCache object. It is not meant to be called
     * directly. It is only public because the mock javax.realtime classes
     * needed access to it.
     */
    public ByteArrayCache() {
        try {
            imm = ImmortalMemory.instance();
            byteBuffers = (Queue) imm.newInstance(Queue.class);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "<init>", e);
            System.exit(-1);
        }
    }
    private int num = 0;
    /**
     * Gets a byte from the ByteArrayCache. A new byte array is created from
     * immortal memory if needed
     * 
     * @return A byte arra
     */
    public byte[] getByteArray() {
        try {
            byte[] ret = (byte[]) byteBuffers.dequeue();
            num++;
            //Thread.dumpStack();
            if(ZenProperties.memDbg1) System.out.write('d');
            if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(num);
            if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln();

            if (ret == null) {
                if(ZenProperties.memDbg1) System.out.write('g');
                if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(byteBuffers.size());
                return (byte[]) imm.newArray(byte.class, 1024);
            } else {
                return ret;
            }
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, getClass(), "getByteArray", e);
            System.exit(-1);
        }
        return null;
    }

    /**
     * Returns a byte array to the ByteArrayCache.
     * 
     * @param buf
     *            The byte buffer to return to the cache.
     */
    public void returnByteArray(byte[] buf) {
        byteBuffers.enqueue(buf);
        num--;
        if(ZenProperties.memDbg1) System.out.write('e');
        if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(num);
        if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(byteBuffers.size());
  }
}
