/**
 * File: JDKTranslator.java
 * Last update: 0.33, 22nd September 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
**/

package idldepend;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;


/**
 * Translator for the JDK.
 * Following is the behaviour of the parameters -pkgPrefix (translated into
 *   the task's parameter package) and -pkgTranslate (translated into translate)
 * 1- pkgPrefix is applied before any pkgTranslate. And pkgTranslate can
 *    modify the change done by pkgPrefix
 * 2- pkgPrefix requires always two parameters: <t>, called in the task
 *    'module' and <prefix>, called 'prefix'. 'module' cannot be complex,
 *    that is, it cannot include several modules, like in "modA.modB", but
 *    prefix can be.
 * 3- The result of applying pkgPrefix is to add the given prefix to any
 *    type whose top level module is 'module'. It also adds the prefix to
 *    any interface or valuetype defined at top level! This means that an
 *    interface called Iface, where -pkgPrefix Iface Add is applied, would
 *    produce files like Add/Iface.java, Add/IfaceHelper.java,
 *    Add/IfacePackage/..., etc
 * 4- pkgPrefix does not modify partial names. If a module is called 'moduleA'
 *    and the change must be done over 'mod', 'moduleA' is not modified. This
 *    behaviour applies the same for -pkgTranslate
 * 5- if two -pkgPrefix are specified, with the same 'module', it is the second
 *    one that stays. Two different -pkgPrefix are not applied on the same
 *    type.
 * 6- pkgTranslate also requires two parameters, here called 'module' and
 *    'package'. The result is that any type whose module structure STARTS with
 *    'module' is modified into 'package'. In this case, 'module' can be complex,
 *    the same as 'package'
 * 7- pkgTranslate does not affect to the names of interfaces or valuetypes,
 *    that is, an interface called Iface does not modify its name when
 *    -pkgTranslate Iface Mod is applied.
 * 8- If two pkgTranslate are defined with the same 'module', only the latest
 *    specification is used.
 * 9- When several pkgTranslates are specified, they are applied secuentially,
 *    in the specification order. But once that a pkgTranslate has modified
 *    a type, no more translations are applied. HOWEVER (bug solved on v0.23),
 *    if two translates are defined over two modules, one of which include the
 *    other, it is applied the more suitable first!
 */
class JDKTranslator extends Translator
{
   List packages, translates, inputPackages, inputTranslates;

   public JDKTranslator(List inputPackages, List inputTranslates) throws BuildException
   {
      this.inputPackages = inputPackages;
      this.inputTranslates = inputTranslates;
      this.packages = new ArrayList();
      this.translates = new ArrayList();

      checkPackages(inputPackages, packages);
      checkTranslates(inputTranslates, translates);
   }

   /**
    * Modifies the file to include the defined translations
    */
   public String translate(String pragmaPrefix, String file, String iface, boolean typeInInterface)
   {
      return applyTranslates(applyPackages(file, iface, typeInInterface));
   }

   /**
    * Modifies the commandLine to reflect the packages and translates founds.
    */
   public void modifyCommandline(Commandline command)
   {
      Iterator it = inputPackages.iterator();
      while (it.hasNext()) {
         PackageTask task = (PackageTask) it.next();
         command.createArgument().setValue("-pkgPrefix");
         command.createArgument().setValue(task.module);
         command.createArgument().setValue(task.prefix);
      }
      if (!translates.isEmpty()) {
         it = inputTranslates.iterator();
         while (it.hasNext()) {
            TranslateTask task = (TranslateTask) it.next();
            command.createArgument().setValue("-pkgTranslate");
            command.createArgument().setValue(task.moduleName);
            command.createArgument().setValue(task.packageName);
         }
      }
   }

   /**
    * Verifies that all the packages contain a prefix and a module, and
    * no auto. It also verifies the values for module and prefix, removing
    * any traling spaces if required
    * @param packages the input packages
    * @param toUse a list to include the packages to be used on later translations
    */
   private void checkPackages(List packages, List toUse) throws BuildException
   {
      Iterator it = packages.iterator();
      while (it.hasNext()) {
         PackageTask task = (PackageTask) it.next();
         if (task.module == null) {
            throw new BuildException("Using compiler JDK, every <package> must contain a module attribute");
         }
         if (task.prefix == null) {
            throw new BuildException("Using compiler JDK, every <package> must contain a prefix attribute");
         }
         if (task.auto) {
            throw new BuildException("Using compiler JDK, <package> does not accept the attribute auto");
         }
         String module = getValidModule(task.module);
         String prefix = normalize(task.prefix, "prefix");
         toUse.add(new PackageTask(module, prefix + File.separatorChar, false));
      }
   }

   /**
    * Verifies that all the translates contain a package and a module
    * It also verifies the values for module and package, removing
    * any trailing spaces if required.
    * If there are two translates with the same moduleName, only the second one is kept.
    * If two translates are not the same but apply to the same module, the larger one is
    * kept first, as it will be more suitable
    * @param translates the input translates
    * @param toUse a list to include the packages to be used on later translations
    */
   private void checkTranslates(List translates, List toUse) throws BuildException
   {  
      Iterator it = translates.iterator();
      while (it.hasNext()) {
         TranslateTask task = (TranslateTask) it.next();
         if (task.moduleName == null || task.packageName == null) {
            throw new BuildException("Using compiler JDK, every <translate> must contain a module and package attribute");
         }
         String moduleName = normalize(task.moduleName, "module") + File.separatorChar;
         String packageName = normalize(task.packageName, "package") + File.separatorChar;
         ListIterator it2 = toUse.listIterator();
         while (true) {
            if (it2.hasNext()) {
               TranslateTask existingTask = (TranslateTask) it2.next();
               String itsModule = existingTask.getModule();       
               if (moduleName.startsWith(itsModule)) {
                  if (moduleName.length() == itsModule.length()) { // same moduleName
                     existingTask.setPackage(packageName);
                  }
                  else { // more suitable, put it before
                     toUse.add(it2.previousIndex(), new TranslateTask(moduleName, packageName));            
                  }                       
                  break;
               }
            }
            else {
               toUse.add(new TranslateTask(moduleName, packageName));
               break;
            }
         }
      }
   }

   /**
    * Apply the packages defined, until it is found one that modifies the result
    * In JDK, this means to find if the starting of the file matches any of the
    * packages. The matching has to be complete for a module (it does not match
    * part of the name of a module). The exceptions are types inside interfaces.
    * Here, it is enough to match up to 'Package'
    */
   private String applyPackages(String file, String iface, boolean typeInInterface)
   {
      // note that the search is done from the end: if two packages are defined
      // for the same module, the last one is the valid
      ListIterator it = packages.listIterator(packages.size());
      while (it.hasPrevious()) {
         PackageTask pack = (PackageTask) it.previous();
         boolean ok = false;
         if (file.startsWith(pack.module)) {
            int len = pack.module.length();
            ok = (file.length() > len) && (file.charAt(len) == File.separatorChar);
         }
         if (!ok && iface != null && pack.module.equals(iface)) {
            // and the interface has to be top level. If it's a type inside
            // an interface, there can be just one level before
            int idx = file.indexOf(File.separatorChar);
            if (typeInInterface) {
               // not needed to check that file starts with iface + "Package/"
               ok = idx == file.lastIndexOf(File.separatorChar);
            }
            else {
               ok = idx == -1;
            }
         }
         if (ok) {
            return pack.prefix + file;
         }
      }
      return file;
   }

   /**
    * Apply the translates defined, until it is found one that modifies the result
    * In JDK, this means to find if the starting of the file matches any of the
    * packages. The matching has to be complete for a module (it does not match
    * part of the name of a module), and this is the reason why the packages were
    * internally stored with a trailing slash
    */
   private String applyTranslates(String file)
   {
      Iterator it = translates.iterator();
      while (it.hasNext()) {
         TranslateTask translate = (TranslateTask) it.next();
         if (file.startsWith(translate.moduleName)) {
            return translate.packageName + file.substring(translate.moduleName.length());
         }
      }
      return file;
   }
}

