/**
 * File: Translator.java
 * Last update: 0.40, 30th September 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend;


import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;


/**
 * Common root to the different translators, used to handle
 * the attributes <package> and <translate>
 */
abstract class Translator
{

   /**
    * Modifies the commandLine to reflext the packages and translates founds
    */
   abstract public void modifyCommandline(Commandline command);

   /**
    * Modifies the file to include the defined translations
    * @param pragmaPrefix is the content of the current #pragma prefix
    * @param file is the name of the file that must be created. In case of
    *    include a directory structure, the separator used is the specific
    *    to the used system
    * @param iface is the name of the interface or the module that originates
    *    the need of this file. If the file doesn't come out from an interface
    *    or module, this parameter is null.
    * @param typeInInterface is set to true when the file is needed after a
    *    type defined inside a module or interface, whose name is given on
    *    the parameter iface
    */
   abstract public String translate(String pragmaPrefix, String file, String iface, boolean typeInInterface);

   /**
    * Verifies that the fiven string only contains valid Modules (not '.',
    * only valid characters). Spaces at the beginning and at the end are removed),
    */
   protected String getValidModule(String s)
   {
      String ret = s.trim();
      int end = ret.length();
      boolean okay = end > 0 && Character.isJavaIdentifierStart(ret.charAt(0));
      for (int start = 1; okay && start < end; start++) {
         okay = Character.isJavaIdentifierPart(ret.charAt(start));
      }
      if (!okay) {
         throw new BuildException(s + " is not a valid value for a module");
      }
      return ret;
   }

   /**
    * Removes spaces and dots from the beginning and end of the given string. Throws
    * an exception if it's empty or it is not valid.
    * It replaces any '.' by the specific file separator.
    */
   protected String normalize(String s, String attribute)
   {
      int start = 0, end = s.length();
      while (start < end) {
         char c = s.charAt(start);
         if (c == '.' || Character.isWhitespace(c)) {
            start++;
         }
         else {
            break;
         }
      }
      while (end > start) {
         char c = s.charAt(end - 1);
         if (c == '.' || Character.isWhitespace(c)) {
            end--;
         }
         else {
            break;
         }
      }
      boolean okay = end > start;
      boolean starting = true;
      for (int i = start; okay && i < end; i++) {
         char c = s.charAt(i);
         if (starting) {
            starting = false;
            okay = Character.isJavaIdentifierStart(c);
         }
         else if (c == '.') {
            starting = true;
         }
         else {
            okay = Character.isJavaIdentifierPart(c);
         }
      }
      if (!okay) {
         throw new BuildException(s + " is not a valid value for the attribute " + attribute);
      }
      return s.substring(start, end).replace('.', File.separatorChar);
   }

   /**
    * Class used to specify the 'package' parameters
    */
   public static class PackageTask extends Task
   {
      String module, prefix;
      boolean auto;
      public PackageTask()
      {}

      public PackageTask(String module, String prefix, boolean auto)
      {
         this.module = module;
         this.prefix = prefix;
         this.auto = auto;
      }

      public void setModule(String module)
      {
         this.module = module;
      }

      public void setPrefix(String prefix)
      {
         this.prefix = prefix;
      }

      public void setAuto(boolean set)
      {
         auto = set;
      }
   }


   /**
    * Class used to specify the 'package' parameters
    */
   public static class TranslateTask extends Task
   {
      String moduleName, packageName;
      public TranslateTask()
      {}

      public TranslateTask(String moduleName, String packageName)
      {
         this.moduleName = moduleName;
         this.packageName = packageName;
      }

      public void setModule(String moduleName)
      {
         this.moduleName = moduleName;
      }

      public void setPackage(String packageName)
      {
         this.packageName = packageName;
      }

      public String getModule()
      {
         return moduleName;
      }
   }

}
