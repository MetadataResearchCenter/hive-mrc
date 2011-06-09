package edu.unc.ils.mrc.hive2.api.impl.test;


import java.io.File;

import java.util.Date;

import javax.xml.namespace.QName;

import junit.framework.Assert;
import junit.framework.TestCase;

import edu.unc.ils.mrc.hive2.api.HiveConcept;
import edu.unc.ils.mrc.hive2.api.HiveIndex;
import edu.unc.ils.mrc.hive2.api.impl.HiveH2IndexImpl;;

public class HiveH2IndexImplTest extends TestCase {

	HiveH2IndexImpl index = null;
	public void setUp() throws Exception
	{
		File tempDir = File.createTempFile("hive", null);
		tempDir.delete();
		tempDir.mkdir();
		index = new HiveH2IndexImpl(tempDir.getAbsolutePath(), "test");	
	}



	
	public void testCreate() throws Exception
	{
		index.createIndex();
		Date created = ((HiveH2IndexImpl)index).getCreated();
		Date lastUpdate = ((HiveH2IndexImpl)index).getLastUpdate();
		Assert.assertNotNull(created);
		Assert.assertNotNull(lastUpdate);
		
		HiveConcept concept = new HiveConcept();
		concept.setPrefLabel("test");
		concept.setQName(new QName("http://www.test.com", "123"));
		
		index.addConcept(concept);
		
		long numConcepts = index.getNumConcepts();
		Assert.assertEquals(numConcepts, 1);
		index.addConcept(concept);
		
		numConcepts = index.getNumConcepts();
		Assert.assertEquals(numConcepts, 1);
		
		concept.setPrefLabel("test2");
		index.updateConcept(concept);
		numConcepts = index.getNumConcepts();
		Assert.assertEquals(numConcepts, 1);
		
		index.removeConcept(concept.getQName());
		
		numConcepts = index.getNumConcepts();
		Assert.assertEquals(numConcepts, 0);
		
	}
}
