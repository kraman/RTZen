package edu.uci.ece.zen.orb.transport.iiop;

import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.policies.*;

public class Transport extends edu.uci.ece.zen.orb.transport.Transport{
    private java.net.Socket sock;
    private java.io.InputStream istream;
    private java.io.OutputStream ostream;


    public Transport( edu.uci.ece.zen.orb.ORB orb , edu.uci.ece.zen.orb.ORBImpl orbImpl , java.net.Socket sock ){
        super( orb , orbImpl );
        sock = sock;
        setSockProps(sock, orb);
    }

    public Transport( edu.uci.ece.zen.orb.ORB orb , edu.uci.ece.zen.orb.ORBImpl orbImpl , String host , int port ){
        super( orb , orbImpl );
        try{
            System.out.println( "Connecting to " + host + ":" + port );
            sock = new java.net.Socket( host , port );
            //setSockProps(sock, orb);
            //             System.err.println( "sock = " + sock ); 
            istream = sock.getInputStream();
            ostream = sock.getOutputStream();
        }catch( Exception ex ){
            ZenProperties.logger.log(
                Logger.WARN,
                "edu.uci.ece.zen.orb.transport.iiop.Transport",
                "<cinit>",
                "Error connecting to remote location. " + ex.toString() );
        }
    }
    public java.io.InputStream getInputStream(){
        return istream;
    }
    public java.io.OutputStream getOutputStream(){
        return ostream;
    }

    //hook method to weave in TCPProtocolProperties
    private void setSockProps(java.net.Socket sock, ORB orb){
        //org.omg.RTCORBA.TCPProtocolProperties tcpPP = ((RTORBImpl)(orb.getRTORB())).tcpPP;

        PolicyManagerImpl pm = (PolicyManagerImpl)(orb.getPolicyManager());

        try{
            if(pm.recv_buffer_size > 0){
                System.out.println("Setting socket props.");
                sock.setReceiveBufferSize(pm.recv_buffer_size);
                sock.setSendBufferSize(pm.send_buffer_size);
                sock.setTcpNoDelay(pm.no_delay);
                sock.setKeepAlive(pm.keep_alive);
                //don't know how to set dont_route
            }
        }catch(java.net.SocketException se){
            se.printStackTrace();
        }


    }

}
