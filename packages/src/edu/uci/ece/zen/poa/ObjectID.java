/* --------------------------------------------------------------------------*
 * $Id: ObjectID.java,v 1.1 2003/11/26 22:26:20 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa;



/**
 * <code> ObjectID </code> is the logical representation of the objectids in ZEN.
 * This class serves as a wrapper around byte[] that implements the equals() and
 * hashcode() method to aid in mapping to Hashtables.
 * 
 * @author Arvind S. Krishna 
 * @author Nishant Shankran
 */ 

final public class ObjectID {


    /**
     * Constructor for the class.
     * @param id
     */
    public ObjectID(byte[] id) {
        this.contents = id;
    }

    /**
     * This method comaptes the contents of Object. Returns true if the contents are equal.
     * @param other
     * @return boolean true if the objects are equal else false.
     */
    public boolean equals(Object other) {
        if (other instanceof ObjectID) {
            byte[] temp = ((ObjectID) other).getId();

            if (temp.length != this.contents.length) {
                return false;
            }

            for (int i = 0; i < this.contents.length; i++) {
                // Logger.debug ("this.contents = " + this.contents[i]);
                // Logger.debug ("other contents = " + temp[i]);
                if (this.contents[i] != temp[i]) {
                    return false;
                }
            }
            return true;
        } else if (other instanceof byte[]) {
            byte[] temp = (byte[]) other;

            if (this.contents.length != temp.length) {
                return false;
            }

            for (int i = 0; i < temp.length; i++) {
                if (this.contents[i] != temp[i]) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Returns the hash code of the Object.
     * @return int
     */
    public int hashCode(byte[] contents) {
        int hash = 0;

        for (int i = 0; i < contents.length; i++) {
            hash += contents[i] * (i + 1);
        }

        return hash;
    }

    /**
     * Returns the object Id
     * @return byte[]
     */
    public byte[] getId() {
        return this.contents;
    }

    private byte[] contents;

}
