/**
 * File: Preprocessor.java
 * Author: LuisM Pena
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.javacc;


import idldepend.javacc.generated.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Reader;


/**
 * Main preprocessor task. It accomplishes the following tasks:
 * -Joins lines ending with '\'
 * -Removes out comments, being replaces by spaces.
 * -Separates preprocessor lines (beginning with #) from the others
 *
 * The output of this class is redirected to a Reader object, making
 * therefore possible for a class to 'read' from its output:
 * -Through this output, is redirected the file's content, already filtered.
 * -The preprocessor lines are converted into //line
 *   For example: /*comment* /  #  define /*comment* / macro
 *   is converted into //define macro
 *
 * Why this is not done directly with javacc?
 * -First, the grammar is not very obvious. For example, a line ending with '\'
 *    must be merged with the following one. To skip "\\" <EOL> is not enough,
 *    as then it is considered a separate token (which is skipped). If that
 *    breaks a directive #include, for example, like in "#inc\
 *    lude <io>" it must be still understood like "#include <io>".
 *    Probably this can be easily done, I have not that expertise.
 *
 * -Second: there is on every moment two different grammars: the grammar of
 *    the lines starting with '#' and the others. In the preprocessor lines,
 *    #include doesn't have to be always considered as a token 'include'
 *    For example, I can define #define A(include) #include
 *
 `* -Third: comments. If a file includes another one that finishes with an
 *    unterminated comment, that comment is supposed to apply to the next
 *    file. Again, this cannot be easily performed with the grammar. For
 *    example, a comment is considered to start with /* and finish with * /
 *    The grammar can have an SKIP sentence for this case, but what happens
 *    if the files does not terminate the comment?: the comments is not
 *    even skipped on the current file.
 *
 * Overall, I found that it was perfectly possible to deal with most of
 * the cases, but adding lot of code to the javacc grammar. Because I had
 * already decided to have a 'pre-preprocessor' to deal with the '\' character
 * at then end of the lines, why not to make this pre-preprocessor a little
 * bit more inteligent?
 *
 * FInally the conversion from #define to //define (and the same for the
 * other directives) means that the grammar can now define tokens like
 * '//include' without fears of finding that token somewhere else. This is
 * not really needed, if it can be assumed that '#' will be only valid on
 * a directive line: this assumption is not taken here.
 *
 * The inclusion of new files is also handled directly by the prepreprocessor
 * If it finds an #include statement, it requests to receive the specified
 *  File. It automatically insert a #line statement, when the new file is
 *  included and at the end of the inclusion. This #line statement has the
 *  syntax ////line number file
 * Note that it uses four initial slashes to differenciate it from other
 *  #line directives that can be existing on the initial file
 */
public class Prepreprocessor extends Thread implements FileIncluder
{

   /**
    * The controller is required to ask for include files.
    * It is possible to specify predefinition through the given string
    * on the parameter addInfo
    */
   public Prepreprocessor(File file, PreprocessorInterface controller, String addInfo)
      throws ParseException, IOException
   {
      this(file, controller, (Prepreprocessor) null);
      this.addInfo = addInfo;
      start();
      yield();
   }

   /**
    * Creates a child prepreprocessor, meant to be used with included files.
    * It parses its own file and, when it finishes, if it's still on a comment,
    * puts the parent prepreprocessor 'on comment'
    */
   private Prepreprocessor(File file, PreprocessorInterface controller,
         Prepreprocessor parent) throws ParseException, IOException
   {
      this.file = file.getCanonicalFile();
      this.controller = controller;
      this.parent = parent;
      fileReader = new FileReader(file);
      input = new LineNumberReader(fileReader);
      if (parent == null) {
         output = new PipedWriter();
         writer = new PrintWriter(output);
         exported = new PipedReader(output);
      }
      else {
         writer = parent.writer;
         exported = parent.exported;
      }
      read = new StringBuffer();
      if (parent != null) {
         startProcess();
         writer = null;
         output = null;
      }
   }

   /**
    * Stops any preprocessing
    */
   public void stopTask()
   {
      onGoing = false;
      if (child != null) {
         child.stopTask();
      }
   }

   /**
    * As the preprocessing is done on a separate thread, this method
    * must be called to verify any errors I/O or during the parsing
    */
   public Reader getFilteredInput()
   {
      return exported;
   }

   /**
    * FileIncluder method
    */
   public File getFile()
   {
      return file;
   }

   /**
    * FileIncluder method
    */
   public FileIncluder getParent()
   {
      return parent;
   }

   public void run()
   {
      IOException ioEx = null;
      ParseException parseEx = null;
      if ((Thread.currentThread() == this) && (parent == null)) {
         setPriority(MAX_PRIORITY);
         try {
            startProcess();
         }
         catch (ParseException pex) {
            parseEx = pex;
         }
         catch (IOException iex) {
            ioEx = iex;
         }
         finally {
            try {
               output.close();
            }
            catch (Exception ex) {}
            try {
               writer.close();
            }
            catch (Exception ex) {}
         }
      }
      if (ioEx != null) {
         controller.asynchronousException(ioEx);
      }
      else if (parseEx != null) {
         controller.asynchronousException(parseEx);
      }
   }

   private void startProcess() throws IOException, ParseException
   {
      try {
         if (addInfo != null) {
            processAddInfo();
         }
         process();
         if ((parent != null) && onComment) {
            parent.onComment = true;
         }
      }
      finally {
         try {
            input.close();
         }
         catch (Exception ex) {}
         try {
            fileReader.close();
         }
         catch (Exception ex) {}
         fileReader = null;
         input = null;
         read = null;
         parent = null;
      }
   }

   private void processAddInfo() throws IOException, ParseException
   {
      LineNumberReader realInput = input;
      try {
         input = new LineNumberReader(new StringReader(addInfo));
         process();
      }
      finally {
         try {
            input.close();
         }
         catch (Exception ex) {}
         input = realInput;
         addInfo = null;
      }
   }

   private void writeLineInfo() throws IOException
   {
      if (addInfo == null) {
         writer.println("////line " + String.valueOf(input.getLineNumber()) + " "
               + file.toString());
      }
   }

   private void process() throws IOException, ParseException
   {
      int pendingReturns = 0;
      writeLineInfo();
      String line = input.readLine();
      while (onGoing && (line != null)) {
         int length = line.length();
         if (length > 0 && line.charAt(length - 1) == '\\') {
            pendingReturns++;
            len += --length;
            read.append(line.substring(0, length));
         }
         else {
            read.append(line);
            len += length;
            if (parseLine()) {
               try {
                  // Bug: it is not possible to include the file now: it could be inside a
                  // conditional block that should be discarded!
                  // It is not possible to ask to the controller: it is needed to wait
                  // until all the information is read (we're sending the info to a stream,
                  // we do not know at which speed is being read) and then ask for it.
                  // To do it, a line is added to the stream //includeFile? and we block
                  // until an answer is received.
                  writer.println("//canIncludeFile");
                  File toInclude = controller.includeFile(read.substring(9).trim(), this);
                  if (toInclude != null) {
                     String info = toInclude.getCanonicalPath();
                     child = new Prepreprocessor(toInclude, controller, this);
                     writeLineInfo();
                  }
               }
               catch (ParseException pex) {
                  throw new LocatedParseException(pex,
                        (addInfo == null ? file.toString() : null), input.getLineNumber());
               }
            }
            else {
               writer.println(read.toString());
            }
            read.setLength(len = 0);
            for (; pendingReturns > 0; --pendingReturns) {
               writer.println();
            }
         }
         line = input.readLine();
      }
   }

   /**
    * Removes comments out of the StringBuffer Read, and verifies wether
    * the line is a preprocessor line (<SPACE>* "#").
    * If the line is a preprocessor line,
    * the 'read' stringbuffer is stripped on its beginning until the first
    * non space character after the '#', and the line is started with '//'
    * Len must contain the length of the buffer
    * @return true if the parsed line is an #included line
    */
   private boolean parseLine() throws IOException, ParseException
   {
      boolean perhapsPreprocessorLine = true, preprocessorLine = false;
      boolean ret = false;
      int start = len;
      pos = 0;
      if (onComment) {
         passComment();
      }
      while (pos < len) {
         char c = read.charAt(pos);
         if (c == '\\') {
            perhapsPreprocessorLine = false;
            ++pos; // do not read next character
         }
         else if (c == '\'') {
            perhapsPreprocessorLine = false;
            passCharacter('\'');
         }
         else if (c == '\"') {
            perhapsPreprocessorLine = false;
            passCharacter('\"');
         }
         else if ((c == '/') && ((pos + 1) < len)) {
            c = read.charAt(pos + 1);
            if (c == '/') {
               // remove the end of the line and finish
               read.setLength(pos);
               break;
            }
            else if (c == '*') {
               read.setCharAt(pos++, ' ');
               read.setCharAt(pos++, ' ');
               passComment();
            }
            else {
               perhapsPreprocessorLine = false;
            }
         }
         else if (perhapsPreprocessorLine) {
            if (!Character.isWhitespace(c)) {
               perhapsPreprocessorLine = false;
               preprocessorLine = c == '#';
            }
         }
         else if (preprocessorLine && (start == len)) {
            if (!Character.isWhitespace(c)) {
               start = pos;
            }
         }
         ++pos;
      }
      if (preprocessorLine) {
         ret = read.substring(start).startsWith("include");
         read.replace(0, start, "//");
      }
      return ret;
   }

   /**
    * Looks into the 'read' StringBuffer, from the position pos+1, looking
    * for the specified character. It lets the variable pos over the
    * character found.
    * @throws ParseException if the character is not found
    */
   private void passCharacter(char pass) throws IOException, ParseException
   {
      while (++pos < len) {
         char c = read.charAt(pos);
         if (c == '\\') {
            ++pos; // do not read next character
         }
         else if (c == pass) {
            return;
         }
      }
      throw new LocatedParseException("Line does no complete is character/string" + " literal",
            (addInfo == null ? file.toString() : null), input.getLineNumber());
   }

   /**
    * Looks into the 'read' StringBuffer, from the position pos+1, looking
    * for "*" "/". It lets the variable pos over the last "/", overwritting
    * the comment with character ' ' (it does not overwrite space characters)
    * If the ending comment characters are not found, the variable 'onComment'
    * is set to 'true'
    */
   private void passComment()
   {
      while (pos < len) {
         char c = read.charAt(pos);
         if (c == '\t') {
            pos++;
         }
         else {
            read.setCharAt(pos++, ' ');
            if (c == '*' && pos < len && read.charAt(pos) == '/') {
               read.setCharAt(pos, ' ');
               onComment = false;
               return;
            }
         }
      }
      onComment = true;
   }

   private File file;  // always stored on Canonical form
   private FileReader fileReader;
   private LineNumberReader input;
   private PrintWriter writer;
   private PipedWriter output;
   private PipedReader exported;
   private String addInfo;

   private StringBuffer read;
   private boolean onComment, onGoing = true;
   private Prepreprocessor parent, child;
   private PreprocessorInterface controller;
   private int pos, len;
}
