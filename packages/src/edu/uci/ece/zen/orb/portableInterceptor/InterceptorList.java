/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.portableInterceptor;


import java.util.Vector;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;


public class InterceptorList {
    public InterceptorList() {
        initialVectorSize = 2;
    }

    public InterceptorList(int size) {
        initialVectorSize = size;
    }

    public void add_client_request_interceptor(ClientRequestInterceptor interceptor)
        throws DuplicateName {
        if (clientInterceptorList == null) {
            clientInterceptorList = new Vector(initialVectorSize);
        }

        // check for dup names only if not anonymous
        if (!"".equals(interceptor.name())) {
            for (int i = 0; i < clientInterceptorList.size(); ++i) {
                if (((ClientRequestInterceptor) clientInterceptorList.get(i)).name().equals(interceptor.name())) {
                    throw new DuplicateName(" Already found: "
                            + interceptor.name(),
                            interceptor.name());
                }
            }
        }

        clientInterceptorList.add(interceptor);
    }

    public void add_server_request_interceptor(ServerRequestInterceptor interceptor)
        throws DuplicateName {
        if (serverInterceptorList == null) {
            serverInterceptorList = new Vector(initialVectorSize);
        }

        // check for dup names only if not anonymous
        if (!"".equals(interceptor.name())) {
            for (int i = 0; i < serverInterceptorList.size(); ++i) {
                if (((ServerRequestInterceptor) serverInterceptorList.get(i)).name().equals(interceptor.name())) {
                    throw new DuplicateName(" Already found: "
                            + interceptor.name(),
                            interceptor.name());
                }
            }
        }

        serverInterceptorList.add(interceptor);
    }

    public void add_ior_interceptor(IORInterceptor interceptor)
        throws DuplicateName {
        if (iorInterceptorList == null) {
            iorInterceptorList = new Vector(initialVectorSize);
        }

        // check for dup names only if not anonymous
        if (!"".equals(interceptor.name())) {
            for (int i = 0; i < iorInterceptorList.size(); ++i) {
                if (((IORInterceptor) iorInterceptorList.get(i)).name().equals(interceptor.name())) {
                    throw new DuplicateName(" Already found: "
                            + interceptor.name(),
                            interceptor.name());
                }
            }
        }

        iorInterceptorList.add(interceptor);
    }

    /*
     public boolean clientInterceptorsInUse()
     {
     return clientInterceptorList != null;
     }
     public boolean serverInterceptorsInUse()
     {
     return serverInterceptorList != null;
     }
     public boolean iorInterceptorsInUse()
     {
     return iorInterceptorList != null;
     }
     */
    public Vector getClientInterceptors() {
        return clientInterceptorList;
    }

    public Vector getServerInterceptors() {
        return serverInterceptorList;
    }

    public Vector getIORInterceptors() {
        return iorInterceptorList;
    }

    private Vector clientInterceptorList;
    private Vector serverInterceptorList;
    private Vector iorInterceptorList;

    private int initialVectorSize;
}
