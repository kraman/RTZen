package edu.uci.ece.zen.orb;
import edu.uci.ece.zen.orb.giop.type.*;

public abstract class ServerRequestHandler {
    protected ORB orb;
    public ServerRequestHandler(ORB orb) {
        this.orb = orb;
    }
    
    /**
     * Call scoped region graph:
     * <p>
     *      Transport thread:<br/>
     *      <p>
     *          Transport scope --ex in--&gt; ORBImpl scope --&gt; <b>Message</b> ==> POA regions
     *      </p>
     * </p>
     */
    public abstract void handleRequest(RequestMessage req);


    public abstract LocateReplyMessage handleLocateRequest(LocateRequestMessage req);
    public abstract void handleCancelRequest(CancelRequestMessage req);

    public abstract org.omg.CORBA.Object getRoot();
    public abstract void initiateShutDown(boolean waitForRequestsToComplete);
    /*
    public abstract edu.uci.ece.zen.poa.POA find_POA(String poaName);
    public abstract ObjectLocation locateObject(edu.uci.ece.zen.poa.ObjectKey key);
    */
}