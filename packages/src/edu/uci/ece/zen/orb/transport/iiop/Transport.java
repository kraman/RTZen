package edu.uci.ece.zen.orb.transport.iiop;

import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;

public class Transport extends edu.uci.ece.zen.orb.transport.Transport{
    private java.net.Socket sock;
    private java.io.InputStream istream;
    private java.io.OutputStream ostream;


    public Transport( edu.uci.ece.zen.orb.ORB orb , java.net.Socket sock ){
        super( orb );
        sock = sock;
        setSockProps(sock, orb);
    }

    public Transport( edu.uci.ece.zen.orb.ORB orb , String host , int port ){
        super( orb );
        try{
            sock = new java.net.Socket( java.net.InetAddress.getByName( "doc.ece.uci.edu" ) , port );
            setSockProps(sock, orb);
            System.err.println( "sock = " + sock );
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
        org.omg.RTCORBA.TCPProtocolProperties tcpPP = ((RTORBImpl)(orb.getRTORB())).tcpPP;

        try{
            sock.setReceiveBufferSize(tcpPP.recv_buffer_size());
            sock.setSendBufferSize(tcpPP.send_buffer_size());
            sock.setTcpNoDelay(tcpPP.no_delay());
            sock.setKeepAlive(tcpPP.keep_alive());
            //don't know how to set dont_route
        }catch(java.net.SocketException se){
            se.printStackTrace();
        }

    }

}
