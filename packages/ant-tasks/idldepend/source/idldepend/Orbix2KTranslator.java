/**
 * File: Orbix2KTranslator.java
 * Author: Brian Wallis
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;


/**
 * Translator for Orbix 2000
 * Following is the behaviour of the parameter -P translated into the task's
 *    attribute 'package'
 * 1- -Ppkg requires just a prefix. It needs a 'package' task where only
 *    the prefix has been specified. Its effect is to prepend this prefix to
 *    every file that must be created. Prefix can be compund, like in
 *    moduleA.moduleB
 * 2- -Pmod-pkg requires two parameters, the module, that cannot be
 *    compound, and the prefix, that can be compound. It prepends any file
 *    whose module is 'module' with the prefix specified. As happens with
 *    --auto-package, it does not apply to top level types (without module)
 *    but it does over types defined in interfaces.
 * 3- If two -Pmod=pkg are specified for the same 'module', the first
 *    is discarded.
 */
class Orbix2KTranslator extends Translator
{
   List packages, inputPackages;
   String genericPackage, genericPackageNormalized;
   String givenPrefix; // the prefix given in the last file
   String convertedPrefix; // that prefix, converted to file format
   String convertedModule; // the prefix, once that the --prefix-packages is applied

   // orbix2k does not accept translates
   public Orbix2KTranslator(List packages, List translates) throws BuildException
   {
      this.inputPackages = packages;
      this.packages = new ArrayList();
      checkPackages(this.inputPackages, this.packages);
      if (!translates.isEmpty()) {
         throw new BuildException("Using compiler Orbix2K, <translate> cannot be used");
      }
   }

   /**
    * Modifies the file to include the defined translations
    */
   public String translate(String pragmaPrefix, String file, String iface, boolean typeInInterface)
   {
      handlePrefix(pragmaPrefix);
      return applyPackages(file, iface, typeInInterface);
   }

   /**
    * Modifies the commandLine to reflect the packages and translates founds.
    */
   public void modifyCommandline(Commandline command)
   {
      //first, we add arguments for the package translation
      if (genericPackage != null) {
         command.createArgument().setValue("-P"+genericPackage);
      }
      if (!packages.isEmpty()) {
         StringBuffer buffer = new StringBuffer("-P"); // length=2;
         Iterator it = inputPackages.iterator();
         while (it.hasNext()) {
            PackageTask task = (PackageTask) it.next();
            buffer.append(task.module).append('=').append(task.prefix);
            command.createArgument().setValue(buffer.toString());
            buffer.setLength(2);
         }
      }
      // It must be noted that arguments starting by -P, -O, -G, -M, -J, -V, -F
      // are to be included after -jpoa or -jbase, separated by ':'
      // Then, those arguments must be removed from the command line and added
      // properly.
      String args[] = command.getArguments();
      StringBuffer onJBase = new StringBuffer("-jbase");
      StringBuffer onJPoa = new StringBuffer("-jpoa");
      ArrayList otherArgs = new ArrayList();
      command.clearArgs();

      boolean jbase = false;
      boolean jpoa = false;
      for (int i = 0; i < args.length; ++i) {
         if (args[i].startsWith("-P") || args[i].startsWith("-O") || args[i].startsWith("-G")
               || args[i].startsWith("-M") || args[i].startsWith("-J")
               || args[i].startsWith("-V") || args[i].startsWith("-F")) {
            onJBase.append(':').append(args[i]);
            onJPoa.append(':').append(args[i]);
         }
         else if (args[i].startsWith("-jbase")) {
            jbase = true;
            onJBase.append(args[i].substring(6));
         }
         else if (args[i].startsWith("-jpoa")) {
            jpoa = true;
            onJBase.append(args[i].substring(5));
         }
         else {
            otherArgs.add(args[i]);
         }
      }

      // if jbase or jpoa, add them the first. Then, add the otherArgs
      // Add the base arguments first
      if (jbase) {
         command.createArgument().setValue(onJBase.toString());
      }
      // and add -jpoa if it was present.
      if (jpoa) {
         command.createArgument().setValue(onJPoa.toString());
      }
      command.addArguments((String[]) otherArgs.toArray(new String[otherArgs.size()]));
   }

   /**
    * Verifies the values for module and prefix, removing
    * any traling spaces if required.
    * The list of inputPackages is just modified to remove any non needed
    * package. The modifications on the packages containing module and
    * prefix is done over the list called 'toUse'. In addition, this list
    * 'toUse' is created on inversal order.
    */
   private void checkPackages(List inputPackages, List toUse) throws BuildException
   {
      Iterator it = inputPackages.iterator();
      while (it.hasNext()) {
         PackageTask task = (PackageTask) it.next();
 
         if (task.prefix == null) {
            throw new BuildException("Using compiler Orbix2K, packages must contain a prefix attribute");
         }
         if (task.auto) {
            throw new BuildException("Using compiler Orbix2K, <package> does not accept the attribute auto");
         }
         if (task.module == null) {
            genericPackage = task.prefix;
            it.remove();
         }
         else {
            String module = getValidModule(task.module);
            String prefix = normalize(task.prefix, "prefix") + File.separatorChar;
            toUse.add(0, new PackageTask(module, prefix, false)); // added in inverse order!!
         }
      }
 
      if (genericPackage != null) {
         genericPackageNormalized = normalize(genericPackage, "prefix") + File.separatorChar;
      }
   }
 
   /**
    * Apply the packages defined, until it is found one that modifies the result
    * @param file the name of the string containing a specif class
    * @param typeInInterface must be set to true if the associated class
    *     is generated after a type belonging to an interface
    */
   private String applyPackages(String file, String iface, boolean typeInInterface)
   {
      int slash = file.indexOf(File.separatorChar);
      int lastSlash = file.lastIndexOf(File.separatorChar);
 
      if (convertedPrefix == null) {
         Iterator it = packages.iterator();
         while (it.hasNext()) {
            PackageTask pack = (PackageTask) it.next();
            boolean ok = false;
            if (file.startsWith(pack.module)) {
               int len = pack.module.length();
               ok = (file.length() > len) && (file.charAt(len) == File.separatorChar);
            }
            if (!ok && iface != null && typeInInterface && pack.module.equals(iface)) {
               ok = slash == lastSlash;
            }
            if (ok) {
               return pack.prefix + file;
            }
         }
      }
      else if (convertedModule != null) {
         return convertedModule + file;
      }
 
      if (genericPackage != null) {
         return genericPackageNormalized + file;
      }
 
      if ((slash == -1) || ((slash == lastSlash) && typeInInterface)) {
         return "defaultpkg" + File.separatorChar + file;
      }
      return file;
   }
 
   /**
    * Handles the given pragmaPrefix, converting it to file format
    * (reversed, modifying '.' by '/' pr '\'. It also verifies
    * wether the prefix is compund (containing therefore '.')
    */
   private void handlePrefix(String prefix)
   {
      if (prefix != givenPrefix) {
         givenPrefix = prefix;
         if (prefix == null) {
            convertedPrefix = null;
         }
         else {
            convertedModule = null;
            int dot = prefix.indexOf('.');
            if (dot == -1) {
               // obtain from the packages the one that applies
               Iterator it = packages.iterator();
               while (convertedModule == null && it.hasNext()) {
                  PackageTask pack = (PackageTask) it.next();
                  if (pack.module.equals(prefix)) {
                     convertedModule = pack.prefix;
                  }
               }
               convertedPrefix = prefix;
            }
            else {
               StringBuffer temp = new StringBuffer(prefix.substring(0, dot));
               int length = prefix.length();
               while (++dot < length) {
                  temp.insert(0, File.separatorChar);
                  int newdot = prefix.indexOf('.', dot);
                  if (newdot == -1) {
                     newdot = length;
                  }
                  temp.insert(0, prefix.substring(dot, newdot));
                  dot = newdot;
               }
               convertedPrefix = temp.toString();
            }
         }
      }
   }
}
