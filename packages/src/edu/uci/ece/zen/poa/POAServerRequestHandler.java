/* --------------------------------------------------------------------------*
 * $Id: POAServerRequestHandler.java,v 1.1 2003/11/26 22:26:32 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa;

/**
 * The interface between the POA and the ORB is not-standardized. In ZEN
 * the POAServerRequestHandler class is the concrete server request handler
 * for the case when there is a POA. In future, the OMG may do away with the
 * POA. In that case, in ZEN, the only addition required will be another
 * concrete ServerRequestHandler.
 * @author Arvind S. Krishna
 * @author Nishant Shankaran
 *
 */

// --- OMG Imports
import java.io.File;
import java.io.FileInputStream;

import org.omg.CORBA.CompletionStatus;

import edu.uci.ece.zen.orb.ObjectLocation;
import edu.uci.ece.zen.orb.ServerRequest;
import edu.uci.ece.zen.orb.giop.GIOPMessageFactory;
import edu.uci.ece.zen.orb.giop.LocateReplyMessage;
import edu.uci.ece.zen.orb.giop.LocateRequestMessage;
import edu.uci.ece.zen.sys.ZenProperties;


public class POAServerRequestHandler extends edu.uci.ece.zen.orb.ServerRequestHandler {

    public POAServerRequestHandler(edu.uci.ece.zen.orb.ORB orb) {
        super(orb);
        this.orb = orb;
        POAMap = new java.util.Hashtable();
    }

    /**
     * Associate this POA with the POAServerRequestHandler class.
     * @param name Complete POA path name
     * @param poa Reference to the POA
     */

    ActiveDemuxLoc addPOA(String name, POA poa) {
        POAMap.put(name, poa);

        // bind to the ActiveDemux Table
        ActiveDemuxLoc loc = this.demuxTable.bind(name, poa);

        return loc;
    }


    /**
     * Handle the client Request
     * @param req entiry corresponding to the client request.
     */
    public void handleRequest(ServerRequest req) {
       // gt the index into the Active Map

       int index = req.message.getObjectKey().poaIndex();
       int genCount = req.message.getObjectKey().poaIndexGenCount();

       POA poa = null;

        if (this.demuxTable.getGenCount(index) == genCount) {
            //Logger.debug ("handleRequest:poa found in the ActiveDemux table");
            poa = this.demuxTable.mapEntry(index).poa;
        } else if (req.message.getObjectKey().isPersistent()) {
            // Logger.debug("handleRequest:POA not found in Active Demux map");
            String poaName = req.message.getObjectKey().getPOAPathName();

            poa = this.find_POA(poaName);
        } else {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST("The transient poa not found");
        }
        // Logger.debug("poa.handleRequest (req)");
        poa.handleRequest(req);
    }

    /**
     * Given the complete POA path name, return the POA reference associated
     * with that name. 
     * @param poaName POAName
     * @return POA null if lookup fails
     */

    public POA find_POA(String poaName) {

        /*
         There are two things here, one is the hash map from which the
         poa's reference is obtained. if the POA is not there in the hash
         map then there has to be a tree traversal from the root poa to
         get the poa or to activate the poa.
         */

        POA poa = (POA) POAMap.get(poaName);

        // Ask the POA to handle the request and return
        if (poa != null) {
            return poa;
        }

        /*
         * The POA is not present in the map hence we need to
         * parse the poa path name and try activating the POA
         */

        POA parent = (POA) this.getRoot();
        StringBuffer childPOAName = new StringBuffer();

        // The first POA name should be that of Root POA plus a slash
        // hence we need not get that once more
        int index = 8;

        // To ease the parsing of the Path Name, we add the end condition "/"
        // to the string.
        poaName = poaName.concat("/");

        int next = Util.getPOAName(poaName, index, childPOAName);

        while (next != -1) {
            parent = parent.getChildPOA(new String(childPOAName));

            // go one beyond the separator that is the starting
            // index of the next POA Name
            index = next;

            childPOAName = null; // enable gc
            childPOAName = new StringBuffer();
            next = Util.getPOAName(poaName, index, childPOAName);

        }

        // check if we have parsed the entire key
        if (index == poaName.length()) {
            // Add it in the POA HashMap
            this.addPOA(poaName, parent);
            return parent;
        } else {
            // Logger.debug("Error: POAPathName corrupted: Last POA Name parsed = "
            // + childPOAName + "POAPathName = " + poaName);
            throw new org.omg.CORBA.BAD_CONTEXT("Invalid POA Path Name in the ObjectKey",
                    0, CompletionStatus.COMPLETED_NO);
        }

    }

    private String imrCall(ObjectKey okey) {
        // Logger.debug("In  IMR");
        String fileName = ZenProperties.getProperty("imr.ior_file");

        try {
            File iorFile = new File(fileName);
            FileInputStream istream = new FileInputStream(iorFile);
            byte[] in = new byte[istream.available()];

            istream.read(in);
            istream.close();
            String ior = new String(in);
            org.omg.CORBA.Object obj = orb.string_to_object(ior);
            edu.uci.ece.zen.imr.LookUp imr = edu.uci.ece.zen.imr.LookUpHelper.narrow(obj);

            if (imr.isThere(okey.toString())) {
                // Logger.debug("Present in the IMR table");
                String fior = imr.getIOR(okey.toString());

                return(fior);
            } else {
                // Logger.debug("Not Present in the IMR table");
                return("ERROR");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return("ERROR");
        }

    }


    /**
     * handle a LOCATE_REQUEST_MESSAGE
     * @param request LocateRequestMessage
     * @return LocateReplyMessage
     */

    public LocateReplyMessage handleLocateRequest(LocateRequestMessage request) {
        // Logger.debug("locateRequest:request id=" + request.getRequestId());
        LocateReplyMessage reply = null;
        ObjectKey okey = request.getObjectKey();
        String fior;
        edu.uci.ece.zen.orb.CDROutputStream cdrtemp = new edu.uci.ece.zen.orb.CDROutputStream();
        int index = okey.poaIndex();
        int genCount = okey.poaIndexGenCount();
        int POAcount;

        try {
            POAcount = this.demuxTable.getGenCount(index);
        } catch (Exception e) {
            POAcount = 0;
            try {
                String isIMR = System.getProperty("IMR");

                if (isIMR.equals("TRUE")) {
                    fior = imrCall(okey);
                    if (fior.equals("ERROR")) {
                        reply = GIOPMessageFactory.createLocateReplyMessage(orb,
                                request.getRequestId(),
                                org.omg.GIOP.LocateStatusType_1_0._UNKNOWN_OBJECT,
                                cdrtemp);
                    } else {
                        reply = GIOPMessageFactory.createLocateReplyMessage(orb,
                                request.getRequestId(),
                                org.omg.GIOP.LocateStatusType_1_0._OBJECT_FORWARD,
                                cdrtemp);
                    }

                    try {
                        reply.marshallHeader();
                    } catch (java.lang.Exception unkn) {
                        unkn.printStackTrace();
                    }
                    if (!fior.equals("ERROR")) {
                        cdrtemp.write_string(fior);
                    }
                    return reply;
                }

            } catch (RuntimeException ee) {
                // Logger.debug("NOT hosting IMR");
                return reply;

            }
        }
        POA poa = null;

        if (this.demuxTable.getGenCount(index) == genCount) {

            // Logger.debug("locateRequest:poa found in the ActiveDemux table");
            poa = this.demuxTable.mapEntry(index).poa;
        } else if (okey.isPersistent()) {
            // Logger.debug("locateRequest:POA not found in Active Demux map");
            String poaName = okey.getPOAPathName();

            try {
                poa = this.find_POA(poaName);

            } catch (Exception e) {
                // Logger.debug("locateRequest:UNKNOWN OBJECT");
                reply = GIOPMessageFactory.createLocateReplyMessage(orb,
                        request.getRequestId(),
                        org.omg.GIOP.LocateStatusType_1_0._UNKNOWN_OBJECT,
                        cdrtemp);

                try {
                    reply.marshallHeader();
                } catch (java.lang.Exception unkn) {
                    unkn.printStackTrace();
                }
                return reply;
            }
        } else {
            // Logger.debug("locateRequest:UNKNOWN OBJECT");
            reply = GIOPMessageFactory.createLocateReplyMessage(orb,
                    request.getRequestId(),
                    org.omg.GIOP.LocateStatusType_1_0._UNKNOWN_OBJECT,
                    cdrtemp);

            try {
                reply.marshallHeader();
            } catch (java.lang.Exception unkn) {
                unkn.printStackTrace();
            }
            return reply;
        }

        try {
            org.omg.PortableServer.Servant servant = poa.id_to_servant(okey.getId());
        } catch (Exception e) {
            // Logger.debug("locateRequest:UNKNOWN OBJECT");
            reply = GIOPMessageFactory.createLocateReplyMessage(orb,
                    request.getRequestId(),
                    org.omg.GIOP.LocateStatusType_1_0._UNKNOWN_OBJECT,
                    cdrtemp);
            try {
                reply.marshallHeader();
            } catch (java.lang.Exception unkn) {
                unkn.printStackTrace();
            }
            return reply;
        }

        // Success. return OBJECT_HERE
        // Logger.debug("locateRequest:OBJECT HERE");
        reply = GIOPMessageFactory.createLocateReplyMessage(orb,
                request.getRequestId(),
                org.omg.GIOP.LocateStatusType_1_0._OBJECT_HERE, cdrtemp);
        try {
            reply.marshallHeader();
        } catch (java.lang.Exception unkn) {
            unkn.printStackTrace();
        }
        return reply;
    }

    public org.omg.CORBA.Object getRoot() {
        return orb.getRootPOA();
    }

    /**
     * Initiate shutdown of the entire POA hierarchy, called during ORB
     * shutdown.
     * @param waitForRequestsToComplete if true wait for requests to complete, else shutdown
     * now
     */

    public void initiateShutDown(boolean waitForRequestsToComplete) {
        if (this.getRoot() != null) {
            ((org.omg.PortableServer.POA) this.getRoot()).destroy(true,
                    waitForRequestsToComplete);
        }
    }
    /**
     *
     * @param key
     * @return ObjectLocatioin
     */

    public ObjectLocation locateObject(ObjectKey key) {
        return new ObjectLocation(ObjectLocation.NOT_HERE);
    }

    /**
     *
     * @return String
     */

    public String toString() {
        return "POAServerRequestHandler";
    }
    /**
     * Remove the POA from the Server Request Handler
     * @param poa
     */

    public void remove(edu.uci.ece.zen.poa.POA poa) {
        POAMap.remove(poa.path_name());
        // remove from the Active Demux Table!
        this.demuxTable.unbind(poa.path_name());
    }

    /**
     * The ORB uses this method to check if the POA is present in the POA
     * hierarchy. The method returns true id such a POA is present or tries
     * to activate the POA. A return value of true indicates that the POA
     * is ready to service requests and the Client may safely invoke requests
     * on that POA. This interaction is similar to the request Processing
     * mechanism in the <code> handle </code> method.
     *
     * @param poaName
     * @return boolean
     */

    public boolean processorPresent(String poaName) {

        POA poa = (POA) POAMap.get(poaName);

        // Ask the POA to handle the request and return
        if (poa != null) {
            return true;
        }

        // Logger.debug("poaPresent:ServerRequestHandler: POA not found in the Map!");

        /*
         * The POA is not present in the map hence we need to
         * parse the poa path name and try activating the POA
         */

        POA parent = (POA) this.getRoot();
        StringBuffer childPOAName = new StringBuffer();

        // The first POA name should be that of Root POA plus a slash
        // hence we need not get that once more
        int index = 8;

        // To ease the parsing of the Path Name, we add the end condition "/"
        // to the string.
        poaName = poaName.concat("/");

        int next = Util.getPOAName(poaName, index, childPOAName);

        while (next != -1) {
            parent = parent.getChildPOA(new String(childPOAName));
            // go one beyond the separator that is the starting
            // index of the next POA Name

            index = next;

            childPOAName = null; // enable gc
            childPOAName = new StringBuffer();
            next = Util.getPOAName(poaName, index, childPOAName);

        }

        // check if we have parsed the entire key
        if (index == poaName.length()) {
            // Add it in the POA HashMap
            this.addPOA(poaName, parent);
            return true;
        } else {
            return false;
        }

    }
    /**
     * Given an Object Key locate the POA corresponding to that ObjectKey
     * @param okey
     * @return POA
     */

    public POA find_POA(ObjectKey okey) {

        int index = okey.poaIndex();
        int genCount = okey.poaIndexGenCount();

        if (this.demuxTable.getGenCount(index) == genCount) {
            return this.demuxTable.mapEntry(index).poa;
        } else if (okey.isPersistent()) {
            return find_POA(okey.getPOAPathName());
        }

        return null;
    }
/*
        public org.omg.PortableServer.ThreadPolicy create_thread_policy(
            final org.omg.PortableServer.ThreadPolicyValue value) {

            return this.serverRequestHandler.create_thread_policy(value);
//throw new RuntimeException();
}

    public org.omg.PortableServer.LifespanPolicy create_lifespan_policy(
            final org.omg.PortableServer.LifespanPolicyValue value) {
            return this.serverRequestHandler.create_lifespan_policy(value);
//throw new RuntimeException();
   }

    public org.omg.PortableServer.IdUniquenessPolicy create_id_uniqueness_policy(
            final org.omg.PortableServer.IdUniquenessPolicyValue value) {
            return this.serverRequestHandler.create_id_uniqueness_policy(value);

//throw new RuntimeException();
}

    public org.omg.PortableServer.IdAssignmentPolicy create_id_assignment_policy(
            final org.omg.PortableServer.IdAssignmentPolicyValue value) {
            return this.serverRequestHandler.create_id_assignment_policy(value);
//throw new RuntimeException();
    }
    public org.omg.PortableServer.ImplicitActivationPolicy create_implicit_activation_policy(
            final org.omg.PortableServer.ImplicitActivationPolicyValue value) {
            return this.serverRequestHandler.create_implicit_activation_policy(value);
//throw new RuntimeException();
    }
        public org.omg.PortableServer.ServantRetentionPolicy create_servant_retention_policy(
            final org.omg.PortableServer.ServantRetentionPolicyValue value) {
            return this.serverRequestHandler.create_servant_retention_policy(value);
//throw new RuntimeException();
    }
    public org.omg.PortableServer.RequestProcessingPolicy create_request_processing_policy(
            final org.omg.PortableServer.RequestProcessingPolicyValue value) {
            return this.serverRequestHandler.create_request_processing_policy(value);
//throw new RuntimeException();
    }
*/

    // --- Data members ---
    private java.util.Hashtable POAMap;
    private edu.uci.ece.zen.orb.ORB orb;

    // --- Active Demux Map for O(1) request processing
    private ActiveDemuxPOATable demuxTable = new ActiveDemuxPOATable();

}
