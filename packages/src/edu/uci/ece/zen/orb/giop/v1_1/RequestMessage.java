package edu.uci.ece.zen.orb.giop.v1_1;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;

public class RequestMessage extends edu.uci.ece.zen.orb.giop.v1_0.RequestMessage {
    private RequestHeader_1_1 header;

    protected static final byte reserved[] = { 0x00, 0x00, 0x00 };

    ///////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Message Read ///////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    /** Do not directly call this method It is provided only to give
     * access to the default constructor of GIOPMessage.
     */
    public RequestMessage() {
        super();
    };


    public RequestMessage ( ClientRequest clr,  int messageId ) {
        //edu.uci.ece.zen.orb.giop.GIOPMessage();
        super();

        header = new RequestHeader_1_1 (
                                        new org.omg.IOP.ServiceContext[0],
                                        messageId,
                                        clr.responseExpected,
                                        this.reserved,
                                        clr.objectKey,
                                        clr.operation,
                                        new byte[0]
                                        );
    }


    public RequestMessage( ORB orb, ReadBuffer stream ) {
        super( orb, stream );
        header = RequestHeader_1_1Helper.read( istream );
        messageBody = stream;
    }

    public void marshal( CDROutputStream out ) {
        RequestHeader_1_1Helper.write( out, header );
    }

}
