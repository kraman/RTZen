/* --------------------------------------------------------------------------*
 * $Id: ObjectKey.java,v 1.2 2004/02/25 06:12:43 nshankar Exp $
 *--------------------------------------------------------------------------*/


package edu.uci.ece.zen.poa;




public class ObjectKey {

    // @@ this is invalid ObjectKey , it is present as a null profile
    // is created as a helper, needs this method to parse profile, should
    // be deleted once proper helpers are generated!
    public ObjectKey() {
        this.contents = null;
    }

    /**
     *
     * @param contents
     */

    public ObjectKey(byte[] contents) {
        this.contents = contents;
    }
    /**
     * read an integer from the byte array
     * @param start
     * @return int
     */
    private int read_int(int start) {
        int first = this.contents[start++];
        int second = this.contents[start++];
        int third = this.contents[start++];
        int fourth = this.contents[start++];

        return (first & 0xFF) << 24 | (second & 0xFF) << 16
                | (third & 0xFF) << 8 | (fourth & 0xFF);
    }

    /**
     * Vadiate Strigified representation of the Object Key
     * @param poaName
     * @return String
     */
    public static String objectKeyString(String poaName) {
        //if (poaName.length() > 32) {
            //throw new org.omg.CORBA.BAD_PARAM("create_POA failed: POA Name > 32 characters long");
        //}

        // create an empty String Buffer
        StringBuffer temp = new StringBuffer();
        int start = 0;
        int index = poaName.indexOf("/", start);

        while (index != -1) {
            if (start < index) {
                temp.append(poaName.substring(start, index));
            }
            temp.append("\\/");
            start = index + 1;
            index = poaName.indexOf("/", start);

        }

        if (start < poaName.length()) {
            temp.append(poaName.substring(start, poaName.length()));
        }

        return new String(temp);
    }

    // /// POA INDEX DEMUXING : ACTIVE DEMUXING  //////////////////////
    /**
     * Return the active demux location of the POA in the ObjectKey
     * @return int
     */
    public int poaIndex() {
        int start = 10; // first two are prefix and hints followed by time
        int index = this.read_int(start);

        // Logger.debug("The index read from the stream = " + index);
        return index;
    }

    /**
     * Return the generation count of the POA index in the Object Key
     * @return int
     */
    public int poaIndexGenCount() {
        int start = 14;
        int count = read_int(start);

        return count;
    }

    /**
     * Return <code> ActiveDemuxLoc </code> representation of the POA index in
     * the object key
     * @return ActiveDemuxLoc
     */
    public ActiveDemuxLoc poaDemuxIndex() {
        return new ActiveDemuxLoc(this.poaIndex(), this.poaIndexGenCount());
    }

    // /////////////////////////////////////////////////////////////////////


    //// SERVANT INDEX DEMUXING : ACTIVE DEMUXING
    /**
     * Return the active demux location of the servant in the object key
     * @return ActiveDemuxLoc
     */
    public ActiveDemuxLoc servDemuxIndex() {
        int start = 18; // place where the servant index will start
        int index = read_int(start);

        // Logger.debug("Servant demux index = " + index);
        int count = read_int(start + 4);

        // Logger.debug("Servant demux gen count =" + count);
        return new ActiveDemuxLoc(index, count); // ActiveDemux Loc
    }

    // /////////////////////////////////////////////////////////////////

    /**
     * Returns true if the POA has Persisten Lifespan policy.
     * @return boolean
     */
    public boolean isPersistent() {
        if (this.contents[0] == (byte) ('P' & 0xFF)) {
            return true;
        }
        return false;
    }

    /**
     * Return if the object key has hints corresponding to servant demux location
     * @return boolean
     */
    public boolean hasHints() {
        return this.contents[1] == (byte) 1 ? true : false;
    }

    /**
     * This method returns the POAPath.
     * @return String
     */
    public String getPOAPathName() {
        if (isPersistent()) {
            int start = 26;
            int len = read_int(start);
            byte[] temp = new byte[len];

            start += 4; // advance now that we have read the length
            System.arraycopy(this.contents, start, temp, 0, len);
            return new String(temp);
        }
        return null;
    }

    /**
     * Compare transient objectkey time stamps 
     *@return boolean
     */
    public boolean compareTimeStamps(byte[] timeStamp) {
        int i = 0, j = 2; // First two bytes are the hints and the prefix

        while (i < 8) {
            if (this.contents[j++] != timeStamp[i++]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the hashcode value for this object key
     * @return int
     */
    public int hashCode() {
        if (this.hash == 0) {
            this.hash = this.toString().hashCode();
        }
        return this.hash;
    }

    /**
     * Strigified representation of the objectkey for debugging purpose
     * @return String
     */

    public String toString() {
        byte[] b = new byte[this.contents.length];

        byte c;

        for (int i = 0; i < b.length; i++) {
            c = this.contents[i];
            b[i] = (c >= (byte) ' ' && c <= (byte) '~') ? c : (byte) '.';
        }
        return new String(b);

    }

    /**
     * Compare if two object keys are equal.
     * @return boolean
     */

    public boolean equals(Object okey) {
        if (okey instanceof Object) {
            byte[] other = ((ObjectKey) okey).getContents();

            for (int i = 0; i < other.length; i++) {
                if (this.contents[i] != other[i]) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /////// OBJECTID OPERATIONS /////////////////////////////////////////

    /**
     * Return the object id embedded in the Object Key
     * @return byte[]
     */
    public byte[] getId() {

        // check if POA does have hints
        int start;
        byte[] buffer;

        if (this.hasHints()) {
            start = 26;
        } else {
            start = 18;
        }

        if (isPersistent()) {
            int skip = read_int(start);
            int begin = start + skip + 4;
            int length = this.contents.length - begin;

            buffer = new byte[length];
            System.arraycopy(this.contents, begin, buffer, 0, length);
            return buffer;
        } else {
            int length = this.contents.length - start;

            buffer = new byte[length];
            System.arraycopy(this.contents, start, buffer, 0, length);
        }
        return buffer;
    }

 

    /**
     * Return byte[] representation of the Object key
     * @return byte[]

     */
    public byte[] getContents() {
        return this.contents;
    }

    private byte[] contents;
    private int hash = 0;

}
