package  edu.uci.ece.zen.poa;




/*
 * This class is the thread specif POA Current in ZEN. fundamentally there are 2 POA Current Objects
 * one is the POA Current that implements the PortableServer Current Interface and the other is the
 * Thread Specific current that is not seen by the end user. The Current( End user opaque ) uses the thread
 * specific Current to implement the operations required by the Spec.
 */

    public class TSSStorage extends java.lang.ThreadLocal {
        public Object initialValue() {
            return null;
        }
    /**
     * Return the Current Object associated with this thread
     * @return ThreadSpecificPOACurrent
     */

     public ThreadSpecificPOACurrent getCurrent() {
          return (ThreadSpecificPOACurrent) super.get();
     }

    /**
     * Put the thread specific current object into the ThreadLocal Store
     * @param current ThreadSpecificPOACurrent
     */
        public void put(ThreadSpecificPOACurrent current) {
            super.set(current);
        }

    }

