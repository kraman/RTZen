/**
 * File: TaskAttributesHandler.java
 * Last update: 0.60, 30th May 2004
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend;


import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteJava;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Path;

/**
 * Handles every task related to the attributes, except packages and
 * translates
 */
class TaskAttributesHandler
{

   private boolean jdk, orbacus, jacorb, openorb, orbix2k;
   private boolean generateClient, generateServer, generateTIE, generateAMI;
   private boolean verboseBASIC, verboseDEBUG;
   private boolean forceExternalExecutable;

   /**
    * The constructor requires the type of compiler decided.
    * @param compiler
    * @param Commandline the commandLine to use to execute the compiler,
    *     updates with information on the compiler
    * @param idlCompiler specifies the location of the compiler. In this case,
    *    it will be executed externally, outside the JVM
    */
   public TaskAttributesHandler(CompilerAttribute compiler, Commandline commandline, String idlCompiler)
   {
      if (idlCompiler!=null){
         forceExternalExecutable=true;
         commandline.setExecutable(idlCompiler);
      }
      String compilerValue = compiler.getValue();
      if (compilerValue.equals(JDK)) {
         jdk = true;
         if (!forceExternalExecutable){
            commandline.setExecutable("com.sun.tools.corba.se.idl.toJavaPortable.Compile");
         }
      }
      else if (compilerValue.equals(IBMJDK)) { //behaves like JDK, exactly, except for the executable itself
         jdk = true;
         if (!forceExternalExecutable){
            commandline.setExecutable("com.ibm.idl.toJavaPortable.Compile");
         }
      }      
      else if (compilerValue.equals(ORBACUS)) {
         orbacus = true;
         if (!forceExternalExecutable){
            commandline.setExecutable("jidl");
         }
      }
      else if (compilerValue.equals(OPENORB)) {
         openorb = true;
         if (!forceExternalExecutable){
            commandline.setExecutable("org.openorb.compiler.IdlCompiler");
         }
      }
      else if (compilerValue.equals(JACORB)) {
         jacorb = true;
         if (!forceExternalExecutable){
            commandline.setExecutable("org.jacorb.idl.parser");
         }
      }
      else {
         orbix2k = true;
         if (!forceExternalExecutable){
            commandline.setExecutable("idl");
         }
      }
   }
   
   /**
    * Executes the compiler for all the files in the given list
    */
   public void execute(Commandline commandline, List idlFiles, Path classpath, Path path, Task owner)
   {
      if (verboseDEBUG) {
         owner.log("Execute: " + commandline.toString() + getFileNames(idlFiles));
      }
      else if (verboseBASIC) {
         owner.log("Processing" + getFileNames(idlFiles));
      }
      if (forceExternalExecutable || orbacus || orbix2k)  // not executed directly in the JVM
      {
         Iterator it = idlFiles.iterator();
         while (it.hasNext()){
            commandline.createArgument().setFile((File)it.next());
         }
         runCommand(owner, commandline.getCommandline(), classpath, path, owner);
      }
      else if (jacorb | openorb) //executed in a forked process
      {
         Java executer = new Java();
         executer.setProject(owner.getProject());
         executer.setTaskName(jacorb? "jacorb" : "openorb"); //for logging purposes
         executer.setFailonerror(true);

         executer.setFork(true); //note that jacorb does not need it! But it does on ant v1.5 
         if (classpath!=null){
            executer.setClasspath(classpath);
         }
         if (path!=null){
            owner.log("Warning: path specified, but cannot be applied with the chosen compiler");
         }
         String args[]=commandline.getArguments();
         for (int i=0;i<args.length;i++){
            executer.createArg().setValue(args[i]);
         }
         Iterator it = idlFiles.iterator();
         while (it.hasNext()){
            executer.createArg().setFile((File)it.next());
         }
         executer.setClassname(commandline.getExecutable());
         executer.execute();
     }
      else // jdk, openorb. Note that jacorb could also be executed like this
      {
         Iterator it = idlFiles.iterator();
         while (it.hasNext()){
            commandline.createArgument().setFile((File)it.next());
         }
         ExecuteJava executer = new ExecuteJava();
         executer.setJavaCommand(commandline);
         if (classpath!=null){
            executer.setClasspath(classpath);
         }
         if (path!=null){
            owner.log("Warning: path specified, but cannot be applied with the chosen compiler");
         }
         executer.execute(owner.getProject());
      }
   }

   /**
    * A utility method that returns a string with the names of the files included in the list,
    * separated by spaces, with a space at the beginning
    */
   private String getFileNames(List files)
   {
      StringBuffer sb = new StringBuffer(256);
      Iterator it = files.iterator();
      while (it.hasNext()){
         sb.append(' ').append((File)it.next());
      }
      return sb.toString();
   }

      
   
   /**
    * A utility method that runs an external command.  Writes the output and
    * error streams of the command to the project log. The logic is extracted
    * from the method with the same name in the ANT distribution
    *
    * @throws BuildException if the command does not return 0.
    */
   private void runCommand(Task task, String[] cmdline, Path classpath, Path path, Task owner) throws BuildException
   {
      try {
         task.log(Commandline.describeCommand(cmdline), Project.MSG_VERBOSE);
         Execute exe = new Execute(new LogStreamHandler(task,Project.MSG_INFO,Project.MSG_ERR));
         int paths=(path==null? 0 : 1)+(classpath==null? 0 : 1);
         if (paths>0){
            String env[] = new String[paths];
            if (path!=null) {
               env[--paths]="PATH="+path.toString();
               exe.setVMLauncher(false);
            }
            if (classpath!=null) {
               env[--paths]="CLASSPATH="+classpath.toString();
            }
            if (verboseDEBUG) {
               owner.log("Using " + env[0]);
               if (env.length>1) {
                  owner.log("Using " + env[1]);
               }
            }
            exe.setEnvironment(env);
         }
         exe.setAntRun(task.getProject());
         exe.setCommandline(cmdline);
         int retval = exe.execute();
         if (retval != 0) {
            throw new BuildException(cmdline[0] + " failed with return code " + retval, task.getLocation());
         }
      }
      catch (java.io.IOException exc) {
         throw new BuildException("Could not launch " + cmdline[0] + ": " + exc, task.getLocation());
      }
   }

   /**
    * Creates the appropiated translator
    */
   public Translator createTranslator(List packages, List translates)
   {
      Translator ret;
      if (jdk) {
         ret = new JDKTranslator(packages, translates);
      }
      else if (orbacus) {
         ret = new OrbacusTranslator(packages, translates);
      }
      else if (openorb) {
         ret = new OpenORBTranslator(packages, translates);
      }
      else if (jacorb) {
         ret = new JacorbTranslator(packages, translates);
      }
      else {
         ret = new Orbix2KTranslator(packages, translates);
      }
      return ret;
   }

   /**
    * After calling to handleSideAttribute, this methods returns a boolean
    * flag to specify wether the client side must be generated
    */
   public boolean toGenerateClient()
   {
      return generateClient;
   }

   /**
    * After calling to handleSideAttribute, this methods returns a boolean
    * flag to specify wether the server side must be generated
    */
   public boolean toGenerateServer()
   {
      return generateServer;
   }

   /**
    * After calling to handleSideAttribute, this methods returns a boolean
    * flag to specify wether the server TIEs must be generated
    */
   public boolean toGenerateTIEs()
   {
      return generateTIE;
   }

   /**
    * After calling to handleAMIFlag, this methods returns a boolean
    * flag to specify wether the ami callbacks must be generated
    */
   public boolean toGenerateAMI()
   {
      return generateAMI;
   }

   /**
    * After calling to handleVerboseLevel, this methods returns a boolean
    * flag to specify wether the verbosity is BASIC
    */
   public boolean isVerbosityNormal()
   {
      return verboseBASIC || verboseDEBUG;
   }

   /**
    * After calling to handleVerboseLevel, this methods returns a boolean
    * flag to specify wether the verbosity is DEBUG
    */
   public boolean isVerbosityDebug()
   {
      return verboseDEBUG;
   }

   /**
    * Convert the specified undefines into a string to be passed to the
    * IDLChecker, and updates the command line to reflect those undefines on a
    * compiler-dependent way
    * @param undefines A List holding Undefine objects
    * @param commandLine The CommandLine to update
    * @return A string containing the undefines as they're expected on the
    *      preprocessor
    */
   public String handleUndefines(List undefines, Commandline commandline)
   {
      StringBuffer ret = new StringBuffer();
      if (!undefines.isEmpty()) {
         String undefineFlag = getUndefineFlag();
         StringBuffer forCompiler = new StringBuffer();
         Iterator it = undefines.iterator();
         while (it.hasNext()) {
            Undefine undefine = (Undefine) it.next();
            if (undefine.name != null) {
               ret.append("#undef ").append(undefine.name).append("\n");
               if (undefineFlag != null) {
                  forCompiler.append(undefineFlag).append(undefine.name);
                  commandline.createArgument().setLine(forCompiler.toString());
                  forCompiler.setLength(0);
               }
            }
         }
      }
      return ret.toString();
   }

   /**
    * Convert the specified defines into a string to be passed to the
    * IDLChecker, and updates the command line to reflect those defines on a
    * compiler-dependent way
    * @param defines A List holding define objects
    * @param commandLine The CommandLine to update
    * @return A string containing the defines as they're expected on the
    *      preprocessor
    * @exception BuildException if some define object is not valid (it does
    *      not contain a name attribute)
    */
   public String handleDefines(List defines, Commandline commandLine)
   {
      StringBuffer ret = new StringBuffer();
      if (!defines.isEmpty()) {
         String defineFlag = getDefineFlag();
         StringBuffer forCompiler = new StringBuffer();
         Iterator it = defines.iterator();
         while (it.hasNext()) {
            Define define = (Define) it.next();
            if (define.name == null) {
               throw new BuildException("defines must always include a name");
            }
            ret.append("#define ").append(define.name);
            forCompiler.append(defineFlag).append(define.name);
            if (define.value == null) {
               ret.append("\n");
            }
            else {
               ret.append(" ").append(define.value).append("\n");
               forCompiler.append("=").append(define.value);
            }
            commandLine.createArgument().setLine(forCompiler.toString());
            forCompiler.setLength(0);
         }
      }
      return ret.toString();
   }

   /**
    * Convert the specified includes into a string to be passed to the
    * IDLChecker, and updates the command line to reflect those includes on a
    * compiler-dependent way
    * @param include A Path object with the paths specified in the task
    * @param commandLine The CommandLine to update
    * @return A string containing the includes as they're expected on the
    *      preprocessor
    */
   public String handleIncludes(Path include, Commandline commandLine)
   {
      StringBuffer ret = new StringBuffer();
      if (include != null) {
         String includeFlag = getIncludeFlag();
      
         boolean twoArgCmd = includeFlag.charAt(includeFlag.length() - 1) == ' ';
         if (twoArgCmd) {
            includeFlag = includeFlag.trim();
         }
      
         String defs[] = include.list();
         for (int i = 0; i < defs.length; i++) {
            if (i > 0) {
               ret.append(File.pathSeparatorChar);
            }
            ret.append(defs[i]);
        
            if (twoArgCmd) {
               commandLine.createArgument().setValue(includeFlag);
               commandLine.createArgument().setValue(defs[i]);
            }
            else {
               commandLine.createArgument().setValue(includeFlag + defs[i]);
            }
         }
      }
      return ret.toString();
   }

   /**
    * Updates the command line to reflect the target dir on a
    * compiler-dependent way
    * @param targetDir
    * @param commandLine The CommandLine to update
    */
   public void handleTargetDir(File targetDir, Commandline commandline)
   {
      String targetFlag = getTargetDirFlag();
      if (targetFlag.charAt(targetFlag.length() - 1) == ' ') {
         // They are two arguments
         commandline.createArgument().setValue(targetFlag.trim());
         commandline.createArgument().setFile(targetDir);
      }
      else {
         // A single argument
         commandline.createArgument().setValue(targetFlag + targetDir.toString());
      }
   }

   /**
    * Updates the command line to reflect the flag checkAll on a
    * compiler-dependent way
    * @param checkAll as specified on the task
    * @param commandLine The CommandLine to update
    */
   public void handleCheckAllFlag(boolean checkAll, Commandline commandline)
   {
      if (checkAll) {
         commandline.createArgument().setLine(getCheckAllFlag());
      }
   }

   /**
    * Updates the command line to reflect the flag AMI on a
    * compiler-dependent way
    * @param checkAll as specified on the task
    * @param commandLine The CommandLine to update
    */
   public void handleAMIFlag(AMIAttribute ami, Commandline commandline)
   {
      if (ami.supportAMI()) {
         commandline.createArgument().setLine(getAMIFlag(ami.getValue()));
         generateAMI=true;
      }
   }

   /**
    * Updates the command line to reflect the attribute verboseLevel on a
    * compiler-dependent way
    * @param level as specified on the task
    * @param commandLine The CommandLine to update
    */
   public void handleVerboseLevel(VerboseLevel level, Commandline commandline)
   {
      String translated = getVerboseFlag(level);
      if (translated != null) {
         commandline.createArgument().setLine(translated);
      }
      verboseBASIC = level.isBasic();
      verboseDEBUG = level.isVerbose();
   }

   /**
    * Convert the specified SideAttribute into 3 boolean flags, as expected
    * by the IDLChecker. Those flags must be accessed through other methods:
    * toGenerateClient(), toGenerateServer(), toGenerateTIEs()
    * It updates as well the command line to reflect the attribute on a
    * compiler-dependent way.
    * If some possibility is not valid for a specific compiler, it issues a
    * warning.
    * @param side A SideAttribute object,as specified in the task
    * @param commandLine The CommandLine to update
    */
   public void handleSideAttribute(SideAttribute side, Commandline commandLine)
   {
      String sideValue = side.getValue();
      generateClient = !sideValue.equals(SERVER) && !sideValue.equals(SERVERTIE);
      generateServer = !sideValue.equals(CLIENT);
      generateTIE = sideValue.equals(SERVERTIE) || sideValue.equals(ALLTIE);

      if (jdk) {
         commandLine.createArgument().setValue("-f" + sideValue);
      }
      else if (orbacus) {
         if (!generateServer) {
            commandLine.createArgument().setValue("--no-skeletons");
         }
         if (!generateClient) {
            throw new BuildException("Compiler " + ORBACUS + " does not accept " + SERVER
                  + " or " + SERVERTIE + " on the attribute <side>");
         }
         if (generateTIE) {
            commandLine.createArgument().setValue("--tie");
         }
      }
      else if (openorb) {
         if (!generateClient) {
            commandLine.createArgument().setValue("-nostub");
         }
         if (!generateServer) {
            commandLine.createArgument().setValue("-noskeleton");
         }
         if (!generateTIE) {
            commandLine.createArgument().setValue("-notie");
         }
      }
      else if (jacorb) {
         if (!generateServer) {
            commandLine.createArgument().setValue("-noskel");
         }
         if (!generateClient) {
            commandLine.createArgument().setValue("-nostub");
         }
         if (generateServer && !generateTIE) {
            throw new BuildException("Compiler " + JACORB + " does not accept " + SERVER
                  + " or " + ALL + " on the attribute <side>");
         }
      }
      else { // orbix2k
         if (generateServer) {
            commandLine.createArgument().setValue("-jpoa");
         }
         if (generateClient) {
            commandLine.createArgument().setValue("-jbase");
         }
         if (generateTIE) {
            throw new BuildException("Compiler " + ORBIX2K + " does not accept " + SERVERTIE
                  + " on the attribute <side>");
         }
      }
   }

   /**
    * Returns the string used by the compiler to specify an undefine
    */
   private String getUndefineFlag()
   {
      String ret = null;
      if (jdk || openorb) {
         throw new BuildException("Compiler " + (jdk ? JDK : OPENORB)
               + " does not accept <undefine> elements");
      }
      else {
         ret = "-U";
      }
      return ret;
   }

   /**
    * Returns the string used by the compiler to specify a define
    */
   private String getDefineFlag()
   {
      return jdk ? "-d " : openorb ? "-D " : "-D";
   }

   /**
    * Returns the string used by the compiler to specify a include
    */
   private String getIncludeFlag()
   {
      return jdk ? "-i " : openorb ? "-I " : "-I";
   }

   /**
    * Returns the string used by the compiler to specify the target dir
    */
   private String getTargetDirFlag()
   {
      return jdk ? "-td " : orbacus ? "--output-dir " : orbix2k ? "-O" : "-d ";
   }

   /**
    * Returns the string used by the compiler to specify how to create all the types
    */
   private String getCheckAllFlag()
   {
      String ret = null;
      if (orbix2k) {
         throw new BuildException("Compiler " + ORBIX2K + " does not accept <checkALL> elements");
      }
      else {
         ret = jdk ? "-emitAll" : orbacus ? "--all" : "-all";
      }
      return ret;
   }

   /**
    * Returns the string used by the compiler to specify the AMI support
    */
   private String getAMIFlag(String AMI) //just now, only CALLBACK, no checks
   {
      String ret = null;
      if (jacorb) {
         ret = "-ami_callback";
      }
      else {
         throw new BuildException("This compiler does not accept <ami> elements");
      }
      return ret;
   }

   /**
    * Returns the string used by the compiler to specify the verbosity level
    * It can be null if no flag is required
    * @param level the Verbosity level to translate
    */
   private String getVerboseFlag(VerboseLevel level)
   {
      String ret = null;
      if (level.getValue().equals(DEBUG)) {
         ret = (jdk || orbix2k) ? "-v" : orbacus ? "-d" : jacorb? "-W 4" : openorb? "-verbose" : null;
      }
      else if (level.getValue().equals(QUIET)) {
         ret = jdk ? "-noWarn" : orbix2k ? "-w" : openorb? "-silence" : null;
      }
      return ret;
   }

   /**
    * Enumerated attribute with the values "quiet", "verbose" and "debug".
    */
   public static class VerboseLevel extends EnumeratedAttribute
   {

      public VerboseLevel()
      {
         setValue(QUIET);
      }

      public String[] getValues()
      {
         return new String[] {QUIET, BASIC, DEBUG};
      }

      public boolean isVerbose()
      {
         return getValue().equals(DEBUG);
      }

      public boolean isBasic()
      {
         return getValue().equals(BASIC);
      }

   }


   /**
    * Enumerated attribute with the values "client","server"... (side parameter)
    */
   public static class SideAttribute extends EnumeratedAttribute
   {

      public SideAttribute()
      {
         setValue(ALLTIE);
      }

      public String[] getValues()
      {
         return new String[] {CLIENT, SERVER, ALL, SERVERTIE, ALLTIE};
      }

   }


   /**
    * Enumerated attribute with the values for the compiler attribute
    */
   public static class CompilerAttribute extends EnumeratedAttribute
   {

      public CompilerAttribute()
      {
         setValue(JDK);
      }

      public boolean checkJacorb2()
      {
         boolean ret = getValue().equals(JACORB2);
         if (ret){
            setValue(JACORB);
         }
         return ret;
      }

      public String[] getValues()
      {
         return new String[] {JDK, ORBACUS, JACORB, JACORB2, OPENORB, ORBIX2K, IBMJDK};
      }

   }


   /**
    * Enumerated attribute with the values for the preprocess attribute
    */
   public static class PreprocessAttribute extends EnumeratedAttribute
   {

      public PreprocessAttribute()
      {
         setValue(DISMISS);
      }

      public String[] getValues()
      {
         return new String[] {DISMISS, STORE, STOREFULL, USE, USEFULL};
      }

      public boolean generatePreprocessorFile()
      {
         return !getValue().equals(DISMISS);
      }

      public boolean usePreprocessorFile()
      {
         return getValue().equals(USE) || getValue().equals(USEFULL);
      }

      public boolean expandFull()
      {
         return getValue().equals(STOREFULL) || getValue().equals(USEFULL);
      }

      public boolean useFull()
      {
         return getValue().equals(USEFULL);
      }

   }


   public static class AMIAttribute extends EnumeratedAttribute
   {

      public AMIAttribute()
      {
         setValue(NO_AMI);
      }

      public String[] getValues()
      {
         return new String[] {NO_AMI, CALLBACK_AMI};
      }

      public boolean supportAMI()
      {
         return !getValue().equals(NO_AMI);
      }

   }


   public static class CallCompilerAttribute extends EnumeratedAttribute
   {

      public CallCompilerAttribute()
      {
         setValue(CC_FOREACH);
      }

      public String[] getValues()
      {
         return new String[] {CC_FOREACH, CC_ONCE, CC_USEALL};
      }

      public boolean isIncremental()
      {
         return getValue().equals(CC_FOREACH);
      }

      public boolean useAllFiles()
      {
         return getValue().equals(CC_USEALL);
      }

   }


   /**
    * Class used to specify the 'define' parameters
    */
   public static class Define extends Task
   {
      String name, value;
      public void setName(String name)
      {
         this.name = name;
      }

      public void setValue(String value)
      {
         this.value = value;
      }
   }


   /**
    * Class used to specify the 'undefine' parameters
    */
   public static class Undefine extends Task
   {
      String name;
      public void setName(String name)
      {
         this.name = name;
      }
   }

   /**
    * CLASS's DATA
    */
   static final String QUIET = "quiet";
   static final String BASIC = "basic";
   static final String DEBUG = "debug";
   static final String CLIENT = "client";
   static final String SERVER = "server";
   static final String ALL = "all";
   static final String SERVERTIE = "serverTIE";
   static final String ALLTIE = "allTIE";
   static final String JDK = "jdk";
   static final String IBMJDK = "ibm";
   static final String ORBACUS = "orbacus";
   static final String JACORB = "jacorb";
   static final String JACORB2 = "jacorb2";
   static final String OPENORB = "openorb";
   static final String ORBIX2K = "orbix2k";
   static final String DISMISS = "dismiss";
   static final String STORE = "store";
   static final String STOREFULL = "storeFull";
   static final String USE = "use";
   static final String USEFULL = "useFull";
   static final String NO_AMI = "no";
   static final String CALLBACK_AMI = "callback";
   static final String CC_FOREACH = "foreach";
   static final String CC_ONCE = "once";
   static final String CC_USEALL = "onceWithAll";
}
