/**
 * File: PreprocessorUser.java
 * Author: LuisM Pena
 * Last update: 0.33, 22nd September 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.preprocessor;


import idldepend.javacc.generated.ParseException;



/**
 * This interfaces defines the external operations which are called by
 * the preprocessor while parsing the input
 */
public interface PreprocessorUser
{

   /**
    * Called when a new file is being included
    */
   public void includingFile(String file) throws ParseException;

   /**
    * Called after a new file has been included
    */
   public void fileIncluded() throws ParseException;

   /**
    * Called when a #directive (unknown) tag has been found.
    */
   public void unknownDirective(String content) throws ParseException;

   /**
    * Asynchronous exception found while preprocessing the file
    */
   public void asynchronousException(Exception exception);

   /**
    * A special situation has been found.
    */
   public void warning(String message);

   /**
    * token read out of the parsing file, filtered through the macros
    */
   public void token(String t);

}
