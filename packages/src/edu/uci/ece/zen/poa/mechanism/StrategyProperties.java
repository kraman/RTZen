/* --------------------------------------------------------------------------*
 * $Id: StrategyProperties.java,v 1.1 2003/11/26 22:29:04 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa.mechanism;


/**
 * The class <code>StrategyProperties</code> contains all the properties
 * that are used to configure different aspect of ZEN Policy Strategies.
 * services.
 *
 * This class tries to read the properties out of a file called
 * strategy.properties, this file should be located either in the mechanism
 * directory.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S Krishna</a>
 * @version 1.0
 */

import java.util.Properties;


public class StrategyProperties {

    static {
        String strategyPropertyFile = "strategy.properties";

        try {
            String filePath = "." + System.getProperty("file.separator")
                    + strategyPropertyFile;
            java.io.FileInputStream propertyStream = new java.io.FileInputStream(new java.io.File(filePath));

            StrategyProperties.properties = new Properties();
            StrategyProperties.properties.load(propertyStream);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

   /**
    * Return propery value corresponding to the name
    * @param name
    * @return String property
    */
    public static final String getProperty(String name) {
        return StrategyProperties.properties.getProperty(name);
    }

   /**
    * Set property with corresponding name to the corresponding value
    * @param name
    * @param value
    */
    public static final void setProperty(String name, String value) {
        StrategyProperties.properties.setProperty(name, value);
    }

    private static Properties properties;
}
