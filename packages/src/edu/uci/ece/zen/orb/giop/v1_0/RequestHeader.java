package edu.uci.ece.zen.orb.giop.v1_0;


import javax.realtime.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.giop.IOP.ServiceContext;
/**
 * Struct definition : RequestHeader_1_0
 *
 * @author OpenORB Compiler
*/
public final class RequestHeader implements org.omg.CORBA.portable.IDLEntity
{

    private static RequestHeader rh;

    public static RequestHeader instance(){

        try{
            if(rh == null)
                rh = (RequestHeader) ImmortalMemory.instance().newInstance( RequestHeader.class );
        }catch(Exception e){
            e.printStackTrace();
        }

        return rh;
    }


    /**
     * Struct member service_context
     */
    public ServiceContext[] service_context;

    /**
     * Struct member request_id
     */
    public int request_id;

    /**
     * Struct member response_expected
     */
    public boolean response_expected;

    /**
     * Struct member object_key
     */
    public byte[] object_key = new byte[1024];
    public int object_key_length = 0;

    /**
     * Struct member operation
     */
    public String operation;

    /**
     * Struct member requesting_principal
     */
    public byte[] requesting_principal = new byte[1024];
    public int requesting_principal_length = 0;



}
