/**
 * File: IDLChecker.java
 * Last update: 0.60, 30th May 2004
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend;


import idldepend.idl.IDLMapperUser;
import idldepend.idl.IDLToJavaMapping;
import idldepend.javacc.generated.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;

import java.util.Collections;
import java.util.Set;

import idldepend.javacc.generated.IDLParser;


/**
 * This class is the responsible to perform the checking of the files to be
 * generated. This checking is done in two ways: or using a dependency file,
 * previously generated, where the dependencies can be obtained, or parsing
 * the specified IDL file (and generating the dependency file for any
 * further checking).
 */
class IDLChecker extends Thread implements IDLMapperUser
{

   /**
    * @param idlFile the file to check
    * @param uniqueId an identity that can identify this generation uniquely,
    *     to track dependencies (two generations of the same file with different
    *     parameters will produce different dependencies)
    * @param dependsDir the directory where the dependency files are stored
    * @param targetDir the directory where the .java files must be located
    * @param includePath an string containing the include path
    * @param defines an string containing all the defines. They must be written in
    *    as the preprocessor expects them: #define symbol [value]
    *    In fact, this string can contain any information, as it is passed to the
    *    preprocessor
    * @param client set to true when the client-side files must be generated
    * @param server set to true when the server-side files must be generated
    * @param ties set to true when the server-side delegated-based files must be generated
    * @param ami set to true when the ami callbacks must be generated
    * @param checkAll set to true when the included files are also checked into the files
    *    that must been generated
    * @param translator the Translator to use to convert the file names
    * @param verbose set to true to get some information during the checking
    * @param debug set to true to get all the information during the checking
    * @param logger is the object to use to log any information
    * @param checkDependencies set to true if the IDL syntax must be checked. If not, build()
    *    won't return any valid value, and generatePreprocessedFile is forced to true
    * @param generatePreprocessedFile set to true if the preprocessed file must be generated
    * @param generatePreprocessedFileFull set to true if the preprocessed file must
    *    be generated in full format (expanding as well the include lines).
    *    Only used if generatePreprocessedFile is true
    */
   public IDLChecker(File idlFile, UniqueDependencyId uniqueId,
         File dependsDir, File targetDir,
         String includePath, String addedToPrep,
         boolean client, boolean server, boolean ties, boolean ami, boolean checkAll,
         Translator translator, boolean verbose, boolean debug, Logger logger,
         boolean checkDependencies, boolean generatePreprocessedFile,
         boolean generatePreprocessedFileFull)
      throws IOException
   {
      this.verbose = verbose;
      this.translator = translator;
      this.logger = logger;
      this.client = client;
      this.server = server;
      this.ties = ties;
      this.ami = ami;

      try {
         //always create the preprocessor, it will decide wether to preprocess the idl
         //file or not
         preprocessor = new PreprocessingManager(idlFile, uniqueId, dependsDir,
            includePath, addedToPrep, verbose, debug, logger, generatePreprocessedFile,
            generatePreprocessedFileFull, checkAll);

         //however, the checker is only created if the dependencies must be checked
         if (checkDependencies) {
            checker = new DependenciesChecker(idlFile, uniqueId, dependsDir, targetDir, 
               verbose, debug, logger);
         }
      }
      catch(IOException ioex) {
         logger.log(ioex.toString());
         throw ioex;
      }
   }



   /**
    * Returns the preprocessed file, it is null if generatePreprocessFile was null
    */
   public File getPreprocessedFile()
   {
      return preprocessor.getPreprocessedFile();
   }

   /**
    * Returns the sources (IDL files) from which this file depends
    */
   public Set getIDLSources()
   {
      return checker==null? Collections.EMPTY_SET : checker.getIDLSources();
   }

   /**
    * Blocks until the parse is completed, returning true if the dependencies
    * show that the source must be compiled
    */
   public boolean build(boolean throwExceptionOnError) throws Exception
   {
      PipedWriter output = null;
      PrintWriter writer = null;
      DependenciesChecker useChecker = checker;
      
      //if there us not need to check the dependencies, build with return 'false'
      if (useChecker==null) {
         resultReady = true;
         buildResult = false;
      }
      //otherwise, check the dependencies. The checker verifies if there is
      //a dependency file, and, if so, reads it to verify whether all the files
      //are up-to-date. If the dependency file exists, there is no need to parse
      //the IDL file, it is enough to verify the outcome of those dependencies.
      else if (!useChecker.generateDependencies()) {
         buildResult = useChecker.build();
         resultReady = true;
         useChecker = null;
      }
      else {
         //in the final case, the IDL file is read, and the dependencies generated
         output = new PipedWriter();
         writer = new PrintWriter(output);
         try {
            idlParser = new IDLParser(new PipedReader(output),
               new IDLToJavaMapping(this, client, server, ties, ami));
            useChecker.startDependenciesGeneration();
         }
         catch(IOException ioex) {
            logger.log(ioex.toString());
            writer.close();
            output.close();
            throw ioex;
         }
         start();
      }

      //independently of the dependencies checking, it must be checked the preprocessor
      //next call will start the preprocessor, which feeds the dependencies checker, and
      //is as well able to generate the preprocessed file
      Exception preprocessingError = preprocessor.process(writer, useChecker, useChecker!=null);
      if (preprocessingError==null) {
         while (!resultReady) {
            synchronized(this) {
               try { wait(); } catch(InterruptedException ex) {}
            }
         }
      }
      else {
         synchronized(this) {
            resultReady = true;
            buildResult = buildResult || output!=null;
            buildException = preprocessingError;
         }
         if  (output!=null) {
            try { output.close(); } catch (Exception ex) {}
            output=null;
         }
      }
      
      
      if (writer!=null) {
         writer.close();
         writer=null;
      }


      if (output!=null) {
         try {output.close();}catch(Exception ex){}
         output=null;
      }

      if ((throwExceptionOnError || preprocessingError!=null) && (buildException != null)) {
         throw buildException;
      }
      
      return buildResult;
   }




   /**
    * Thread public method.
    * It starts the IDLParser on a second thread.
    */
   public void run()
   {
      if (this == currentThread()) {
         try {
            idlParser.parse();
            checker.stopDependenciesGeneration(true);
            synchronized(this) {
               if (!resultReady) {
                  resultReady = true;
                  buildResult = checker.build();
                  notifyAll();
               }
            }
         }
         catch (ParseException ex) {
            // when a parse exception is found parsing the idl file,
            // a message is printed out, and the file is considered to
            // need a rebuild (which should fail as well, perhaps it
            // would be better just to exit on error)
            preprocessor.stopPreprocessing();
            checker.stopDependenciesGeneration(false);
            if (verbose) {
               logger.log(ex.toString());
            }
            synchronized(this) {
               if (!resultReady) {
                  resultReady = true;
                  buildResult=true;
                  buildException = ex;
                  notifyAll();
               }
            }
         }
      }
   }

   /**
    * IDLMapperUser public method, issued by the IDL parser when a new type
    * is found to be required.
    */
   public void fileNeeded(String pragmaPrefix, String file, String iface, boolean typeInInterface)
   {
      if (!resultReady) {
         String translatedFile = translator.translate(pragmaPrefix, file, iface, typeInInterface);
         checker.addTarget(translatedFile);
      }
   }

   private IDLParser idlParser;
   private boolean verbose, client, server, ties, ami;
   private DependenciesChecker checker;
   private Translator translator;
   private Logger logger;
   private PreprocessingManager preprocessor;

   private Exception buildException;
   private boolean buildResult, resultReady;
}


