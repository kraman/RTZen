package edu.uci.ece.zen.orb.portableInterceptor;


//import edu.uci.ece.zen.orb.protocols.Profile;
//import edu.uci.ece.zen.orb.protocols.ProfileList;
import org.omg.IOP.TaggedComponent;
import org.omg.CORBA.CompletionStatus;
import org.omg.IOP.ServiceContext;


public class ClientRequestInfo // extends org.omg.CORBA.LocalObject
    extends RequestInfo
    implements org.omg.PortableInterceptor.ClientRequestInfo {
    public org.omg.CORBA.Object target() {
        return target;
    }

    public org.omg.CORBA.Object effective_target() {
        return effective_target;
    }

    public org.omg.IOP.TaggedProfile effective_profile() {
        return effective_profile;
    }

    public org.omg.CORBA.Any received_exception() {
        return received_exception;
    }

    public java.lang.String received_exception_id() {
        return received_exception_id;
    }

    public TaggedComponent get_effective_component(int id) {
//        for (int i = 0; i < profiles.length(); ++i) {
//            java.util.Vector comps = profiles.get(i).getTaggedComponents();
//
//            for (int j = 0; j < comps.size(); ++j) {
//                TaggedComponent tc = (TaggedComponent) (comps.get(j));
//
//                if (tc.tag == id) {
//                    return tc;
//                }
//
//            }
//        }

        return null;

    }

    public TaggedComponent[] get_effective_components(int id){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Policy get_request_policy(int type){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void add_request_service_context(ServiceContext service_context, boolean replace) {
//        java.util.Vector contexts = requestMessage.getServiceContext();
//
//        for (int i = 0; i < contexts.size(); ++i) {
//            if (service_context.context_id
//                    == ((ServiceContext) contexts.get(i)).context_id) {
//                if (replace) {
//                    contexts.insertElementAt(service_context, i);
//                    return;
//                } else {
//                    throw new org.omg.CORBA.BAD_INV_ORDER("Service Context already exists.",
//                            15, CompletionStatus.COMPLETED_MAYBE);
//                }
//            }
//        }
//
//        contexts.add(service_context);
        //System.out.println("Added service contexts.");
    }

    /*
     public void add_request_service_context(org.omg.IOP.ServiceContext service_context, boolean replace)
     {
     org.omg.IOP.ServiceContext[] contexts = requestMessage.getServiceContext();
     org.omg.IOP.ServiceContext[] newContexts = null;
     int foundInd = -1;

     for(int i = 0; i < contexts.length && foundInd < 0; ++i)
     if(service_context.context_id == contexts[i].context_id)
     foundInd = i;

     if(foundInd > 0 && replace)
     contexts[foundInd].context_data = service_context.context_data;
     else if(foundInd > 0 && !replace)
     throw new org.omg.CORBA.BAD_INV_ORDER(
     "Service Context already exists.", 15, CompletionStatus.COMPLETED_MAYBE);
     else
     {
     newContexts = new org.omg.IOP.ServiceContext[contexts.length+1];
     for(int i = 0; i < contexts.length; ++i)
     newContexts[i] = contexts[i];

     newContexts[newContexts.length-1] = service_context;

     requestMessage.setServiceContext(newContexts);
     }

     }
     */
    public org.omg.CORBA.Object target = null;
    public org.omg.CORBA.Object effective_target = null;
    public org.omg.IOP.TaggedProfile effective_profile = null;
    public org.omg.CORBA.Any received_exception = null;
    public java.lang.String received_exception_id = null;

//    public ProfileList profiles = null;

}
