/**
 * File: Logger.java
 * Last update: 0.33, 22nd September 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend;


/**
 * Interface to cover the logging (without depending on the
 *   ant's 'task', which has that functionality built in
 */
public interface Logger
{
   public void log(String msg);
}
