/* --------------------------------------------------------------------------*
 * $Id: PersistentStrategy.java,v 1.2 2004/02/25 06:12:46 nshankar Exp $
 *--------------------------------------------------------------------------*/


package edu.uci.ece.zen.poa.mechanism;


import edu.uci.ece.zen.poa.ActiveDemuxLoc;
import edu.uci.ece.zen.poa.ActiveDemuxLocOperations;


public final class PersistentStrategy extends LifespanStrategy {

    public PersistentStrategy() {
        this.value = -1;

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
     * The persistent strategy does not have a time-stamp. Returns -1
     * to indicate error.
     * @return long timestamp value
     */
    public long timeStamp() {
        return this.value;
    }

   /**
    * Create a Persistent Object-Key w/o hints
    * @param path_name The poa-path name
    * @param oid object id
    * @param loc active demux location
    * @return edu.uci.ece.zen.poa.ObjectKey corresponding object key
    */
    public edu.uci.ece.zen.poa.ObjectKey create(String path_name,
                                                byte[] oid,
                                                ActiveDemuxLoc loc) {

        return IdNoHintStrategy.create(PersistentStrategy.prefix, path_name,
                this.timeStamp, oid, loc.marshall());
    }

   /**
    * Create Object key with hints
    * @param path_name poa-path name
    * @param oid object-id
    * @param poaLoc demux location of the POA
    * @param servLoc demxu location of the servant
    * @return edu.uci.ece.zen.poa.ObjectKey
    */
    public edu.uci.ece.zen.poa.ObjectKey create(String path_name,
                                                byte[] oid,
                                                ActiveDemuxLoc poaLoc,
                                                int index, int count) {

        return IdHintStrategy.create(PersistentStrategy.prefix, path_name,
                this.timeStamp, oid, poaLoc.marshall(), ActiveDemuxLocOperations.marshall(index,count));

    }

   /**
    * Check if the Object corresponds to a persistent object-key
    * @param ok ObjectKey
    * @throws org.omg.CORBA.OBJECT_NOT_EXIST
    */
    public void validate(edu.uci.ece.zen.poa.ObjectKey ok) throws
                org.omg.CORBA.OBJECT_NOT_EXIST {

        if (!ok.isPersistent() && !ok.compareTimeStamps(this.timeStamp)) {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST(2,
                    org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }
    }

    protected long value;
    protected byte[] timeStamp;
    // --the character that has to be prepened
    protected static char prefix = 'P';
}
