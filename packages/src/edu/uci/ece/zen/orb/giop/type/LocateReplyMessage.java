package edu.uci.ece.zen.orb.giop.type;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.giop.GIOPMessage;
import edu.uci.ece.zen.utils.ReadBuffer;

/**
 * Parent class for different GIOP versions' LocateReplyMessage. Put any
 * functionality that you want to be common to LocateReplyMessage classes here.
 * See CORBA v3.0 Spec section 15.4.6
 * 
 * @author bmiller
 */
public abstract class LocateReplyMessage extends GIOPMessage {
    public LocateReplyMessage() {
    }

    public LocateReplyMessage(ORB orb, ReadBuffer stream) {
        super(orb, stream);
    }

    public void init(ORB orb, ReadBuffer stream) {
        super.init(orb, stream);
    }

    // Private variables that are set or left null based on the locate status
    // type.
    protected org.omg.IOP.IOR forwardIOR = null;

    protected org.omg.GIOP.SystemExceptionReplyBody systemException = null;

    protected short addressingDisposition = 0;

    // Abstract declarations in addition to those in GIOPMessage

    /**
     * Return the integer constant defined in
     * generated/org.omg.GIOP.LocateStatusType_1_0 or
     * generated/org.omg.GIOP.LocateStatusType_1_2
     * 
     * @return integer constant designated status of return
     */
    public abstract int getLocateStatusValue();

    /**
     * Return IOR that may be used as the target of requests for the object if
     * getLocateStatusValue is OBJECT_FORWARD or OBJECT_FORWARD_PERM. See CORBA
     * v3.0 Spec section 15.4.6.
     * 
     * @return null if locate status is not OBJECT_FORWARD or
     *         OBJECT_FORWARD_PERM, IOR otherwise.
     */
    public org.omg.IOP.IOR getForwardIOR() {
        return forwardIOR;
    }

    /**
     * Return SystemExceptionReplyBody if LocateStatus is LOC_SYSTEM_EXCEPTION
     * 
     * @return null if locate status is not LOC_SYSTEM_EXCEPTION, a
     *         SystemExceptionReplyBody otherwise.
     */
    public org.omg.GIOP.SystemExceptionReplyBody getSystemException() {
        return systemException;
    }

    /**
     * Return addressingDisposition if LocateStatus is LOC_NEEDS_ADDRESSING_MODE
     * 
     * @return short for addressingDisposition in locate status is
     *         LOC_NEEDS_ADDRESSING_MODE, a nonsensical value otherwise (assume
     *         it's 0)
     */
    public short getAddressingDisposition() {
        return addressingDisposition;
    }

    /**
     * Read and store the body of the LocateReplyMessage based on the type of
     * message that it is. Called by constructors.
     */
    protected void readBody() {
        if (this.getGiopVersion() == 10 ) { //||
            // this
            // instanceof
            // edu.uci.ece.zen.orb.giop.v1_1.LocateReplyMessage)
            // {
            if (getLocateStatusValue() == org.omg.GIOP.LocateStatusType_1_0._OBJECT_FORWARD) {
                forwardIOR = org.omg.IOP.IORHelper.read(this.istream);
            }
            // Do nothing for OBJECT_HERE and UNKNOWN_OBJECT.
        }/*
          * else if (this instanceof
          * edu.uci.ece.zen.orb.giop.v1_2.LocateReplyMessage) { int aReplyStatus =
          * getLocateStatusValue(); switch ( aReplyStatus ) { case
          * org.omg.GIOP.LocateStatusType_1_2._OBJECT_FORWARD: case
          * org.omg.GIOP.LocateStatusType_1_2._OBJECT_FORWARD_PERM: forwardIOR =
          * org.omg.IOP.IORHelper.read(this.istream); break; case
          * org.omg.GIOP.LocateStatusType_1_2._LOC_SYSTEM_EXCEPTION:
          * systemException =
          * org.omg.GIOP.SystemExceptionReplyBodyHelper.read(this.istream);
          * break; case
          * org.omg.GIOP.LocateStatusType_1_2._LOC_NEEDS_ADDRESSING_MODE:
          * addressingDisposition =
          * org.omg.GIOP.AddressingDispositionHelper.read(this.istream); break; //
          * Do nothing for OBJECT_HERE and UNKNOWN_OBJECT. default: throw new
          * org.omg.CORBA.NO_IMPLEMENT ("Unhandled reply status " +
          * aReplyStatus); } }
          */
        else {
            throw new org.omg.CORBA.NO_IMPLEMENT("Unhandled instance type of "
                    + this);
        }
    }
}