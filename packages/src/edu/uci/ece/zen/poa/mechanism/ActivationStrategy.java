package edu.uci.ece.zen.poa.mechanism;

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.Policy;

import edu.uci.ece.zen.poa.POARunnable;

/**
 * This class is a generalization of the activation strategies. It allows to create Creates 
 * either a Implicit/Explicit Activation strategy for the POA based on the policies specified.
 * 
 * @author Arvind S. Krishna
 * @author Juan Colmenares
 * @version 1.0
 */
public abstract class ActivationStrategy
{
    // TODO Auto-criticism: this code DUPLICATES the functionality of the instanceof operator.
    
    /** ID for the Implicit Activation Strategy*/
    public static final int IMPLICIT_ACTIVATION = 0;

    /** ID for the Explicit Activation Strategy*/
    public static final int EXPLICIT_ACTIVATION = 1;
    
    /**
     *  ImplicitActivationStrategy singleton created in immortal memory. 
     */
    public static final ActivationStrategy IMPLICIT_ACTIVATION_STRATEGY = 
        new ImplicitActivationStrategy();
    
    /**
     *  ExplicitActivationStrategy singleton created in immortal memory. 
     */
    public static final ActivationStrategy EXPLICIT_ACTIVATION_STRATEGY = 
        new ExplicitActivationStrategy();
    
    /**
     * Returns an activation strategy instance according to the specified activation policy.
     * If explicit activation policy or no activation policy is specified, this operation 
     * returns an <code>ExplicitActivationStrategy</code> object (default strategy for child POAs). 
     * Only if implicit activation policy is specified, then this operation returns an    
     * <code>ImplicitActivationStrategy</code> object. The implicit activation policy requires
     * that: RETAIN as a retention policy and SYSTEM_ID as a id assignment policy; otherwise this
     * operation retunrs <code>null</code>.  
     * 
     * @param policy array of specified policies.
     * @param assignmentStrategy the id assignment strategy.  
     * @param retentionStrategy the retention strategy.
     * @param exceptionValue 
     * @return an activation strategy instance according to the specified activation policy.
     */
    public static ActivationStrategy init(Policy[] policy, IdAssignmentStrategy assignmentStrategy,
            ServantRetentionStrategy retentionStrategy, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.NoException;
        
        if (PolicyUtils.useImplicitActivationPolicy(policy))
        {
            // Check if the other policies are Retain and System Id
            retentionStrategy.validate(ServantRetentionStrategy.RETAIN, exceptionValue);
        
            if (exceptionValue.value != POARunnable.NoException)
            {
                exceptionValue.value = POARunnable.InvalidPolicyException;
                return null;
            }

            assignmentStrategy.validate(IdAssignmentStrategy.SYSTEM_ID, exceptionValue);
            if (exceptionValue.value != POARunnable.NoException)
            {
                exceptionValue.value = POARunnable.InvalidPolicyException;
                return null;
            }

            return IMPLICIT_ACTIVATION_STRATEGY;
        } 
        else
        {
            return EXPLICIT_ACTIVATION_STRATEGY;
        }
    }
    
    /**
    * Returns <code>true</code> if <code>id</code> is equal to the identifier of 
    * concrete strategy class; otherwise returns <code>false</code>. 
    * Usually used to check if two strategies are the same.
    * 
    * @param id strategy's identifier
    * @return <code>true</code> if <code>id</code> corresponds to the identifier of 
    *         concrete strategy class; otherwise 
    */
    public abstract boolean validate(int id);   

    
    
    /** 
     * Represents the strategy related to the implicit activation policy. 
     */
    private static final class ImplicitActivationStrategy extends ActivationStrategy 
    {
        /** Package constructor */
        ImplicitActivationStrategy() {};
        
        /* (non-Javadoc)
         * @see edu.uci.ece.zen.poa.mechanism.ActivationStrategy#validate(int)
         */
        public boolean validate(int id) 
        {
            return (ActivationStrategy.IMPLICIT_ACTIVATION == id);
        }
    }
   
    /** 
     * Represents the strategy related to the no implicit activation policy. 
     */
    private static final class ExplicitActivationStrategy extends ActivationStrategy
    {
        /** Package constructor */
        public ExplicitActivationStrategy() {};
        
        /* (non-Javadoc)
         * @see edu.uci.ece.zen.poa.mechanism.ActivationStrategy#validate(int)
         */    
        public boolean validate(int id)
        {
            return (ImplicitActivationStrategy.EXPLICIT_ACTIVATION == id);
        }
    }
}

