package edu.uci.ece.zen.poa.mechanism;

import org.omg.CORBA.IntHolder;

import edu.uci.ece.zen.poa.ObjectKeyHelper;
import edu.uci.ece.zen.poa.POARunnable;
import edu.uci.ece.zen.utils.FString;

public final class PersistentStrategy extends LifespanStrategy {
    public PersistentStrategy() {
        this.value = -1;
        timeStamp.reset();
        timeStamp.append((byte) ((value >>> 56) & 0xFF));
        timeStamp.append((byte) ((value >>> 48) & 0xFF));
        timeStamp.append((byte) ((value >>> 40) & 0xFF));
        timeStamp.append((byte) ((value >>> 32) & 0xFF));
        timeStamp.append((byte) ((value >>> 24) & 0xFF));
        timeStamp.append((byte) ((value >>> 16) & 0xFF));
        timeStamp.append((byte) ((value >>> 8) & 0xFF));
        timeStamp.append((byte) (value & 0xFF));
    }

    /**
     * The persistent strategy does not have a time-stamp. Returns -1 to
     * indicate error.
     * 
     * @return long timestamp value
     */
    public long timeStamp() {
        return this.value;
    }

    /**
     * Create a Persistent Object-Key w/o hints
     * 
     * @param path_name
     *            The poa-path name
     * @param oid
     *            object id
     * @param loc
     *            active demux location
     * @return edu.uci.ece.zen.poa.ObjectKey corresponding object key
     */
    public void create(FString path_name, FString oid, int locIndex,
            int locGenCount, FString okey_out) {
        // I don't know what you want here, but the loc is a byte[] and needs to
        // be a FString. Maybe create one and append?
        IdNoHintStrategy.create(PersistentStrategy.prefix, path_name,
                this.timeStamp, oid, locIndex, locGenCount, okey_out);
    }

    /**
     * Create Object key with hints
     * 
     * @param path_name
     *            poa-path name
     * @param oid
     *            object-id
     * @param poaLoc
     *            demux location of the POA
     * @param servLoc
     *            demxu location of the servant
     * @return edu.uci.ece.zen.poa.ObjectKey
     */
    public void create(FString path_name, FString oid, int poaLocIndex,
            int poaLocGenCount, int servLocIndex, int servLocGenCount,
            FString okey_out) {
        // Same as above
        IdHintStrategy.create(PersistentStrategy.prefix, path_name,
                this.timeStamp, oid, poaLocIndex, poaLocGenCount, servLocIndex,
                servLocGenCount, okey_out);

    }

    /**
     * Check if the Object corresponds to a persistent object-key
     * 
     * @param ok
     *            ObjectKey
     * @throws org.omg.CORBA.OBJECT_NOT_EXIST
     */
    public void validate(FString ok, IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        if (!ObjectKeyHelper.isPersistent(ok)
                && !ObjectKeyHelper.compareTimeStamps(this.timeStamp, ok)) {
            exceptionValue.value = POARunnable.ObjNotExistException;
        }
    }

    protected long value;

    protected FString timeStamp;

    // --the character that has to be prepened
    protected static char prefix = 'P';
}
