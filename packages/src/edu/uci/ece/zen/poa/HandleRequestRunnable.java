package edu.uci.ece.zen.poa;

import edu.uci.ece.zen.orb.*;
import org.omg.CORBA.IntHolder;
import edu.uci.ece.zen.orb.giop.type.*;
import javax.realtime.*;

public class HandleRequestRunnable implements Runnable{
    POA poa;
    RequestMessage req;
    IntHolder exceptionValue;

    public void init( RequestMessage req ){
        this.req = req;
        this.poa = (POA) req.getAssociatedPoa();
        exceptionValue = new IntHolder(0);
    }

    public void run(){
        POAImpl pimpl = ((POAImpl)poa.poaMemoryArea.getPortal());
        try{
            pimpl.requestProcessingStrategy.handleRequest( req , poa , poa.numberOfCurrentRequests , exceptionValue );
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
}

