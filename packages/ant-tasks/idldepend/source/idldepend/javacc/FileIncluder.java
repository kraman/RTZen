/**
 * File: FileIncluder.java
 * Author: LuisM Pena
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.javacc;


import java.io.File;


/**
 * The FileIncluder interface addresses the functionality required to
 * include new files. Following the ANSI C++ preprocessor, there are two
 * ways to handle a #include statement
 * #include <file> : looks for the file into the directories specified at
 *     startup time.
 * #include "file" : it looks first into the directories where the current
 *     file (and ancestors) are located.
 * To handel the second case, it is needed therefore to know which is the
 * file being parsed (and ancestors);
 */
public interface FileIncluder
{

   /**
    * Returns the file being parsed, always on its canonical form
    */
   public File getFile();

   /**
    * Returns the FileIncluder ancestor, null if there is none
    */
   public FileIncluder getParent();
}
