/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.portableInterceptor;

public class ServerRequestInfo extends RequestInfo
    implements org.omg.PortableInterceptor.ServerRequestInfo {

    public org.omg.CORBA.Any sending_exception(){

        if(exception != null)
            return exception;

        throw new org.omg.CORBA.BAD_INV_ORDER(14, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    public byte[] object_id(){

//        if(request != null)
//            return request.getObjectKey().getId();

        throw new org.omg.CORBA.BAD_INV_ORDER(14, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    public byte[] adapter_id() {

        if(poa != null)
            return poa.id();

        throw new org.omg.CORBA.BAD_INV_ORDER(14, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    public java.lang.String target_most_derived_interface(){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Policy get_server_policy(int type){
        /*@@@not sure if this is right. The spec says:

            "This operation returns the policy in effect for this operation for the given policy type.
            The returned CORBA::Policy object shall only be a policy whose type was registered
            via register_policy_factory (see Section 21.7.2.12, “register_policy_factory,” on
            page 21-55).
            If a policy for the given type was not registered via register_policy_factory, this
            operation will raise INV_POLICY with a standard minor code of 3."

          I need to look at what "register_policy_factory" does.

        */

//        if(poa != null){
//            for(int i = 0; i < poa.policy_list().length; ++i)
//                if(poa.policy_list()[i].policy_type() == type)
//                    return poa.policy_list()[i];
//
//        }

        throw new org.omg.CORBA.INV_POLICY(3, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    public void set_slot(int id, org.omg.CORBA.Any data) throws org.omg.PortableInterceptor.InvalidSlot{
        current.set_slot(id, data);
    }

    public boolean target_is_a(java.lang.String id){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void add_reply_service_context(org.omg.IOP.ServiceContext service_context, boolean replace){

        //@@@ needs to be tested
//
//        if(replyMessage != null){
//
//            org.omg.IOP.ServiceContext[] list = replyMessage.getServiceContext();
//
//            //if its already there, take the appropriate action, replace or throw error
//            //assuming no null slots
//            for(int i = 0; i < list.length; ++i){
//                if(list[i].context_id == service_context.context_id){
//                    if(replace){
//                        list[i] = service_context;
//                        return;
//                    }
//                    else
//                        throw new org.omg.CORBA.BAD_INV_ORDER(11, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
//                }
//            }
//
//            // if it doesn't already exits, just add to the end
//            org.omg.IOP.ServiceContext[] tempList = new org.omg.IOP.ServiceContext[list.length + 1];
//
//            for(int i = 0; i < list.length; ++i){
//                tempList[i] = list[i];
//            }
//            tempList[list.length] = service_context;
//
//            replyMessage.setServiceContext(tempList);
//
//        }
    }

    /**
     * Read accessor for adapter_name attribute
     * @return the attribute value
     */
    public String[] adapter_name(){

        /*@@@make sure the name is correct, may also need to use poa.path_name()

        */
        String [] adapter_name = new String[1];

        if(poa != null){
            adapter_name[0] = poa.the_name();
            return adapter_name;
        }

        throw new org.omg.CORBA.BAD_INV_ORDER(14, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    /**
     * Read accessor for server_id attribute
     * @return the attribute value
     */
    public String server_id(){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Read accessor for orb_id attribute
     * @return the attribute value
     */
    public String orb_id(){
        return "RTZen";
    }

    //public edu.uci.ece.zen.orb.ServerRequest request = null;
    public edu.uci.ece.zen.poa.POA poa = null;
    //public org.omg.CORBA.Object target = null;

    public org.omg.CORBA.Any exception = null;

}
