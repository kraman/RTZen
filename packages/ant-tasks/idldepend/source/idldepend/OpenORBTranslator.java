/**
 * File: OpenORBTranslator.java
 * Last update: 0.33, 22nd September 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend;


import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;


/**
 * Translator for OpenORB
 * Following is the behaviour of the parameters -package, -noprefix
 *    all of them translated into the task's attribute 'package'
 * 1- -package requires just a prefix. It needs a 'package' task where only
 *    the prefix has been specified. Its effect is to prepend this prefix to
 *    every file that must be created. Prefix can be compund, like in
 *    moduleA.moduleB
 * 2- -noprefix requires no parameters. It is specified always, unless
 *    a package auto is found!
 * 4- There is no interactions between -noprefix and package
 * 5- If more than one -package is specified, it is used the latest one.
 * 6- It is first applied the pragma prefix (unless -noprefix is specified)
 *    and then the package.
 */
class OpenORBTranslator extends Translator
{
   boolean autoPackage;
   String genericPackage, genericPackageNormalized;
   String givenPrefix; // the prefix given in the last file
   String convertedPrefix; // that prefix, converted to file format

   // orbacus does not accept translates
   public OpenORBTranslator(List packages, List translates) throws BuildException
   {
      if (!translates.isEmpty()) {
         throw new BuildException("Using compiler OpenORB, <translate> cannot be used");
      }
      checkPackages(packages);
   }

   /**
    * Modifies the file to include the defined translations
    */
   public String translate(String pragmaPrefix, String file, String iface, boolean typeInInterface)
   {
      if (autoPackage) {
         handlePrefix(pragmaPrefix);
      }
      return applyPackages(file, iface, typeInInterface);
   }

   /**
    * Modifies the commandLine to reflect the packages and translates founds.
    */
   public void modifyCommandline(Commandline command)
   {
      if (!autoPackage) {
         command.createArgument().setValue("-noprefix");
      }
      if (genericPackage != null) {
         command.createArgument().setValue("-package");
         command.createArgument().setValue(genericPackage);
      }
   }

   /**
    * Verifies that all the packages contain an auto or/and a prefix.
    * Is several prefixes are defines, is used just the last one
    */
   private void checkPackages(List packages) throws BuildException
   {
      Iterator it = packages.iterator();
      while (it.hasNext()) {
         PackageTask task = (PackageTask) it.next();
         if (task.module != null) {
            throw new BuildException
                  ("Using compiler OpenORB, a <package> cannot include a module");
         }
         if (task.auto) {
            autoPackage = true;
         }
         if (task.prefix != null) {
            genericPackage = task.prefix;
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
      StringBuffer temp = null;
      if (genericPackageNormalized != null) {
         temp = new StringBuffer(genericPackageNormalized);
      }
      if (convertedPrefix != null) {
         if (temp == null) {
            temp = new StringBuffer(convertedPrefix);
         }
         else {
            temp.append(convertedPrefix);
         }
      }
      if (temp == null) {
         return file;
      }
      temp.append(file);
      return temp.toString();
   }

   /**
    * Handles the given pragmaPrefix, converting it to file format
    * (reversed, modifying '.' by '/' pr '\'.
    */
   private void handlePrefix(String prefix)
   {
      if (prefix != givenPrefix) {
         givenPrefix = prefix;
         if (prefix == null) {
            convertedPrefix = null;
         }
         else {
            int lastDot = 0;
            int dot = prefix.indexOf('.');
            StringBuffer temp = new StringBuffer(File.separator);
            while (dot != -1) {
               temp.insert(0, prefix.substring(lastDot, dot));
               temp.insert(0, File.separatorChar);
               lastDot = dot + 1;
               dot = prefix.indexOf('.', lastDot);
            }
            temp.insert(0, prefix.substring(lastDot));
            convertedPrefix = temp.toString();
         }
      }
   }
}
