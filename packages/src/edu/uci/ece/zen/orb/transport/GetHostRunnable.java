package edu.uci.ece.zen.orb.transport;

import java.net.InetAddress;

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
            e.printStackTrace();
        }
    }
}