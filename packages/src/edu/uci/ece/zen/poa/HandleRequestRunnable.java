package edu.uci.ece.zen.poa;

import org.omg.CORBA.IntHolder;

import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

public class HandleRequestRunnable implements Runnable {
    POA poa;

    RequestMessage req;

    IntHolder exceptionValue = new IntHolder(0);

    public void init(RequestMessage req) {
        this.req = req;
        this.poa = (POA) req.getAssociatedPOA();
    }

    public void run() {
        POAImpl pimpl = ((POAImpl) poa.poaMemoryArea.getPortal());
        try {
            pimpl.requestProcessingStrategy.handleRequest(req, poa,
                    poa.numberOfCurrentRequests, exceptionValue);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "run", e);
        }
    }
}

