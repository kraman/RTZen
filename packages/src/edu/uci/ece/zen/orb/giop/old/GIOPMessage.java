package edu.uci.ece.zen.orb.giop;

import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.CDRInputStream;
import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.ReadBuffer;

public abstract class GIOPMessage{
    protected CDRInputStream istream;
    protected ScopedMemory scope;

    protected GIOPMessage(){
    }
    
    protected GIOPMessage( ORB orb , ReadBuffer stream ){
        this.istream = CDRInputStream.instance();
        this.istream.init( orb , stream );
    }

    public abstract boolean isRequest();
    public abstract boolean isReply();
    public abstract int getRequestId();

    public CDRInputStream getCDRInputStream(){
        return istream;
    }
    public final void setScope( ScopedMemory scope ){ this.scope = scope; }
    public final ScopedMemory getScope(){ return scope; }

    public abstract void marshall( CDROutputStream out );
}
