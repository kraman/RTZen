/**
 * File: InternalMacroDefined.java
 * Author: LuisM Pena
 * Last update: 0.33, 22nd September 2003
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
 * Definition of the internal macro 'defined'.
 * It needs one parameter, and can only be used on evaluation sentences,
 *  (if or elif)
 * Additionally, the parameters are not expanded into their content: defined(A),
 * when A is defined as B is not translated into defined(B)
 */
class InternalMacroDefined extends Macro
{
   public InternalMacroDefined(MacroHandler macroHandler)
   {
      complex = true;
      numberOfParameters = 1;
      name = "defined";
      this.macroHandler = macroHandler;
   }

   public String toString()
   {
      return "internal defined";
   }

   public List expand(List parameters) throws ParseException
   {
      List def = (List) parameters.get(0);
      if (def.size() != 1) {
         throw new ParseException
               ("Incorrect use of defined(): only a macro name must be supplied");
      }
      Token token = (Token) def.get(0);
      if (token.kind != PreprocessorConstants.ID) {
         throw new ParseException("Incorrect use of defined(): a macro identifier is expected");
      }
      List ret = new ArrayList();
      ret.add(macroHandler.isMacro(token.toString()) ? trueToken : falseToken);
      return ret;
   }

   public boolean mandatoryParenthesis()
   {
      return false;
   }

   public boolean isOnlyForEvaluation()
   {
      return true;
   }

   public boolean enableParameterTranslation(int parameter)
   {
      return false;
   }

   private MacroHandler macroHandler;

   static protected Token trueToken, falseToken;
   static {
      trueToken = new Token();
      trueToken.kind = OTHER;
      trueToken.image = "1";
      falseToken = new Token();
      falseToken.kind = OTHER;
      falseToken.image = "0";
   }
}

