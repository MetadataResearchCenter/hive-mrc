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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.GET;
// import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.namespace.QName;




// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.SKOSSearcher;
import edu.unc.ils.mrc.hive.api.SKOSServer;
import edu.unc.ils.mrc.hive.api.SKOSTagger;
// import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSTaggerImpl;

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
   * Utility method to transform a list of SKOSConcept objects into JSON.
   * 
   * @param   skosConcepts   the list of SKOSConcept objects
   * @return  jsonString     the JSON string
   */
  public String conceptListToJSON(List<SKOSConcept> skosConcepts) {
	  
	logger.info("conceptListToJSON");
	
	/////////////
	//SKOSConcept skosConceptTemp = null;
    //String prefLabelTemp = null;
    
   // String schemaURI = "tbd";
	/*
    SKOSSearcher skosSearcherTemp = ConfigurationListener.getSKOSSearcher();
      
      if (skosSearcherTemp != null) {
        skosConceptTemp = skosSearcherTemp.searchConceptByURI(schemaURI, localPart);
        if (skosConceptTemp != null) {
          prefLabelTemp = skosConceptTemp.getPrefLabel();
        }
      }
    */
	
    ///////////////////
	
    StringBuffer jsonStringBuffer = new StringBuffer("{ \"concepts\": [ \n");
    
    int numConcepts = skosConcepts.size(); 
    int count = 1;
    for (SKOSConcept skosConcept : skosConcepts) {
        if (skosConcept != null) {
            //String jsonFormat = skosConcept.getJSONFormat();   // define local method to convert concept to JSON
            String jsonFormat = conceptToJSON(skosConcept);
            jsonStringBuffer.append(jsonFormat);
            if (count < numConcepts)  
            	jsonStringBuffer.append(",\n");
            count++;
        }
    }
    jsonStringBuffer.append("\n]");
    jsonStringBuffer.append("\n}");  
    
    String jsonString = jsonStringBuffer.toString();
    
    logger.info("JSONSTRING:" +  jsonString);
    return jsonString;
  }
  
  public String conceptToJSON(SKOSConcept concept) {
	  SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
	  StringBuffer json =  new StringBuffer();
	    
	  json.append("{   \"uri\": " + "\"" +  concept.getQName().getNamespaceURI() + concept.getQName().getLocalPart() + "\",\n");
      json.append("   \"type\": [\"http://www.w3.org/2004/02/skos/core#Concept\"],\n");
	  json.append("   \"prefLabel\": " + "\"" +  concept.getPrefLabel() + "\"\n");
		
      int num = concept.getAltLabels().size();
	  int count = 0;
	  if (!concept.getAltLabels().isEmpty()) {
		  json.append("   ,\"altLabel\": [\n");
		  for(String alt : concept.getAltLabels()) {
		     if (count < num-1) 
			     json.append("   \"" + alt + "\",\n"); 
		     else
		    	 json.append("   \"" + alt + "\"\n"); 
		     count++;
		  }    
          json.append("]\n");
       }
		
		num = concept.getBroaders().size();
		count = 0;
		if (!concept.getBroaders().isEmpty()) {
			json.append("   ,\"broader\": [ \n");
			for (String broader : concept.getBroaders().keySet()) {
				json.append("      { \"uri\": \"");
				String schemaURI = concept.getBroaders().get(broader).getNamespaceURI();
				String localPart = concept.getBroaders().get(broader).getLocalPart();
				json.append(schemaURI);
				json.append(localPart  + "\",");
				
				if (skosSearcher != null) {
			        SKOSConcept conceptTemp = skosSearcher.searchConceptByURI(schemaURI, localPart);
			        if (conceptTemp != null) {
			          String prefLabelTemp = conceptTemp.getPrefLabel();
			          json.append("       \"prefLabel\": \"" + prefLabelTemp); //  + "\"" );	
			        }
			      }

				if (count < num-1) 
					json.append("\"},");
		    	else
		    		json.append("\"}");
		        count++;
			}
			json.append("\n]");
		}
		
		num = concept.getNarrowers().size();
		count = 0;
		if (!concept.getNarrowers().isEmpty()) {
			json.append("  ,\"narrower\": [ \n");
			for(String narrower : concept.getNarrowers().keySet()) {
				json.append("      { \"uri\": \"");
				String schemaURI = concept.getNarrowers().get(narrower).getNamespaceURI();
				String localPart = concept.getNarrowers().get(narrower).getLocalPart();
				json.append(schemaURI); 
				json.append(localPart  + "\",");  
				
				if (skosSearcher != null) {
			        SKOSConcept conceptTemp = skosSearcher.searchConceptByURI(schemaURI, localPart);
			        if (conceptTemp != null) {
			          String prefLabelTemp = conceptTemp.getPrefLabel();
			          json.append("       \"prefLabel\": \"" + prefLabelTemp); //  + "\"" );	
			        }
			      }
				
				if (count < num-1) 
					json.append("\"},");
		    	else
		    		json.append("\"}");
		        count++;
			}
			json.append("\n]");
		}
		
		num = concept.getRelated().size();
		count = 0;
		if (!concept.getRelated().isEmpty()) {
			json.append("  ,\"related\": [ \n");
			for(String related : concept.getRelated().keySet()) {
				json.append("      { \"uri\": \"");
				String schemaURI = concept.getRelated().get(related).getNamespaceURI();
				String localPart = concept.getRelated().get(related).getLocalPart();
				json.append(schemaURI);  
				json.append(localPart  + "\","); 
				
				if (skosSearcher != null) {
			        SKOSConcept conceptTemp = skosSearcher.searchConceptByURI(schemaURI, localPart);
			        if (conceptTemp != null) {
			          String prefLabelTemp = conceptTemp.getPrefLabel();
			          json.append("       \"prefLabel\": \"" + prefLabelTemp); //  + "\"" );	
			        }
			      }
				
				if (count < num-1) 
					json.append("\"},");
		    	else
		    		json.append("\"}");
		        count++;
			}
			json.append("\n]");
		}
		
		
		json.append("   ,\"inScheme\": \"" +  concept.getQName().getNamespaceURI() + "\",\n");
	    json.append("   \"score\": \"" +  concept.getScore() + "\"\n");
	    json.append("\n}");
	
	    logger.trace(json.toString());
		return json.toString();
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
   * @param  schemeName     the scheme name, dummy value: "multi"        
   * @param  vocs           one or more vocabularies to use for tagging (indexing);  format: uat%2Bagrovoc%2Blcsh
   * @param  File           the file to be analyzed
   * @return                a list of SKOS concepts in JSON format
   * @author jpboone
   */

  @GET
  @Path("tags/doc")
  @Produces("application/json")
  public String tagDocument(
		  @PathParam("schemeName") String schemeName, 
		  @QueryParam("vocs") String vocs,  
          File file) 
  {
    String jsonString = "";
    if (file != null) {
      String inputFilePath = file.getAbsolutePath();
      logger.debug("inputFilePath: " + inputFilePath);
      List<SKOSConcept> skosConcepts = null;
      List<String> vocabularyList = new ArrayList<String>();
      String[] vocNames = vocs.split("[+]");
      for (int i=0; i<vocNames.length; i++) {
          vocabularyList.add(vocNames[i]);
      } 
    
      SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
      SKOSTagger skosTagger = ConfigurationListener.getSKOSTagger("maui");
      
      if (skosSearcher != null && skosTagger != null) {
        skosConcepts = 
          skosTagger.getTags(inputFilePath, vocabularyList, skosSearcher, 10);
        jsonString = conceptListToJSON(skosConcepts);
      }
      file.delete();
    }
    
    return jsonString;
  }
  

/**
 * Analyzes a URL to search for tags that match one or more vocabularies
 * and returns the matching tags as a list of vocabulary concepts.
 * 
 * @param  schemeName     the scheme name, dummy value: "multi"       
 * @param  URL            the URL to be analyzed
 * @param  vocs           one or more vocabularies to use for tagging (indexing);  format: uat%2Bagrovoc%2Blcsh
 * @return                a list of SKOS concepts in XML format
 * @author jpboone
 */
/*
@GET
@Path("tagURLxml")
public String tagURLxml(
		@PathParam("schemeName") String schemeName,
		@QueryParam("url") String url,     
		@QueryParam("vocs") String vocs)  
{
    String xmlString = "";
    List<SKOSConcept> skosConcepts = null;
    List<String> vocabularyList = new ArrayList<String>();
   System.out.println("vocs=" + vocs); 
    String[] vocNames = vocs.split("[+]");
    for (int i=0; i<vocNames.length; i++) {
        vocabularyList.add(vocNames[i]);
    } 
    if (!url.startsWith("http://") && !url.startsWith("https://"))
		url = "http://" + url;
       
    SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
    SKOSTagger skosTagger = ConfigurationListener.getSKOSTagger("maui");
    try {
       if (skosSearcher != null && skosTagger != null) {
        
		   skosConcepts = skosTagger.getTags(new URL(url), vocabularyList, skosSearcher, 0, 10, true);
      
         xmlString = conceptListToXML(skosConcepts);
       } 
    }
    catch (MalformedURLException e) {
       logger.debug("malformed URL exception: " + url);	 
    }

  return xmlString;
}
*/

/**
 * Analyzes a URL to search for tags that match one or more vocabularies
 * and returns the matching tags as a list of vocabulary concepts in JSON format
 * 
 * @param  schemeName     the scheme name, dummy value: "multi"       
 * @param  URL            the URL to be analyzed
 * @param  vocs           one or more vocabularies to use for tagging (indexing);  format: uat%2Bagrovoc%2Blcsh
 * @return                a list of SKOS concepts in JSON format
 * @author jpboone
 */
/*
@GET
@Path("tagURLjson")
@Produces("text/plain")
public String tagURLjson(
		@PathParam("schemeName") String schemeName,
		@QueryParam("url") String url,     
		@QueryParam("vocs") String vocs)  
{
    String jsonString = "";
    List<SKOSConcept> skosConcepts = null;
    List<String> vocabularyList = new ArrayList<String>();
    System.out.println("vocs=" + vocs); 
    String[] vocNames = vocs.split("[+]");
    for (int i=0; i<vocNames.length; i++) {
        //System.out.println("voc " + i + ": " + vocNames[i]); 
        vocabularyList.add(vocNames[i]);
    } 
    if (!url.startsWith("http://") && !url.startsWith("https://"))
		url = "http://" + url;
      
    SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
    SKOSTagger skosTagger = ConfigurationListener.getSKOSTagger("maui");
    try {
       if (skosSearcher != null && skosTagger != null) {
        
		   skosConcepts = skosTagger.getTags(new URL(url), vocabularyList, skosSearcher, 0, 10, true);
      
           jsonString = conceptListToJSON(skosConcepts);
       } 
    }
    catch (MalformedURLException e) {
       logger.debug("malformed URL exception: " + url);	 
    }

    logger.info("in tagURLjson(): jsonString return value:" + jsonString);
    return jsonString;
} 
*/

/**
 * Analyzes a URL to search for tags that match one or more vocabularies
 * and returns the matching tags as a list of vocabulary concepts in JSON format
 * 
 * @param  schemeName     the scheme name, dummy value: "multi"       
 * @param  URL            the URL to be analyzed
 * @param  vocs           one or more vocabularies to use for tagging (indexing);  format: uat%2Bagrovoc%2Blcsh
 * @return                a list of SKOS concepts in JSON format
 * @author jpboone
 */

@GET
@Path("tags/url")
@Produces("application/json")
public String tagURL(
		@PathParam("schemeName") String schemeName,
		@QueryParam("url") String url,     
		@QueryParam("vocs") String vocs)  
{
    String jsonString = "";
    List<SKOSConcept> skosConcepts = null;
    List<String> vocabularyList = new ArrayList<String>();
    System.out.println("vocs=" + vocs); 
    String[] vocNames = vocs.split("[+]");
    for (int i=0; i<vocNames.length; i++) {
        //System.out.println("voc " + i + ": " + vocNames[i]); 
        vocabularyList.add(vocNames[i]);
    } 
    if (!url.startsWith("http://") && !url.startsWith("https://"))
		url = "http://" + url;
        
    SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
    SKOSTagger skosTagger = ConfigurationListener.getSKOSTagger("maui");
    try {
       if (skosSearcher != null && skosTagger != null) {
           // Last 3 arguments take defaults:
    	   //    maxHops = 0,   max number of links to travers
    	   //    numTerms = 10, max number of terms to return
    	   //    diff = true,   only index differences between base page and others
		   skosConcepts = skosTagger.getTags(new URL(url), vocabularyList, skosSearcher, 0, 10, true);
      
           jsonString = conceptListToJSON(skosConcepts);
       } 
    }
    catch (MalformedURLException e) {
       logger.debug("malformed URL exception: " + url);	 
    }

    logger.info("in tagURLjson(): jsonString return value:" + jsonString);
  return jsonString;
} 



/**
 * Analyzes a URL to search for tags that match a given vocabulary
 * and returns the matching tags as a list of vocabulary concepts.
 * 
 * @param  schemeName     the scheme name, dummy value: "multi"       
 * @param  URL            the URL to be analyzed
 * @param  vocs           one or more vocabularies to use for tagging (indexing)
 * @param  maxHops        max number of links to be traversed (hops)
 * @param  numTerms       number of terms to be returned
 * @param  diff           extract only differences between base page and subsequent pages
 * @return                a list of SKOS concepts in XML format
 * @author jpboone
 */

@GET
@Path("tagURLoptions")
public String tagURLwithOptions(
		@PathParam("schemeName") String schemeName,
		@QueryParam("url") String url,     // with or without 'http:/'
		@QueryParam("vocs") String vocs,   // format: uat%2Bagrovoc%2Blcsh
		@QueryParam("maxhops") String maxHops,
		@QueryParam("numterms") String numTerms,
		@QueryParam("diff") String diff) 
{
    int hops, terms;
    boolean diffd; 
	String xmlString = "";
    List<SKOSConcept> skosConcepts = null;
    List<String> vocabularyList = new ArrayList<String>();
    System.out.println("vocs=" + vocs); 
    String[] vocNames = vocs.split("[+]");
    for (int i=0; i<vocNames.length; i++) {
    	 System.out.println("voc " + i + ": " + vocNames[i]); 
        vocabularyList.add(vocNames[i]);
    } 
    if (!url.startsWith("http://") && !url.startsWith("https://"))
		url = "http://" + url;
    //System.out.println("url=" + url);
    //System.out.println("Options: " + maxHops + numTerms + diff);
    
    if (maxHops == null) hops = 0;
    else {
    	try {
            hops = Integer.parseInt(maxHops );
            if (hops < 0) hops = 0;  }
        catch( Exception e ) { hops = 0; }
    }
    
    if (numTerms == null) terms = 10;
    else {
    	try {
            terms = Integer.parseInt(numTerms );
            if (terms < 1) terms = 10;  }
        catch( Exception e ) { terms = 10; }
    }
    
    if (diff == null) diffd=true;
    else {
       if (diff.equalsIgnoreCase("false")) diffd=false;
       else diffd=true;
    }
    //System.out.println("Options: " + hops + terms + diffd);
    
    SKOSSearcher skosSearcher = ConfigurationListener.getSKOSSearcher();
    SKOSTagger skosTagger = ConfigurationListener.getSKOSTagger("maui");
    try {
       if (skosSearcher != null && skosTagger != null) {
        
		   skosConcepts = skosTagger.getTags(new URL(url), vocabularyList, skosSearcher, hops, terms, diffd);
      
         xmlString = conceptListToXML(skosConcepts);
       } 
    }
    catch (MalformedURLException e) {
       logger.debug("malformed URL exception: " + url);	 
    }

  return xmlString;
}  


}
  
