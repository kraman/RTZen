package edu.uci.ece.zen.orb.transport.iiop;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.policies.PolicyManagerImpl;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ZenProperties;
import java.net.InetAddress;

public class Transport extends edu.uci.ece.zen.orb.transport.Transport {
    private java.net.Socket sock;

    private java.io.InputStream istream;

    private java.io.OutputStream ostream;

    //Acceptor
    public Transport(edu.uci.ece.zen.orb.ORB orb,
            edu.uci.ece.zen.orb.ORBImpl orbImpl, java.net.Socket sock) {
        super(orb, orbImpl);
        setProtocolFactory( edu.uci.ece.zen.orb.protocol.giop.GIOPMessageFactory.class );
        try {
            ZenProperties.logger.log("*****IIOP Transport: Trying to get io streams");
            this.sock = sock;
            istream = sock.getInputStream();
            ostream = sock.getOutputStream();
            ZenProperties.logger.log("IIOP Transport: Got io streams");
            if (ZenProperties.dbg) ZenProperties.logger.log("IIOP Transport ready: "
                    + istream + " " + ostream);
            //setSockProps(sock, orb);
        } catch (Exception ex) {
            ZenProperties.logger.log(Logger.WARN,
                    getClass(), "<init>",
                    "Error connecting to remote location.", ex);
        }
    }

    //Connector
    public Transport(edu.uci.ece.zen.orb.ORB orb,
            edu.uci.ece.zen.orb.ORBImpl orbImpl, String host, int port) {
        super(orb, orbImpl);
        setProtocolFactory( edu.uci.ece.zen.orb.protocol.giop.GIOPMessageFactory.class );


/*

        int retries = 1;

        while(retries <= 20)
            try{

                break;
            }catch(Exception e){
                retries++;
                System.out.println("Cannot connect to server -- retrying");
                try{
                    Thread.currentThread().sleep(1000);
                }catch(Exception e1){
                    e.printStackTrace();
                }
            }
*/
            try {
                if (ZenProperties.dbg) ZenProperties.logger.log("Connecting to "
                        + host + ":" + port);
                if (ZenProperties.dbg) ZenProperties.logger.log("Current transport thread is of type "
                                + javax.realtime.RealtimeThread
                                        .currentRealtimeThread());

                if(ZenProperties.CAN_USE_STRINGS)
                    sock = new java.net.Socket(host, port);
                else{
                    sock = new java.net.Socket( InetAddress.getByAddress( new byte[]{
                        ((byte)(128&0xFF)),
                        ((byte)(195&0xFF)),
                        ((byte)(174&0xFF)),
                        ((byte)(20&0xFF))})  , port );
                }


                ZenProperties.logger.log("Connected");
                //setSockProps(sock, orb);
                //             System.err.println( "sock = " + sock );
                istream = sock.getInputStream();
                ostream = sock.getOutputStream();
                if (ZenProperties.dbg) ZenProperties.logger.log("Transport ready: "
                        + istream + " " + ostream);
            } catch (java.net.UnknownHostException ex) {
                ZenProperties.logger.log(Logger.WARN,
                        getClass(), "<init>",
                        "Error connecting to remote location.", ex);

            } catch (java.io.IOException ex) {
                ZenProperties.logger.log(Logger.WARN,
                        getClass(), "<init>",
                        "Error connecting to remote location.", ex);

            }

    }

    public java.io.InputStream getInputStream() {
        return istream;
    }

    public java.io.OutputStream getOutputStream() {
        return ostream;
    }

    public String toString(){
        return sock.toString();
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

