/* --------------------------------------------------------------------------*
 * $Id: ThreadSpecificPOACurrent.java,v 1.1 2003/11/26 22:26:35 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa;


/**
 * <code> ThreadSpecificPOACurrent </code> is the internal Object associated
 * with individual threads in ZEN.
 * @author <ahref="mailto:krishnaa@uci.edu"> Arvind S.Krishna </a>
 * @version 1.0
 */



public class ThreadSpecificPOACurrent {
    public ThreadSpecificPOACurrent(org.omg.PortableServer.POA poa,
            ObjectKey okey,
            org.omg.PortableServer.Servant servant) {
        this.poa = poa;
        this.objectKey = okey;
        this.servant = servant;
    }

    /**
     *
     * @return org.omg.PortableServer.Servant
     */
    public org.omg.PortableServer.Servant getServant() {
        return this.servant;
    }

    /**
     *
     * @return org.omg.PortableServer.POA
     */
    public org.omg.PortableServer.POA getPOA() {
        return this.poa;
    }

    /**
     *
     * @return ObejctKey
     */
    public ObjectKey getObjectKey() {
        return this.objectKey;
    }

    /**
     *
     * @param poa org.omg.PortableServer.POA
     * @param ok Objectkey
     * @param servant org.omg.PortableServer.Servant
     */
    public static void putInvocationContext(org.omg.PortableServer.POA poa,
            ObjectKey ok,
            org.omg.PortableServer.Servant servant) {
        ThreadSpecificPOACurrent current = new ThreadSpecificPOACurrent(poa, ok,
                servant);

        POATSS.tss.put(current);
    }

    // --- PRIVATE VARIBLES ---
    private org.omg.PortableServer.POA poa;
    private ObjectKey objectKey;
    private org.omg.PortableServer.Servant servant;

}
