package edu.uci.ece.zen.utils;
import javax.realtime.*;

/** A class that provides a way to represent a mutable String of fixed size.
 * This class internally maintains a byte buffer. It doesnot recycle the byte
 * buffer so be wary when allocating a very big string.
 *
 * @author Krishna Raman
 */
public class FString{
    /** Static function to create a FString object of 128 bytes in size from
     * immortal memory.
     * @return A FString object of 128 bytes allocated from ImmortalMemory
     */
    private static FString fromImmortal(){
        FString fs = null;
        try{
            fs = (FString) ImmortalMemory.instance().newInstance( FString.class );
            fs.init(128);
        }catch(Exception e){
            e.printStackTrace();
        }
        return fs;
    }

    /** I have no idea. */
    public static FString instance(FString fs){
        if(fs == null){
            fs = fromImmortal();
            System.out.println("new FString");
        } else {
            fs.reset();
        }

        return fs;
    }

    /** Constructor to call when useing <code>newInstance</code>. Make sure you
     * call <code>init(...)</code> after creating the instance.
     */
    public FString(){}

    /** Constructor to call when using <code>new</code>. Do not call
     * <code>init</code> after this constructor.
     * @param maxSize The maximum size of string data.
     */
    public FString( int maxSize ){
        this.maxSize = maxSize;
        this.currentSize = 0;
        this.data = new byte[maxSize];
    }

    /** Maximum size of data stored in this FString */
    int maxSize;
    /** Current size do data stored in the FString */
    int currentSize;
    /** Byte array to store the data in */
    byte[] data;

    /** Method to initialize the size of the FString when using the 1st
     * constructor.
     * @param maxSize The maximum size of data that will be stored in the FString
     * @see #FString()
     */
    public void init( int maxSize ) throws InstantiationException,IllegalAccessException,InaccessibleAreaException{
        this.maxSize = maxSize;
        this.currentSize = 0;
        byte[] dataTemp =  (byte[]) MemoryArea.getMemoryArea(this).newArray( byte.class , maxSize );
        this.data = dataTemp;
    }

    /** This method appends data to the FString 
     * @param data The data to append to the string.
     */
    public void append( byte[] data ){
        append( data , 0 , data.length );
    }

    /** Reads length bytes from the read buffer into the FString. This function
     * does not throw an ArrayIndexOutOfBounds is the amount of space in the
     * FString is not enough.
     * @param istream The ReadBuffer object to read from
     * @param length The number of bytes to read.
     */
    public void read( ReadBuffer istream, int length ){
        if( currentSize + length < maxSize ){
            //KLUDGE: ERROR here
        }
        istream.readByteArray(data, currentSize, length);
        currentSize += length;
    }

    /** Reads length bytes from the CDR stream into the FString. This function
     * does not throw an ArrayIndexOutOfBounds is the amount of space in the
     * FString is not enough.
     * @param istream The CDR input stream to read from
     * @param length The number of bytes to read.
     */
    public void read( org.omg.CORBA.portable.InputStream istream, int length ){
        if( currentSize + length < maxSize ){
            //KLUDGE: ERROR here
        }
        istream.read_octet_array(data, currentSize, length);
        currentSize += length;
    }

    /** Dump all data from FString to the CDR output stream. It does not null
     * terminate.
     * @param ostream The CDR output stream to write to.
     */
    public void write( org.omg.CORBA.portable.OutputStream ostream){
        ostream.write_octet_array(data, 0, currentSize);
    }

    /** Append bytes to the end of the current FString. The bytes between offset
     * and offset+length will be appended.
     * @param data The array to add from.
     * @param offset The array index to begin copying from.
     * @param length The number of bytes to copy.
     */
    public void append( byte[] data , int offset , int length ){
        if( currentSize + length < maxSize ){
            //KLUDGE: ERROR here
        }
        System.arraycopy( data , offset , this.data , currentSize , length );
        currentSize += length;
    }

    /** Append data from the string.
     * @param str The string to append data from.
     */
    public void append( String str ){
        for(int i = 0; i < str.length(); ++i)
            append(str.charAt(i));
    }

    /** Append the byte to the FString
     * @param b The byte value to append.
     */
    public void append( byte b ){
        data[currentSize++] = b;
    }

    /** Append the char to the FString
     * @param c The char value to append.
     */
    public void append( char c ){
        data[currentSize++] = (byte) c;
    }

    /** Append the marshalled short to the FString
     * @param value The marshalled short value to append.
     */
    public void append( short value ){
        data[currentSize++] = (byte) ((value >>> 8) & 0xFF);
        data[currentSize++] = (byte) (value & 0xFF);
    }

    /** Append the marshalled int to the FString
     * @param value The marshalled int value to append.
     */
    public void append( int value ){
        data[currentSize++] = (byte) ((value >>> 24) & 0xFF);
        data[currentSize++] = (byte) ((value >>> 16) & 0xFF);
        data[currentSize++] = (byte) ((value >>> 8) & 0xFF);
        data[currentSize++] = (byte) (value & 0xFF);
    }

    /** Append the marshalled long to the FString
     * @param value The marshalled long value to append.
     */
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

    /** Append the FString to the end of the current FString
     * @param str The FString to append.
     */
    public void append( FString str ){
        append( str.getData() , 0 , str.length() );
    }

    /** Return a reference to the internal data array.
     * @return a reference to the internal data array.
     */
    public byte[] getData(){
        return this.data;
    }

    /** Returns the length of the data currently stored in the FString
     * @return The length of the data.
     */
    public int length(){
        return currentSize;
    }

    /**
     * Returns a new byte array allocated from the current memory region.
     * @return A byte array with a copy of the data.
     */
    public byte[] getTrimData(){
        byte[] ret = new byte[currentSize];
        System.arraycopy( data , 0 , ret , 0 , currentSize );
        return ret;
    }

    /**
     * Returns a new byte array allocated in the specified memory region with a
     * copy of the data present in this FString.
     * @param mem The memory region to allocate the new array from.
     * @return A byte array with a copy of the data.
     */
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

    /**
     * Returns the FString as a java String. No conversion is performed 
     * @return The converted string.
     */
    public String toString(){
        return new String(getTrimData());
    }

    /**
     * Convert this FString into a string, using the inverse
     * algorithm as in stringToCDRByteArray
     * @see #byteArrayToString(byte[])
     */
    public String decode(){
        return byteArrayToString(getTrimData());
    }

    /** Resets the FString. */
    public void reset(){
        currentSize = 0;
    }

    /** Checks if the passed in object is logically equal to this FString.
     * @param obj The object to compare.
     * @return True is the objects are equal.
     */
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

    /**
     * Convert an array of bytes into a string, using the inverse
     * algorithm as in stringToCDRByteArray
     */
    public static String byteArrayToString(byte[] b)
    {
        int bLen = b.length;
        int resultLen = 4 + 2 * b.length;
        StringBuffer result = new StringBuffer(resultLen);


        for (int src = 0; src < bLen; src++)
        {
            byte c = b[src];

            result.append(intToHexChar((c >> 4) & 0xF));
            result.append(intToHexChar(c & 0xF));
            result.append(" ");
        }

        return result.toString();
    }

    /**
     * Convert a numeric value in the [0:15] range into its hex digit.
     */
    private static char intToHexChar(int i)
    {
        switch (i)
        {
            case 0 :
                return '0';

            case 1 :
                return '1';

            case 2 :
                return '2';

            case 3 :
                return '3';

            case 4 :
                return '4';

            case 5 :
                return '5';

            case 6 :
                return '6';

            case 7 :
                return '7';

            case 8 :
                return '8';

            case 9 :
                return '9';

            case 10 :
                return 'A';

            case 11 :
                return 'B';

            case 12 :
                return 'C';

            case 13 :
                return 'D';

            case 14 :
                return 'E';

            case 15 :
                return 'F';

            default :

                return '0';
        }
    }

}
