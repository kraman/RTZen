/**
 * File: JacorbTranslator.java
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend;


import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;


/**
 * Translator for the Jacorb compiler
 * Following is the behaviour of the parameters -p (translated into
 *   the task's parameter package) and -i2jpackage (translated into translate)
 * 1- -p has just one attribute, the prefix to be added to every generated
 *    file. This prefix can be compound (like in modulA.moduleB). If it is
 *    specified several times, only the last one is taken.
 * 2- -i2jpackage requires two parameters: the module to modify and the package
 *    on which it is translated. The module cannot be compound, but the
 *    replacing package does can.
 * 3- i2jpackage does not modify interfaces or valuetypes themselves. That is,
 *    an interface called Iface produces the same files although a translation
 *    like -i2jpackage Iface:change is provided. Nevertheless, it does applies
 *    to types defined inside an interface. If the previous Iface interface
 *    defines a type, this type will generate on standard way a file called
 *    IfacePackage/type.idl
 *    Applying -i2jpackage Iface:change, the generated file would be
 *    change/type.idl (note that it has been specified Iface and not
 *    IfacePackage)
 * 4- If two i2jpackages are specified with the same module name, only the last
 *    one remains.
 * 5- Every applicable i2jpackage is used. This differs from JDK or Orbacus,
 *    where, as soon as a translation is performed, the rest is dismissed.
 * 6- The order of applicance is: first every i2jpackage specified, and then
 *    the package (if given).
 */
class JacorbTranslator extends Translator
{
   Map translates = new HashMap();
   List initialTranslates;
   String packageString, initialPackageString;

   public JacorbTranslator(List packages, List translates) throws BuildException
   {
      checkPackages(packages);
      checkTranslates(translates);
      initialTranslates = translates;
   }

   /**
    * Modifies the file to include the defined translations
    */
   public String translate(String pragmaPrefix, String file, String iface, boolean typeInInterface)
   {
      return applyPackages(applyTranslates(file, iface, typeInInterface));
   }

   /**
    * Modifies the commandLine to reflect the packages and translates founds.
    */
   public void modifyCommandline(Commandline command)
   {
      if (packageString != null) {
         command.createArgument().setValue("-p");
         command.createArgument().setValue(initialPackageString);
      }
      if (!translates.isEmpty()) {
         Iterator it = initialTranslates.iterator();
         while (it.hasNext()) {
            TranslateTask task = (TranslateTask) it.next();
            StringBuffer buffer = new StringBuffer();
            buffer.append(task.moduleName).append(':').append(task.packageName);
            command.createArgument().setValue("-i2jpackage");
            command.createArgument().setValue(buffer.toString());
         }
      }
   }

   /**
    * Verifies that all the packages contain a module, and no prefix or
    * auto. It also verifies the values for prefix, removing
    * any traling spaces if required
    */
   private void checkPackages(List packages) throws BuildException
   {
      Iterator it = packages.iterator();
      while (it.hasNext()) {
         PackageTask task = (PackageTask) it.next();
         if (task.module != null || task.auto) {
            throw new BuildException("Using compiler Jacorb, <package> does not accept the attribute auto or module");
         }
         if (task.prefix == null) {
            throw new BuildException("Using compiler Jacorb, every <package> must contain a prefix attribute");
         }
         initialPackageString = task.prefix;
         packageString = normalize(task.prefix, "prefix");
      }
   }

   /**
    * Verifies that all the translates contain a package and a module
    * It also verifies the values for module and package, removing
    * any traling spaces if required.
    * It stores the modules in the HashMap
    */
   private void checkTranslates(List translates) throws BuildException
   {
      Iterator it = translates.iterator();
      while (it.hasNext()) {
         TranslateTask task = (TranslateTask) it.next();
         if (task.moduleName == null || task.packageName == null) {
            throw new BuildException("Using compiler Orbacus, every <translate> must contain a module and package attribute");
         }
         String moduleName = getValidModule(task.moduleName);
         String packageName = normalize(task.packageName, "package");
         this.translates.put(moduleName, packageName);
      }
   }

   /**
    * Apply the package defined, just prefixing the file with it
    */
   private String applyPackages(String file)
   {
      return packageString == null ? file : packageString + File.separatorChar + file;
   }

   /**
    * Apply the translates defined, until it is found one that modifies the result
    * @param file the name of the string containing a specif class
    * @param typeInInterface must be set to true if the associated class
    *     is generated after a type belonging to an interface
    */
   private String applyTranslates(String file, String iface,
         boolean typeInInterface)
   {
      if (translates.isEmpty()) {
         return file;
      }
      int lastSlash = file.lastIndexOf(File.separatorChar);
      if (lastSlash == -1) {
         return file;
      }
      int saveLastSlash = lastSlash;
      if (typeInInterface) {
         lastSlash = file.lastIndexOf(File.separatorChar, lastSlash - 1);
      }
      StringBuffer ret = new StringBuffer();
      int first = 0;
      int slash = file.indexOf(File.separatorChar);
      while (slash <= lastSlash && slash != -1) {
         String module = file.substring(first, slash);
         String translate = (String) translates.get(module);
         if (translate == null) {
            ret.append(module);
         }
         else {
            ret.append(translate);
         }
         ret.append(File.separatorChar);
         first = slash + 1;
         slash = file.indexOf(File.separatorChar, first);
      }
      if (typeInInterface) {
         String translate = (String) translates.get(iface);
         if (translate != null) {
            ret.append(translate);
            first = saveLastSlash;
         }
      }
      ret.append(file.substring(first));
      return ret.toString();
   }
}
