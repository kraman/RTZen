package edu.uci.ece.zen.poa.mechanism;

import org.omg.CORBA.IntHolder;

import edu.uci.ece.zen.poa.ObjectKeyHelper;
import edu.uci.ece.zen.poa.POARunnable;
import edu.uci.ece.zen.utils.FString;

public final class TransientStrategy extends LifespanStrategy {

    /**
     * Create Transient Strategy
     */
    public TransientStrategy() {
        value = 0;//System.currentTimeMillis();
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

        this.ftimeStamp = new FString(0);
        //ftimeStamp.append(timeStamp);
    }

    /**
     * Return time stamp
     * 
     * @return long time stamp
     */
    public long timeStamp() {
        return this.value;
    }

    /**
     * create Transient Object Key with no hints
     * 
     * @param path_name
     *            The poa path name
     * @param oid
     *            object id
     * @param index
     *            poa demux index
     * @return edu.uci.ece.zen.poa.ObjectKey corresponding object key
     */

    public void create(FString path_name, FString oid, int index, int genCount,
            FString okey_out) {
        IdNoHintStrategy.create(prefix, oid, this.ftimeStamp, index, genCount,
                okey_out);
    }

    /**
     * Create transient Object key without hints
     * 
     * @param path_name
     *            POA path
     * @param oid
     *            object id
     * @param poaLoc
     *            poa index
     * @param servLoc
     *            servant index
     * @return edu.uci.ece.zen.poa.ObjectKey corresponding object key
     */
    public void create(FString path_name, FString oid, int poaLocIndex,
            int poaLocGenCount, int servLocIndex, int servLocGenCount,
            FString okey_out) {
        IdHintStrategy.create(prefix, oid, this.ftimeStamp, poaLocIndex,
                poaLocGenCount, servLocIndex, servLocGenCount, okey_out);
    }

    /**
     * Validate Object Key
     * 
     * @param ok
     *            object Key
     * @throws org.omg.CORBA.OBJECT_NOT_EXIST
     */
    public void validate(FString ok, IntHolder exceptionHolder) {
        exceptionHolder.value = POARunnable.NoException;
	/*
        if (!ObjectKeyHelper.compareTimeStamps(this.ftimeStamp, ok)
                && ObjectKeyHelper.isPersistent(ok)) {
            // if (this.value != ok.timeStamp())
            exceptionHolder.value = POARunnable.ObjNotExistException;
        }*/
    }

    protected long value; // contains the time stamp of the POA

    protected static char prefix = 'T'; // Transient Prefix

    protected byte[] timeStamp;

    protected FString ftimeStamp;
}
