/**
 * File: UniqueDependencyId.java
 * Author: LuisM Pena
 * Last update: 0.31, 16th July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend;


import java.io.File;
import org.apache.tools.ant.types.Commandline;


/**
 * Class used to generate uniqueIds for each file + dependencies
 * used on its generation
 */
public class UniqueDependencyId
{

   /**
    * The constructor requires the commandLine that will be applied
    * with or without the file on top of which is it applied, as the file
    * must be still provided to generate the final UniqueId
    */
   public UniqueDependencyId(Commandline commandLine, String preprocessMode)
   {
      initialNumber = generateId(commandLine.toString() + preprocessMode, 0);
   }

   /**
    * Generates an uniqueId, consisting on 1 to 4 hexadecimal letters
    */
   public String generateUniqueId(File file)
   {
      int id = generateId(file.toString(), initialNumber);
      return Integer.toHexString(id);
   }

   /**
    * Generates an id from the given string. It iterates over its
    * characters, and for each of them it generates a new number
    * It returns a number between 0 and 65535
    */
   private int generateId(String s, int initialNumber)
   {
      int len = s.length();
      int highNumber = initialNumber / 256;
      int lowNumber = initialNumber % 256;
      for (int i = 0; i < len; i += 2) {
         lowNumber = formule(lowNumber, s.charAt(i));
      }
      for (int i = 1; i < len; i += 2) {
         highNumber = formule(highNumber, s.charAt(i));
      }
      return highNumber * 256 + lowNumber;
   }

   private int formule(int number, int character)
   {
      return (((number << 1) ^ character) + (number & 128) / 128) & 255;
   }

   private int initialNumber;
}
