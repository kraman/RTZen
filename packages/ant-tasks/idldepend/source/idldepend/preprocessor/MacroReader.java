/**
 * File: MacroReader.java
 * Author: LuisM Pena
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.preprocessor;


import idldepend.javacc.generated.ParseException;
import idldepend.javacc.generated.PreprocessorConstants;
import idldepend.javacc.generated.Token;

import java.util.ArrayList;
import java.util.List;


/**
 * A MacroReader is associated to a given macro: it receives tokens until
 * the macro can be expanded.
 */
class MacroReader implements PreprocessorConstants
{

   /**
    * Stack to read a macro.
    */
   public MacroReader(Macro macro)
   {
      this.macro = macro;
      this.parameters = macro.getNumberOfParameters();
      if (parameters > 0) {
         parameterList = new ArrayList();
         currentParameter = new ArrayList();
      }
      openParenthesis = 0;
   }

   /**
    * Returns the name of the contained macro
    */
   public String getMacroName()
   {
      return macro.getName();
   }

   /**
    * Expands the contained macro with the parameters that have been read.
    * It should be called only when the macro has been completed
    */
   public List expandMacro() throws ParseException
   {
      return macro.expand(parameterList);
   }
  
   /**
    * Returns true if the next token must be translated, or false if the
    * current macro has disabled the macro expansion
    **/
   public boolean translateIdentifiers()
   {
      // it is possible to know the current parameter with the size of parameterList
      return macro.enableParameterTranslation(parameterList.size());
   }

   /**
    * Reads the token into the current macro.
    * Returns true if the token completes the macro
    */
   public boolean readToken(Token token)  throws ParseException
   {
      switch (token.kind) {
         case LEFTPAR:
            readLeftPar(token);
            break;
         case RIGHTPAR:
            readRightPar(token);
            break;
         case COMMA:
            readComma(token);
            break;
         case SPACE:
            if (lastNonSpaceRead > 0) {
               currentParameter.add(token);
            }
            break;
         default:
            readNormalToken(token);
            break;
      }
      return ended;
   }

   /**
    * Reads a left parenthesis
    */
   private void readLeftPar(Token token) throws ParseException
   {
      if (++openParenthesis > 1) {
         if (parameters == 0) {
            wrongMacroExpansion(true);
         }
         addToCurrentParameter(token);
      }
      else {
         lastNonSpaceRead = 0;
      }
   }

   /**
    * Reads a right parenthesis
    */
   private void readRightPar(Token token) throws ParseException
   {
      if (openParenthesis == 0) {
         wrongMacroExpansion(false);
      }
      else if (--openParenthesis > 0) {
         addToCurrentParameter(token);
      }
      else {
         if (parameters == 0) {
            ended = true;
         }
         else if ((parameterList.size() + 1) != parameters) {
            wrongMacroExpansion(false);
         }
         else {
            rtrimCurrentParameter();
            if (currentParameter.isEmpty()) {
               wrongMacroExpansion(false);
            }
            else {
               parameterList.add(currentParameter);
               currentParameter = null;
               ended = true;
            }
         }
      }
   }

   /**
    * Reads a comma
    */
   private void readComma(Token token) throws ParseException
   {
      if (openParenthesis == 0) {
         wrongMacroExpansion(false);
      }
      else if (parameters == 0) {
         wrongMacroExpansion(true);
      }
      else if (openParenthesis > 1) {
         addToCurrentParameter(token);
      }
      else {
         if (parameterList.size() >= (parameters - 1)) {
            wrongMacroExpansion(true);
         }
         else {
            rtrimCurrentParameter();
            if (currentParameter.isEmpty()) {
               wrongMacroExpansion(false);
            }
            else {
               parameterList.add(currentParameter);
               currentParameter = new ArrayList();
            }
         }
      }
   }

   /**
    * Reads a normal token (not being '(',')' or','
    */
   private void readNormalToken(Token token) throws ParseException
   {
      if (openParenthesis == 0) {
         if (macro.mandatoryParenthesis()){
            wrongMacroExpansion(false);
         }
         addToCurrentParameter(token);
         parameterList.add(currentParameter);
         currentParameter = null;
         ended = true;
      }
      else if (parameters == 0) {
         wrongMacroExpansion(true);
      }
      else {
         addToCurrentParameter(token);
      }
   }

   /**
    * Adds a non space token to the current parameter
    */
   private void addToCurrentParameter(Token token)
   {
      currentParameter.add(token);
      lastNonSpaceRead = currentParameter.size();
   }

   /**
    * Removes the space characters from the end
    */
   private void rtrimCurrentParameter()
   {
      int size = currentParameter.size();
      for (int i = lastNonSpaceRead; i < size; i++) {
         currentParameter.remove(lastNonSpaceRead);
      }
      lastNonSpaceRead = 0;
   }

   /**
    * Throws a parse exception, which can be because there are too many
    *   parameters, or no parameters enough
    */
   private void wrongMacroExpansion(boolean tooManyParameters)
      throws ParseException
   {
      throw new ParseException("Error expanding macro " + macro.getName() + ": "
            + (tooManyParameters ? "too many" : "insufficient") + " parameters");
   }

   private int lastNonSpaceRead = 0;
   private Macro macro;
   private List currentParameter;
   private List parameterList;
   private int parameters, openParenthesis;
   private boolean ended;
}

