/**
 * File: PreprocessorController.java
 * Author: LuisM Pena
 * Last update: 0.33, 22nd September 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.preprocessor;


import idldepend.javacc.FileIncluder;
import idldepend.javacc.LocatedParseException;
import idldepend.javacc.PreprocessorInterface;
import idldepend.javacc.generated.ParseException;
import idldepend.javacc.generated.Preprocessor;
import idldepend.javacc.generated.PreprocessorConstants;
import idldepend.javacc.generated.Token;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * This class is the interface to the Preprocessor.
 * It receives any calls from the Preprocessor as it parses the input
 * file. Those calls are processed and filtered to the PreprocessorUser
 */
public class PreprocessorController
   implements PreprocessorConstants, PreprocessorInterface
{
   public PreprocessorController(PreprocessorUser user)
   {
      this.user = user;
      macroHandler = new MacroHandler(this);
      contentExpander = new ContentExpander(user, macroHandler);
      includesHandler = new IncludesHandler();
      ifLevel = new IfLevel();
      validLevel = ifLevel.isValid();
      sync = new Boolean(true);
   }

   /**
    * Adds the given path to the list of paths to find for include files.
    * The given path can be a list of paths, separated by the system-dependent
    * path separator.
    */
   public void addPath(String path)
   {
      includesHandler.addPath(path);
   }

   /**
    * Parses the input file.
    * @param addedInfo: added defintions (preinserted to the file)
    */
   public synchronized void parseInput(File file, String addedInfo)
      throws IOException, ParseException
   {
      preprocessor = new Preprocessor(file, addedInfo, this);
      try {
         while (preprocessor.parse()) {
            ;
         }
      }
      catch (ParseException pex) {
         throw new LocatedParseException(pex, getParsingFile(), getLine());
      }
   }

   public void stopParsing()
   {
      if (preprocessor != null) {
         preprocessor.stopParsing();   
      }
      stopped = true;
   }

   public int getLine()
   {
      return preprocessor.getReadLine();
   }

   public String getParsingFile()
   {
      return preprocessor.getReadFile();
   }

   public void asynchronousException(Exception exception)
   {
      user.asynchronousException(exception);
   }

   /**
    * Called by the preprocessor after a special situation has been found.
    * It is propagated to the PreprocessorUser
    */
   public void warning(String message)
   {
      user.warning(message);
   }

   /**
    * Called by the preprocessor when an #include tag has been found.
    * The specified file includes everything specified after the #include tag,
    * including any '<' or '>' characters
    * The prepreprocessor makes this question after posting a sentence on
    * its normal output to write weather it must include a file: the file is
    * only included if the current context is being processed (not discarded
    * because of conditional compilation). For this reason, it waits until
    * the sentence is found (in that moment, verifyFileInclusion is called)
    * to check if the context is valid or not
    */
   public File includeFile(String name, FileIncluder includer)
      throws ParseException
   {
      synchronized (sync) {  
         while (!preprocessorIssuedIncludeQuestion && !stopped) {
            try {
               sync.wait();
            }
            catch (InterruptedException ie) {}
         }
      }
      preprocessorIssuedIncludeQuestion = false;
      File ret = null;
      if (validLevel && !stopped) {
         ret = includesHandler.findFile(name, includer);
         if (ret == null) {
            throw new ParseException("Included file " + name + " not found");
         }
      }
      return ret;
   }

   /**
    * Called by the preprocessor when detects an #include statement
    * In this moment, the Prepreprocessor is waiting to know if its must
    * proceed to read the file or it is in a conditional block does must
    * be discarded.
    */
   public void verifyFileInclusion() throws ParseException
   {
      synchronized (sync) {
         preprocessorIssuedIncludeQuestion = true;
         sync.notifyAll();
      }
      Thread.yield();
   }

   /**
    * Called by the preprocessor when a new file is being included
    */
   public void includingFile(String file) throws ParseException
   {
      user.includingFile(file);
   }

   /**
    * Called by the preprocessor after a new file has been included
    */
   public void fileIncluded() throws ParseException
   {
      user.fileIncluded();
   }

   /**
    * Called by the preprocessor when an #error tag has been found. The tokens
    * given after the error message are already passed through 'readToken'
    */
   public void errorDirective() throws ParseException
   {
      if (validLevel) {
         String message = storedTokens.toString();
         throw new ParseException("Error directive: " + message);
      }
   }

   /**
    * Called by the preprocessor when a # unknown tag has been found. The tokens
    * given after the error message are already passed through 'readToken'
    */
   public void unknownDirective() throws ParseException
   {
      if (validLevel) {
         user.unknownDirective(storedTokens.toString());
      }
   }

   /**
    * Called by the preprocessor when a #define macro with arguments is found.
    * In this call, they're only specified the macro name and the list with
    * the parameter names, which can be empty. This call is followed by
    * several calls to readToken and then one to macroDefined
    */
   public void defineMacro(String macroName, List macroParameters)
   {
      if (validLevel) {
         macro = new Macro(macroName,
               macroParameters == null ? new ArrayList() : macroParameters);
         tokenStatus = MACRO_TOKEN;
      }
   }

   /**
    * Called by the preprocessor when a #define macro without arguments is found.
    * In this call, it's only specified the macro name.This call is followed by
    * several calls to readToken and then one to macroDefined
    */
   public void defineMacro(String macroName)
   {
      if (validLevel) {
         macro = new Macro(macroName, null);
         tokenStatus = MACRO_TOKEN;
      }
   }

   /**
    * Called by the preprocessor when a #define macro has been completely read.
    * It follows a call to one of the two definedMacro methods and (probably)
    * several calls to readToken
    */
   public void macroDefined() throws ParseException
   {
      if (validLevel) {
         macro.completed();
         Macro previous = macroHandler.addMacro(macro);
         if (previous != null) {
            if (previous.isInternal()) {
               warning("Internal macro cannot be redefined: " + macro.getName());
               macroHandler.addMacro(previous);
            }
            else {
               warning("Macro redefined: " + macro.getName());
            }
         }
         macro = null;
         tokenStatus = TRANSFORM_TOKEN;
      }
   }

   /**
    * Called by the preprocessor when a #undef directive is parsed
    */
   public void undefineMacro(String macroName)
   {
      if (validLevel) {
         Macro previous = macroHandler.removeMacro(macroName);
         if (previous != null && previous.isInternal()) {
            user.warning("Internal macro cannot be undefined: " + macro.getName());
            macroHandler.addMacro(previous);
         }
      }
   }

   /**
    * Called by the preprocessor when a list of tokens is going to be read,
    * which must be stored for some purpose (for example, it is called on
    * an include directive to store the file name, or in the macro definition).
    * When the read ends, it is called again with the parameter set to false,
    * which means that the following tokens can be parsed in the 'normal' way
    * (transformed in the ContentExpander)
    */
   public void storeTokens(boolean start)
   {
      if (validLevel) {
         if (start) {
            tokenStatus = STORE_TOKEN;
            storedTokens.setLength(0);
         }
         else {
            tokenStatus = TRANSFORM_TOKEN;
         }
      }
   }

   /**
    * Called by the preprocessor when a token is read
    */
   public void readToken(Token token) throws ParseException
   {
      switch (tokenStatus) {
         case DISCARD_TOKEN:
            break;
         case MACRO_TOKEN:
            macro.add(token);
            break;
         case TRANSFORM_TOKEN:
            contentExpander.expandToken(token);
            break;
         case STORE_TOKEN:
            storedTokens.append(token.toString());
            break;
      }
   }

   /**
    * Called by the preprocessor when an #if or #elif directive is found. The
    * tokens coming after it must be evaluated, until a call to
    * completeEvaluation is received.
    * @param sameLevel is set to true for #elif directives, and false for #if
    */
   public void startEvaluation(boolean sameLevel)
   {
      if (validLevel || (sameLevel && ifLevel.evaluateElif())) {
         contentExpander.startEvaluation();
         tokenStatus = TRANSFORM_TOKEN;
      }
   }

   /**
    * Called by the preprocessor when an #else or #endif directive is found.
    * @param sameLevel is set to true for #endif directives, and false for #else
    */
   public void alternateEvaluationLevel(boolean closeLevel)throws ParseException
   {
      validLevel = closeLevel ? ifLevel.addEndif() : ifLevel.addElse();
      tokenStatus = validLevel ? TRANSFORM_TOKEN : DISCARD_TOKEN;
   }

   /**
    * Called by the preprocessor when an #if or #elif directive has been
    * completed.
    * @param sameLevel is set to true for #endif directives, and false for #else
    */
   public void completeEvaluation(boolean sameLevel) throws ParseException
   {
      boolean evaluation;
      if (validLevel || (sameLevel && ifLevel.evaluateElif())) {
         try {
            evaluation = contentExpander.completeEvaluation();
         }
         catch (ParseException pex) {
            throw new ParseException("Invalid conditional clause");
         }
      }
      else {
         evaluation = !validLevel;
      }
      validLevel = sameLevel ? ifLevel.addElif(evaluation) : ifLevel.addIf(evaluation);
      tokenStatus = validLevel ? TRANSFORM_TOKEN : DISCARD_TOKEN;
   }

   /**
    * Called by the preprocessor when an #ifdef or #ifndef directive has been
    * found
    * @param id the name of the macro to be checked
    * @param defined is set to true fo #ifdef, and not for #ifndef
    */
   public void simpleEvaluation(String id, boolean defined)
   {
      validLevel = ifLevel.addIf(macroHandler.isMacro(id) == defined);
      if (!validLevel) {
         tokenStatus = DISCARD_TOKEN;
      }
   }

   private Preprocessor preprocessor;
   private PreprocessorUser user;
   private IfLevel ifLevel;
   private IncludesHandler includesHandler;
   private boolean validLevel;
   private ContentExpander contentExpander;
   private MacroHandler macroHandler;
   private Macro macro;
   private StringBuffer storedTokens = new StringBuffer();
   private int tokenStatus = TRANSFORM_TOKEN;
   private boolean preprocessorIssuedIncludeQuestion = false, stopped = false;
   private Boolean sync;
   private final static int DISCARD_TOKEN = 0x8881;
   private final static int MACRO_TOKEN = 0x8882;
   private final static int TRANSFORM_TOKEN = 0x8883;
   private final static int STORE_TOKEN = 0x8884;
}

