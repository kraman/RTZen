/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.protocol.IOP;

import edu.uci.ece.zen.utils.FString;

/**
 * Helper class for : ServiceContextList
 *
 * @author OpenORB Compiler
 */
public class ServiceContextListHelper {

    /**
     * Read ServiceContextList from a marshalled stream
     *
     * @param istream
     *            the input stream
     * @return the readed ServiceContextList value public static
     *         ServiceContext[] read(org.omg.CORBA.portable.InputStream istream) {
     *         ServiceContext[] new_one = ServiceContext.arrayInstance();
     *         ServiceContext.length = istream.read_ulong(); //new_one = new
     *         org.omg.IOP.ServiceContext[size7]; for (int i7=0; i7
     *         <ServiceContext.length; i7++) new_one[i7] =
     *         ServiceContextHelper.read(istream, i7); return new_one; }
     */

    public static FString read(org.omg.CORBA.portable.InputStream istream,
            FString fs) {
        //ServiceContext[] new_one = ServiceContext.arrayInstance();

        int length = istream.read_ulong();
        fs.append((int) length);
        //new_one = new org.omg.IOP.ServiceContext[size7];
        for (int i7 = 0; i7 < length; i7++)
            ServiceContextHelper.read(istream, fs);

        return fs;
    }

    /**
     * Write ServiceContextList into a marshalled stream
     *
     * @param ostream
     *            the output stream
     * @param value
     *            ServiceContextList value
     */
    public static void write(org.omg.CORBA.portable.OutputStream ostream,
            FString value) {
        /*
         * ostream.write_ulong(ServiceContext.length); for (int i7=0; i7
         * <ServiceContext.length; i7++) {
         * ServiceContextHelper.write(ostream,value[i7]); }
         */
    }

}