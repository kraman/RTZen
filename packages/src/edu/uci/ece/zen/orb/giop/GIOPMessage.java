package edu.uci.ece.zen.orb.giop;

import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.CDRInputStream;
import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.ReadBuffer;

/**
 * Parent of all GIOP Message types.  Put functionality to be common
 * to all types of GIOP Messages here.  However, functionality for any
 * particular message type goes in edu.uci.ece.zen.ocb.giop.types.*
 * classes.  functionality for each type.
 *
 * @author Bruce Miller
 */

public abstract class GIOPMessage{
    protected CDRInputStream istream;
    protected ScopedMemory scope;
    protected ReadBuffer messageBody;
    protected GIOPMessage() {
    }
    
    protected GIOPMessage( ORB orb , ReadBuffer stream ) {
        this.istream = CDRInputStream.instance();
        this.istream.init( orb , stream );
    }

    public abstract int getRequestId();
    public abstract void marshal( CDROutputStream out );
    public abstract int getGiopVersion();

    public CDRInputStream getCDRInputStream() {
        return istream;
    }
    public final void setScope( ScopedMemory scope ) { this.scope = scope; }
    public final ScopedMemory getScope(){ return scope; }
}
