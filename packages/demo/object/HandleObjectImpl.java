/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.object;

class HandleObjectImpl extends HandleObjectPOA{
    public org.omg.CORBA.Object echoObject(org.omg.CORBA.Object inVal, org.omg.CORBA.ObjectHolder outVal){
        org.omg.CORBA.Object retVal = inVal;
        outVal.value = inVal;
        return retVal;
    }
}
                                
