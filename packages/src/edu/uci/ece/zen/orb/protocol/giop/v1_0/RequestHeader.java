package edu.uci.ece.zen.orb.protocol.giop.v1_0;

import javax.realtime.ImmortalMemory;

import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

/**
 * Struct definition : RequestHeader_1_0
 *
 * @author OpenORB Compiler
 */
public final class RequestHeader implements org.omg.CORBA.portable.IDLEntity{

    public static RequestHeader instance() {
        try {
            return (RequestHeader) ImmortalMemory.instance().newInstance(RequestHeader.class);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, RequestHeader.class, "instance", e);
        }
        return null;
    }

    /**
     * Struct member service_context
     */
    //public ServiceContext[] service_context;
    public FString service_context;

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
    //public byte[] object_key = new byte[1024];
    //public int object_key_length = 0;
    public FString object_key;

    //byte [] object_key1;

    /**
     * Struct member operation
     */
    public FString operation;

    //String operation1;

    /**
     * Struct member requesting_principal
     */
    //public byte[] requesting_principal = new byte[1024];
    //public int requesting_principal_length = 0;
    public FString requesting_principal;

    //byte [] requesting_principal1;

    /**
     * Default constructor
     */
    public RequestHeader() {
        this.object_key = FString.instance();
        this.operation = FString.instance();
        this.requesting_principal = FString.instance();
        this.service_context = FString.instance();
    }

    public void finalize(){
        FString.free( this.object_key );
        FString.free( this.operation );
        FString.free( this.requesting_principal );
        FString.free( this.service_context );
    }

    /**
     * Constructor with fields initialization
     *
     * @param service_context
     *            service_context struct member
     * @param request_id
     *            request_id struct member
     * @param response_expected
     *            response_expected struct member
     * @param object_key
     *            object_key struct member
     * @param operation
     *            operation struct member
     * @param requesting_principal
     *            requesting_principal struct member
     */
    public void init(int request_id, boolean response_expected, FString object_key, String operation, byte[] requesting_principal) {
        this.request_id = request_id;
        this.response_expected = response_expected;
        
        this.service_context.reset();
        this.object_key.reset();
        this.operation.reset();
        this.requesting_principal.reset();
        
        this.object_key.append(object_key);
        this.operation.append(operation);
        this.requesting_principal.append(requesting_principal);
    }

    public void init( FString contexts , int request_id, boolean response_expected, FString object_key, String operation, byte[] requesting_principal) {
        init( request_id , response_expected , object_key , operation , requesting_principal );
        this.service_context.append( contexts );
    }

    public void reset(){
        if(service_context != null) service_context.reset();
        object_key.reset();
        operation.reset();
        if(requesting_principal != null) requesting_principal.reset();
    }

}
