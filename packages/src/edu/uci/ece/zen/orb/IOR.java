package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.utils.*;
import org.omg.IOP.*;
import org.omg.IIOP.*;
import javax.realtime.*;

public class IOR
{
    private static final char[] intToHex = { '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' , '9' , 'A' , 'B' , 'C' , 'D' , 'E' , 'F' };

    private static ReadBuffer stringToByteBuffer( String s ){
        int line=1;
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
            case 'A': case 'a': return (byte)10;
            case 'B': case 'b': return (byte)11;
            case 'C': case 'c': return (byte)12;
            case 'D': case 'd': return (byte)13;
            case 'E': case 'e': return (byte)14;
            case 'F': case 'f': return (byte)15;
        }
        return (byte)-1;
    }

    public static org.omg.IOP.IOR parseString(edu.uci.ece.zen.orb.ORB orb, String ior)
    {
        int line=1;
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
    public static org.omg.CORBA.Object makeCORBAObject(ORB orb, String typeID, FString objKey, MemoryArea clientArea)
            throws IllegalAccessException,InstantiationException
    {
        System.out.println( "makeCORBAObject 1 -- client area: " + clientArea);
        org.omg.IOP.IOR ior = (org.omg.IOP.IOR) clientArea.newInstance(org.omg.IOP.IOR.class);
        ior.type_id = typeID;
        ior.profiles = orb.getAcceptorRegistry().getProfiles(objKey, clientArea);
        System.out.println( "makeCORBAObject 2" );

        ObjectImpl objectImpl = (ObjectImpl) clientArea.newInstance(edu.uci.ece.zen.orb.ObjectImpl.class);
        objectImpl.init(ior);
        ObjRefDelegate delegate = ObjRefDelegate.instance();
        objectImpl._set_delegate(delegate);
        System.out.println( "makeCORBAObject 3" );

        ORBImpl orbImpl = (ORBImpl) orb.orbImplRegion.getPortal();
        delegate.init( ior, objectImpl, orb , orbImpl );
        System.out.println( "makeCORBAObject 4" );
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
