/**
 * File: LocatedParseException.java
 * Author: LuisM Pena
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.javacc;


import idldepend.javacc.generated.ParseException;


/**
 * Extends the ParseException class to deal with location exceptions.
 * As the file being processed by the parsers have no relationship
 * (probably) with the input, the line numbers shown would have
 * otherwise not special meaning
 */
public class LocatedParseException extends ParseException
{
   public LocatedParseException(LocatedParseException lpex, String file, int lineNumber)
   {
      super(lpex.getMessage());
   }

   public LocatedParseException(ParseException pex, String file, int lineNumber)
   {
      super(buildMessage(pex, file, lineNumber));
   }

   public LocatedParseException(String message, String file, int lineNumber)
   {
      super(buildMessage(message, file, lineNumber));
   }

   static private String buildMessage(ParseException pex, String file, int lineNumber)
   {
      String message;
      if (pex.currentToken == null) {
         message = pex.getMessage();
      }
      else {
         message = "Unexpected token: " + pex.currentToken.next.toString();
      }
      return buildMessage(message, file, lineNumber);
   }

   static private String buildMessage(String message, String file, int lineNumber)
   {
      if (file == null) {
         return message + "\n(on macro predefinitions)";
      }
      else {
         return message + "\nFile: " + file + ", line: " + lineNumber;
      }
   }
}
