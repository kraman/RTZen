package edu.uci.ece.zen.poa;

import javax.realtime.*;
import java.util.Vector;

public class POARunnable implements Runnable{
    public static final INIT=0;
    public static final HANDLE_REQUEST=1;
    public static final SERVANT_TO_ID=2;
    public static final SERVANT_TO_REFERENCE=3;
    public static final REFERENCE_TO_SERVANT=4;
    public static final REFERENCE_TO_ID=5;
    public static final ID_TO_SERVANT=6;
    public static final ID_TO_REFERENCE=0;
    public static final ACTIVATE_OBJECT=7;
    public static final ACTIVATE_OBJECT_WITH_ID=8;
    public static final DEACTIVATE_OBJECT=9;
    public static final REMOVE_FROM_PARENT=10;
    public static final DESTROY=11;
    public static final ID=12;
    public static final GET_SERVANT_MANAGER=13;
    public static final SET_SERVANT_MANAGER=14;
    public static final GET_SERVANT=15;
    public static final SET_SERVANT=16;
    public static final CREATE_REFERENCE=17;
    public static final CREATE_REFERENCE_WITH_ID=18;
    public static final GET_POLICY_LIST=19;
    
    private int operation;
    private Vector args;

    private int exception;
    public Object retVal;

    public POARunnable( int op ){
        operation = op;
        args = new Vector();
    }

    public addParam( Object arg ){
        args.addElement( arg );
    }

    public void run(){
        Object portal = ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).getPortal();
        POAImpl pimpl = null;
        if( portal != null )
            pimpl = (POAImpl) portal;

        switch( operation ){
            case INIT:
                pimpl = new POAImpl();
                pimpl.init( (ORB) args.elementAt(0) , 
                    (POA) args.elementAt(1) ,
                    (Policy[]) args.elementAt(2) ,
                    (POA) args.elementAt(3) ,
                    (POAManager) args.elementAt(4) ,
                    this );
                ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).setPortal(pimpl);
                break;
            case HANDLE_REQUEST:
                pimpl.handleRequest( (ServerRequest) args.elementAt(0) , this );
                break;
            case SERVANT_TO_ID:
                pimpl.servant_to_id( (Servant) args.elementAt(0) , 
                    (MemoryArea) args.elementAt(1) ,
                    this );
                break;
            case SERVANT_TO_REFERENCE:
                pimpl.servant_to_reference( (Servant) args.elementAt(0) , 
                    (MemoryArea) args.elementAt(1) ,
                    this );
                break;
            case REFERENCE_TO_SERVANT:
                pimpl.reference_to_servant( 
                    (org.omg.CORBA.Object) args.elementAt(0) , 
                    (MemoryArea) args.elementAt(1) ,
                    this );
                break;
            case REFERENCE_TO_ID:
                pimpl.reference_to_id( (org.omg.CORBA.Object) args.elementAt(0) , 
                    (MemoryArea) args.elementAt(1) ,
                    this );
                break;
            case ID_TO_SERVANT:
                pimpl.id_to_servant( (byte[]) args.elementAt(0) , 
                    (MemoryArea) args.elementAt(1) ,
                    this );
                break;
            case ID_TO_REFERENCE:
                pimpl.id_to_reference( (byte[]) args.elementAt(0) , 
                    (MemoryArea) args.elementAt(1) ,
                    this );
                break;
            case ACTIVATE_OBJECT:
                pimpl.activate_object( (Servant) args.elementAt(0) , 
                    (MemoryArea) args.elementAt(1) ,
                    this );
                break;
            case ACTIVATE_OBJECT_WITH_ID:
                pimpl.activate_object_with_id( 
                    (byte[]) args.elementAt(0) , 
                    (Servant) args.elementAt(1) , 
                    (MemoryArea) args.elementAt(2) ,
                    this );
                break;
            case DEACTIVATE_OBJECT:
                pimpl.deactivate_object( 
                    (byte[]) args.elementAt(0) , 
                    (MemoryArea) args.elementAt(1) ,
                    this );
                break;
            case REMOVE_FROM_PARENT:
                pimpl.removeFromParent( (POA) args.elementAt(0) );
                break;
            case DESTROY:
                pimpl.destroy( (POA) args.elementAt(0) );
                break;
            case ID:
                pimpl.id( (MemoryArea) args.elementAt(0), this );
                break;
            case GET_SERVANT_MANAGER:
                pimpl.get_servant_manager( this );
                break;
            case SET_SERVANT_MANAGER:
                pimpl.set_servant_manager( 
                    (ServantManager) args.elementAt(0) ,
                    this );
                break;
            case GET_SERVANT:
                pimpl.get_servant( this );
                break;
            case SET_SERVANT:
                pimpl.set_servant( 
                    (Servant) args.elementAt(0) ,
                    this );
                break;
            case CREATE_REFERENCE:
                pimpl.create_reference( 
                    (String) args.elementAt(0) ,
                    (MemoryArea) args.elementAt(1),
                    this );
                break;
            case CREATE_REFERENCE_WITH_ID:
                pimpl.create_reference_with_id( 
                    (byte[]) args.elementAt(0) ,
                    (String) args.elementAt(1) ,
                    (MemoryArea) args.elementAt(2),
                    this );
            case GET_POLICY_LIST:
                pimpl.policy_list( 
                    (MemoryArea) args.elementAt(0),
                    this );
                break;
        }
    }
}
