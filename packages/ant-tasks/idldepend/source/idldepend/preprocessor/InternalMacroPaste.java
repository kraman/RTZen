/**
 * File: InternalMacroPaste.java
 * Author: LuisM Pena
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.preprocessor;


import idldepend.javacc.generated.Token;

import java.util.List;


/**
 * Definition of the internal macro '##'.
 * It needs two parameter, which are pasted together
 */
class InternalMacroPaste extends Macro
{
   public InternalMacroPaste()
   {
      complex = true;
      numberOfParameters = 2;
      name = "##";
   }

   public String toString()
   {
      return "internal ##";
   }

   public List expand(List parameters)
   {
      List ret = (List) parameters.get(0);
      List second = (List) (parameters.get(1));
      int size = ret.size();
      Token change = (Token) (ret.remove(size - 1));
      Token newToken = Token.newToken(ID);
      newToken.image = new String(change.image + second.get(0).toString());
      ret.add(newToken);
      if (second.size() > 1) {
         parameters.addAll(1, second);
      }
      return ret;
   }

   public boolean enableParameterTranslation(int parameter)
   {
      return parameter == 0; // only the first parameter is translated (if needed)
   }

}
