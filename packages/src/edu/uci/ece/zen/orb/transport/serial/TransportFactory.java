package edu.uci.ece.zen.orb.transport.serial;

public class TransportFactory extends edu.uci.ece.zen.orb.transport.TransportFactory{
    public abstract void internalInit(){}
    public abstract void createAcceptorImpl( FString args ){
    }
    public void connectImpl( TaggedComponent profile , ObjectImpl obj , boolean isCollocated ){
        if( isCollocated ){
            ZenProperties.logger.log("+++++++++++++++++++++++++++++++++++++++++++++Collocated object, Skipping Serial");
            return;
        }

        ZenProperties.logger.log("ObjRefDel processTaggedProfile SERIAL 1");

        byte[] data = profile.profile_data;
        if (ZenProperties.dbg) ZenProperties.logger.log("ObjRefDel processTaggedProfile SERIAL prof data len:" + data.length);
        CDRInputStream in = CDRInputStream.fromOctetSeq(data, orb);

        FString object_key = in.getBuffer().readFString(false);
        long connectionKey = -TAG_SERIAL.value;
        ScopedMemory transportScope = null;
        synchronized(edu.uci.ece.zen.orb.transport.serial.SerialPort.class){
            transportScope = orb.getConnectionRegistry().getConnection(connectionKey);
        }
        ZenProperties.logger.log("ObjRefDel processTaggedProfile SERIAL 2");
        if( transportScope == null ){
            try{
                transportScope = edu.uci.ece.zen.orb.transport.serial.Connector
                    .instance().connect(null, (short)0, orb, orbImpl);
                orb.getConnectionRegistry().putConnection(connectionKey, transportScope);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        if (transportScope != null) {
            System.out.println( "Serial connection succesful" );
            addLaneData(
                    javax.realtime.PriorityScheduler.instance().getMinPritority() ,
                    javax.realtime.PriorityScheduler.instance().getMaxPritority() ,
                    transportScope, object_key, edu.uci.ece.zen.orb.protocol.giop.GIOPMessageFactory.class );
        }else{
            System.out.println( "Serial connection unsuccesful" );
        }
        ZenProperties.logger.log("ObjRefDel processTaggedProfile SERIAL 3");
        in.free();
    }
}
