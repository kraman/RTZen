package edu.uci.ece.zen.utils;

import javax.realtime.ImmortalMemory;

import org.omg.CORBA.BAD_CONTEXT;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_TYPECODE;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.DATA_CONVERSION;
import org.omg.CORBA.FREE_MEM;
import org.omg.CORBA.IMP_LIMIT;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INTF_REPOS;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.INV_FLAG;
import org.omg.CORBA.INV_IDENT;
import org.omg.CORBA.INV_OBJREF;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.NO_MEMORY;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.NO_RESPONSE;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.OBJ_ADAPTER;
import org.omg.CORBA.PERSIST_STORE;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.portable.IndirectionException;
import org.omg.CORBA.portable.UnknownException;

/**
 * This factory of CORBA system exceptions is a singleton allocated in immortal memory.
 * It creates the exception objects in immortal and caches them in order to be reused.
 * @author Juan Colmenares.
 * @version 1.0  
 */
public class SystemExceptionFactory
{
    public static final IllegalArgumentException ILLEGAL_ARG_EXCEPTION = 
        new IllegalArgumentException();
    public static final NullPointerException NULL_POINTER_EXCEPTION = new NullPointerException();
    
    // See Apendix A of CORBA spec 3.0.3.
    private final static int ACTIVITY_COMPLETED      = 0;
    private final static int ACTIVITY_REQUIRED       = 1;
    private final static int BAD_CONTEXT             = 2;
    private final static int BAD_INV_ORDER           = 3;
    private final static int BAD_OPERATION           = 4;
    private final static int BAD_PARAM               = 5;
    private final static int BAD_TYPECODE            = 6;
    private final static int COMM_FAILURE            = 7;    
    private final static int DATA_CONVERSION         = 8;
    private final static int FREE_MEM                = 9;
    private final static int IMP_LIMIT               = 10;
    private final static int INITIALIZE              = 11; 
    private final static int INTERNAL                = 12;
    private final static int INTF_REPOS              = 13;
    private final static int INV_FLAG                = 14;
    private final static int INV_IDENT               = 15;
    private final static int INV_OBJREF              = 16; 
    private final static int INV_POLICY              = 17;
    private final static int INVALID_TRANSACTION     = 18; 
    private final static int MARSHAL                 = 19;
    private final static int NO_IMPLEMENT            = 20;
    private final static int NO_MEMORY               = 21;
    private final static int NO_PERMISSION           = 22;
    private final static int NO_RESOURCES            = 23;
    private final static int NO_RESPONSE             = 24;
    private final static int OBJ_ADAPTER             = 25;
    private final static int OBJECT_NOT_EXIST        = 26;
    private final static int PERSIST_STORE           = 27;
    private final static int TRANSACTION_REQUIRED    = 28; 
    private final static int TRANSACTION_ROLLEDBACK  = 29;
    private final static int TRANSIENT               = 30;
    private final static int UNKNOWN                 = 31;
    private final static int PORTABLE_INDIRECTION    = 32;
    private final static int PORTABLE_UNKNOWN_EXCEPTION = 33;
    
    private static final int MAX_NUM_OF_SYS_EX = 34;
    private static final int NUM_OF_STD_MINOR_CODES = 150; // See Apendix A of CORBA spec 3.0.3.
    private static final int COMPLETION_POSSIBILITIES = 3; // COMPLETED_MAYBE, COMPLETED_NO, and COMPLETED_YES 

    private static final SystemException[] sysExCache = 
        new SystemException[NUM_OF_STD_MINOR_CODES * COMPLETION_POSSIBILITIES];
    
    private int numOfExCached = 0;
    
    /** Singleton in immortal memory. */
    public final static SystemExceptionFactory INSTANCE = new SystemExceptionFactory();

    /** Return the singleton allocated in immortal memory. */
    public final static SystemExceptionFactory getInstance() {
        return SystemExceptionFactory.INSTANCE;
    }
    
    /** Private constructor */
    private SystemExceptionFactory() {};
    

    /**
     * Returns a CORBA system exception created in immortal memory. After being created, the 
     * exception is cached so that the can be reused in future invocations.
     * 
     * @param   sysEx number that identifies the type of the system exception.
     * @param   minor minor code of the system exception.
     * @param   completed completion status.
     * @return  a CORBA system exception created in immortal memory.
     * @throws  NullPointerException if <code>completed</code> is null.
     * @throws  IllegalArgumentException if <code>sysEx</code> is invalid, or 
     *          <code>minor</code> is negative or zero.
     */
    public SystemException getSystemException(int sysEx, int minor, CompletionStatus completed) {
        if (completed == null) {
            ZenProperties.logger.log(Logger.WARN, this.getClass(), "getSystemException", 
                                     "completed cannot be is null");
            throw NULL_POINTER_EXCEPTION;
        }
        
        if (minor <= 0) {
            ZenProperties.logger.log(Logger.WARN, this.getClass(), "getSystemException", 
                                     "minor must be possitive");
            throw ILLEGAL_ARG_EXCEPTION;
        }
        
        Class sysExClass = this.getSysExClass(sysEx);
        
        for (int i = 0; i < numOfExCached; i++) {
            if (sysExCache[i].getClass() == sysExClass  && sysExCache[i].minor == minor && 
                sysExCache[i].completed == completed) {
                return sysExCache[i];
            }    
        }

        SystemException e = null;
        
        try {
            e = (SystemException) ImmortalMemory.instance().newInstance(sysExClass);
            e.minor = minor;
            e.completed = completed;
            sysExCache[numOfExCached++] = e;
        }
        catch (Exception ex) {
            ZenProperties.logger.log(Logger.FATAL, this.getClass(), "getSystemException", ex);
        }
        
        return e;
    }

    /**
     * Returns the class object corresponding to the ode that identifies the type of the system exception
     * @param sysEx number that identifies the type of the system exception.
     * @return
     */
    private Class getSysExClass(int sysEx) {
        if (sysEx < 0 || sysEx > MAX_NUM_OF_SYS_EX) { 
            ZenProperties.logger.log(Logger.WARN, this.getClass(), "getSysExClass", 
                                     "illegal exception code");
            throw ILLEGAL_ARG_EXCEPTION;
        }
        
        switch (sysEx) {
            case ACTIVITY_COMPLETED:
                ZenProperties.logger.log(Logger.WARN, this.getClass(), "getSysExClass", 
                                         "The exception ACTIVITY_COMPLETED is not supported.");
                throw ILLEGAL_ARG_EXCEPTION; // return org.omg.CORBA.ACTIVITY_COMPLETED.class;
            case ACTIVITY_REQUIRED:
                ZenProperties.logger.log(Logger.WARN, this.getClass(), "getSysExClass", 
                                         "The exception ACTIVITY_REQUIRED is not supported.");
                throw ILLEGAL_ARG_EXCEPTION; // return org.omg.CORBA.ACTIVITY_REQUIRED.class;
            case BAD_CONTEXT:
                return BAD_CONTEXT.class;
            case BAD_INV_ORDER:
                return BAD_INV_ORDER.class;
            case BAD_OPERATION:
                return BAD_OPERATION.class;
            case BAD_PARAM:
                return BAD_PARAM.class;
            case BAD_TYPECODE:
                return BAD_TYPECODE.class;
            case COMM_FAILURE:
                return COMM_FAILURE.class;
            case DATA_CONVERSION:
                return DATA_CONVERSION.class;
            case FREE_MEM:
                return FREE_MEM.class;
            case IMP_LIMIT:
                return IMP_LIMIT.class;
            case INITIALIZE:
                return INITIALIZE.class;
            case INTERNAL:
                return INTERNAL.class;
            case INTF_REPOS:
                return INTF_REPOS.class;
            case INV_FLAG:
                return INV_FLAG.class;
            case INV_IDENT:
                return INV_IDENT.class;
            case INV_OBJREF:
                return INV_OBJREF.class;
            case INV_POLICY:
                return INV_POLICY.class;
            case INVALID_TRANSACTION:
                return INVALID_TRANSACTION.class;
            case MARSHAL:
                return MARSHAL.class;
            case NO_IMPLEMENT:
                return NO_IMPLEMENT.class;
            case NO_MEMORY:
                return NO_MEMORY.class;
            case NO_PERMISSION:
                return NO_PERMISSION.class;
            case NO_RESOURCES:
                return NO_RESOURCES.class;
            case NO_RESPONSE:
                return NO_RESPONSE.class;
            case OBJ_ADAPTER:
                return OBJ_ADAPTER.class;
            case OBJECT_NOT_EXIST:
                return OBJECT_NOT_EXIST.class;
            case PERSIST_STORE: 
                return PERSIST_STORE.class;
            case TRANSACTION_REQUIRED:
                return TRANSACTION_REQUIRED.class;
            case TRANSACTION_ROLLEDBACK:
                return TRANSACTION_ROLLEDBACK.class;
            case TRANSIENT:
                return TRANSIENT.class;
            case UNKNOWN:
                return UNKNOWN.class;
            case PORTABLE_INDIRECTION:
                return IndirectionException.class;
            case PORTABLE_UNKNOWN_EXCEPTION:
                return UnknownException.class;
            default:
                throw ILLEGAL_ARG_EXCEPTION; // it shouldn't happen
        } 
    }
}