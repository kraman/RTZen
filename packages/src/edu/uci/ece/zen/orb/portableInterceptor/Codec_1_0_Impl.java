/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.portableInterceptor;


import org.omg.CORBA.*;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.*;

import edu.uci.ece.zen.orb.CDRInputStream;
import edu.uci.ece.zen.orb.CDROutputStream;

/**
 *
 *
 * @author Malli
 */

public class Codec_1_0_Impl extends org.omg.CORBA.LocalObject implements Codec
{
    public Codec_1_0_Impl(ORB orb)
    {
        this.orb = orb;
    }

    public byte[] encode(Any data) throws InvalidTypeForEncoding
    {
//        CDROutputStream out = new CDROutputStream();
//        out.write_any(data);
//        byte[] stream = out.getBuffer();
//        return stream;
        return null;
    }

    public Any decode(byte[] data) throws FormatMismatch
    {
//        CDRInputStream in = new CDRInputStream(orb, data);
//        Any  myAny = in.read_any();
//        return myAny;
        return null;
    }


    public byte[] encode_value(Any data) throws InvalidTypeForEncoding
    {
//        CDROutputStream out = new CDROutputStream();
//        data.write_value(out);
//        byte[] stream = out.getBuffer();
//        return stream;
        return null;

    }

    public Any decode_value(byte[] data, TypeCode tc) throws FormatMismatch, TypeMismatch
    {
//        CDRInputStream in = new CDRInputStream(orb, data);
//        Any myAny = orb.create_any();
//        myAny.read_value(in, tc);
//        return myAny;

        return null;
    }

    private ORB orb = null;

}
