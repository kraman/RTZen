/**
 * File: IDLMapperUser.java
 * Author: LuisM Pena
 * Last update: 0.30, 1st July 2003
 * Please visit http://grasia.fdi.ucm.es/~luismi/idldepend for
 *   updates, download, license and copyright information
 **/

package idldepend.idl;


/**
 * Callback from the IDLParser informing on the files that
 * must been created
 */
public interface IDLMapperUser
{

   /**
    * Called when the parser founds a new type, requiring an associated file
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
   public void fileNeeded(String pragmaPrefix, String file, String iface, boolean typeInInterface);
}
