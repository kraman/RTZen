/**
 * File: DependenciesChecker.java
 * Last update: 0.60, 30th May 2004
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Class keeping the dependencies between sources and targets.
 * It checks wether any of the added targets is older than any of
 * the sources.
 * It also keeps a dependencies file for each worked file, where the
 * dependencies for that file are kept. This file has the name
 * file.uniqueDependencyId.depends, like in naming.idl.C91B.depends
 * The unique id is used to identify the dependency (a given file
 * can have different dependencies depending on how is used) and the
 * file itself (to distinguish files in different locations and
 * sharing the same name).
 * The content of this file is ASCII, containing the sources and
 * targets for his file: a source is a dependency for this file,
 * while a target is a product of this file.
 * Each file is preceeded by a character '<' in case of a source,
 * and by '>' in case of a target.
 * Finally, the file is ended with a line containg '--'
 */
class DependenciesChecker
{

   /**
    * @param dependsFile the file whose dependencies are to be studied
    * @param uniqueId an id for the kind of dependencies used
    * @param dependsDir the directory where the dependency files are stored.
    * @param baseDir the directory used as root for the files specified
    *    on any operation addSource or addTarget.
    * @param verbose. Is set to true to printout some basic information
    * @param debug. Is set to true to printout all the dependencies
    * @param logger is the object to use to log any information
    */
   public DependenciesChecker(File dependsFile, UniqueDependencyId uniqueId,
         File dependsDir, File baseDir, boolean verbose, boolean debug, Logger logger)
      throws FileNotFoundException
   {
      this.logger = logger;
      initialFile = dependsFile;
      dependsFileTime = dependsFile.lastModified();
      StringBuffer name = new StringBuffer(dependsFile.getName());
      name.append('.').append(uniqueId.generateUniqueId(dependsFile)).append(".depends");

      if (verbose || debug) {
         logger.log("Scan: " + name.toString());
      }
      dependencyFile = new File(dependsDir, name.toString());
      this.baseDir = baseDir;
      this.debug = debug;
      this.verbose = verbose;
      toBuild = false;
      checkDependencyFile(dependsFile);
   }

   /**
    * Returns true if it is needed to regenerate the dependencies.
    */
   public boolean generateDependencies()
   {
      return toGenerateDependencies;
   }

   /**
    * Returns the sources (IDL files) from which this file depends
    */
   public Set getIDLSources()
   {
      return dependencies;
   }

   /**
    * Communicates that the generation of dependencies start. An exception
    * can be thrown if the dependency file cannot be created
    */
   public void startDependenciesGeneration() throws IOException
   {
      if (verbose || debug) {
         logger.log("Scan: " + initialFile.getName());
      }
      fileWriter = new FileWriter(dependencyFile);
      printWriter = new PrintWriter(fileWriter);
   }

   /**
    * Communicates that the generation of dependencies stops.
    * @param generationCompleted set to true if the generation has ended fine
    */
   public synchronized void stopDependenciesGeneration(boolean generationCompleted)
   {
      if (fileWriter != null) {
         if (generationCompleted) {
            Iterator it = sourcesFound.iterator();
            while (it.hasNext()) {
               printWriter.println("<" + (String) it.next());
            }
            it = targetsFound.iterator();
            while (it.hasNext()) {
               printWriter.println(">" + (String) it.next());
            }
            printWriter.println("--");
         }
         printWriter.close();
         try {
            fileWriter.close();
         }
         catch (Exception ex) {}
         toGenerateDependencies = false;
         printWriter = null;
         fileWriter = null;
      }
      if (!generationCompleted) {
         dependencyFile.delete();
      }
   }

   /**
    * Returns true if it's needed to rebuild (or to reparse) the original
    * file because the dependencies show that any of the sources is newer
    * than the targets
    */
   public boolean build()
   {
      return toBuild;
   }

   /**
    * Specifies a new source. It returns true if the file specified
    * is newer than any of the previously specified targets
    */
   public void addSource(File file)
   {
      dependencies.add(file);
      String fileName = file.toString();
      if ((fileWriter!=null) && sourcesFound.add(fileName)) {
         if (debug) {
            logger.log("Source: " + fileName);
         }
         if (!toBuild) {
            addSourcePrivate(file);
         }
      }
   }

   /**
    * Specifies a new target.
    */
   public void addTarget(String fileName)
   {
      File file = new File(baseDir, fileName);
      String itsName = file.toString();
      if ((fileWriter!=null) && targetsFound.add(itsName)) {
         if (debug) {
            logger.log("Target: " + itsName);
         }
         if (!toBuild) {
            addTargetPrivate(file);
         }
      }
   }

   /**
    * Specifies a new source read on the dependencies file
    */
   private void addDependSource(String fileName)
   {
      if (debug) {
         logger.log("Dependency file, source: " + fileName);
      }
      File file = new File(fileName);
      dependencies.add(file);
      addSourcePrivate(file);
   }

   /**
    * Specifies a new target read on the dependencies file
    */
   private void addDependTarget(String fileName)
   {
      if (debug) {
         logger.log("Dependency file, target: " + fileName);
      }
      addTargetPrivate(new File(fileName));
   }

   /**
    * Specifies a new source. It modifies toBuild to true if
    * the target is older than any source
    */
   private void addSourcePrivate(File file)
   {
      long time = file.lastModified();
      if (time == 0) {
         if (debug) {
            logger.log("Source " + file.toString() + " not found!");
         }
      }
      else {
         if (time > source) {
            newestSource = file;
            source = time;
            if (source > target) {
               toBuild = true;
               if (debug) {
                  logger.log("Source " + newestSource.toString() + " newer than target "
                        + oldestTarget.toString());
               }
            }
         }
      }
   }

   /**
    * Specifies a new target. It modifies toBuild to true if
    * the target is older than any source
    */
   private void addTargetPrivate(File file)
   {
      long time = file.lastModified();
      if (time == 0) {
         if (debug) {
            logger.log("Target " + file.toString() + " not found!");
         }
         toBuild = true;
      }
      else {
         if (time < target) {
            oldestTarget = file;
            target = time;
            if (source > target) {
               toBuild = true;
               if (debug) {
                  logger.log("Source " + newestSource.toString() + " newer than target "
                        + oldestTarget.toString());
               }
            }
         }
      }
   }

   /**
    * Checks the dependencyFile to verify if it is needed to parse again the
    * file
    */
   private void checkDependencyFile(File source)
   {
      init();
      // first check: dependency file is newer than original file
      long original = source.lastModified();
      long thisOne = dependencyFile.lastModified();
      toGenerateDependencies = (original == 0L) || (thisOne == 0L) || (original > thisOne);
      if (!toGenerateDependencies) {
         // second: check the contents of the dependency file
         FileReader fileReader = null;
         BufferedReader reader = null;
         try {
            fileReader = new FileReader(dependencyFile);
            reader = new BufferedReader(fileReader);
            String line = reader.readLine();
            boolean ended = false;
            while (!toBuild && !toGenerateDependencies && (line != null)) {
               if (line.length() < 2) {
                  toGenerateDependencies = true;
               }
               else {
                  char c = line.charAt(0);
                  if (c == '<') {
                     addDependSource(line.substring(1));
                  }
                  else if (c == '>') {
                     addDependTarget(line.substring(1));
                  }
                  else if (c == '-' && line.length() == 2 && line.charAt(1) == '-'
                        && reader.readLine() == null) {
                     ended = true;
                  }
                  else {
                     toGenerateDependencies = true;
                  }
                  line = reader.readLine();
               }
            }
            if (!toGenerateDependencies && !ended && !toBuild) {
               toGenerateDependencies = true;
            }
         }
         catch (IOException ioex) {
            toGenerateDependencies = true;
         }
         finally {
            try {
               reader.close();
            }
            catch (Exception ex) {}
            try {
               fileReader.close();
            }
            catch (Exception ex) {}
         }
      }
      if (toGenerateDependencies) {
         if (debug) {
            logger.log("Incorrect/Old dependencies file");
         }
         init();
      }
   }

   private void init()
   {
      oldestTarget = null;
      newestSource = initialFile;
      target = Long.MAX_VALUE;
      source = dependsFileTime;
      toBuild = false;
   }

   private File dependencyFile, initialFile;
   private FileWriter fileWriter;
   private PrintWriter printWriter;
   private File oldestTarget, newestSource;
   private long target, source, dependsFileTime;
   private boolean verbose, debug, toBuild, toGenerateDependencies;
   private File baseDir;
   private Logger logger;
   private Set dependencies = new HashSet();
   private Set sourcesFound = new HashSet();
   private Set targetsFound = new HashSet();
}

