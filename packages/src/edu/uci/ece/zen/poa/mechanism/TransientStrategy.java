/* --------------------------------------------------------------------------*
 * $Id: TransientStrategy.java,v 1.4 2004/03/11 19:31:37 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.mechanism;


import edu.uci.ece.zen.poa.ActiveDemuxLoc;
import edu.uci.ece.zen.poa.ActiveDemuxLocOperations;
import edu.uci.ece.zen.poa.ObjectKey;

public final class TransientStrategy extends LifespanStrategy {

    /**
     * Create Transient Strategy
     */
    public TransientStrategy() {
        value = System.currentTimeMillis();
        this.timeStamp = new byte[8];
        int nextFreeByte = 0;

        timeStamp[nextFreeByte++] = (byte) ((value >>> 56) & 0xFF);
        timeStamp[nextFreeByte++] = (byte) ((value >>> 48) & 0xFF);
        timeStamp[nextFreeByte++] = (byte) ((value >>> 40) & 0xFF);
        timeStamp[nextFreeByte++] = (byte) ((value >>> 32) & 0xFF);
        timeStamp[nextFreeByte++] = (byte) ((value >>> 24) & 0xFF);
        timeStamp[nextFreeByte++] = (byte) ((value >>> 16) & 0xFF);
        timeStamp[nextFreeByte++] = (byte) ((value >>> 8) & 0xFF);
        timeStamp[nextFreeByte++] = (byte) (value & 0xFF);
    }

   /**
    * Return time stamp
    * @return long time stamp
    */
    public long timeStamp() {
        return this.value;
    }

   /**
    * create Transient Object Key with no hints
    * @param path_name The poa path name
    * @param oid object id
    * @param index poa demux index
    * @return edu.uci.ece.zen.poa.ObjectKey corresponding object key
    */

    public byte[]  create(String path_name,
                                                byte[] oid,
                                                ActiveDemuxLoc index) {
          System.out.println("ok no servant loc here ");

        return IdNoHintStrategy.create(prefix, oid, this.timeStamp,
                index.marshall());
    }

   /**
    * Create transient Object key without hints
    * @param path_name POA path
    * @param oid object id
    * @param poaLoc poa index
    * @param servLoc servant index
    * @return edu.uci.ece.zen.poa.ObjectKey corresponding object key
    */
    public byte[] create(String path_name,
            byte[] oid,
            ActiveDemuxLoc poaLoc,
            int index, int count) {
         System.out.println("ok servant loc here ");

        return IdHintStrategy.create(prefix, oid, this.timeStamp,
                poaLoc.marshall(), ActiveDemuxLocOperations.marshall(index,count));

    }

   /**
    * Validate Object Key
    * @param ok object Key
    * @throws org.omg.CORBA.OBJECT_NOT_EXIST
    */
    public void validate(byte[] ok)
        throws org.omg.CORBA.OBJECT_NOT_EXIST {
        if (! ObjectKey.compareTimeStamps(ok, this.timeStamp) && ObjectKey.isPersistent(ok) ) {

            // if (this.value != ok.timeStamp())
            throw new org.omg.CORBA.OBJECT_NOT_EXIST(2,
                    org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }

    }

    protected long  value; // contains the time stamp of the POA
    protected static char prefix = 'T';   // Transient Prefix
    protected byte[] timeStamp;
}
