package edu.uci.ece.zen.orb.transport.iiop;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.policies.PolicyManagerImpl;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ZenProperties;

public class Transport extends edu.uci.ece.zen.orb.transport.Transport {
    private java.net.Socket sock;

    private java.io.InputStream istream;

    private java.io.OutputStream ostream;

    //Acceptor
    public Transport(edu.uci.ece.zen.orb.ORB orb,
            edu.uci.ece.zen.orb.ORBImpl orbImpl, java.net.Socket sock) {
        super(orb, orbImpl);
        try {
            sock = sock;
            istream = sock.getInputStream();
            ostream = sock.getOutputStream();
            if (ZenProperties.dbg) ZenProperties.logger.log("Transport ready: "
                    + istream + " " + ostream);
            //setSockProps(sock, orb);
        } catch (Exception ex) {
            ZenProperties.logger.log(Logger.WARN,
                    getClass(), "<cinit>",
                    "Error connecting to remote location.", ex);
        }
    }

    //Connector
    public Transport(edu.uci.ece.zen.orb.ORB orb,
            edu.uci.ece.zen.orb.ORBImpl orbImpl, String host, int port) {
        super(orb, orbImpl);
        try {
            if (ZenProperties.dbg) ZenProperties.logger.log("Connecting to "
                    + host + ":" + port);
            if (ZenProperties.dbg) ZenProperties.logger.log("Current transport thread is of type "
                            + javax.realtime.RealtimeThread
                                    .currentRealtimeThread());

            sock = new java.net.Socket(host, port);
            ZenProperties.logger.log("Connected");
            //setSockProps(sock, orb);
            //             System.err.println( "sock = " + sock );
            istream = sock.getInputStream();
            ostream = sock.getOutputStream();
            if (ZenProperties.dbg) ZenProperties.logger.log("Transport ready: "
                    + istream + " " + ostream);
        } catch (Exception ex) {
            ZenProperties.logger.log(Logger.WARN,
                    getClass(), "<cinit>",
                    "Error connecting to remote location.", ex);
        }
    }

    public java.io.InputStream getInputStream() {
        return istream;
    }

    public java.io.OutputStream getOutputStream() {
        return ostream;
    }

    //hook method to weave in TCPProtocolProperties
    private void setSockProps(java.net.Socket sock, ORB orb) {
        //org.omg.RTCORBA.TCPProtocolProperties tcpPP =
        // ((RTORBImpl)(orb.getRTORB())).tcpPP;

        PolicyManagerImpl pm = (PolicyManagerImpl) (orb.getPolicyManager());

        try {
            if (pm.recv_buffer_size > 0) {
                ZenProperties.logger.log("Setting socket props.");
                sock.setReceiveBufferSize(pm.recv_buffer_size);
                sock.setSendBufferSize(pm.send_buffer_size);
                sock.setTcpNoDelay(pm.no_delay);
                sock.setKeepAlive(pm.keep_alive);
                //don't know how to set dont_route
            }
        } catch (java.net.SocketException se) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "setSockProps", se);
        }
    }
}

