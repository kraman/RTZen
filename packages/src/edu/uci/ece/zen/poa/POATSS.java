package  edu.uci.ece.zen.poa;




/*
 * This class is the thread specif POA Current in ZEN. fundamentally there are 2 POA Current Objects
 * one is the POA Current that implements the PortableServer Current Interface and the other is the
 * Thread Specific current that is not seen by the end user. The Current( End user opaque ) uses the thread
 * specific Current to implement the operations required by the Spec.
 */


public class POATSS {
    public static TSSStorage tss = new TSSStorage();

}
