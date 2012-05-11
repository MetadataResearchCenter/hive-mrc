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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.SKOSSearcher;
import edu.unc.ils.mrc.hive.api.SKOSServer;
import edu.unc.ils.mrc.hive.api.SKOSTagger;

/**
 * The ConceptsResource class utilizes the SKOSConcept, SKOSSearcher,
 * and SKOSTagger interfaces to provides JAX-RS based RESTful access 
 * to SKOSConcept objects and their attributes and to query a scheme 
 * to find matching concepts.
 * 
 * The basic URI pattern for all methods in the class is
 * 'schemes/{schemeName}/concepts'.
 * 
 * Many, though not all, of the methods return "application/xml" output,
 * so this is the default Content-Type. Other methods return "text/plain"
 * and these use the @Produces annotation to override the default setting.
 * 
 * @author dcosta
 *
 */
@Produces("application/xml")
@Path("schemes/{schemeName}/concepts")
public class ConceptsResource {
  
  /*
   * Class fields
   */
  private static final String XML_DECLARATION = 
    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
  
  
  /*
   * Instance fields
   */
  
  private Logger logger = Logger.getLogger(ConceptsResource.class);

  
  /*
   * Class methods
   */
  
  /**
   * Utility method to transform a list of SKOSConcept objects into XML.
   * 
   * @param   skosConcepts   the list of SKOSConcept objects
   * @return  xmlString      the XML string
   */
  public static String conceptListToXML(List<SKOSConcept> skosConcepts) {
    String xmlString = "";
    StringBuffer xmlStringBuffer = 
      new StringBuffer(XML_DECLARATION);
    
    xmlStringBuffer.append("<SKOSConcepts>\n");
    
    for (SKOSConcept skosConcept : skosConcepts) {
      if (skosConcept != null) {
        String skosFormat = skosConcept.getSKOSFormat();
        xmlStringBuffer.append("<SKOSConcept>\n");
        xmlStringBuffer.append(skosFormat + "\n");
        xmlStringBuffer.append("</SKOSConcept>\n");
      }
    }
    
    xmlStringBuffer.append("</SKOSConcepts>");
    xmlString = xmlStringBuffer.toString();
    return xmlString;
  }
  

  /**
   * Utility method to transform a map of String keys and QName values
   * into XML.
   * 
   * @param    treeMap    the map of String keys and QName values
   * @return   xmlString  the XML string
   */
  public static String conceptTreeMapToXML(TreeMap<String, QName> treeMap) {
    String xmlString = "";
    StringBuffer xmlStringBuffer = new StringBuffer(XML_DECLARATION);
    
    xmlStringBuffer.append("<concepts>\n");

    for (String s : treeMap.keySet()) {
      QName qName = treeMap.get(s);
      if (qName != null) {
        xmlStringBuffer.append("<concept>\n");
        xmlStringBuffer.append("  <prefLabel>" + s + "</prefLabel>\n");
        xmlStringBuffer.append("  <QName>\n");
        xmlStringBuffer.append("    <namespaceURI>" + qName.getNamespaceURI() + "</namespaceURI>\n");
        xmlStringBuffer.append("    <prefix>" + qName.getPrefix() + "</prefix>\n");
        xmlStringBuffer.append("    <localPart>" + qName.getLocalPart() + "</localPart>\n");
        xmlStringBuffer.append("    <string>" + qName.toString() + "</string>\n");
        xmlStringBuffer.append("  </QName>\n");
        xmlStringBuffer.append("</concept>\n");
      }
    }
    
    xmlStringBuffer.append("</concepts>");
    xmlString = xmlStringBuffer.toString();
    return xmlString;
  }
  

  /**
   * Utility method to transform a QName to XML.
   * 
   * @param    qName      the QName object to be transformed
   * @return   xmlString  the XML string
   */
  public static String qNameToXML(QName qName) {
    String xmlString = "";
    StringBuffer xmlStringBuffer = new StringBuffer(XML_DECLARATION);
    
    if (qName != null) {
      xmlStringBuffer.append("  <QName>\n");
      xmlStringBuffer.append("    <namespaceURI>" + qName.getNamespaceURI() + "</namespaceURI>\n");
      xmlStringBuffer.append("    <prefix>" + qName.getPrefix() + "</prefix>\n");
      xmlStringBuffer.append("    <localPart>" + qName.getLocalPart() + "</localPart>\n");
      xmlStringBuffer.append("    <string>" + qName.toString() + "</string>\n");
      xmlStringBuffer.append("  </QName>");
      xmlString = xmlStringBuffer.toString();
    }
    
    return xmlString;
  }
  
  
  /*
   * Constructors
   */
  

  /*
   * Instance methods
   */

  /**
   * Modifies the schemaURI to make it compatible with the format expected
   * by the SKOSSearcher object:
   * 
   *   lter -- append a '#' character
   *   nbii -- append the string "/Concept/"
   *   
   * @param schemaURI        the original schema URI
   * @param schemeName       the scheme name, e.g. "lter"
   * @return                 the modified schema URI, e.g. "lter#"
   */
  static String modifySchemaURI(String schemaURI, String schemeName) {
    StringBuffer modifiedSchemaURI = new StringBuffer(schemaURI);
       
    if (schemaURI != null && !schemaURI.endsWith("#")) {
          modifiedSchemaURI.append("#");
    }
    
    return modifiedSchemaURI.toString();
  }

  
  /**
   * Gets a list of alternate labels using the SKOSConcept.getAltLabels()
   * method.
   * 
   * @param schemeName     the scheme name, e.g. "nbii"
   * @param localPart      id of the concept to be accessed, e.g. "285"         
   * @return  a list of alternate labels, one per line
   */
  @GET
  @Path("{localPart}/altLabels")
  @Produces("text/plain")
  public String getAltLabels(@PathParam("schemeName") String schemeName,
                             @PathParam("localPart") String localPart) {
    SKOSConcept skosConcept = null;
    StringBuffer altLabels = new StringBuffer("");
    
    SchemesResource schemesResource = new SchemesResource();
    String schemaURI = schemesResource.getSchemaURI(schemeName);
    if (schemaURI != null) {
      schemaURI = schemaURI.trim();
      schemaURI = modifySchemaURI(schemaURI, schemeName);   
      SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
      
      if (skosSearcher != null) {
        skosConcept = skosSearcher.searchConceptByURI(schemaURI, localPart);
        if (skosConcept != null) {
          List<String> altLabelsList = skosConcept.getAltLabels();
          for (String s : altLabelsList) {
            altLabels.append(s + "\n");
          }
        }
      }
    }
    
    return altLabels.toString().trim();
  }
  
  
  /**
   * Gets an XML representation of the list of broader concepts corresponding
   * to a given concept.
   * 
   * @param schemeName     the scheme name, e.g. "nbii"
   * @param localPart      id of the concept to be accessed, e.g. "285"         
   * @return an XML string representing the list of broader concepts
   */
  @GET
  @Path("{localPart}/broaders")
  public String getBroaders(@PathParam("schemeName") String schemeName,
                            @PathParam("localPart") String localPart) {
    SKOSConcept skosConcept = null;
    String broadersXML = "";
    
    SchemesResource schemesResource = new SchemesResource();
    String schemaURI = schemesResource.getSchemaURI(schemeName);
    if (schemaURI != null) {
      schemaURI = schemaURI.trim();
      schemaURI = modifySchemaURI(schemaURI, schemeName);   
      SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
      
      if (skosSearcher != null) {
        skosConcept = skosSearcher.searchConceptByURI(schemaURI, localPart);
        if (skosConcept != null) {
          TreeMap<String, QName> broadersMap = skosConcept.getBroaders();
          broadersXML = conceptTreeMapToXML(broadersMap);
        }
      }
    }
    
    return broadersXML;
  }
    

  /**
   * Gets an XML representation of the list of child concepts corresponding
   * to a given concept.
   * 
   * @param schemeName     the scheme name, e.g. "nbii"
   * @param localPart      id of the concept to be accessed, e.g. "285"         
   * @return an XML string representing the list of child concepts
   */
  @GET
  @Path("{localPart}/children")
  public String getChildren(@PathParam("schemeName") String schemeName,
                            @PathParam("localPart") String localPart) {
    TreeMap<String, QName> skosConceptMap = null;
    String childrenXML = "";
    
    SchemesResource schemesResource = new SchemesResource();
    String schemaURI = schemesResource.getSchemaURI(schemeName);
    if (schemaURI != null) {
      schemaURI = schemaURI.trim();
      schemaURI = modifySchemaURI(schemaURI, schemeName);   
      SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
      
      if (skosSearcher != null) {
        skosConceptMap = skosSearcher.searchChildrenByURI(schemaURI, localPart);
        if (skosConceptMap != null) {
          childrenXML = conceptTreeMapToXML(skosConceptMap);
        }
      }
    }
    
    return childrenXML;
  }
    

  /**
   * Gets an XML representation of the list of narrower concepts corresponding
   * to a given concept.
   * 
   * @param schemeName     the scheme name, e.g. "nbii"
   * @param localPart      id of the concept to be accessed, e.g. "285"         
   * @return an XML string representing the list of narrower concepts
   */
  @GET
  @Path("{localPart}/narrowers")
  public String getNarrowers(@PathParam("schemeName") String schemeName,
                             @PathParam("localPart") String localPart) {
    SKOSConcept skosConcept = null;
    String narrowersXML = "";
    
    SchemesResource schemesResource = new SchemesResource();
    String schemaURI = schemesResource.getSchemaURI(schemeName);
    if (schemaURI != null) {
      schemaURI = schemaURI.trim();
      schemaURI = modifySchemaURI(schemaURI, schemeName);   
      SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
      
      if (skosSearcher != null) {
        skosConcept = skosSearcher.searchConceptByURI(schemaURI, localPart);
        if (skosConcept != null) {
          TreeMap<String, QName> narrowersMap = skosConcept.getNarrowers();
          narrowersXML = conceptTreeMapToXML(narrowersMap);
        }
      }
    }
    
    return narrowersXML;
  }
    

  /**
   * Gets the preferred label value for a given concept.
   * 
   * @param schemeName     the scheme name, e.g. "nbii"
   * @param localPart      id of the concept to be accessed, e.g. "285"         
   * @return               the preferred label string
   */
  @GET
  @Path("{localPart}/prefLabel")
  @Produces("text/plain")
  public String getPrefLabel(@PathParam("schemeName") String schemeName,
                             @PathParam("localPart") String localPart) {
    SKOSConcept skosConcept = null;
    String prefLabel = null;
    
    SchemesResource schemesResource = new SchemesResource();
    String schemaURI = schemesResource.getSchemaURI(schemeName);
    if (schemaURI != null) {
      schemaURI = schemaURI.trim();
      schemaURI = modifySchemaURI(schemaURI, schemeName);   
      SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
      
      if (skosSearcher != null) {
        skosConcept = skosSearcher.searchConceptByURI(schemaURI, localPart);
        if (skosConcept != null) {
          prefLabel = skosConcept.getPrefLabel();
        }
      }
    }
    
    return prefLabel;
  }
  
  
  /**
   * Gets the list of preferred labels for a given scheme.
   * 
   * @param schemeName     the scheme name, e.g. "nbii"        
   * @return   a list of preferred labels, one per line
   */
  @GET
  @Path("prefLabels")
  @Produces("text/plain")
  public String getPrefLabels(@PathParam("schemeName") String schemeName) {
    StringBuffer prefLabelsBuffer = new StringBuffer("");
    
    SKOSServer skosServer = ConfigurationListener.getSKOSServer();
    
    if (schemeName != null) {
      TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
      
      for (String s : skosSchemes.keySet()) {
        if (s.equalsIgnoreCase(schemeName)) {
          SKOSScheme skosScheme = skosSchemes.get(s);
          if (skosScheme != null) {
            Map<String, QName> alphaIndex = skosScheme.getAlphaIndex();
            if (alphaIndex != null) {
              for (String prefLabel : alphaIndex.keySet()) {
                prefLabelsBuffer.append(prefLabel + "\n");
              }
            }
          }
        }
      }
    }
    
    return prefLabelsBuffer.toString().trim();
  }
  
  
  /**
   * Gets the list of preferred labels for a given scheme where each
   * label starts with the specified letters.
   * 
   * @param schemeName     the scheme name, e.g. "nbii"        
   * @param startLetters   the start letters of the labels to be returned
   * @return               a list of preferred labels, one per line
   */
  @GET
  @Path("prefLabels/{startLetters}")
  @Produces("text/plain")
  public String getPrefLabelsStartLetters(
      @PathParam("schemeName") String schemeName,
      @PathParam("startLetters") String startLetters
      ) {
    StringBuffer prefLabelsBuffer = new StringBuffer("");
    
    SKOSServer skosServer = ConfigurationListener.getSKOSServer();
    
    if (schemeName != null && startLetters != null) {
      TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
      
      for (String s : skosSchemes.keySet()) {
        if (s.equalsIgnoreCase(schemeName)) {
          SKOSScheme skosScheme = skosSchemes.get(s);
          if (skosScheme != null) {
            TreeMap<String, QName> alphaIndex = 
              skosScheme.getSubAlphaIndex(startLetters);         
            if (alphaIndex != null) {
              for (String prefLabel : alphaIndex.keySet()) {
                prefLabelsBuffer.append(prefLabel + "\n");
              }
            }
          }
        }
      }
    }
    
    return prefLabelsBuffer.toString().trim();
  }
  
  
  /**
   * Gets an XML representation of the QName for a given concept.
   * 
   * @param schemeName     the scheme name, e.g. "nbii"
   * @param localPart      id of the concept to be accessed, e.g. "285"         
   * @return               an XML string representing the QName of the concept
   */
  @GET
  @Path("{localPart}/QName")
  public String getQName(@PathParam("schemeName") String schemeName,
                         @PathParam("localPart") String localPart) {
    SKOSConcept skosConcept = null;
    String qNameXML = "";
    
    SchemesResource schemesResource = new SchemesResource();
    String schemaURI = schemesResource.getSchemaURI(schemeName);
    if (schemaURI != null) {
      schemaURI = schemaURI.trim();
      schemaURI = modifySchemaURI(schemaURI, schemeName);   
      SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
      
      if (skosSearcher != null) {
        skosConcept = skosSearcher.searchConceptByURI(schemaURI, localPart);
        if (skosConcept != null) {
          QName qName = skosConcept.getQName();
          qNameXML = qNameToXML(qName);
        }
      }
    }
    
    return qNameXML.trim();
  }
  
  
  /**
   * Gets an XML representation of the list of related concepts corresponding
   * to a given concept.
   * 
   * @param schemeName     the scheme name, e.g. "nbii"
   * @param localPart      id of the concept to be accessed, e.g. "285"         
   * @return an XML string representing the list of related concepts
   */
  @GET
  @Path("{localPart}/relateds")
  public String getRelateds(@PathParam("schemeName") String schemeName,
                            @PathParam("localPart") String localPart) {
    SKOSConcept skosConcept = null;
    String relatedXML = "";
    
    SchemesResource schemesResource = new SchemesResource();
    String schemaURI = schemesResource.getSchemaURI(schemeName);
    if (schemaURI != null) {
      schemaURI = schemaURI.trim();
      schemaURI = modifySchemaURI(schemaURI, schemeName);   
      SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
      
      if (skosSearcher != null) {
        skosConcept = skosSearcher.searchConceptByURI(schemaURI, localPart);
        if (skosConcept != null) {
          TreeMap<String, QName> relatedMap = skosConcept.getRelated();
          relatedXML = conceptTreeMapToXML(relatedMap);
        }
      }
    }
    
    return relatedXML;
  }
    

  /**
   * Gets the SKOS representation for a given concept based on its localPart
   * identifier.
   * 
   * @param schemeName     the scheme name, e.g. "nbii"
   * @param localPart      id of the concept to be accessed, e.g. "285"         
   * @return               the concept's SKOS representation in XML format
   */
  @GET
  @Path("{localPart}/SKOSFormat")
  public String getSKOSFormat(@PathParam("schemeName") String schemeName,
                                @PathParam("localPart") String localPart) {
    SKOSConcept skosConcept = null;
    StringBuffer xmlStringBuffer = new StringBuffer(XML_DECLARATION);
    
    SchemesResource schemesResource = new SchemesResource();
    String schemaURI = schemesResource.getSchemaURI(schemeName);
    if (schemaURI != null) {
      schemaURI = schemaURI.trim();
      schemaURI = modifySchemaURI(schemaURI, schemeName);   
      SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
      
      if (skosSearcher != null) {
        skosConcept = skosSearcher.searchConceptByURI(schemaURI, localPart);
        if (skosConcept != null) {
          xmlStringBuffer.append(skosConcept.getSKOSFormat());
        }
      }
    }
    
    return xmlStringBuffer.toString();
  }

  
  /**
   * Gets the SKOS representation for a given concept based on its preferred
   * label.
   * 
   * @param schemeName     the scheme name, e.g. "nbii"      
   * @param prefLabel      the preferred label, e.g. "Eruptions"
   * @return               the concept's SKOS representation in XML format
   */
  @GET
  @Path("concept/SKOSFormat")
  public String getConceptFromPrefLabel(
      @PathParam("schemeName") String schemeName,
      @QueryParam("prefLabel") String prefLabel) {
    StringBuffer xmlStringBuffer = 
      new StringBuffer(XML_DECLARATION);
    
    if (prefLabel != null) {
      SchemesResource schemesResource = new SchemesResource();
      String schemaURI = schemesResource.getSchemaURI(schemeName);
      
      if (schemaURI != null) {
        schemaURI = schemaURI.trim();
        schemaURI = modifySchemaURI(schemaURI, schemeName);   
        
        SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
        if (skosSearcher != null) {
          List<SKOSConcept> skosConcepts = 
                                skosSearcher.searchConceptByKeyword(prefLabel);
          for (SKOSConcept skosConcept : skosConcepts) {
            String conceptPrefLabel = skosConcept.getPrefLabel();
            if (prefLabel.equalsIgnoreCase(conceptPrefLabel)) {
              QName qName = skosConcept.getQName();
              String namespaceURI = qName.getNamespaceURI();
              if (schemaURI.equals(namespaceURI)) {
                xmlStringBuffer.append(skosConcept.getSKOSFormat());
              }
            }
          }
        }
      }
    }
    
    return xmlStringBuffer.toString();
  }

  
  /**
   * Gets a list of concepts that match a given keyword.
   * 
   * @param schemeName     the scheme name, e.g. "nbii"        
   * @param keyword        the keyword to be matched to the concepts
   * @return               a list of concepts in XML format
   */
  @GET
  @Path("SKOSFormat")
  public String searchConceptsByKeyword(
      @PathParam("schemeName") String schemeName,
      @QueryParam("keyword") String keyword) {
    List<SKOSConcept> skosConcepts = null;
    String xmlString = null;
    
    SchemesResource schemesResource = new SchemesResource();
    String schemaURI = schemesResource.getSchemaURI(schemeName);
    if (schemaURI != null && keyword != null) {
      List<SKOSConcept> schemeConcepts = new ArrayList<SKOSConcept>();
      schemaURI = schemaURI.trim();
      schemaURI = modifySchemaURI(schemaURI, schemeName);   
      SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
      
      if (skosSearcher != null) {
        skosConcepts = skosSearcher.searchConceptByKeyword(keyword);
        
        // Filter matching concepts based on the specified schemeName
        for (SKOSConcept skosConcept : skosConcepts) {
          QName qName = skosConcept.getQName();
          String namespaceURI = qName.getNamespaceURI();
          if (schemaURI.equals(namespaceURI)) {
            schemeConcepts.add(skosConcept);
          }
        }
        
        xmlString = conceptListToXML(schemeConcepts);
      }
    }
    
    return xmlString;
  }
  
  
  /**
   * Analyzes a document to search for tags that match a given vocabulary
   * and returns the matching tags as a list of vocabulary concepts.
   * 
   * @param  schemeName     the scheme name, e.g. "nbii"        
   * @param  file           the file to be analyzed
   * @return                a list of SKOS concepts in XML format
   */
  @PUT
  @Path("tags/SKOSFormat")
  public String tagDocument(
		  @PathParam("schemeName") String schemeName, 
		  @QueryParam("algorithm") String algorithm,
          File file) 
  {
    String xmlString = "";
    if (file != null) {
      String inputFilePath = file.getAbsolutePath();
      logger.debug("inputFilePath: " + inputFilePath);
      List<SKOSConcept> skosConcepts = null;
      List<String> vocabularyList = new ArrayList<String>();
      vocabularyList.add(schemeName); 
    
      SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
      SKOSTagger skosTagger = ConfigurationListener.getSKOSTagger(algorithm);
      
      if (skosSearcher != null && skosTagger != null) {
        skosConcepts = 
          skosTagger.getTags(inputFilePath, vocabularyList, skosSearcher, 10);
        xmlString = conceptListToXML(skosConcepts);
      }
      file.delete();
    }
    
    return xmlString;
  }
  
}
  