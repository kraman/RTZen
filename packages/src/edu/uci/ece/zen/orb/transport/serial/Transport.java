package edu.uci.ece.zen.orb.transport.serial;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.policies.PolicyManagerImpl;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ZenProperties;

public class Transport extends edu.uci.ece.zen.orb.transport.Transport {
    private NativeSerialPort sock;

    private java.io.InputStream istream;
    private java.io.OutputStream ostream;

    //Acceptor
    public Transport(edu.uci.ece.zen.orb.ORB orb, edu.uci.ece.zen.orb.ORBImpl orbImpl, NativeSerialPort sock) {
        super(orb, orbImpl);
        try {
            ZenProperties.logger.log("++++++Serial Transport: Trying to get io streams");
            this.sock = sock;
            istream = sock.getInputStream();
            ostream = sock.getOutputStream();
            ZenProperties.logger.log("+++++++Serial Transport: Got io streams");
            if (ZenProperties.dbg) ZenProperties.logger.
                    log("Serial Transport ready: " + istream + " " + ostream);
            //setSockProps(sock, orb);
        } catch (Exception ex) {
            ZenProperties.logger.log(Logger.WARN,
                    getClass(), "<init>",
                    "Error connecting to remote location.", ex);
        }
    }

    //Connector
    public Transport(edu.uci.ece.zen.orb.ORB orb, edu.uci.ece.zen.orb.ORBImpl orbImpl) {
        this( orb , orbImpl , NativeSerialPort.instance() );
        /*
        try{
            ZenProperties.logger.log("++++++Serial Transport: Trying to get lock");
            NativeSerialPort.instance().lock.acquire();  
            ZenProperties.logger.log("++++++Serial Transport: Got lock");
        }catch(java.lang.InterruptedException ie){
            ZenProperties.logger.log(Logger.WARN,
                    getClass(), "<init>",
                    "Error ", ie);      
        }*/
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

        //try {
            if (pm.recv_buffer_size > 0) {
                ZenProperties.logger.log("Setting socket props. No properties supported.");
                //sock.setReceiveBufferSize(pm.recv_buffer_size);
                //sock.setSendBufferSize(pm.send_buffer_size);
                //sock.setTcpNoDelay(pm.no_delay);
                //sock.setKeepAlive(pm.keep_alive);
                //don't know how to set dont_route
            }
        //} catch (java.net.SocketException se) {
        //    ZenProperties.logger.log(Logger.WARN, getClass(), "setSockProps", se);
        //}
    }

    public void finalize(){
        NativeSerialPort.instance().lock.release();
    }
}

