/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.poa;

import edu.uci.ece.zen.utils.FString;

public class ObjectKeyHelper {
    /**
     * read an integer from the byte array
     * 
     * @param start
     * @return int
     */
    private static int read_int(byte[] contents, int start) {
        int first = contents[start++];
        int second = contents[start++];
        int third = contents[start++];
        int fourth = contents[start++];

        return (first & 0xFF) << 24 | (second & 0xFF) << 16
                | (third & 0xFF) << 8 | (fourth & 0xFF);
    }

    /**
     * Vadiate Strigified representation of the Object Key
     * 
     * @param poaName
     * @return String / public static String objectKeyString(String poaName) {
     *         if (poaName.length() > 32) { throw new
     *         org.omg.CORBA.BAD_PARAM("create_POA failed: POA Name > 32
     *         characters long"); } // create an empty String Buffer
     *         StringBuffer temp = new StringBuffer(); int start = 0; int index =
     *         poaName.indexOf("/", start); while (index != -1) { if (start <
     *         index) { temp.append(poaName.substring(start, index)); }
     *         temp.append("\\/"); start = index + 1; index =
     *         poaName.indexOf("/", start); } if (start < poaName.length()) {
     *         temp.append(poaName.substring(start, poaName.length())); } return
     *         new String(temp); }
     */

    // /// POA INDEX DEMUXING : ACTIVE DEMUXING //////////////////////
    /**
     * Return the active demux location of the POA in the ObjectKey
     * 
     * @return int
     */
    public static int getPOAIndex(FString objKey) {
        int start = 10; // first two are prefix and hints followed by time
        int index = read_int(objKey.getData(), start);
        return index;
    }

    /**
     * Return the generation count of the POA index in the Object Key
     * 
     * @return int
     */
    public static int getPOAGeneration(FString objKey) {
        int start = 14;
        int count = read_int(objKey.getData(), start);

        return count;
    }

    // /////////////////////////////////////////////////////////////////////

    public static int servDemuxIndex(FString objKey) {
        int start = 18; // place where the servant index will start
        return read_int(objKey.getData(), start);
    }

    public static int servDemuxGenCount(FString objKey) {
        int start = 18; // place where the servant index will start
        return read_int(objKey.getData(), start + 4);
    }

    // /////////////////////////////////////////////////////////////////

    /**
     * Returns true if the POA has Persisten Lifespan policy.
     * 
     * @return boolean
     */
    public static boolean isPersistent(FString objKey) {
        return (objKey.getData()[0] == (byte) ('P' & 0xFF));
    }

    /**
     * Return if the object key has hints corresponding to servant demux
     * location
     * 
     * @return boolean
     */
    public static boolean hasHints(FString objKey) {
        return objKey.getData()[1] == (byte) 1;
    }

    /**
     * This method returns the POAPath.
     * 
     * @return String
     */
    public static void getPOAPathName(FString objKey, FString poaPathOut) {
        poaPathOut.reset();
        if (isPersistent(objKey)) {
            int start = 26;
            int len = read_int(objKey.getData(), start);
            start += 4; // advance now that we have read the length
            poaPathOut.append(objKey.getData(), start, len);
        }
    }

    /**
     * Compare transient objectkey time stamps
     * 
     * @return boolean
     */
    public static boolean compareTimeStamps(FString timeStamp, FString objKey) {
        int i = 0, j = 2; // First two bytes are the hints and the prefix

        while (i < 8) {
            if (timeStamp.getData()[j++] != objKey.getData()[i++]) { return false; }
        }
        return true;
    }

    /////// OBJECTID OPERATIONS /////////////////////////////////////////

    /**
     * Return the object id embedded in the Object Key
     * 
     * @return byte[]
     */
    public static void getId(FString objKey, FString oidOut) {
        oidOut.reset();

        // check if POA does have hints
        int start;
        byte[] buffer;

        if (hasHints(objKey)) {
            start = 26;
        } else {
            start = 18;
        }

        if (isPersistent(objKey)) {
            int skip = read_int(objKey.getData(), start);
            int begin = start + skip + 4;
            int length = objKey.length() - begin;

            oidOut.append(objKey.getData(), begin, length);
        } else {
            int length = objKey.length() - start;

            oidOut.append(objKey.getData(), start, length);
        }
    }
}