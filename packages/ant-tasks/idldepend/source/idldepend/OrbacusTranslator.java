/**
 * File: OrbacusTranslator.java
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
 * Translator for Orbacus
 * Following is the behaviour of the parameters --package, --auto-package
 *    and --prefix-package, all of them translated into the task's
 *    attribute 'package'
 * 1- --package requires just a prefix. It needs a 'package' task where only
 *    the prefix has been specified. Its effect is to prepend this prefix to
 *    every file that must be created. Prefix can be compund, like in
 *    moduleA.moduleB
 * 2- --auto-package requires no parameters. It is specified using a 'package'
 *    task containing just one attribute, auto, set to "true". Its efect is
 *    to prepend every generated file with its top level module. For example,
 *    a file being moduleA/type.java is converted into moduleA/moduleA/type.java
 * 3- --autoPackage does not apply to types defined outside modules (i.e,
 *    top-level-defined). File type.java is not converted into type/type.java
 *    Nevertheless, it applies to types defined inside interfaces or valuetypes.
 *    The file IfacePackage/type.java is transformed into
 *    Iface/IfacePackage/type.java (note that in this case the autoPackage is
 *    the name of the interface, not the name of the directory, which includes
 *    'Package'.
 * 4- If both, --auto-package and --package are specified, --auto-package is
 *    discarded.
 * 5- If more than one --package is specified, it is used the latest one.
 * 6- --prefix-package requires two parameters, the module, that cannot be
 *    compound, and the prefix, that can be compound. It prepends any file
 *    whose module is 'module' with the prefix specified. As happens with
 *    --auto-package, it does not apply to top level types (without module)
 *    but it does over types defined in interfaces.
 *    For example, --prefix=package Iface PLUS over IfacePackage/type.java
 *    will produce PLUS/IfacePackage/type.java
 * 7- If two --prefix-package are specified for the same 'module', the first
 *    is discarded.
 * 8- It is first applied any --prefix-package. If none applies, it's then
 *    considered any --package or --auto-package specification
 * 9- Unless --package is specified, types defined top-level (without modules)
 *    are included into a directory called defaultpkg
 * 10-This behaviour is slightly different if a #pragma prefix is found on the
 *    source. In this case, it must be considered part of the prefix
 *    A- --prefix-package is only valid if the pragma prefix is simple. In
 *        this case, if there is a prefix that matches the #pragma prefix,
 *        the result is the original name preceeded by the given #pragma prefix
 *    B- if the previous case does not apply, auto-package is only processed
 *        if there is no --package (like normal).
 *    C- if --auto-package applies, the result is to prepend any file by
 *        the inverse of the prefix. For example "luicpend.com" will
 *        produce files com/luicpend/....
 *    D- Attention: if there is a new file, any previous #pragma is lost
 */
class OrbacusTranslator extends Translator
{
   List packages, inputPackages;
   boolean autoPackage;
   String genericPackage, genericPackageNormalized;
   String givenPrefix; // the prefix given in the last file
   String convertedPrefix; // that prefix, converted to file format
   String convertedModule; // the prefix, once that the --prefix-packages is applied

   // orbacus does not accept translates
   public OrbacusTranslator(List packages, List translates) throws BuildException
   {
      this.inputPackages = packages;
      this.packages = new ArrayList();

      checkPackages(this.inputPackages, this.packages);
      if (!translates.isEmpty()) {
         throw new BuildException("Using compiler Orbacus, <translate> cannot be used");
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
      if (autoPackage) {
         command.createArgument().setValue("--auto-package");  // v0.21
      }
      if (genericPackage != null) {
         command.createArgument().setValue("--package"); // v0.21
         command.createArgument().setValue(genericPackage); // v0.21
      }
      if (!packages.isEmpty()) {
         Iterator it = inputPackages.iterator();
         while (it.hasNext()) {
            PackageTask task = (PackageTask) it.next();
            command.createArgument().setValue("--prefix-package");   // v0.21
            command.createArgument().setValue(task.module); // v0.21
            command.createArgument().setValue(task.prefix); // v0.21
         }
      }
   }

   /**
    * Verifies that all the packages contain or an auto (and nothing else)
    * or at least a prefix. If there is an auto package, it is removed,
    * and a flag is set. The same happens with generic packages, and
    * the variable genericPackage is modified. Note that it cannot
    * happen that autoPackage is true with genericPackage containing
    * a string (not being null).
    * On those packages containing both a module and a prefix, It also
    * verifies the values for module and prefix, removing
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
         if (task.auto) {
            autoPackage = true;
            if (task.module != null || task.prefix != null) {
               throw new BuildException
                     ("Using compiler Orbacus, a <package> defined with auto=\"true\" cannot include a module or prefix");
            }
            it.remove();
         }
         else {
            if (task.prefix == null) {
               throw new BuildException("Using compiler Orbacus, non auto packages must contain a prefix attribute");
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
      }
      if (genericPackage != null) {
         genericPackageNormalized = normalize(genericPackage, "prefix") + File.separatorChar;
         autoPackage = false;
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

      if (autoPackage) {
         if (convertedPrefix == null) {
            if (slash != -1) {
               if (!typeInInterface || (slash != lastSlash)) {
                  return file.substring(0, slash + 1) + file;
               }
               else {
                  return iface + File.separatorChar + file;
               }
            }
         }
         else {
            return convertedPrefix + File.separatorChar + file;
         }
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

