package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.utils.*;
import org.omg.IOP.*;
import org.omg.IIOP.*;

public class IOR
{
    private static final char[] intToHex = { '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' , '9' , 'A' , 'B' , 'C' , 'D' , 'E' , 'F' };

    private static ReadBuffer stringToByteBuffer( String s ){
        ReadBuffer buffer = ReadBuffer.instance();
        byte[] tmpBuffer = ByteArrayCache.instance().getByteArray();
        int len=0;

        int readPos=4;
        while (readPos < s.length())
        {
            char first = s.charAt(readPos++);
            char second = s.charAt(readPos++);
            byte combined =
                (byte) (((hexToInt(first) & 0xF) << 4)
                    | (hexToInt(second) & 0xF));
            tmpBuffer[len++] = combined;
        }

        buffer.writeByteArray( tmpBuffer , 0 , len );
        ByteArrayCache.instance().returnByteArray( tmpBuffer );
        return buffer;
    }

    private static byte hexToInt( char c ){
        switch( c ){
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return (byte)c;
            case 'A': case 'a': return 10;
            case 'B': case 'b': return 11;
            case 'C': case 'c': return 12;
            case 'D': case 'd': return 13;
            case 'E': case 'e': return 14;
            case 'F': case 'f': return 15;
        }
        return -1;
    }

    public static org.omg.IOP.IOR parseString(edu.uci.ece.zen.orb.ORB orb, String ior)
    {
        CDRInputStream cdrIn = CDRInputStream.instance();
        cdrIn.init( orb , stringToByteBuffer( ior ));
        cdrIn.setEndian( cdrIn.read_boolean() );
        org.omg.IOP.IOR ret = org.omg.IOP.IORHelper.read( cdrIn );
        cdrIn.free();
        return ret;
    }


    /**
     * Factory method to create a CORBA object.
     *
     * @param orb The ORB reference used to create this object.  This orb reference contains the
     * connector and acceptor registries used by this object.
     * @param ior This object's IOR.
     * @return The CORBA object.
     */
    public static org.omg.CORBA.Object makeCORBAObject(ORB orb, String typeID, byte [] objKey, int objKeyLength)
    {

        org.omg.IOP.IOR ior = new org.omg.IOP.IOR();
        ior.type_id = typeID;
        ior.profiles = orb.getAcceptorRegistry().getProfiles();

        ObjectImpl objectImpl = new ObjectImpl(ior);
        ObjRefDelegate delegate = ObjRefDelegate.instance();
        objectImpl._set_delegate(delegate);

        delegate.init(ior, objectImpl, orb);

        return objectImpl;
    }

    public static String makeIOR( WriteBuffer ior ){
        ReadBuffer rd = ior.getReadBuffer();
        int len = (int)rd.getLimit();
        int resultLen = (int)(6 + 2 * len);
        StringBuffer result = new StringBuffer(resultLen);

        result.append('I');
        result.append('O');
        result.append('R');
        result.append(':');
        //byte order : big endian
        result.append('0');
        result.append('0');
        //padding : 3 bytes
        result.append('0');
        result.append('0');
        result.append('0');
        result.append('0');
        result.append('0');
        result.append('0');

        for (int src = 0; src < len; src++)
        {
            byte c = rd.readByte();
            result.append(intToHex[(c >> 4) & 0xF]);
            result.append(intToHex[c & 0xF]);
        }

        rd.free();

        return result.toString();
    }
}
