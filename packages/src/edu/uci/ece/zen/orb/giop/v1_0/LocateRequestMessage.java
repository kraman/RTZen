packaage edu.uci.ece.zen.orb.giop.v1_0;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.ORB;

public class LocateRequestMessage extends edu.uci.ece.zen.orb.giop.GIOPMessage {
    private LocateRequestHeader_1_0 header;


    public LocateRequestMessage( ORB orb, ReadBuffer stream) {
        super (orb, stream);
        header = LocateRequestHeader_1_0Helper.read(istream);
    }

}
