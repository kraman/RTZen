package edu.uci.ece.zen.orb.transport.serial;

import java.net.UnknownHostException;

public class InetAddress
{
    public static InetAddress getLocalHost() throws UnknownHostException {
        return new InetAddress();
    }

    public String getHostAddress() {
        System.out.println("Returning dummy host address: serial_port");
        return "serial_port";
    }
}
