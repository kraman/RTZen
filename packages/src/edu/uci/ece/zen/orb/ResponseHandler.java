package edu.uci.ece.zen.orb;

public class ResponseHandler implements org.omg.CORBA.portable.ResponseHandler {
    public ResponseHandler(ORB orb, int requestID, byte majorVersion, byte minorVersion) {
        this.requestID = requestID;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.orb = orb;
    }

    /**
     * This method is called by servant code to send back a reply.
     *
     * @return a <code>ServerReply</code> which inherits from <code>CDROutputStream</code>.
     */
    public org.omg.CORBA.portable.OutputStream createReply() {
        return new ServerReply(orb, requestID, majorVersion, minorVersion);
    }

    /**
     * this method is called by the Servant Code and indicates the case
     * where an User Exception has ocured.
     *
     * @return a <code>ServerReply</code> which inherits from <code>CDROutputStream</code>.
     */
    public org.omg.CORBA.portable.OutputStream createExceptionReply() {
        return new ServerReply(orb, requestID, majorVersion, minorVersion,
                org.omg.GIOP.ReplyStatusType_1_0._USER_EXCEPTION);
    }

    // -- Private data     
    private ORB orb;
    protected int requestID;
    protected byte majorVersion;
    protected byte minorVersion;
}
