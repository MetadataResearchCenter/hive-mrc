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

import java.util.TreeMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.namespace.QName;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.SKOSServer;


/**
 * The SchemesResource class utilizes the SKOSServer
 * interface to provides JAX-RS based RESTful access to SKOSScheme objects
 * and their attributes and to query a scheme for its index of concepts.
 * 
 * The basic URI pattern for all methods in the class is "schemes"
 * 
 * Many, though not all, of the methods return "text/plain" output,
 * so this is the default Content-Type. Other methods return "application/xml"
 * and these use the @Produces annotation to override the default setting.
 * 
 * @author dcosta
 *
 */
@Produces("text/plain")
@Path("schemes")
public class SchemesResource {

  /**
   * Gets the list of scheme names supported by the HIVE-CORE system,
   * one scheme name per line.
   * 
   * @return         a list of scheme names, one per line. For example:
   *                   lter
   *                   nbii
   */
  @GET
  @Path("schemeNames")
  public String getSchemeNames() {
    StringBuffer schemeNames = new StringBuffer("");
    SKOSServer skosServer = ConfigurationListener.getSKOSServer();
    
    TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
    
    for (String s : skosSchemes.keySet()) {
      schemeNames.append(s);
      schemeNames.append("\n");
    }
    
    return schemeNames.toString().trim();
  }
  
  
  /**
   * Gets the lastDate value for a given scheme.
   * 
   * @param schemeName      the specified scheme name, e.g. "nbii"
   * @return                the lastDate timestamp value, for example: 
   *                            "Fri Jun 25 14:28:32 MDT 2010"
   */
  @GET
  @Path("{schemeName}/lastDate")
  public String getLastDate(@PathParam("schemeName") String schemeName) {
    String lastDate = "";
    SKOSServer skosServer = ConfigurationListener.getSKOSServer();
      
    if (schemeName != null) {
      TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
      
      for (String s : skosSchemes.keySet()) {
        if (s.equalsIgnoreCase(schemeName)) {
          SKOSScheme skosScheme = skosSchemes.get(s);
          if (skosScheme != null) {
            lastDate = skosScheme.getLastDate();
          }
        }
      }
    }
    
    return lastDate;
  }
    
  
  /**
   * Gets the name value for a given scheme. Essentially the
   * same name that is passed in is returned, but in its
   * case-sensitive form.
   * 
   * @param schemeName      the specified scheme, e.g. "nbii"
   * @return                the name value, for example: "NBII"
   */
  @GET
  @Path("{schemeName}/name")
  public String getName(@PathParam("schemeName") String schemeName) {
    String name = "";
    SKOSServer skosServer = ConfigurationListener.getSKOSServer();
      
    if (schemeName != null) {
      TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
      
      for (String s : skosSchemes.keySet()) {
        if (s.equalsIgnoreCase(schemeName)) {
          SKOSScheme skosScheme = skosSchemes.get(s);
          if (skosScheme != null) {
            name = skosScheme.getName();
          }
        }
      }
    }
     
    return name;
  }
    
  
  /**
   * Gets the longName value for a given scheme.
   * 
   * @param schemeName      the specified scheme name, e.g. "nbii"
   * @return                the longName value, for example:
   *                           "CSA/NBII Biocomplexity Thesaurus"
   */
  @GET
  @Path("{schemeName}/longName")
  public String getLongName(@PathParam("schemeName") String schemeName) {
    String longName = "";
    SKOSServer skosServer = ConfigurationListener.getSKOSServer();
      
    if (schemeName != null) {
      TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
      
      for (String s : skosSchemes.keySet()) {
        if (s.equalsIgnoreCase(schemeName)) {
          SKOSScheme skosScheme = skosSchemes.get(s);
          if (skosScheme != null) {
            longName = skosScheme.getLongName();
          }
        }
      }
    }
     
    return longName;
  }
    
  
  /**
   * Gets the number of concepts for a given scheme.
   * 
   * @param schemeName      the specified scheme name, e.g. "nbii"
   * @return                the number of concepts, e.g. "10000"
   */
  @GET
  @Path("{schemeName}/numberOfConcepts")
  public String getNumberOfConcepts(@PathParam("schemeName") String schemeName) {
    Integer numberOfConcepts = -1;
    SKOSServer skosServer = ConfigurationListener.getSKOSServer();
      
    if (schemeName != null) {
      TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
    
      for (String s : skosSchemes.keySet()) {
        if (s.equalsIgnoreCase(schemeName)) {
          SKOSScheme skosScheme = skosSchemes.get(s);
          if (skosScheme != null) {
            numberOfConcepts = skosScheme.getNumberOfConcepts();
          }
        }
      }
    }
     
    return numberOfConcepts.toString();
  }
    
    
  /**
   * Gets the number of relations for a given scheme.
   * 
   * @param schemeName      the specified scheme name, e.g. "nbii"
   * @return                the number of relations, e.g. "22356"
   */
  @GET
  @Path("{schemeName}/numberOfRelations")
  public String getNumberOfRelations(@PathParam("schemeName") String schemeName) {
    Integer numberOfRelations = -1;
    SKOSServer skosServer = ConfigurationListener.getSKOSServer();
      
    if (schemeName != null) {
      TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
      
      for (String s : skosSchemes.keySet()) {
        if (s.equalsIgnoreCase(schemeName)) {
          SKOSScheme skosScheme = skosSchemes.get(s);
          if (skosScheme != null) {
            numberOfRelations = skosScheme.getNumberOfRelations();
          }
        }
      }
    }
    
    return numberOfRelations.toString();
  }
    
  
  /**
   * Gets the schema URI value for a given scheme.
   * 
   * @param schemeName   the specified scheme name, e.g. "nbii"
   * @return             the schema URI value, e.g. "http://thesaurus.nbii.gov"
   */
  @GET
  @Path("{schemeName}/schemaURI")
  public String getSchemaURI(@PathParam("schemeName") String schemeName) {
    String schemaURI = "";
    SKOSServer skosServer = ConfigurationListener.getSKOSServer();
      
    if (schemeName != null) {
      TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
      
      for (String s : skosSchemes.keySet()) {
        if (s.equalsIgnoreCase(schemeName)) {
          SKOSScheme skosScheme = skosSchemes.get(s);
          if (skosScheme != null) {
            schemaURI = skosScheme.getSchemaURI();
          }
        }
      }
    }
     
    return schemaURI;
  }


  /**
   * Gets the alpha index for a given scheme, represented as an XML formatted
   * list of concepts.
   * 
   * @param schemeName      the specified scheme name, e.g. "nbii"
   * @return                an XML formatted list of concepts
   */
  @GET
  @Path("{schemeName}/alphaIndex")
  @Produces("application/xml")
  public String getAlphaIndex(@PathParam("schemeName") String schemeName) {
    String xmlString = "";
    SKOSServer skosServer = ConfigurationListener.getSKOSServer();
      
    if (schemeName != null) {
      TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
      
      for (String s : skosSchemes.keySet()) {
        if (s.equalsIgnoreCase(schemeName)) {
          SKOSScheme skosScheme = skosSchemes.get(s);
          if (skosScheme != null) {
            TreeMap<String, QName> alphaIndex = 
              skosScheme.getAlphaIndex();
            if (alphaIndex != null) {
              xmlString = 
                ConceptsResource.conceptTreeMapToXML(alphaIndex);
            }
          }
        }
      }
    }
     
    return xmlString;
  }
    

  /**
   * Gets the alpha index for a given scheme for all concepts that start
   * with the specified letter sequence.
   * 
   * @param schemeName      the specified scheme name, e.g. "nbii"
   * @param  startLetters   the specified start letter sequence
   * @return                an XML formatted list of concepts
   */
  @GET
  @Path("{schemeName}/subAlphaIndex/{startLetters}")
  @Produces("application/xml")
  public String getSubAlphaIndex(@PathParam("schemeName") String schemeName, 
                                 @PathParam("startLetters") String startLetters
                                ) {
    String xmlString = "";
    SKOSServer skosServer = ConfigurationListener.getSKOSServer();
      
    if (schemeName != null && startLetters != null) {
      TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
      
      for (String s : skosSchemes.keySet()) {
        if (s.equalsIgnoreCase(schemeName)) {
          SKOSScheme skosScheme = skosSchemes.get(s);
          if (skosScheme != null) {
            TreeMap<String, QName> subAlphaIndex = 
              skosScheme.getSubAlphaIndex(startLetters);
            if (subAlphaIndex != null) {
              xmlString = ConceptsResource.conceptTreeMapToXML(subAlphaIndex);
            }
          }
        }
      }
    }
     
    return xmlString;
  }
    

  /**
   * Gets the top concept index for a given scheme. (Top concepts are any
   * concepts in the scheme that do not have any parent concepts.)
   * 
   * @param schemeName      the specified scheme name, e.g. "nbii"
   * @return                an XML formatted list of top concepts
   */
  @GET
  @Path("{schemeName}/topConceptIndex")
  @Produces("application/xml")
  public String getTopConceptIndex(@PathParam("schemeName") String schemeName) {
    String xmlString = "";
    SKOSServer skosServer = ConfigurationListener.getSKOSServer();
      
    if (schemeName != null) {
      TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
      
      for (String s : skosSchemes.keySet()) {
        if (s.equalsIgnoreCase(schemeName)) {
          SKOSScheme skosScheme = skosSchemes.get(s);
          if (skosScheme != null) {
            TreeMap<String, QName> topConceptIndex = 
              skosScheme.getTopConceptIndex();
            if (topConceptIndex != null) {
              xmlString = 
                ConceptsResource.conceptTreeMapToXML(topConceptIndex);
            }
          }
        }
      }
    }
     
    return xmlString;
  }
    

  /**
   * Gets the sub-top-concept index for a given scheme for all top concepts 
   * that start with the specified letter sequence.
   * 
   * @param schemeName      the specified scheme name, e.g. "nbii"
   * @param  startLetters   the specified start letter sequence
   * @return                an XML formatted list of top concepts
   */
  @GET
  @Path("{schemeName}/subTopConceptIndex/{startLetters}")
  @Produces("application/xml")
  public String getSubTopConceptIndex(@PathParam("schemeName") String schemeName, 
                                 @PathParam("startLetters") String startLetters
                                ) {
    String xmlString = "";
    SKOSServer skosServer = ConfigurationListener.getSKOSServer();
      
    if (schemeName != null && startLetters != null) {
      TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
      
      for (String s : skosSchemes.keySet()) {
        if (s.equalsIgnoreCase(schemeName)) {
          SKOSScheme skosScheme = skosSchemes.get(s);
          if (skosScheme != null) {
            TreeMap<String, QName> topConceptIndex = 
              skosScheme.getSubTopConceptIndex(startLetters);
            if (topConceptIndex != null) {
              xmlString = 
                ConceptsResource.conceptTreeMapToXML(topConceptIndex);
            }
          }
        }
      }
    }
     
    return xmlString;
  }
    
}