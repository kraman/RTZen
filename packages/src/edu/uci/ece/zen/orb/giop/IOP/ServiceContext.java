package edu.uci.ece.zen.orb.giop.IOP;

import javax.realtime.*;
import edu.uci.ece.zen.utils.*;
/**
 * Struct definition : ServiceContext
 *
 * @author OpenORB Compiler
*/
public final class ServiceContext implements org.omg.CORBA.portable.IDLEntity
{
/*
    private static final int CAP = 128;
    //private static ServiceContext sc;
    private static ServiceContext [] scList;
    public static int length = 0;

    public static ServiceContext [] arrayInstance(){
        try{
            if(scList == null)
                scList = (ServiceContext []) ImmortalMemory.instance().newArray( ServiceContext.class, CAP );
        }catch(Exception e){
            e.printStackTrace();
        }
        return scList;
    }

    public static ServiceContext instance(int pos){

        if(pos >= length)
            if(ZenProperties.dbg) System.out.println( "ServiceContext.instance: array out of bounds");

        try{

            if(scList[pos] == null)
                scList[pos] = (ServiceContext) ImmortalMemory.instance().newInstance( ServiceContext.class );
        }catch(Exception e){
            e.printStackTrace();
        }

        return scList[pos];
    }
*/
    private static FString sc;

    public static FString instance(){
        return FString.instance(sc);
    }

    /**
     * Struct member context_id
     */
    public int context_id;

    /**
     * Struct member context_data
     */
    public byte[] context_data;// = new byte[1024];
    public int context_data_length = 0;


    /**
     * Constructor with fields initialization
     * @param context_id context_id struct member
     * @param context_data context_data struct member

    public static void init(int context_id, byte[] context_data, int context_data_length)
    {
        this.context_id = context_id;
        this.context_data = context_data;
        this.context_data_length = context_data_length;
    }*/

}
