package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.orb.giop.type.*;

public class ResponseHandler implements org.omg.CORBA.portable.ResponseHandler {
    private RequestMessage req;
    private ORB orb;
    
    public ResponseHandler(ORB orb, RequestMessage req ) {
        this.orb = orb;
        this.req = req;
    }

    /**
     * This method is called by servant code to send back a reply.
     *
     * @return a <code>ServerReply</code> which inherits from <code>CDROutputStream</code>.
     */
    public org.omg.CORBA.portable.OutputStream createReply() {
        return edu.uci.ece.zen.orb.giop.GIOPMessageFactory.constructReplyMessage( orb , req );
    }

    /**
     * this method is called by the Servant Code and indicates the case
     * where an User Exception has ocured.
     *
     * @return a <code>ServerReply</code> which inherits from <code>CDROutputStream</code>.
     */
    public org.omg.CORBA.portable.OutputStream createExceptionReply() {
        return edu.uci.ece.zen.orb.giop.GIOPMessageFactory.constructExceptionMessage( orb , req );
    }
}
