/**
 * File: IfLevel.java
 * Author: LuisM Pena
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.preprocessor;


import idldepend.javacc.generated.ParseException;

import java.util.ArrayList;
import java.util.List;


/**
 * Class to manage the #if levels
 */
class IfLevel
{

   /**
    * Called when an #if directive is found
    * @param valid: set to true if the conditional has been evaluated as
    *    truth. If the current state of the stack is 'NOK' (tokens being
    *    discarded), this parameter can have any value.
    * @return true if the stack's state is 'OK', that is, the coming tokens
    *    must be processed. Note that it can only be true if the parameter
    *    valid is specified with value 'true'
    */
   public boolean addIf(boolean valid)
   {
      boolean now = notFrom == stack.size();
      if (now) {
         if (valid) {
            notFrom++;
         }
         else {
            now = false;
         }
      }
      stack.add(valid ? COND_OK : COND_NOK);
      return now;
   }

   /**
    * Returns true if the current status is'ok'
    */
   public boolean isValid()
   {
      return notFrom == stack.size();
   }

   /**
    * This method can be called to verify wether is needed to evaluate
    * an #elif condition, or the result does not affect to the stack's state
    * @return true if the outcome of the evaluation of an #elif sentence will
    *    alter the stack's state
    */
   public boolean evaluateElif()
   {
      return notFrom + 1 >= stack.size();
   }

   /**
    * Called when an #elif directive is found
    * @param valid: set to true if the conditional has been evaluated as
    *    truth. If evaluateElif returns false, this parameter can have any value.
    * @return true if the stack's state is 'OK', that is, the coming tokens
    *    must be processed. Note that it can only be true if the parameter
    *    valid is specified with value 'true'
    */
   public boolean addElif(boolean valid) throws ParseException
   {
      int size = stack.size();
      if (size == 0) {
         throw new ParseException("#elif without a previous #if");
      }
      Object top = stack.remove(size - 1);
      if (top == COND_OK_ENDED || top == COND_NOK_ENDED) {
         throw new ParseException("#elif found after #else");
      }
      boolean now = notFrom == size - 1;
      if (now) {
         if (valid) {
            notFrom++;
         }
         else {
            now = false;
         }
      }
      stack.add(valid ? COND_OK : COND_NOK);
      return now;
   }

   /**
    * Called when an #else directive is found
    * @param valid: set to true if the conditional has been evaluated as
    *    truth. If evaluateElif returns false, this parameter can have any value.
    * @return true if the stack's state is 'OK', that is, the coming tokens
    *    must be processed.
    */
   public boolean addElse() throws ParseException
   {
      int size = stack.size();
      if (size == 0) {
         throw new ParseException("#else without a previous #if");
      }
      Object top = stack.remove(size - 1);
      if (top == COND_OK_ENDED || top == COND_NOK_ENDED) {
         throw new ParseException("#else found after another #else");
      }
      boolean now = notFrom == size - 1;
      if (now) {
         notFrom++;
      }
      stack.add(top == COND_OK ? COND_NOK_ENDED : COND_OK_ENDED);
      return now;
   }

   /**
    * Called when an #endif directive is found
    * @return true if the stack's state is 'OK', that is, the coming tokens
    *    must be processed.
    */
   public boolean addEndif() throws ParseException
   {
      int size = stack.size();
      if (size == 0) {
         throw new ParseException("#endif without a previous #if");
      }
      stack.remove(--size);
      boolean now = size <= notFrom;
      if (now) {
         notFrom = size;
      }
      return now;
   }

   private int notFrom = 0; // must be equal to the stack.size() to mean that the
   // current position is ok
   private List stack = new ArrayList();

   private static final Object COND_OK = new Integer(0x0001);
   private static final Object COND_NOK = new Integer(0x0002);
   private static final Object COND_OK_ENDED = new Integer(0x0010);
   private static final Object COND_NOK_ENDED = new Integer(0x0011);
}
