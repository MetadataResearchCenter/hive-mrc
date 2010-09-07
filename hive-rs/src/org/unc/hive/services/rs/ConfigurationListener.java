/********************************************************************
 * Copyright 2010 the University of New Mexico.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific 
 * language governing permissions and limitations under the License.
 ********************************************************************/

package org.unc.hive.services.rs;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.unc.ils.mrc.hive.api.SKOSSearcher;
import edu.unc.ils.mrc.hive.api.SKOSServer;
import edu.unc.ils.mrc.hive.api.SKOSTagger;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSServerImpl;


/**
 * The ConfigurationListener class initializes the LTER-HIVE
 * restful web application. The initialization code executes when
 * the web application context starts up. This class is a subclass of
 * ServletContextListener.
 * 
 * @author dcosta
 *
 */
public class ConfigurationListener implements ServletContextListener {

  /*
   * Class fields
   */
  
  private static final Logger logger = 
    Logger.getLogger(ConfigurationListener.class);
  
  private static SKOSSearcher skosSearcher = null;
  private static SKOSServer skosServer = null;
  private static SKOSTagger skosTagger = null;
  
  
  /*
   * Instance fields
   */

  
  /*
   * Class methods
   */
  
  /**
   * Getter for the skosSearcher class field.
   * @return   the skosSearcher class field
   */
  public static SKOSSearcher getSKOSSearcher() {
      return skosSearcher;
  }
  
  
  /**
   * Getter for the skosServer class field.
   * @return   the skosServer class field
   */
  public static SKOSServer getSKOSServer() {
    return skosServer;
  }


  /**
   * Getter for the skosTagger class field.
   * @return   the skosTagger class field
   */
  public static SKOSTagger getSKOSTagger() {
    return skosTagger;
  }


  /*
   * Instance methods
   */
  
  /**
   * This method can be used to execute code when the web application
   * shuts down.
   * 
  * @param  servletContextEvent     The ServletContextEvent object
   */
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
  }

  
  /**
   * Run initialization code when at web application start-up.
   * 
   * @param  servletContextEvent     The ServletContextEvent object
   */
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ServletContext servletContext = servletContextEvent.getServletContext();

    String CONFIG_DIR = servletContext.getInitParameter("CONFIG_DIR");
    String dirPath = servletContext.getRealPath(CONFIG_DIR);
    initialize(dirPath);
  }
  
  
  /**
   * Initialize the logger, the skosServer, and the skosSearcher when
   * starting up the web application.
   * 
   * @param dirPath      the directory path where configuration files are found
   */
  public void initialize(String dirPath) {
    // Initialize the properties file for log4j
    String log4jPropertiesPath = dirPath + "/log4j.properties";
    PropertyConfigurator.configureAndWatch(log4jPropertiesPath);
    
    String vocabulariesPath = dirPath + "/vocabularies";
    skosServer = new SKOSServerImpl(vocabulariesPath);
    
    if (skosServer != null) {
      skosSearcher = skosServer.getSKOSSearcher();
      logger.info("skosSearcher initialized");
      skosTagger = skosServer.getSKOSTagger();
      logger.info("skosTagger initialized");
    } 
  }

}
