/**
 * File: IDLCheckerTask.java
 * Author: LuisM Pena
 * Last update: 0.61, 29th June 2004
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;


/**
 * Task to execute the compiler transforming IDL specifications
 * into java files.
 * It accepts the following arguments:
 *
 * - force : boolean : optional, default to false
 *   If set, it doesn't perform any check, launching the executables
 *      directly
 *
 * - failOnError : boolean : optional, default to false
 *   If set, it stops the ANT task as soon as a parsing error is found,
 *      without starting the compilation.
 *
 * - checkall : boolean : optional, default to false
 *   It checks all the types that must be generate, including those
 *      defined in #included files
 *
 * - preprocess : [dismiss, store, storeFull, useFull], default to dismiss
 *   If 'store', it saves the preprocessed file in the dependencies directory
 *   If 'storeFull', it is like store, but it also expands the included files
 *   If 'use', it uses the stored preprocessed file as input to the IDL compiler
 *   If 'useFull', it uses the storedFull preprocessed file as input to the IDL compiler
 *
 * - side: [client/server/all/serverTIE/allTIE], default to allTIE
 *   It checks that the files requested on the specific side are valid
 *
 * - compiler : [jdk/orbacus/jacorb/jacorb2/openorb], default to jdk
 *
 * - callCompiler : [foreach/once/onceWithAll], default to once
 *   Defines how the compiler will be called: once with all the required files,
 *   or several times with each file. The special option onceWithAll will compile
 *   all the files if one of them has changed
 *
 * - ami : [no/callback], default to no
 *   Adds support for the Asynchronous Messaging Interface
 *
 * - define : nested, allows name=, value= (name is mandatory)
 *   Defines new macros to be defined before executing the preprocessor
 *
 * - undefine : nested, allows name=(name is mandatory)
 *   Undefines macros before executing the preprocessor
 *
 * - file : File : optional, but this or fileset must be present
 *   Specify the IDL file (including extension) to ve verified. It is
 *      possible to specify a sequence of files using fileset
 *
 * - fileset : FileSet : optional, but this or file must be present
 *   Specify the IDL files to ve verified.
 *
 * - include : Path : optional
 *   Specifies the path(s) to be used when looking for included files
 *      on the IDL files
 *
 * - classpath : Path : optional
 *   Specifies the classpath(s) to be used when launching the idl compiler
 *
 * - path : Path : optional
 *   Specifies the path(s) to be used when launching the idl compiler
 *      It is only effective on the idl compilers executed outside the JVM
 *      (not on jdk & jacorb)
 *
 * - targetDir : File : optional, default to the project base dir
 *   Target directory used on the file generation
 *
 * - verbose : String : quiet | basic | debug : optional, quiet by default
 *   Specifies the verbosity level
 *
 *  -package, with the following attributes:
 *    -module. string
 *    -prefix. string
 *    -auto. boolean
 *
 *  -translate, with the following attributes:
 *    -module. string
 *    -package. string
 *
 *  -dependsDir: File
 *   The dependencies are stored in the directory specified by this
 *   argument. If is not specified, it is used targetDir, and, if this
 *   is neither specified, the base directory
 *
 *  -args: String (nested)
 *      It allows the user to specify additional arguments. Note that if this
 *      argument makes the compiler modify the name or number of created
 *      files, force should be set to "true", as dependencies would not be
 *      correctly verified
 *
 */
public class IDLCheckerTask extends Task implements Logger
{

   public IDLCheckerTask()
   {}

   /***
    * *
    *  METHODS TO SET THE TASK'S ARGUMENTS
    * *
    ***/

   public void setForce(boolean set)
   {
      force = set;
   }

   public void setFailOnError(boolean set)
   {
      failOnError = set;
   }

   public void setCheckall(boolean set)
   {
      checkAll = set;
   }

   public void setVerbose(TaskAttributesHandler.VerboseLevel level)
   {
      verboseLevel = level;
   }

   public void setSide(TaskAttributesHandler.SideAttribute attribute)
   {
      side = attribute;
   }

   public void setCompiler(TaskAttributesHandler.CompilerAttribute attribute)
   {
      compiler = attribute;
   }

   public void setPreprocess(TaskAttributesHandler.PreprocessAttribute attribute)
   {
      preprocess = attribute;
   }

   public void setAMI(TaskAttributesHandler.AMIAttribute attribute)
   {
      ami = attribute;
   }

   public void setCallCompiler(TaskAttributesHandler.CallCompilerAttribute attribute)
   {
      callCompiler = attribute;
   }

   public void setDependsdir(File dir)
   {
      dependsDir = dir;
   }

   public Task createDefine()
   {
      TaskAttributesHandler.Define newDefine
            = new TaskAttributesHandler.Define();
      defines.add(newDefine);
      return newDefine;
   }

   public Task createUndefine()
   {
      TaskAttributesHandler.Undefine newUndefine
            = new TaskAttributesHandler.Undefine();
      undefines.add(newUndefine);
      return newUndefine;
   }

   public Path createInclude()
   {
      if (include == null) {
         include = new Path(getProject());
      }
      return include;
   }

   public Path createClasspath()
   {
      if (classpath == null) {
         classpath = new Path(getProject());
      }
      return classpath;
   }

   public Path createPath()
   {
      if (path == null) {
         path = new Path(getProject());
      }
      return path;
   }

   public FileSet createFileset()
   {
      if (idlFiles == null) {
         idlFiles = new FileSet();
      }
      return idlFiles;
   }

   public Task createPackage()
   {
      Task ret = new Translator.PackageTask();
      packages.add(ret);
      return ret;
   }

   public Task createTranslate()
   {
      Task ret = new Translator.TranslateTask();
      translates.add(ret);
      return ret;
   }

   public void setFile(File file)
   {
      idlFile = file;
   }

   public void setTargetdir(File dir)
   {
      targetDir = dir;
   }

   public void setCompilerPath(String compiler)
   {
      idlCompiler=compiler;
   }

   public Commandline.Argument createArg()
   {
      return commandLine.createArgument();
   }

   /**
    * Execute the task, once the arguments have been set
    */
   public void execute()
   {
      if (verboseLevel.isVerbose() || verboseLevel.isBasic()){
         log(VERSION);
      }

      if (preprocess.useFull()) {
         checkAll = true;
      }

      //converts the introduced parameters, modifying the command line
      convertParameters();

      Translator translator = attributesHandler.createTranslator(packages, translates);
      translator.modifyCommandline(commandLine);

      UniqueDependencyId uniqueId = new UniqueDependencyId(commandLine, preprocess.getValue());

      // include now the arguments that should not affect the uniqueId
      attributesHandler.handleVerboseLevel(verboseLevel, commandLine);

      if (idlFile != null) {
         check(idlFile, uniqueId, translator);
      }
      if (idlFiles != null) {
         DirectoryScanner scanner = idlFiles.getDirectoryScanner(getProject());
         File baseDir = scanner.getBasedir();
         String which[] = scanner.getIncludedFiles();
         for (int i = 0; i < which.length; i++) {
            if (check(new File(baseDir, which[i]), uniqueId, translator) && callCompiler.isIncremental()){
               callIDLCompiler(candidateFiles, (Commandline) commandLine.clone());
            }
         }
      }
      //if some files have changed, it is needed to compile them now
      if (!candidateFiles.isEmpty()){
         if (callCompiler.useAllFiles()){
            callIDLCompiler(sortOnDependencies(allFiles), commandLine);
         }
         else {
            callIDLCompiler(sortOnDependencies(candidateFiles), commandLine);
         }
      }
   }

   /**
    * Verifies one independent file, once that the arguments have been checked
    * and preprocessed. The file is added to the list allFiles and, if it requires
    * being idl-compiled, to the candidateFiles list.
    * @return true if the file must be compiled
    */
   private boolean check(File idlFile, UniqueDependencyId uniqueId, Translator translator)
   {
      boolean requiresCompilation=true;
      boolean usePreprocessedFile = preprocess.usePreprocessorFile();
      if (!force || usePreprocessedFile) {
         if (!dependsDir.isDirectory()) {
            throw new BuildException("Directory " + dependsDir.toString() + " does not exist");
         }
         try {
            IDLChecker checker = new IDLChecker(idlFile, uniqueId, dependsDir, targetDir,
                  checkerIncludes, checkerPreprocessor, attributesHandler.toGenerateClient(),
                  attributesHandler.toGenerateServer(), attributesHandler.toGenerateTIEs(),
                  attributesHandler.toGenerateAMI(),
                  checkAll, translator, attributesHandler.isVerbosityNormal(),
                  attributesHandler.isVerbosityDebug(), this, !force,
                  preprocess.generatePreprocessorFile(), preprocess.expandFull());

            if (checker.build(failOnError || usePreprocessedFile) || force){
               if (usePreprocessedFile) {
                  idlFile = checker.getPreprocessedFile();
               }
            }
            else {
               requiresCompilation = false;
            }
	    dependencies.put(idlFile, checker.getIDLSources());
         }
         catch (Exception ex) {
            throw new BuildException(ex.getMessage());
         }
      }
      allFiles.add(idlFile);
      if (requiresCompilation){
         candidateFiles.add(idlFile);
      }
      if (verboseLevel.isVerbose() || verboseLevel.isBasic()){
         log("Status: " + idlFile.getName() + (requiresCompilation? " requires" : " does not require")+ " idl compilation");
      }
      return requiresCompilation;
   }

   /**
    * Processes the files found in the list, with the options given in the command line
    * The list is then removed
    */
   private void callIDLCompiler(List files, Commandline commandLine)
   {
      attributesHandler.execute(commandLine, files, classpath, path, this);
      files.clear();
   }

   /**
    * Returns a list composed by the same elements in the original list, but
    * whose order depends on the dependencies between files. That is, if file
    * a.idl depends on b.idl, b.idl will appear first in the final list
    */
   private List sortOnDependencies(List files)
   {
      List ret = new ArrayList();
      boolean extracted;
      //we will iterate on the given list; for each element, we study its dependencies
      //if a file depends on other file that is (still)in the same list, means that
      //this file should go later in the final list.
      //to avoid infinite loops, in the (as principle) impossible case where A depends
      //on B and B depends on A, if a loop does not extract any file, the ordering
      //ends, and the files are dumped like they are to the final list
      do 
      {
         extracted = false;
         Iterator it = files.iterator();
         while(it.hasNext()){
            File file = (File) it.next();
            boolean independent=true;
            Iterator it2 = ((Set) dependencies.get(file)).iterator();
            while(independent && it2.hasNext()){
               if (files.contains(it2.next())){
                  independent=false;
               }
            }
            if (independent){
               ret.add(file);
               it.remove();
               extracted=true;
            }
         }
      } 
      while (extracted);
      ret.addAll(files);
      return ret;
   }
   
   /**
    * Converts the parameters given into a Commandline, with information that
    * is already dependant on the compiler. It also updates the global variables
    * checkerPreprocessor, checkerIncludes.
    * The only element not included is the debug information, which must be included later
    */
   private void convertParameters()
   {
      if (idlFile == null && idlFiles == null) {
         throw new BuildException("\"file\" or \"FileSet\" must be specified");
      }

      if (targetDir == null) {
         targetDir = getProject().getBaseDir();
      }
      if (dependsDir == null) {
         dependsDir = targetDir;
      }
      if (compiler.checkJacorb2()) {
         log("Warning: compiler=jacorb2 is deprecated, use compiler=jacorb instead");
      }

      attributesHandler = new TaskAttributesHandler(compiler, commandLine, idlCompiler);
      attributesHandler.handleCheckAllFlag(checkAll, commandLine);
      attributesHandler.handleSideAttribute(side, commandLine);
      attributesHandler.handleTargetDir(targetDir, commandLine);
      attributesHandler.handleAMIFlag(ami, commandLine);
      String checkerUndefines = attributesHandler.handleUndefines(undefines, commandLine);
      String checkerDefines = attributesHandler.handleDefines(defines, commandLine);
      checkerIncludes = attributesHandler.handleIncludes(include, commandLine);
      checkerPreprocessor = checkerDefines + checkerUndefines;
   }

   public void log(String msg)
   {
      super.log(msg);
   }

   public final static void main(String args[])
   {
      System.out.println(VERSION);
   }

   private boolean checkAll;
   private boolean force;
   private boolean failOnError;
   private File targetDir, dependsDir;
   private File idlFile;
   private FileSet idlFiles;
   private Path include;
   private Path classpath, path;
   private String idlCompiler;
   private Commandline commandLine = new Commandline();
   private List defines = new ArrayList();
   private List undefines = new ArrayList();
   private List packages = new ArrayList();
   private List translates = new ArrayList();
   private TaskAttributesHandler.VerboseLevel verboseLevel = new TaskAttributesHandler.VerboseLevel();
   private TaskAttributesHandler.SideAttribute side = new TaskAttributesHandler.SideAttribute();
   private TaskAttributesHandler.CompilerAttribute compiler = new TaskAttributesHandler.CompilerAttribute();
   private TaskAttributesHandler.PreprocessAttribute preprocess = new TaskAttributesHandler.PreprocessAttribute();
   private TaskAttributesHandler.AMIAttribute ami = new TaskAttributesHandler.AMIAttribute();
   private TaskAttributesHandler.CallCompilerAttribute callCompiler = new TaskAttributesHandler.CallCompilerAttribute();

   private List allFiles = new ArrayList(); //all the specified files, probably converted into preprocessed form
   private List candidateFiles = new ArrayList(); //from allFiles, those that require being idl-compiled
   private Map dependencies = new HashMap(); //each candidate file, with a Set on which it depends on
   
   private TaskAttributesHandler attributesHandler;
   private String checkerPreprocessor, checkerIncludes;

   private final static String VERSION="idldepend v0.61 - http://grasia.fdi.ucm.es/~luismi/idldepend";
}

