package edu.uci.ece.zen.poa.mechanism;

import edu.uci.ece.zen.poa.*;
import edu.uci.ece.zen.utils.*;
import org.omg.CORBA.IntHolder;


public final class SystemIdStrategy extends IdAssignmentStrategy {

    /**
     * Create System Id Strategy Strat key genration from 0
     */
    public SystemIdStrategy() {
        this.id = 0;
    }

   /**
    * Obtain new system id.
    * @return byte[]
    */
    public void nextId( FString id_out , IntHolder exceptionValue ){
        exceptionValue.value = POARunnable.NoException;
        writeInt(++id , id_out );
    }
    
   /**
    * Check if strategy same as this strategy
    * @param policyName policy type
    * @return boolean
    */
    public boolean isPresent(int policyName) {
        if (IdAssignmentStrategy.SYSTEM_ID == policyName) {
            return true;
        } else {
            return false;
        }
    }
    
   /**
    * validate policy type
    * @param policy policy-type
    */
    public void validate(int policy , IntHolder exceptionValue){
        exceptionValue.value = POARunnable.NoException;
        if (!isPresent(policy)) {
            exceptionValue.value = POARunnable.WrongPolicyException;
        }
    }

    /**
     * Verify if the id was generated by this strategy
     * @param id
     * @return boolean true if yes, else false
     */
   public boolean verifyID( FString id) {
        // The only possible way of identification is if this id
        // corresponds to an integer else it could not be generated
        // by the system
        boolean retVal = true;
        for( int i=0;i<id.length()&&retVal;i++ )
            if( '0' > id.getData()[i] || id.getData()[i] > '9' )
                retVal = false;
        return retVal;
    }

    private void writeInt(int value , FString id_out ) {
        id_out.append((byte) ((value >>> 24) & 0xFF));
        id_out.append((byte) ((value >>> 16) & 0xFF));
        id_out.append((byte) ((value >>> 8) & 0xFF));
        id_out.append((byte) (value & 0xFF));
    }

    private int id;
}
