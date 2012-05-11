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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unc.hive.services.rs.ConfigurationListener;
import org.unc.hive.services.rs.SchemesResource;

import junit.framework.JUnit4TestAdapter;


public class SchemesResourceTest  {

  /*
   * Class fields
   */
  
  private static boolean initialized = false;
  private static ConfigurationListener configurationListener = null;
  private static final String dirPath = "WebRoot/WEB-INF/conf";
  private static final String LTER_SCHEME_NAME = "lter";
  private static final String NBII_SCHEME_NAME = "nbii";
 
  /*
   * Instance fields
   */
  
  SchemesResource schemesResource = null;

  
  /*
   * Constructors
   */

  
  /*
   * Class methods
   */
  
  public static junit.framework.Test suite() { 
    return new JUnit4TestAdapter(SchemesResourceTest.class); 
  }
  
  
  /*
   * Instance methods
   */
  
  /**
   * Establish a fixture by initializing appropriate objects.
   */
  @Before public void setUp() {
    this.schemesResource = new SchemesResource();
    
    if (!initialized) {
      configurationListener = new ConfigurationListener();
      configurationListener.initialize(dirPath);
      initialized = true;
    }
  }
  
  
  /**
   * Run an initial test that always passes to check that the test harness
   * is working.
   */
  @Test public void initialize() {
    assertTrue(1 == 1);
  }
  
  
  /**
   * Test the getAlphaIndex() method.
   */
  @Test public void testGetAlphaIndex() {
    final String expectedSubstring1 = "<concepts>";
    final String expectedSubstring2 = "<concept>";
    final String expectedSubstring3 = "zooplankton";

    String returnedString = schemesResource.getAlphaIndex(LTER_SCHEME_NAME);
    
    if (returnedString != null) {
      assertTrue("Missing expected substring: " + expectedSubstring1,
          returnedString.contains(expectedSubstring1));
      assertTrue("Missing expected substring: " + expectedSubstring2,
          returnedString.contains(expectedSubstring2));
      assertTrue("Missing expected substring: " + expectedSubstring3,
          returnedString.contains(expectedSubstring3));
    }
    else {
      assertNotNull("returnedString has null value", returnedString);
    }
  }
  
  
  /**
   * Test the getLastDate() method. Since we don't know the actual date string
   * that should be returned, all we can do is check that is has the expected
   * length.
   */
  @Test public void testGetLastDate() {
    final String similarDateString = "Wed Jun 23 09:08:13 MDT 2010";

    String returnedDateString = schemesResource.getLastDate(LTER_SCHEME_NAME);
    
    if (returnedDateString != null) {
      int similarDateStringLength = similarDateString.length();
      int returnedDateStringLength = returnedDateString.length();
      assertEquals("Returned date string length does not " + 
                   "equal expected date string length: ",
                   similarDateStringLength,
                   returnedDateStringLength);
    }
    else {
      assertNotNull("returnedDateString has null value", returnedDateString);
    }
  }
  
  
  /**
   * Test the getName() method.
   */
  @Test public void testGetName() {
    final String expectedName = "LTER";

    String returnedName = schemesResource.getName(LTER_SCHEME_NAME);
    
    if (returnedName != null) {
      assertEquals("Returned name does not match expected name: ",
                   expectedName,
                   returnedName);
    }
    else {
      assertNotNull("returnedName has null value", returnedName);
    }
  }
  
  
  /**
   * Test the getLongName() method.
   */
  @Test public void testGetLongName() {
    final String expectedLongName = "Long Term Ecological Research Network Controlled Vocabulary";

    String returnedLongName = schemesResource.getLongName(LTER_SCHEME_NAME);
    
    if (returnedLongName != null) {
      assertEquals("Returned long name does not match expected long name: ",
                   expectedLongName,
                   returnedLongName);
    }
    else {
      assertNotNull("returnedLongName has null value", returnedLongName);
    }
  }
  
  
  /**
   * Test the getNumberOfConcepts() method.
   */
  @Test public void testGetNumberOfConcepts() {
    String returnedNumberOfConcepts = 
      schemesResource.getNumberOfConcepts(LTER_SCHEME_NAME);
    
    if (returnedNumberOfConcepts != null) {
      try {
        Integer.parseInt(returnedNumberOfConcepts, 10);
      }
      catch (NumberFormatException e) {
        fail("Unexpected value for number of concepts: " +
              returnedNumberOfConcepts);
      }
    }
    else {
      assertNotNull("returnedNumberOfConcepts has null value", 
                    returnedNumberOfConcepts);
    }
  }

  
  /**
   * Test the getNumberOfRelations() method.
   */
  @Test public void testGetNumberOfRelations() {
    String returnedNumberOfRelations = 
      schemesResource.getNumberOfRelations(LTER_SCHEME_NAME);
    
    if (returnedNumberOfRelations != null) {
      try {
        Integer.parseInt(returnedNumberOfRelations, 10);
      }
      catch (NumberFormatException e) {
        fail("Unexpected value for number of relations: " +
              returnedNumberOfRelations);
      }
    }
    else {
      assertNotNull("returnedNumberOfRelations has null value", 
                    returnedNumberOfRelations);
    }
  }

  
  /**
   * Test the getSchemaURI() method. 
   */
  @Test public void testGetSchemaURI() {
    final String expectedSchemaURI = "http://www.lternet.edu/vocabulary";

    String returnedSchemaURI = schemesResource.getSchemaURI(LTER_SCHEME_NAME);
    
    if (returnedSchemaURI != null) {
      assertEquals("Returned schema URI does not match expected schema URI: ",
                   expectedSchemaURI,
                   returnedSchemaURI);
    }
    else {
      assertNotNull("returnedSchemaURI has null value", returnedSchemaURI);
    }
  }

  
  /**
   * Test the getSchemeNames() method.
   */
  @Test public void testGetSchemeNames() {
    String returnedString = schemesResource.getSchemeNames();
    
    if (returnedString != null) {
      assertTrue("Missing expected substring: " + LTER_SCHEME_NAME,
          returnedString.contains(LTER_SCHEME_NAME));
      assertTrue("Missing expected substring: " + NBII_SCHEME_NAME,
          returnedString.contains(NBII_SCHEME_NAME));
    }
    else {
      assertNotNull("returnedString has null value", returnedString);
    }
  }

  
  /**
   * Test the getSubAlphaIndex() method.
   */
  @Test public void testGetSubAlphaIndex() {
    final String expectedSubstring1 = "<concepts>";
    final String expectedSubstring2 = "<concept>";
    final String expectedSubstring3 = "atmospheric deposition";
    final String startLetters = "a";

    String returnedString = 
      schemesResource.getSubAlphaIndex(LTER_SCHEME_NAME, startLetters);
    
    if (returnedString != null) {
      assertTrue("Missing expected substring: " + expectedSubstring1,
          returnedString.contains(expectedSubstring1));
      assertTrue("Missing expected substring: " + expectedSubstring2,
          returnedString.contains(expectedSubstring2));
      assertTrue("Missing expected substring: " + expectedSubstring3,
          returnedString.contains(expectedSubstring3));
    }
    else {
      assertNotNull("returnedString has null value", returnedString);
    }
  }

  
  /**
   * Test the getSubTopConceptIndex() method.
   */
  @Test public void testGetSubTopConceptIndex() {
    final String expectedSubstring1 = "<concepts>";
    final String expectedSubstring2 = "<concept>";
    final String expectedSubstring3 = "Activity";
    final String startLetters = "a";

    String returnedString = 
      schemesResource.getSubTopConceptIndex(NBII_SCHEME_NAME, startLetters);
    
    if (returnedString != null) {
      assertTrue("Missing expected substring: " + expectedSubstring1,
          returnedString.contains(expectedSubstring1));
      assertTrue("Missing expected substring: " + expectedSubstring2,
          returnedString.contains(expectedSubstring2));
      assertTrue("Missing expected substring: " + expectedSubstring3,
          returnedString.contains(expectedSubstring3));
    }
    else {
      assertNotNull("returnedString has null value", returnedString);
    }
  }

  
  /**
   * Test the getTopConceptIndex() method.
   */
  @Test public void testGetTopConceptIndex() {
    final String expectedSubstring1 = "<concepts>";
    final String expectedSubstring2 = "<concept>";
    final String expectedSubstring3 = "Activity";

    String returnedString = 
      schemesResource.getTopConceptIndex(NBII_SCHEME_NAME);
    
    if (returnedString != null) {
      assertTrue("Missing expected substring: " + expectedSubstring1,
          returnedString.contains(expectedSubstring1));
      assertTrue("Missing expected substring: " + expectedSubstring2,
          returnedString.contains(expectedSubstring2));
      assertTrue("Missing expected substring: " + expectedSubstring3,
          returnedString.contains(expectedSubstring3));
    }
    else {
      assertNotNull("returnedString has null value", returnedString);
    }
  }


  /**
   * Release any objects after tests are complete.
   */
  @After public void tearDown() {
    this.schemesResource = null;
  }
  
}
