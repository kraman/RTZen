package edu.uci.ece.zen.poa;

import edu.uci.ece.zen.orb.*;
import org.omg.CORBA.IntHolder;
import edu.uci.ece.zen.orb.giop.type.*;
import javax.realtime.*;

public class HandleRequestRunnable implements Runnable{
    POA poa;
    RequestMessage req;
    IntHolder exceptionValue = new IntHolder(0);

    public void init( RequestMessage req ){
        Thread.dumpStack();
        System.out.println( "HandleRequestRunnable is being init'd" );
        this.req = req;
        this.poa = (POA) req.getAssociatedPOA();
        System.out.println( "HandleRequestRunnable init complete" );
    }

    public void run(){
        POAImpl pimpl = ((POAImpl)poa.poaMemoryArea.getPortal());
        try{
            System.out.println( "HandleRequestRunnable.run() 1" );
            pimpl.requestProcessingStrategy.handleRequest( req , poa , poa.numberOfCurrentRequests , exceptionValue );
            System.out.println( "HandleRequestRunnable.run() 2" );
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
}

