/**
 * File: MacroHandler.java
 * Author: LuisM Pena
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.preprocessor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class has two purposes:
 * -It contains the defined macros. New macros can be added or old one removed.
 *   In addition, it allows to disable temporarily a macro to deal with
 *   recursive definitions.
 * -It maintains a stack with the current macros being expanded
 *  (class MacroReader).
 */
class MacroHandler
{

   /**
    * The constructor requires the PreprocessorController, to be able to retrieve the
    * file being processed or the parsing status (parsing line, for example)
    */
   public MacroHandler(PreprocessorController controller)
   {
      addMacro(new InternalMacroPaste());
      addMacro(new InternalMacroString());
      addMacro(new InternalMacroDefined(this));
      addMacro(new InternalMacroFile(controller));
      addMacro(new InternalMacroLine(controller));
      addMacro(new InternalMacroDate(controller, "__DATE__", "MMM dd yyyy"));
      addMacro(new InternalMacroDate(controller, "__TIMESTAMP__", "EEE MMM dd HH:mm:ss yyyy"));
      addMacro(new InternalMacroDate(controller, "__TIME__", "HH:mm:ss"));
   }

   /**
    * Returns the macro that existed before with that name (if any)
    */
   public Macro addMacro(Macro macro)
   {
      return (Macro) macros.put(macro.getName(), macro);
   }

   /**
    * Returns the macro that existed before with that name (if any)
    */
   public Macro removeMacro(String name)
   {
      return (Macro) macros.remove(name);
   }

   /**
    * Returns true if there is a macro gith that name
    */
   public boolean isMacro(String name)
   {
      return macros.containsKey(name);
   }

   /**
    * Returns the macro associated to the given name
    */
   public Macro getMacro(String name)
   {
      return (Macro) macros.get(name);
   }

   /**
    * Returns true if there is some macro being expanded
    */
   public boolean hasNotCompletedMacro()
   {
      return readingMacros.size() > 0;
   }

   /**
    * Returns the current macro being expanded (as a MacroReader)
    */
   public MacroReader getMacroReader()
   {
      int size = readingMacros.size();
      return size == 0 ? null : (MacroReader) readingMacros.get(size - 1);
   }

   /**
    * Adds (and creates) a MacroReader for the given Macro
    */
   public MacroReader addMacroReader(Macro macro)
   {
      MacroReader newMacro = new MacroReader(macro);
      readingMacros.add(newMacro);
      return newMacro;
   }

   /**
    * Informs that a macro reader has completed its task. It is removed from
    * the list, and the next one is returned.
    */
   public MacroReader endedMacroReader()
   {
      int size = readingMacros.size() - 1;
      readingMacros.remove(size);
      return size == 0 ? null : (MacroReader) readingMacros.get(size - 1);
   }

   Map macros = new HashMap();           // Macro
   List readingMacros = new ArrayList(); // MacroReader
}
