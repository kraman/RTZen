/**
 * File: PreprocessorInterface.java
 * Author: LuisM Pena
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.javacc;


import idldepend.javacc.generated.ParseException;
import idldepend.javacc.generated.Token;

import java.io.File;
import java.util.List;


/**
 * Interface used by the Preprocessor
 */
public interface PreprocessorInterface
{

   /**
    * Called by the preprocessor if an asynchronous exception parsing the
    * file is found
    */
   public void asynchronousException(Exception exception);

   /**
    * Called by the preprocessor after a special situation has been found.
    */
   public void warning(String message) throws ParseException;

   /**
    * Called by the preprocessor when an #include tag has been found.
    * The specified file includes everything specified after the #include tag,
    * including any '<' or '>' characters
    * @param file The string containing the file to include. It includes
    *     any character found after the #include chars (without the spaces
    *     on both sides)
    * @param includer The FileIncluder requesting this file
    */
   public File includeFile(String file, FileIncluder includer)
      throws ParseException;
    
   /**
    * Called by the preprocessor when there is a file that could
    * be included, depending on the current state (is included unless
    * there is a conditional block resolved to be NOT executed).
    */
   public void verifyFileInclusion() throws ParseException;

   /**
    * Called by the preprocessor when a new file is being included
    */
   public void includingFile(String file) throws ParseException;

   /**
    * Called by the preprocessor after a new file has been included
    */
   public void fileIncluded() throws ParseException;

   /**
    * Called by the preprocessor when an #error tag has been found. The tokens
    * given after the error message are already passed through 'readToken'
    */
   public void errorDirective() throws ParseException;

   /**
    * Called by the preprocessor when a # unkown tag has been found. The tokens
    * given after the error message are already passed through 'readToken'
    */
   public void unknownDirective() throws ParseException;

   /**
    * Called by the preprocessor when a #define macro with arguments is found.
    * In this call, they're only specified the macro name and the list with
    * the parameter names, which can be empty. This call is followed by
    * several calls to readToken and then one to macroDefined
    */
   public void defineMacro(String macroName, List macroParameters)
      throws ParseException;

   /**
    * Called by the preprocessor when a #define macro without arguments is found.
    * In this call, it's only specified the macro name.This call is followed by
    * several calls to readToken and then one to macroDefined
    */
   public void defineMacro(String macroName) throws ParseException;

   /**
    * Called by the preprocessor when a #define macro has been completely read.
    * It follows a call to one of the two definedMacro methods and (probably)
    * several calls to readToken
    */
   public void macroDefined() throws ParseException;

   /**
    * Called by the preprocessor when a #undef directive is parsed
    */
   public void undefineMacro(String macroName) throws ParseException;

   /**
    * Called by the preprocessor when a list of tokens is going to be read,
    * which must be stored for some purpose (for example, it is called on
    * an include directive to store the file name, or in the macro definition).
    * When the read ends, it is called again with the parameter set to false,
    * which means that the following tokens can be parsed in the 'normal' way
    * (transformed in the ContentExpander)
    */
   public void storeTokens(boolean start) throws ParseException;

   /**
    * Called by the preprocessor when a token is read
    */
   public void readToken(Token token) throws ParseException;

   /**
    * Called by the preprocessor when an #if or #elif directive is found. The
    * tokens coming after it must be evaluated, until a call to
    * completeEvaluation is received.
    * @param sameLevel is set to true for #elif directives, and false for #if
    */
   public void startEvaluation(boolean sameLevel) throws ParseException;

   /**
    * Called by the preprocessor when an #else or #endif directive is found.
    * @param sameLevel is set to true for #endif directives, and false for #else
    */
   public void alternateEvaluationLevel(boolean closeLevel)throws ParseException;

   /**
    * Called by the preprocessor when an #if or #elif directive has been
    * completed.
    * @param sameLevel is set to true for #endif directives, and false for #else
    */
   public void completeEvaluation(boolean sameLevel) throws ParseException;

   /**
    * Called by the preprocessor when an #ifdef or #ifndef directive has been
    * found
    * @param id the name of the macro to be checked
    * @param defined is set to true fo #ifdef, and not for #ifndef
    */
   public void simpleEvaluation(String id, boolean defined)
      throws ParseException;
}

