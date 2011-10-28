package edu.unc.ils.mrc.hive2.api.impl.test;


import java.io.File;
import java.util.List;


import javax.xml.namespace.QName;

import junit.framework.Assert;
import junit.framework.TestCase;

import edu.unc.ils.mrc.hive2.api.HiveConcept;
import edu.unc.ils.mrc.hive2.api.HiveIndex;
import edu.unc.ils.mrc.hive2.api.HiveVocabulary;
import edu.unc.ils.mrc.hive2.api.impl.HiveH2IndexImpl;
import edu.unc.ils.mrc.hive2.api.impl.HiveVocabularyImpl;

public class HiveVocabularyImplTest extends TestCase {

	HiveVocabulary vocabulary = null;
	public void setUp() throws Exception
	{
		File tempDir = File.createTempFile("hive", null);
		tempDir.delete();
		tempDir.mkdir();
		vocabulary = HiveVocabularyImpl.getInstance(tempDir.getAbsolutePath(), "test");	
	}
	

	public void testFindConcepts() throws Exception
	{
		HiveH2IndexImpl h2Index = (HiveH2IndexImpl) ((HiveVocabularyImpl)vocabulary).getH2Index();
		
		// Import the test thesaurus
		vocabulary.importConcepts("/Users/cwillis/dev/hive/hive-data/chocolate/chocolate.rdf");
		
		List<HiveConcept> hcs = h2Index.findConceptsByName("m%", false);
		Assert.assertEquals(hcs.size(), 3);
	}
	
	
	public void testImport() throws Exception
	{
		HiveIndex h2Index = ((HiveVocabularyImpl)vocabulary).getH2Index();
		HiveIndex luceneIndex = ((HiveVocabularyImpl)vocabulary).getLuceneIndex();
		
		// Import the test thesaurus
		vocabulary.importConcepts("/Users/cwillis/dev/hive/hive-data/chocolate/chocolate.rdf");
		
		//System.out.println("=====");
		//((HiveVocabularyImpl)vocabulary).dumpStatements();
		
		// Confirm totals
		long numStatements = ((HiveVocabularyImpl)vocabulary).getNumStatements();
		Assert.assertEquals(numStatements, 38);
		long numBroader = ((HiveVocabularyImpl)vocabulary).getNumBroader();
		Assert.assertEquals(numBroader, 5);
		long numNarrower = ((HiveVocabularyImpl)vocabulary).getNumNarrower();
		Assert.assertEquals(numNarrower, 5);
		long numRelated = ((HiveVocabularyImpl)vocabulary).getNumRelated();
		Assert.assertEquals(numRelated, 7);
		long numTopConcepts = ((HiveVocabularyImpl)vocabulary).getNumTopConcepts();
		Assert.assertEquals(numTopConcepts, 2);
		
		// Confirm number of concepts
		Assert.assertEquals(vocabulary.getNumConcepts(), 7);
		Assert.assertEquals(h2Index.getNumConcepts(), 7);
		Assert.assertEquals(luceneIndex.getNumConcepts(), 7);
		
		
		// Confirm 1546
		HiveConcept hc = vocabulary.findConcept(
				new QName("http://www.godiva.com/product/godiva-solid-dark-chocolate-bars-24-pc-/id/1546.gdv?#", 
						"concept"));
		// Confirm relations
		Assert.assertEquals(hc.getPrefLabel(), "Solid dark chocolate bars");
		Assert.assertEquals(hc.getBroaderConcepts().size(), 1);
		Assert.assertEquals(hc.getNarrowerConcepts().size(), 0);
		Assert.assertEquals(hc.getRelatedConcepts().size(), 1);
		
		
		// Get the broader concept (217)
	    hc = vocabulary.findConcept(
				new QName("http://www.godiva.com/subcategory/chocolate-collections-treats/chocolate-bars/id/217.gdv#", 
				"concept"));
		// Confirm relations
		Assert.assertEquals(hc.getPrefLabel(), "Chocolate bars");
		Assert.assertEquals(hc.getBroaderConcepts().size(), 0);
		Assert.assertEquals(hc.getNarrowerConcepts().size(), 2);
		Assert.assertEquals(hc.getRelatedConcepts().size(), 1);
		Assert.assertTrue(
				hc.getNarrowerConcepts().contains("http://www.godiva.com/product/godiva-dark-chocolate-bars-24-pc-/id/1550.gdv?#concept"));
		
		// Get the narrower concept (1550)
		hc = vocabulary.findConcept(
				new QName("http://www.godiva.com/product/godiva-dark-chocolate-bars-24-pc-/id/1550.gdv?#", 
						"concept"));
		// Confirm relations
		Assert.assertEquals(hc.getPrefLabel(), "Dark chocolate bars");
		Assert.assertEquals(hc.getBroaderConcepts().size(), 1);
		Assert.assertEquals(hc.getNarrowerConcepts().size(), 1);
		Assert.assertEquals(hc.getRelatedConcepts().size(), 1);
		
		
		// Import updated concept. This update removes broader relationship between 1550 and 217
		vocabulary.importConcept(
				new QName("http://www.godiva.com/product/godiva-dark-chocolate-bars-24-pc-/id/1550.gdv?#", 
						"concept"),  "/Users/cwillis/dev/hive/hive-data/chocolate/chocolate_update2.rdf");
		
		// Get the broader concept (217)
		hc = vocabulary.findConcept(
				new QName("http://www.godiva.com/subcategory/chocolate-collections-treats/chocolate-bars/id/217.gdv#", 
						"concept"));
		// Confirm relations
		Assert.assertEquals(hc.getPrefLabel(), "Chocolate bars");
		Assert.assertEquals(hc.getBroaderConcepts().size(), 0);
		Assert.assertEquals(hc.getNarrowerConcepts().size(), 1);
		Assert.assertEquals(hc.getRelatedConcepts().size(), 1);
		// Confirm narrower relation has been removed
		Assert.assertFalse(
				hc.getNarrowerConcepts().contains("http://www.godiva.com/product/godiva-dark-chocolate-bars-24-pc-/id/1550.gdv?#concept"));

		// Get 1546 (narrower of 1550)
		hc = vocabulary.findConcept(
				new QName("http://www.godiva.com/product/godiva-solid-dark-chocolate-bars-24-pc-/id/1546.gdv?#", 
						"concept"));
		// Confirm relations
		Assert.assertEquals(hc.getPrefLabel(), "Solid dark chocolate bars");
		Assert.assertEquals(hc.getBroaderConcepts().size(), 1);
		Assert.assertEquals(hc.getNarrowerConcepts().size(), 0);
		Assert.assertEquals(hc.getRelatedConcepts().size(), 1);
		Assert.assertTrue(
				hc.getBroaderConcepts().contains("http://www.godiva.com/product/godiva-dark-chocolate-bars-24-pc-/id/1550.gdv?#concept"));
	
		
		// Get 1545 (related to 1550)
		hc = vocabulary.findConcept(
				new QName("http://www.godiva.com/product/godiva-solid-milk-chocolate-bar-24-pc-/id/1545.gdv?#", 
						"concept"));
		Assert.assertTrue(
				hc.getRelatedConcepts().contains("http://www.godiva.com/product/godiva-dark-chocolate-bars-24-pc-/id/1550.gdv?#concept"));
	
		// Get the narrower concept (1550)
		hc = vocabulary.findConcept(
				new QName("http://www.godiva.com/product/godiva-dark-chocolate-bars-24-pc-/id/1550.gdv?#", 
						"concept"));
		// Confirm relations
		Assert.assertEquals(hc.getPrefLabel(), "Dark chocolate bars");
		Assert.assertEquals(hc.getBroaderConcepts().size(), 0);
		Assert.assertEquals(hc.getNarrowerConcepts().size(), 1);
		Assert.assertEquals(hc.getRelatedConcepts().size(), 1);
		
		//System.out.println("=====");
		//((HiveVocabularyImpl)vocabulary).dumpStatements();
		
		// Confirm totals
		numStatements = ((HiveVocabularyImpl)vocabulary).getNumStatements();
		Assert.assertEquals(numStatements, 36);
		numBroader = ((HiveVocabularyImpl)vocabulary).getNumBroader();
		Assert.assertEquals(numBroader, 4);
		numNarrower = ((HiveVocabularyImpl)vocabulary).getNumNarrower();
		Assert.assertEquals(numNarrower, 4);
		numRelated = ((HiveVocabularyImpl)vocabulary).getNumRelated();
		Assert.assertEquals(numRelated, 7);
		numTopConcepts = ((HiveVocabularyImpl)vocabulary).getNumTopConcepts();
		Assert.assertEquals(numTopConcepts, 3);
		
		// Remove 1550. This should remove the relation with the narrower (1546) and related (1545) concept;
		vocabulary.removeConcept(hc.getQName());

		Assert.assertEquals(vocabulary.getNumConcepts(), 6);
		Assert.assertEquals(h2Index.getNumConcepts(), 6);
		Assert.assertEquals(luceneIndex.getNumConcepts(), 6);
		
		//System.out.println("=====");
		//((HiveVocabularyImpl)vocabulary).dumpStatements();
		
		// Confirm totals
		numStatements = ((HiveVocabularyImpl)vocabulary).getNumStatements();
		Assert.assertEquals(numStatements, 28);
		numBroader = ((HiveVocabularyImpl)vocabulary).getNumBroader();
		Assert.assertEquals(numBroader, 3);
		numNarrower = ((HiveVocabularyImpl)vocabulary).getNumNarrower();
		Assert.assertEquals(numNarrower, 3);
		numRelated = ((HiveVocabularyImpl)vocabulary).getNumRelated();
		Assert.assertEquals(numRelated, 4);
		numTopConcepts = ((HiveVocabularyImpl)vocabulary).getNumTopConcepts();
		Assert.assertEquals(numTopConcepts, 2);
		
		// Get the narrower
		hc = vocabulary.findConcept(
				new QName("http://www.godiva.com/product/godiva-solid-dark-chocolate-bars-24-pc-/id/1546.gdv?#", 
						"concept"));
		Assert.assertFalse(
				hc.getBroaderConcepts().contains("http://www.godiva.com/product/godiva-dark-chocolate-bars-24-pc-/id/1550.gdv?#concept"));

		
		// Get the related
		hc = vocabulary.findConcept(
				new QName("http://www.godiva.com/product/godiva-solid-milk-chocolate-bar-24-pc-/id/1545.gdv?#", 
						"concept"));
		Assert.assertFalse(
				hc.getRelatedConcepts().contains("http://www.godiva.com/product/godiva-dark-chocolate-bars-24-pc-/id/1550.gdv?#concept"));
		
	}
	
	
	/*

	
	public void testImport() throws Exception 
	{

		// Import main RDF file
		vocabulary.importConcepts("/Users/cwillis/dev/hive/hive-data/test/test.rdf");
		int numConcepts = vocabulary.getNumConcepts();
		Assert.assertEquals(numConcepts, 6);
		try
		{
			vocabulary.importConcept(new QName("http://hive.nescent.org/test/test04#", "concept"), "/Users/cwillis/dev/hive/hive-data/test/remove_broader.rdf");
			numConcepts = vocabulary.getNumConcepts();
			HiveConcept hc = vocabulary.findConcept(new QName("http://hive.nescent.org/test/test04#", "concept"));
			Assert.assertEquals(hc.getBroaderConcepts().size(), 0);
		} catch (Exception e ) {
			e.printStackTrace();
		}
	}
	
	public void testCreate() throws Exception 
	{
		HiveConcept concept = new HiveConcept();
		concept.setPrefLabel("test");
		concept.setQName(new QName("http://www.test.com", "123"));
		
		vocabulary.addConcept(concept);
		
		int numConcepts = vocabulary.getNumConcepts();
		Assert.assertEquals(numConcepts, 1);
		vocabulary.addConcept(concept);
		
		numConcepts = vocabulary.getNumConcepts();
		Assert.assertEquals(numConcepts, 1);
		
		concept.setPrefLabel("test2");
		vocabulary.updateConcept(concept);
		numConcepts = vocabulary.getNumConcepts();
		Assert.assertEquals(numConcepts, 1);
		
		vocabulary.removeConcept(concept);
		
		 numConcepts = vocabulary.getNumConcepts();
		Assert.assertEquals(numConcepts, 0);
	}
	
	public void testAddConcept() throws Exception 
	{
		
		// Add a concept to an empty store
		HiveConcept concept = new HiveConcept();
		concept.setPrefLabel("Test Concept");
		concept.setQName(new QName("http://hive.nescent.org/test/testConcept#", "concept"));
		concept.addBroaderConcept("http://hive.nescent.org/test/testBroader1#concept");
		concept.addBroaderConcept("http://hive.nescent.org/test/testBroader2#concept");
		concept.addNarrowerConcept("http://hive.nescent.org/test/testNarrower1#concept");
		concept.addRelatedConcept("http://hive.nescent.org/test/testRelated1#concept");
		vocabulary.addConcept(concept);
		
		// Placeholder concepts are created for all relations
		int numConcepts = vocabulary.getNumConcepts();
		Assert.assertEquals(numConcepts, 5);
		
		// Confirm that the 
		HiveConcept tmp = vocabulary.findConcept(new QName("http://hive.nescent.org/test/testConcept#", "concept"));
		Assert.assertEquals(tmp.getPrefLabel(), "Test Concept");
		Assert.assertEquals(tmp.getBroaderConcepts().get(0), "http://hive.nescent.org/test/testBroader1#concept");
		Assert.assertEquals(tmp.getBroaderConcepts().get(1), "http://hive.nescent.org/test/testBroader2#concept");
		Assert.assertEquals(tmp.getNarrowerConcepts().get(0), "http://hive.nescent.org/test/testNarrower1#concept");
		Assert.assertEquals(tmp.getRelatedConcepts().get(0), "http://hive.nescent.org/test/testRelated1#concept");
		
		HiveConcept tmp2 = new HiveConcept();
		tmp2.setPrefLabel("Broader Concept");
		tmp2.setQName(new QName("http://hive.nescent.org/test/testBroader1#", "concept"));
		vocabulary.updateConcept(tmp2);
		
		numConcepts = vocabulary.getNumConcepts();
		Assert.assertEquals(numConcepts, 5);
		
		HiveConcept tmp3 = vocabulary.findConcept(new QName("http://hive.nescent.org/test/testBroader1#", "concept"));
		Assert.assertEquals(tmp3.getPrefLabel(), "Broader Concept");

	}
	*/

}
