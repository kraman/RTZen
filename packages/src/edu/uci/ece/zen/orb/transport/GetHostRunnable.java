package edu.uci.ece.zen.orb.transport;

import java.net.InetAddress;

import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

class GetHostRunnable implements Runnable {
    public InetAddress inetaddr;

    public String host;

    public GetHostRunnable(String host) {
        this.host = host;
    }

    public void run() {
        try {
            inetaddr = InetAddress.getByName(new String(host.getBytes()));
            inetaddr.getHostAddress();
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "run", e);
        }
    }
}