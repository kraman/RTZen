package edu.uci.ece.zen.poa;

import java.util.Vector;

import javax.realtime.MemoryArea;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import org.omg.CORBA.Policy;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.CDROutputStream;
import org.omg.CORBA.PolicyListHelper;
import org.omg.CORBA.Policy;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ZenProperties;

//TODO Modify this class to be type safe.
public class POARunnable implements Runnable {
    public static final int INIT = 0;

    public static final int HANDLE_REQUEST = 1;

    public static final int SERVANT_TO_ID = 2;

    public static final int SERVANT_TO_REFERENCE = 3;

    public static final int REFERENCE_TO_SERVANT = 4;

    public static final int REFERENCE_TO_ID = 5;

    public static final int ID_TO_SERVANT = 6;

    public static final int ACTIVATE_OBJECT = 7;

    public static final int ACTIVATE_OBJECT_WITH_ID = 8;

    public static final int DEACTIVATE_OBJECT = 9;

    public static final int REMOVE_FROM_PARENT = 10;

    public static final int DESTROY = 11;

    public static final int ID = 12;

    public static final int GET_SERVANT_MANAGER = 13;

    public static final int SET_SERVANT_MANAGER = 14;

    public static final int GET_SERVANT = 15;

    public static final int SET_SERVANT = 16;

    public static final int CREATE_REFERENCE = 17;

    public static final int CREATE_REFERENCE_WITH_ID = 18;

    public static final int GET_POLICY_LIST = 19;

    public static final int ID_TO_REFERENCE = 20;

    public static final int ACTIVATE_OBJECT_WITH_PRIORITY = 21;

    public static final int GET_CLIENT_EXPOSED_POLICIES = 22;

    public static final int NoException = 0;

    public static final int InvalidPolicyException = 1;

    public static final int TransientException = 2;

    public static final int ObjAdapterException = 3;

    public static final int ObjNotExistException = 4;

    public static final int ServantNotActiveException = 5;

    public static final int WrongPolicyException = 6;

    public static final int BadInvOrderException = 7;

    public static final int ObjNotActiveException = 8;

    public static final int ForwardRequestException = 9;

    public static final int InternalException = 10;

    public static final int NoServant = 11;

    public static final int SERVANT_ALREADY_ACTIVE = 12;

    private int operation;

    private Vector args;

    public int exception; // add getException();

    public Object retVal;

    private ORB orb;
    private short priority; //kludge: this is an arg, but don't want to create short holder

    public POARunnable(int op) {
        operation = op;
        args = new Vector();
    }

    public POARunnable(int op, ORB orb) {
        this(op);
        this.orb = orb;
    }

    public void addParam(Object arg) {
        args.addElement(arg);
    }

    public void setPriority(short pr) {
        priority = pr;
    }

    public void run() {
        try{
            Object portal = ((ScopedMemory) RealtimeThread.getCurrentMemoryArea()).getPortal();
            POAImpl pimpl = null;
            if (portal != null) pimpl = (POAImpl) portal;

            switch (operation) {
                case INIT:
                    pimpl = new POAImpl();
                    pimpl.init((ORB) args.elementAt(0), (POA) args.elementAt(1),
                            (Policy[]) args.elementAt(2), (POA) args.elementAt(3),
                            (POAManager) args.elementAt(4), this);
                    ((ScopedMemory) RealtimeThread.getCurrentMemoryArea()).setPortal(pimpl);
                    break;
                case HANDLE_REQUEST:
                    pimpl.handleRequest(
                            (edu.uci.ece.zen.orb.protocol.type.RequestMessage) args.elementAt(0), this);
                    break;
                case SERVANT_TO_ID:
                    pimpl.servant_to_id((Servant) args.elementAt(0), (MemoryArea) args.elementAt(1), this);
                    break;
                case SERVANT_TO_REFERENCE:
                    retVal = pimpl.servant_to_reference((Servant) args.elementAt(0), (MemoryArea) args.elementAt(1), this);
                    break;
                case REFERENCE_TO_SERVANT:
                    pimpl.reference_to_servant((org.omg.CORBA.Object) args
                            .elementAt(0), (MemoryArea) args.elementAt(1), this);
                    break;
                case REFERENCE_TO_ID:
                    // pimpl.reference_to_id((org.omg.CORBA.Object) args.elementAt(0),
                    //                       (MemoryArea) args.elementAt(1), this );
                    break;
                case ID_TO_SERVANT:
                    pimpl.id_to_servant((byte[]) args.elementAt(0),
                            (MemoryArea) args.elementAt(1), this);
                    break;
                case ID_TO_REFERENCE:
                    pimpl.id_to_reference((byte[]) args.elementAt(0),
                            (MemoryArea) args.elementAt(1), this);
                    break;
                case ACTIVATE_OBJECT:
                    pimpl.activate_object((Servant) args.elementAt(0),
                            (MemoryArea) args.elementAt(1),
                            this);
                    break;
                case ACTIVATE_OBJECT_WITH_ID:
                    pimpl.activate_object_with_id((byte[]) args.elementAt(0),
                            (Servant) args.elementAt(1), (MemoryArea) args
                            .elementAt(2), this);
                    break;
                case ACTIVATE_OBJECT_WITH_PRIORITY:
                    pimpl.activate_object_with_priority((Servant) args.elementAt(0),
                            ((Short) args.elementAt(1)).intValue(), (MemoryArea) args.elementAt(2),
                            this);
                    break;
                case DEACTIVATE_OBJECT:
                    pimpl.deactivate_object((byte[]) args.elementAt(0),
                            (MemoryArea) args.elementAt(1), this);
                    break;
                case REMOVE_FROM_PARENT:
                    pimpl.removeFromParent((POA) args.elementAt(0));
                    break;
                case DESTROY:
                    pimpl.destroy((POA) args.elementAt(0));
                    break;
                case ID:
                    pimpl.id((MemoryArea) args.elementAt(0), this);
                    break;
                case GET_SERVANT_MANAGER:
                    pimpl.get_servant_manager(this);
                    break;
                case SET_SERVANT_MANAGER:
                    pimpl.set_servant_manager((ServantManager) args.elementAt(0),
                            this);
                    break;
                case GET_SERVANT:
                    pimpl.get_servant(this);
                    break;
                case SET_SERVANT:
                    pimpl.set_servant((Servant) args.elementAt(0), this);
                    break;
                case CREATE_REFERENCE:
                    pimpl.create_reference((String) args.elementAt(0),
                            (MemoryArea) args.elementAt(1), this);
                    break;
                case CREATE_REFERENCE_WITH_ID:
                    pimpl.create_reference_with_id((byte[]) args.elementAt(0),
                            (String) args.elementAt(1), (MemoryArea) args
                            .elementAt(2), this);
                case GET_POLICY_LIST:
                    pimpl.policy_list((MemoryArea) args.elementAt(0), this);
                    break;

                case GET_CLIENT_EXPOSED_POLICIES:
                    if(pimpl.getClientExposedPolicies().length == 0){
                        //System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");

                    }else{
                        CDROutputStream out = CDROutputStream.instance();
                        out.init(orb);
                        out.write_boolean(false); //BIGENDIAN
                        Policy[] policies = pimpl.getClientExposedPolicies();
                        int length = policies.length;
                        out.write_ulong(length);
                        for(int i = 0; i < length; ++i){
                            if(policies[i] instanceof
                                    org.omg.RTCORBA.PriorityModelPolicy){

                                edu.uci.ece.zen.orb.transport.Acceptor.
                                        marshalPriorityModelValue(
                                        (org.omg.RTCORBA.PriorityModelPolicy)
                                        policies[i], orb, out, priority);

                            }else{
                                ZenProperties.logger.log(Logger.FATAL, getClass(),
                                        "run()", "Unsupported client-exposed policy" );
                            }
                        }

                        //PolicyListHelper.write(out, pimpl.getClientExposedPolicies());
                        retVal = out;
                    }
                    break;
            }
            args.clear();
        }
        catch(Throwable ex){
            ex.printStackTrace();
        }
    }
}
