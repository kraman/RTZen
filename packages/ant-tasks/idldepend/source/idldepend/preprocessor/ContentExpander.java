/**
 * File: ContentExpander.java
 * Author: LuisM Pena
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.preprocessor;


import idldepend.javacc.generated.Evaluator;
import idldepend.javacc.generated.ParseException;
import idldepend.javacc.generated.PreprocessorConstants;
import idldepend.javacc.generated.Token;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Class reading tokens and expanding them with the defined macros
 */
class ContentExpander
{

   public ContentExpander(PreprocessorUser user, MacroHandler macroHandler)
   {
      this.macroHandler = macroHandler;
      this.user = user;
      macroReader = macroHandler.getMacroReader();
      translateMacroIdentifiers = true;
      evaluating = false;
   }

   /**
    * Instructs the class that the next tokens must be evaluated as a condition,
    * until completeEvaluation is called
    */
   public void startEvaluation()
   {
      evaluationBuffer.setLength(0);
      evaluating = true;
   }

   /**
    * Requests the result for the requested evaluation, throwing a
    * ParseException if the evaluation is incorrect
    */
   public boolean completeEvaluation() throws ParseException
   {
      evaluating = false;
      return new Evaluator(evaluationBuffer.toString()).evaluate();
   }

   /**
    * Expands the presented token. It is expanded into a macro, or
    * used into an already expanding macro.
    */
   public void expandToken(Token token) throws ParseException
   {
      if (translateMacroIdentifiers && token.kind == PreprocessorConstants.ID) {
         String id = token.toString();
         if (disabledMacros.contains(id)) {
            throw new ParseException("Recursive macro definition in " + id);
         }
         else {
            Macro macro = macroHandler.getMacro(token.toString());
            if (macro == null || (!evaluating && macro.isOnlyForEvaluation())) {
               expandNormalToken(token);
            }
            else if (macro.isComplex()) { // macro complex, create a new macro reader
               macroReader = macroHandler.addMacroReader(macro);
               translateMacroIdentifiers = macroReader.translateIdentifiers();
            }
            else { // simple macro: expand it
               expandMacro(macro.getName(), macro.expand(null));
            }
         }
      }
      else {
         expandNormalToken(token);
      }
   }

   /**
    * Expands a token that does not define a macro
    */
   private void expandNormalToken(Token token) throws ParseException
   {
      if (macroReader == null) {
         passToken(token);
      }
      else if (macroReader.readToken(token)) {
         MacroReader completed = macroReader;

         // remove this reader
         macroReader = macroHandler.endedMacroReader();

         if (macroReader == null) {
            translateMacroIdentifiers = true;
         }
         else {
            translateMacroIdentifiers = macroReader.translateIdentifiers();
         }

         // macro completed : expand
         expandMacro(completed.getMacroName(), completed.expandMacro());
      }
      else {
         translateMacroIdentifiers = macroReader.translateIdentifiers();
      }
   }

   /**
    * Expands a macro, specifying the arguments to be used on the expansion
    * @param name: the name of the macro
    * @param expansion: a list containing as many elements as arguments require
    *    the macro
    */
   private void expandMacro(String name, List expansion) throws ParseException
   {
      disabledMacros.add(name);
      Iterator it = expansion.iterator();
      while (it.hasNext()) {
         expandToken((Token) it.next());
      }
      disabledMacros.remove(name);
   }

   /**
    * Terminal method, called after a token has been filtered through the
    * existing macros and is suitable to be evaluated or passed out of the
    * preprocessor
    */
   private void passToken(Token token) throws ParseException
   {
      if (evaluating) {
         evaluationBuffer.append(token.toString());
      }
      else {
         user.token(token.toString());
      }
   }

   private PreprocessorUser user;
   private MacroHandler macroHandler;
   private MacroReader macroReader;
   private Set disabledMacros = new HashSet();
   private boolean translateMacroIdentifiers, evaluating;
   private StringBuffer evaluationBuffer = new StringBuffer();
}
