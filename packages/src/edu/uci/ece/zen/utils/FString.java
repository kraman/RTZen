package edu.uci.ece.zen.utils;

import javax.realtime.*;

public class FString{

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

    public static FString instance(FString fs){
        if(fs == null){
            fs = fromImmortal();
            System.out.println("new FString");
        } else {
            fs.reset();
        }

        return fs;
    }


    public FString(){}
    public FString( int maxSize ){
        this.maxSize = maxSize;
        this.currentSize = 0;
        this.data = new byte[maxSize];
    }

    int maxSize;
    int currentSize;
    byte[] data;

    public void init( int maxSize ) throws InstantiationException,IllegalAccessException,InaccessibleAreaException{
        this.maxSize = maxSize;
        this.currentSize = 0;
           // this.data = (byte[]) MemoryArea.getMemoryArea(this).newArray( byte.class , maxSize );
           byte[] dataTemp =  (byte[]) MemoryArea.getMemoryArea(this).newArray( byte.class , maxSize );
           this.data = dataTemp;
    }

    public void append( byte[] data ){
        append( data , 0 , data.length );
    }

    public void read( ReadBuffer istream, int length ){
        if( currentSize + length < maxSize ){
            //KLUDGE: ERROR here
        }
        istream.readByteArray(data, currentSize, length);
        currentSize += length;
    }

    public void read( org.omg.CORBA.portable.InputStream istream, int length ){
        if( currentSize + length < maxSize ){
            //KLUDGE: ERROR here
        }
        istream.read_octet_array(data, currentSize, length);
        currentSize += length;
    }

    public void write( org.omg.CORBA.portable.OutputStream ostream){
        ostream.write_octet_array(data, 0, currentSize);
    }

    public void append( byte[] data , int offset , int length ){
        if( currentSize + length < maxSize ){
            //KLUDGE: ERROR here
        }
        System.arraycopy( data , offset , this.data , currentSize , length );
        currentSize += length;
    }

    public void append( String str ){
        for(int i = 0; i < str.length(); ++i)
            append(str.charAt(i));
        
        //append( str.getBytes() , 0 , str.length() );
    }

    public void append( byte b ){
        data[currentSize++] = b;
    }

    public void append( char c ){
        data[currentSize++] = (byte) c;
    }

    public void append( short value ){
        data[currentSize++] = (byte) ((value >>> 8) & 0xFF);
        data[currentSize++] = (byte) (value & 0xFF);
    }

    public void append( int value ){
        data[currentSize++] = (byte) ((value >>> 24) & 0xFF);
        data[currentSize++] = (byte) ((value >>> 16) & 0xFF);
        data[currentSize++] = (byte) ((value >>> 8) & 0xFF);
        data[currentSize++] = (byte) (value & 0xFF);
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

    public String toString(){
        return new String(getTrimData());
    }

    public String decode(){
        return byteArrayToString(getTrimData());
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
