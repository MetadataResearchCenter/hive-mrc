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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import junit.framework.JUnit4TestAdapter;

import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.SKOSSearcher;
import edu.unc.ils.mrc.hive.api.SKOSServer;


public class ConceptsResourceTest {

  /*
   * Class fields
   */
  
  private static boolean initialized = false;
  private static ConfigurationListener configurationListener = null;
  private static final String dirPath = "WebRoot/WEB-INF/conf";
  private static final String testPath = "test/data";
  private static final String LTER_SCHEME_NAME = "lter";
  private static final String NBII_SCHEME_NAME = "nbii";
  private static SKOSSearcher skosSearcher = null;
  private static SKOSServer skosServer = null;

  
  /*
   * Instance fields
   */
  
  ConceptsResource conceptsResource = null;
  
  /*
   * Constructors
   */
  
  
  /*
   * Class methods
   */
  
  /**
   * Create a suite of tests to be run together.
   */
  public static junit.framework.Test suite() { 
    return new JUnit4TestAdapter(ConceptsResourceTest.class); 
  }
  
  
  /*
   * Instance methods
   */
  
  /**
   * Establish a fixture by initializing appropriate objects.
   */
  @Before public void setUp() {
    this.conceptsResource = new ConceptsResource();
    
    if (!initialized) {
      configurationListener = new ConfigurationListener();
      configurationListener.initialize(dirPath);
      skosSearcher = ConfigurationListener.getSKOSSearcher();
      skosServer = ConfigurationListener.getSKOSServer();
      initialized = true;
    }
  }
  
  
  /**
   * Run an initial test that always passes to check that the test harness
   * is working.
   */
  @Test public void testInitialize() {
    assertTrue(1 == 1);
  }
  
  
  /**
   * Test the XML output out the ConceptsResource.conceptListToXML() method.
   */
  @Test public void testConceptListToXML() {
    final String expectedSubstring1 = "<SKOSConcepts>";
    final String expectedSubstring2 = "</SKOSConcepts>";
    final String keyword = "activity";
    List<SKOSConcept> schemeConcepts = new ArrayList<SKOSConcept>();
    schemeConcepts = skosSearcher.searchConceptByKeyword(keyword);
    String xmlString = ConceptsResource.conceptListToXML(schemeConcepts);
    if (xmlString != null ) {
      assertTrue("Missing expected substring: " + expectedSubstring1,
                 xmlString.contains(expectedSubstring1));
      assertTrue("Missing expected substring: " + expectedSubstring2,
                 xmlString.contains(expectedSubstring2));
    }
    else {
      assertNotNull("xmlString has null value", xmlString);
    }
  }
  

  /**
   * Test the XML output out the ConceptsResource.conceptTreeMapToXML() method.
   */
  @Test public void testConceptTreeMapToXML() {
    final String expectedSubstring1 = "<concepts>";
    final String expectedSubstring2 = "<concepts>";
    TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
    SKOSScheme skosScheme = skosSchemes.get(LTER_SCHEME_NAME);
    TreeMap<String, QName> alphaIndex = skosScheme.getSubAlphaIndex("a");
    String xmlString = ConceptsResource.conceptTreeMapToXML(alphaIndex);
    if (xmlString != null ) {
      assertTrue("Missing expected substring: " + expectedSubstring1,
                 xmlString.contains(expectedSubstring1));
      assertTrue("Missing expected substring: " + expectedSubstring2,
                 xmlString.contains(expectedSubstring2));
    }
    else {
      assertNotNull("xmlString has null value", xmlString);
    }
  }
  

  /**
   * Test the conversion of a QName to XML format.
   */
  @Test public void testQNameToXML() {
    final String expectedSubstring1 = "<QName>";
    final String expectedSubstring2 = "</QName>";
    String schemaURI = null;
    final String localPart = "c_7";
    SKOSConcept skosConcept = null;
    String qNameXML = "";
    TreeMap<String, SKOSScheme> skosSchemes = skosServer.getSKOSSchemas();
    SKOSScheme skosScheme = skosSchemes.get(LTER_SCHEME_NAME);
    
    if (skosScheme != null) {
      schemaURI = skosScheme.getSchemaURI();
      if (schemaURI != null) {
        schemaURI = schemaURI.trim();
        schemaURI = ConceptsResource.modifySchemaURI(schemaURI, LTER_SCHEME_NAME);   
        skosConcept = skosSearcher.searchConceptByURI(schemaURI, localPart);
        if (skosConcept != null) {
          QName qName = skosConcept.getQName();
          if (qName != null) {
            qNameXML = ConceptsResource.qNameToXML(qName);
            if (qNameXML != null ) {
              assertTrue("Missing expected substring: " + expectedSubstring1,
                         qNameXML.contains(expectedSubstring1));
              assertTrue("Missing expected substring: " + expectedSubstring2,
                         qNameXML.contains(expectedSubstring2));
            }
            else {
              assertNotNull("xmlString has null value", qNameXML);
            }
          }
          else {
            assertNotNull("qName has null value", qName);
          }
        }
        else {
          assertNotNull("skosConcept has null value", skosConcept);
        }
      }
      else {
        assertNotNull("schemaURI has null value", schemaURI);
      }
    }
    else {
      assertNotNull("skosScheme has null value", skosScheme);
    }
  }
  
  
  /**
   * Test getAltLabels() method.
   */
  @Test public void testGetAltLabels() {
    final String localPart = "c_7";
    final String expectedAltLabel = "anc";
    String returnedAltLabel = 
      conceptsResource.getAltLabels(LTER_SCHEME_NAME, localPart);
    assertTrue("Missing expected substring: " + expectedAltLabel,
               returnedAltLabel.contains(expectedAltLabel));
  }
  
  
  /**
   * Test getBroaders() method.
   */
  @Test public void testGetBroaders() {
    final String localPart = "285";
    final String expectedBroader = "<localPart>7860</localPart>";
    String returnedBroader = 
      conceptsResource.getBroaders(NBII_SCHEME_NAME, localPart);
    assertTrue("Missing expected substring: " + expectedBroader,
               returnedBroader.contains(expectedBroader));
  }
  
  
  /**
   * Test getChildren() method. 
   */
  @Test public void testGetChildren() {
    final String localPart = "285";
    final String expectedChild = "<prefLabel>Acid deposition</prefLabel>";
    String returnedChildren = 
      conceptsResource.getChildren(NBII_SCHEME_NAME, localPart);
    assertTrue("Missing expected substring: " + expectedChild,
               returnedChildren.contains(expectedChild));
  }
  
  
  /**
   * Test getConceptFromPrefLabel() method.  
   */
  @Test public void testGetConceptFromPrefLabel() {
    final String prefLabel = "protozoa";
    final String expectedConcept = "<skos:prefLabel>protozoa</skos:prefLabel>";
    String returnedConcept = 
      conceptsResource.getConceptFromPrefLabel(LTER_SCHEME_NAME, prefLabel);
    assertTrue("Missing expected substring: " + expectedConcept,
               returnedConcept.contains(expectedConcept));
  }
  
  
  /**
   * Test getNarrowers() method.
   */
  @Test public void testGetNarrowers() {
    final String localPart = "285";
    final String expectedNarrower = "<localPart>56</localPart>";
    String returnedNarrower = 
      conceptsResource.getNarrowers(NBII_SCHEME_NAME, localPart);
    assertTrue("Missing expected substring: " + expectedNarrower,
               returnedNarrower.contains(expectedNarrower));
  }
  
  
  /**
   * Test getPrefLabel() method.
   */
  @Test public void testGetPrefLabel() {
    final String expectedPrefLabel1 = "acid neutralizing capacity";
    final String expectedPrefLabel2 = "Air pollution";
    final String localPart1 = "c_7";
    final String localPart2 = "285";
    String returnedPrefLabel1 = 
      conceptsResource.getPrefLabel(LTER_SCHEME_NAME, localPart1);
    assertTrue("Missing expected substring: " + expectedPrefLabel1,
               returnedPrefLabel1.contains(expectedPrefLabel1));
    String returnedPrefLabel2 = 
      conceptsResource.getPrefLabel(NBII_SCHEME_NAME, localPart2);
    assertTrue("Missing expected substring: " + expectedPrefLabel2,
               returnedPrefLabel2.contains(expectedPrefLabel2));
  }
  
  
  /**
   * Test getPrefLabels() method.
   */
  @Test public void testGetPrefLabels() {
    final String expectedPrefLabel1 = "acid neutralizing capacity";
    final String expectedPrefLabel2 = "Air pollution";
    String lterPrefLabels = 
      conceptsResource.getPrefLabels(LTER_SCHEME_NAME);
    assertTrue("Missing expected substring: " + expectedPrefLabel1,
               lterPrefLabels.contains(expectedPrefLabel1));
    String nbiiPrefLabels = 
      conceptsResource.getPrefLabels(NBII_SCHEME_NAME);
    assertTrue("Missing expected substring: " + expectedPrefLabel2,
               nbiiPrefLabels.contains(expectedPrefLabel2));
  }
  
  
  /**
   * Test getPrefLabelsStartLetters() method.
   */
  @Test public void testGetPrefLabelsStartLetters() {
    final String expectedPrefLabel1 = "acid neutralizing capacity";
    final String expectedPrefLabel2 = "Air pollution";
    final String startLetters = "a";
    String lterPrefLabels = 
      conceptsResource.getPrefLabelsStartLetters(LTER_SCHEME_NAME, startLetters);
    assertTrue("Missing expected substring: " + expectedPrefLabel1,
               lterPrefLabels.contains(expectedPrefLabel1));
    String nbiiPrefLabels = 
      conceptsResource.getPrefLabelsStartLetters(NBII_SCHEME_NAME, startLetters);
    assertTrue("Missing expected substring: " + expectedPrefLabel2,
               nbiiPrefLabels.contains(expectedPrefLabel2));
  }
  
  
  /**
   * Test getGetQName() method.
   */
  @Test public void testGetQName() {
    final String localPart = "c_7";
    final String expectedQName = "<localPart>c_7</localPart>";
    String returnedQName = 
      conceptsResource.getQName(LTER_SCHEME_NAME, localPart);
    assertTrue("Missing expected substring: " + expectedQName,
               returnedQName.contains(expectedQName));
  }
  
  
  /**
   * Test getGetRelateds() method.
   */
  @Test public void testGetRelateds() {
    final String localPart = "285";
    final String expectedRelated = "<localPart>10892</localPart>";
    String returnedRelateds = 
      conceptsResource.getRelateds(NBII_SCHEME_NAME, localPart);
    assertTrue("Missing expected substring: " + expectedRelated,
               returnedRelateds.contains(expectedRelated));
  }
  
  
  /**
   * Test getGetSKOSFormat() method. 
   */
  @Test public void testGetSKOSFormat() {
    final String localPart = "c_7";
    final String expectedSKOSFormat = 
      "<skos:prefLabel>acid neutralizing capacity</skos:prefLabel>";
    String returnedSKOSFormat = 
      conceptsResource.getSKOSFormat(LTER_SCHEME_NAME, localPart);
    assertTrue("Missing expected substring: " + expectedSKOSFormat,
               returnedSKOSFormat.contains(expectedSKOSFormat));
  }
  
  
  /**
   * Test modifySchemaURI() method.
   * 
   * http://www.lternet.edu/vocabulary --> http://www.lternet.edu/vocabulary#
   * http://thesaurus.nbii.gov --> http://thesaurus.nbii.gov/Concept/
   */
  @Test public void testModifySchemaURI() {
    final String lterOriginalSchemaURI = "http://www.lternet.edu/vocabulary";
    final String lterExpectedModifiedSchemaURI = 
                                         "http://www.lternet.edu/vocabulary#";
    final String nbiiOriginalSchemaURI = "http://thesaurus.nbii.gov";
    final String nbiiExpectedModifiedSchemaURI = 
                                         "http://thesaurus.nbii.gov/Concept/";
    String lterReturnedModifiedSchemaURI =
      ConceptsResource.modifySchemaURI(lterOriginalSchemaURI, LTER_SCHEME_NAME);
    assertTrue("Unexpected modified schema: " + lterReturnedModifiedSchemaURI,
        lterReturnedModifiedSchemaURI.equals(lterExpectedModifiedSchemaURI));

    String nbiiReturnedModifiedSchemaURI =
      ConceptsResource.modifySchemaURI(nbiiOriginalSchemaURI, NBII_SCHEME_NAME);
    assertTrue("Unexpected modified schema: " + nbiiReturnedModifiedSchemaURI,
        nbiiReturnedModifiedSchemaURI.equals(nbiiExpectedModifiedSchemaURI));
  }
  
  
  /**
   * Test searchConceptsByKeyword() method.
   */
  @Test public void testSearchConceptsByKeyword() {
    final String keyword = "activity";
    final String expectedConcept = 
      "<skos:prefLabel>microbial activity</skos:prefLabel>";
    String returnedConcepts = 
      conceptsResource.searchConceptsByKeyword(LTER_SCHEME_NAME, keyword);
    assertTrue("Missing expected substring: " + expectedConcept,
               returnedConcepts.contains(expectedConcept));
  }
  
  
  /**
   * Test tagDocument() method. Uses a test EML file stored on the file system
   * as an input parameter to the tagDocument() method, then compares the
   * returned XML results to expected substrings.
   */
  @Test public void testTagDocument() {
    final String expectedLabel1 = "<skos:prefLabel>marshes</skos:prefLabel>";
    final String expectedLabel2 = "<skos:prefLabel>stems</skos:prefLabel>";
    final String expectedLabel3 = "<skos:altLabel>creek</skos:altLabel>";
    final String originalFileName = "nin_lter_spartina_census.xml";
    File originalFile = new File(testPath, originalFileName);
    
    if (originalFile != null) {
      String originalFilePath = originalFile.getAbsolutePath();
      String testFilePath = originalFilePath + ".bak";
      
      try {
        /* The test file gets deleted from the server after it is tagged,
         * so we need to operate on a backup (.bak) copy of our test file, 
         * not the original.
         */
        FileIO.copyFile(originalFilePath, testFilePath);
        File testFile = new File(testFilePath);
      
        if (testFile != null) {
          String returnedConcepts = 
                        conceptsResource.tagDocument(LTER_SCHEME_NAME, testFile);
          assertTrue("Missing expected substring: " + expectedLabel1,
                      returnedConcepts.contains(expectedLabel1));
          assertTrue("Missing expected substring: " + expectedLabel2,
                     returnedConcepts.contains(expectedLabel2));
          assertTrue("Missing expected substring: " + expectedLabel3,
                     returnedConcepts.contains(expectedLabel3));
        }
        else {
          assertNotNull("Unable to open source file for testing", testFile);
        }
      }
      catch (IOException e) {
        fail("Unable to create a copy of the source file for testing: " + 
             e.getMessage()
            );
      }
    }
    else {
      assertNotNull("Unable to find source file for testing", originalFile);
    }
  }
  
  
  /**
   * Release any objects after tests are complete.
   */
  @After public void tearDown() {
    this.conceptsResource = null;
  }
  
}
