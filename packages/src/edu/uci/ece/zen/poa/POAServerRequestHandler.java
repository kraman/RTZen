package edu.uci.ece.zen.poa;

import org.omg.CORBA.CompletionStatus;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.orb.giop.type.*;
import edu.uci.ece.zen.utils.*;


public class POAServerRequestHandler extends edu.uci.ece.zen.orb.ServerRequestHandler {
    edu.uci.ece.zen.utils.ActiveDemuxTable demuxTable;
    
    public POAServerRequestHandler() {
        int numPOAs = Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.poa.maxNumPOAs" , "1" ) );
        demuxTable = new edu.uci.ece.zen.utils.ActiveDemuxTable();
        demuxTable.init( numPOAs );
    }

    public int addPOA( FString path, org.omg.PortableServer.POA poa ){
        int idx =  demuxTable.bind( path , poa );
        return idx;
    }

    public int getPOAGenCount( int poaIdx ){
        return demuxTable.getGenCount( poaIdx );
    }

    /**
     * Call scoped region graph:
     * <p>
     *      Transport thread:<br/>
     *      <p>
     *          Transport scope --ex in--&gt; ORBImpl scope --&gt; <b>Message</b> --ex in--&gt; ORBImpl scope --&gt; 
     *              POAImpl region --ex in--&gt; ORBImpl scope --&gt; TP Region 
     *      </p>
     *      TP Thread:<br/>
     *      <p>
     *          <b>TP Region</b> --ex in--&gt; ORBImpl scope --&gt; Message Region --ex in--&gt; ORBImpl region --&gt; Transport Scope
     *      </p>
     * </p>
     */
    public void handleRequest(RequestMessage req) {
        if(ZenProperties.devDbg) System.out.println( "POAServerRequestHandler.handleRequest: Got a request to process: " + req );
        
       // gt the index into the Active Map
       FString objKey = req.getObjectKey();

       //System.out.println("Inside ServerRequestHandler.handleRequest and mem area: " + javax.realtime.RealtimeThread.getCurrentMemoryArea());
       int index = ObjectKeyHelper.getPOAIndex( objKey );
       int genCount = ObjectKeyHelper.getPOAGeneration( objKey );

       POA poa = null;

       if(ZenProperties.devDbg) System.out.println( "IOR: " + index + "," + genCount );
       if(ZenProperties.devDbg) System.out.println( "POA: " + index + "," + demuxTable.getGenCount(index) );

       //TODO: Cant throw exception here. marshall and send back
       if (demuxTable.getGenCount(index) == genCount) {
           poa = ((POA)this.demuxTable.mapEntry(index));
       } else if (ObjectKeyHelper.isPersistent( objKey )) {
           throw new org.omg.CORBA.NO_IMPLEMENT();
           /* Transient objects only for now
           byte[] poaPath = ObjectKeyHelper.getPOAPathName( objKey );
           poa = findPOA( poaPath );
           */
       } else {
           throw new org.omg.CORBA.OBJECT_NOT_EXIST("The transient poa not found");
       }

       poa.handleRequest(req);
    }

    public void handleCancelRequest( CancelRequestMessage crm ){
    }

    public POA findPOA( byte[] poaPath ) {

        /*
         There are two things here, one is the hash map from which the
         poa's reference is obtained. if the POA is not there in the hash
         map then there has to be a tree traversal from the root poa to
         get the poa or to activate the poa.
         * /

        POA poa = (POA) demuxTable.get( poaPath , poaPath.length );

        // Ask the POA to handle the request and return
        if (poa != null) {
            return poa;
        }

        /*
         * The POA is not present in the map hence we need to
         * parse the poa path name and try activating the POA
         */
         
/*
        POA parent = (POA) getRoot();
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
*/
        return null;
    }

    /**
     * handle a LOCATE_REQUEST_MESSAGE
     * @param request LocateRequestMessage
     * @return LocateReplyMessage
     */
    public LocateReplyMessage handleLocateRequest(LocateRequestMessage request) {
        /*
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
        */
        return null;
    }

    public org.omg.CORBA.Object getRoot() {
        return orb.rootPOA;
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
        /*
        POAMap.remove(poa.path_name());
        // remove from the Active Demux Table!
        this.demuxTable.unbind(poa.path_name());
        */
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
        /*
        POA poa = (POA) POAMap.get(poaName);

        // Ask the POA to handle the request and return
        if (poa != null) {
            return true;
        }

        // Logger.debug("poaPresent:ServerRequestHandler: POA not found in the Map!");

        /*
         * The POA is not present in the map hence we need to
         * parse the poa path name and try activating the POA
         * /

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
        */
        return true;
    }

    /**
     * Given an Object Key locate the POA corresponding to that ObjectKey
     * @param okey
     * @return POA
     */
    public POA find_POA( FString okey ) {
        /*
        int index = okey.poaIndex();
        int genCount = okey.poaIndexGenCount();

        if (this.demuxTable.getGenCount(index) == genCount) {
            return this.demuxTable.mapEntry(index).poa;
        } else if (okey.isPersistent()) {
            return find_POA(okey.getPOAPathName());
        }
        */
        return null;
    }
}
