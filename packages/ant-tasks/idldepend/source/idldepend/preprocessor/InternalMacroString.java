/**
 * File: InternalMacroString.java
 * Author: LuisM Pena
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.preprocessor;


import idldepend.javacc.generated.Token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Definition of the internal macro '#'.
 * It needs one parameter, which is translated into a string. If the parameter
 *   contains the characters '"' or '\', those are scaped.
 * Additionally, the parameters are not expanded into their content: #A,
 * when A is defined as B is not translated into #B
 */
class InternalMacroString extends Macro
{
   public InternalMacroString()
   {
      complex = true;
      numberOfParameters = 1;
      name = "#";
   }

   public String toString()
   {
      return "internal #";
   }

   public List expand(List parameters)
   {
      List ret = new ArrayList();
      ret.add(quoteToken);
      Iterator it = ((List) parameters.get(0)).iterator();
      while (it.hasNext()) {
         Token next = (Token) it.next();
         if (next.kind == STRING) {
            String str = next.toString();
            next.image = ("\\" + str.substring(0, str.length() - 1) + "\\\"");
            ret.add(next);
         }
         else {
            if (next.image.equals(quoteToken.image) || next.image.equals(slashToken)) {
               ret.add(slashToken);
            }
            ret.add(next);
         }
      }
      ret.add(quoteToken);
      return ret;
   }

   public boolean enableParameterTranslation(int parameter)
   {
      return false;
   }

   static protected Token quoteToken;
   static {
      quoteToken = new Token();
      quoteToken.kind = OTHER;
      quoteToken.image = "\"";
   }
}

