package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;
import java.net.*;

class GetHostRunnable implements Runnable{
    public InetAddress inetaddr;
    public String host;

    public GetHostRunnable( String host ){
        this.host = host;
    }

    public void run(){
        try{
            inetaddr = InetAddress.getByName( new String(host.getBytes()) );
            inetaddr.getHostAddress();
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
}
