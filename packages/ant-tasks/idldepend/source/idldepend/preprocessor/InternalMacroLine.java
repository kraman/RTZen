/**
 * File: InternalMacroLine.java
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
 * Definition of the internal macro '__LINE__'.
 * It needs no parameters
 */
class InternalMacroLine extends Macro
{

   /**
    * The constructor requires the PreprocessorController, to be able to retrieve the
    * file being processed
    */
   public InternalMacroLine(PreprocessorController controller)
   {
      complex = false;
      numberOfParameters = 0;
      name = "__LINE__";
      this.controller = controller;
   }

   public List expand(List parameters) throws ParseException
   {
      Token line = new Token();
      line.kind = PreprocessorConstants.OTHER; // no really...
      line.image = String.valueOf(controller.getLine());
      List ret = new ArrayList();
      ret.add(line);
      return ret;
   }

   private PreprocessorController controller;
}

