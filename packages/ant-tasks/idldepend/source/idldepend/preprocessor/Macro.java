/**
 * File: Macro.java
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class defines the content of a macro
 * It is implemented as a list containing Strings and Integers
 * Each integer is eventually substituted by the corresponding parameter
 * (i.e, Integer(1) means the first parameter
 */
class Macro implements PreprocessorConstants
{

   protected Macro()
   { // only for subclasses (should be)
      internal = true;
   }

   /**
    * Builds a macro content that must be feed with tokens using
    * the methods 'add', until the method 'completed' is invoked
    * @param name The name of the macro
    * @param parameterNames A list with the tokens with the parameters. A null
    *     list means a simple macro. A list empty means a complex macro
    *     without parameters (to be called as a function)
    */
   public Macro(String name, List parameterNames)
   {
      this.name = name;
      internal = false;
      content = new ArrayList();
      complex = parameterNames != null;
      if (complex) {
         numberOfParameters = parameterNames.size();
         if (numberOfParameters > 0) {
            parameters = new HashMap(numberOfParameters);
            int n = 0;
            Iterator it = parameterNames.iterator();
            while (it.hasNext()) {
               parameters.put(((Token) it.next()).toString(), new Integer(n++));
            }
         }
      }
   }

   /**
    * Returns true if the macro has been defined as internal
    */
   public boolean isInternal()
   {
      return internal;
   }

   /**
    * Returns true if the macro can only be used on evaluation statements
    * (if, elif)
    */
   public boolean isOnlyForEvaluation()
   {
      return false;
   }

   /**
    * Returns true if the internal parameter N must be checked against macro
    * definitions. It is only used for complex macros
    * @param parameter, the number of the parameter (starts in zero)
    */
   public boolean enableParameterTranslation(int parameter)
   {
      return true;
   }

   /**
    * Adds a new token.
    */
   public void add(Token token) throws ParseException
   {
      if (token.kind == PreprocessorConstants.SPECIAL) {
         addSpecialToken(token);
      }
      else if ((token.kind != PreprocessorConstants.SPACE)
            || (noSpacesRead && !endingInternalMacro)) {
         noSpacesRead = true;
         Integer parameter = (parameters == null)
               ? null
               : (Integer) parameters.get(token.toString());
         if (parameter == null) {
            content.add(token);
         }
         else {
            content.add(parameter);
         }
         if (endingInternalMacro) {
            content.add(rightParToken);
            endingInternalMacro = false;
         }
         if (token.kind != PreprocessorConstants.SPACE) {
            lastNotSpaceRead = content.size();
         }
      }
   }

   /**
    * Throws a ParseException stating that the macro is wrongly defined
    */
   private void wrongMacro() throws ParseException
   {
      throw new ParseException("Incorrect macro definition: " + name);
   }

   /**
    * Adds a new special token (##, #). They must be converted into
    * pre-form A##B ==> ##(A,B)
    */
   private void addSpecialToken(Token token) throws ParseException
   {
      if (endingInternalMacro) {
         wrongMacro();
      }
      else {
         endingInternalMacro = true;
         if (token.toString().equals(pasteToken.image)) {
            if (content.isEmpty()) {
               wrongMacro();
            }
            int open = 0;
            for (int position = content.size() - 1; position >= 0; --position) {
               String str = content.get(position).toString();
               if (str.equals(rightParToken.image)) {
                  open += 1;
               }  
               else if (str.equals(leftParToken.image)) {
                  open -= 1;
               }
               else if (open == 0) {
                  content.add(position, leftParToken);
                  content.add(position, pasteToken);
                  content.add(commaToken);
                  return;
               }
            }
            wrongMacro();
         }
         else if (token.toString().equals(toStringToken.image)) {
            content.add(toStringToken);
            content.add(leftParToken);
         }
      }
   }

   /**
    * Called when the macro has been completely defined
    */
   public void completed() throws ParseException
   {
      int size = content.size();
      for (int i = lastNotSpaceRead; i < size; i++) {
         content.remove(lastNotSpaceRead); // remove the last spaces
      }
      if (parameters != null) {
         parameters.clear();
         parameters = null;
      }
      if (endingInternalMacro) {
         wrongMacro();
      }
   }

   /**
    * Returns the macro name
    */
   public String getName()
   {
      return name;
   }

   /**
    * Returns true if it's a complex macro (with parenthesis)
    */
   public boolean isComplex()
   {
      return complex;
   }

   /**
    * Returns true if it allows to be used without parentheses,
    * in case that only one parameter is required
    */
   public boolean mandatoryParenthesis()
   {
      return true;
   }

   /**
    * Returns the number of parameters of the macro
    */
   public int getNumberOfParameters()
   {
      return numberOfParameters;
   }

   /**
    * Returns an stringfied from of the macro
    */
   public String toString()
   {
      StringBuffer ret = new StringBuffer(name);
      if (complex) {
         ret.append("():");
         Iterator it = content.iterator();
         while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof Token) {
               ret.append(((Token) next).toString());
            }
            else {
               ret.append("$" + (Integer) next);
            }
         }
      }
      return ret.toString();
   }

   /**
    * Expands the token with the list of parameters. The result is as well a
    * list of tokens
    * @param parameters a list with the definitions given for each of the
    *   arguments of the macro. It must contain as many elements as number
    *   of arguments has the macro. Each of the elements must be a list of
    *   tokens.
    */
   public List expand(List parameters) throws ParseException
   {
      List ret = new ArrayList();
      Iterator it = content.iterator();
      while (it.hasNext()) {
         Object next = it.next();
         if (next instanceof Token) {
            ret.add(next);
         }
         else {
            List parameter = (List) parameters.get(((Integer) next).intValue());
            ret.addAll(parameter);
         }
      }
      return ret;
   }

   static protected Token pasteToken, toStringToken, leftParToken,
         rightParToken, commaToken, slashToken;
   static {
      pasteToken = new Token();
      pasteToken.kind = ID;
      pasteToken.image = "##";
      toStringToken = new Token();
      toStringToken.kind = ID;
      toStringToken.image = "#";
      leftParToken = new Token();
      leftParToken.kind = LEFTPAR;
      leftParToken.image = "(";
      rightParToken = new Token();
      rightParToken.kind = RIGHTPAR;
      rightParToken.image = ")";
      commaToken = new Token();
      commaToken.kind = COMMA;
      commaToken.image = ",";
      slashToken = new Token();
      slashToken.kind = OTHER;
      slashToken.image = "\\";
   }

   private boolean noSpacesRead = false;
   private int lastNotSpaceRead = 0;
   private boolean internal;
   private boolean endingInternalMacro = false;
   protected boolean complex;
   protected int numberOfParameters;
   private List content;// list of tokens or integers
   protected String name;
   private Map parameters;
}
