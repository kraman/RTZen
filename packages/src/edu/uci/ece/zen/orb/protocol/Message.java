/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.protocol;

import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.CDRInputStream;
import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.PriorityMappingImpl;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import org.omg.RTCORBA.PriorityModel;

/**
 * Parent of all Message types. Put functionality to be common to all types
 * of Messages here. However, functionality for any particular message type
 * goes in edu.uci.ece.zen.orb.protocol.types.* classes. functionality for each
 * type.
 *
 * @author Bruce Miller
 * @author Mark Panahi
 */

public abstract class Message {
    protected CDRInputStream istream;
    protected ScopedMemory scope;
    protected ReadBuffer messageBody;
    private static final short defaultPriority = 
        PriorityMappingImpl.toCORBA( (short) javax.realtime.PriorityScheduler.instance().getNormPriority() );
    protected short priority;
    protected boolean clientPropagated;
        

    protected Message() { }

    public Message(ORB orb, ReadBuffer stream) {
        this.istream = CDRInputStream.instance();
        this.istream.init(orb, stream);
        priority = defaultPriority;
        clientPropagated = false;
        ZenProperties.logger.log( "Setting default norm priority in Message.init()" );
    }

    public  void init(ORB orb, ReadBuffer stream) {
        this.istream = CDRInputStream.instance();
        this.istream.init(orb, stream);
        priority = defaultPriority;
        clientPropagated = false;
        ZenProperties.logger.log( "Setting default norm priority in Message.init()" );
    }
    
    public short getPriority(){
        return priority;
    }

    public void setPriority(short p){
        priority = p;
    }
    
    public boolean isClientPropagated(){
        return clientPropagated;
    }

    public void setClientPropagated(boolean cp){
        clientPropagated = cp;
    }    
    
    public abstract int getRequestId();
    public abstract void marshal(CDROutputStream out);
    public abstract int getVersion();

    public CDRInputStream getCDRInputStream() {
        return istream;
    }

    public final void setScope(ScopedMemory scope) {
        this.scope = scope;
    }

    public final ScopedMemory getScope() {
        return scope;
    }

    protected ScopedMemory transport;
    public void setTransport(ScopedMemory t) {
        transport = t;
    }

    public ScopedMemory getTransport() {
        return transport;
    }

    public final void free(){
        if(istream != null) istream.free();
        transport = null;
        istream = null;
        scope = null;
        priority = defaultPriority;
        internalFree();
        //messageBody.free();
    }

    public final void freeWithoutBufferRelease(){
        transport = null;
        istream = null;
        scope = null;
        internalFree();
        //messageBody.free();
    }

    public abstract void internalFree();

    private Class protocolFactory;
    public void setProtocolFactory( Class pf ){ this.protocolFactory = pf; }
    public Class getProtocolFactory(){ return this.protocolFactory; }
}
