/**
 * File: IncludesHandler.java
 * Author: LuisM Pena
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.preprocessor;


import idldepend.javacc.FileIncluder;
import idldepend.javacc.generated.ParseException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * The IncludesHandler handles every task relationated with including
 * files.
 * It allows its user to specify one or more paths to look for included
 * files, being important the order on which they are specified.
 * The rules to find included files are the following:
 * -Using quoted form: look for include files in the same directory of
 *    the file that contains the #include statement, and then in the
 *    directories of whatever files that include (#include) that file.
 *    The preprocessor then searches along the path specified by the user
 * -Using angle-bracket form: The preprocessor searches along the path
 *    specified by the user.
 *
 * It could be checked the environment variable to look for INCLUDE
 *   definitions, but it is not done : that is not really generic, and could
 *   be still performed by this class's user by invoking:
 *   addPath(System.getProperty("INCLUDE"));
 **/
class IncludesHandler
{

   /**
    * Looks for the file given in the parameter path.
    */
   public File findFile(String path, FileIncluder fileIncluder) throws ParseException
   {
      File inMap = (File) includesAlreadyHandled.get(path);
      if (inMap == null) {
         boolean wrong = true, quoted = false;
         int length = path.length();
         if (length > 2) {
            char c = path.charAt(0);
            if ((c == '"') && (path.charAt(length - 1) == '"')) {
               quoted = true;
               wrong = false;
            }
            else {
               wrong = (c != '<') || (path.charAt(length - 1) != '>');
            }
         }
         if (wrong) {
            throw new ParseException("Include file incorrectly specified: " + path);
         }

         if (quoted) {
            inMap = findQuotedFile(path.substring(1, length - 1), fileIncluder);
         }
         else {
            inMap = findAngledBracketFile(path.substring(1, length - 1));
         }
         if (inMap != null) { // this check is not really needed.
            includesAlreadyHandled.put(path, inMap);
         }
      }
      return inMap;
   }

   /**
    * Looks for the file that has been specified using quotes: on the directory
    *  where the included file is located
    * (and any of the files which included that one) and then into the directories
    * specified via addPath)
    * @param path The name of the file to find, without the quotes
    */
   private File findQuotedFile(String path, FileIncluder includer)
   {
      File ret = null;
      while ((ret == null) && (includer != null)) {
         File including = includer.getFile().getParentFile();
         if (including != null) {
            ret = findFileOnDir(including, path);
         }
         includer = includer.getParent();
      }
      return ret == null ? findFileOnDirs(path) : ret;
   }

   /**
    * Looks for the file that has been specified with angled-brackets (just
    * on the directories specified via addPath)
    * @param path The name of the file to find, without the angled brackets
    */
   private File findAngledBracketFile(String path)
   {
      return findFileOnDirs(path);
   }

   /**
    * Looks for the file on the directories included in the path
    */
   private File findFileOnDirs(String path)
   {
      File ret = null;
      Iterator it = pathsDirs.iterator();
      while ((ret == null) && (it.hasNext())) {
         ret = findFileOnDir((File) it.next(), path);
      }
      return ret;
   }

   /**
    * Looks for the file on the directory specified
    */
   private File findFileOnDir(File dir, String fileName)
   {
      File possibility = new File(dir, fileName);
      return possibility.isFile() ? possibility : null;
   }

   /**
    * Adds the given path to the list of paths to find for include files.
    * The given path can be a list of paths, separated by the system-dependent
    * path separator.
    */
   public void addPath(String path)
   {
      int pos = 0, end;
      do {
         end = path.indexOf(pathSeparator, pos);
         if (end == -1) {
            if (pos == 0) {
               addSimplePath(path);
            }
            else {
               addSimplePath(path.substring(pos));
            }
         }
         else {
            addSimplePath(path.substring(pos, end));
            pos = end + 1;
         }
      } while (end != -1);
   }

   /**
    * Adds the path to the given list, checking first if is not yet included.
    * The path is considered 'simple', not a list of paths. If the path does
    * not exist (must be a directory), is just not included
    */
   private void addSimplePath(String path)
   {
      String name = path.trim();
      if (name.length() > 0) {
         File newPath = new File(name);
         if (newPath.isDirectory()) {
            if (!pathsDirs.contains(newPath)) {
               pathsDirs.add(newPath);
            }
         }
      }
   }

   static private String pathSeparator = System.getProperty("path.separator");
   private List pathsDirs = new ArrayList(); // a set does not respect the order
   private Map includesAlreadyHandled = new HashMap();
}

