package edu.unc.ils.mrc.hive2.api.impl.test;

import java.io.File;
import java.util.List;


import javax.xml.namespace.QName;

import junit.framework.Assert;
import junit.framework.TestCase;

import edu.unc.ils.mrc.hive2.api.HiveConcept;
import edu.unc.ils.mrc.hive2.api.HiveIndex;
import edu.unc.ils.mrc.hive2.api.impl.HiveLuceneIndexImpl;

public class HiveLuceneIndexImplTest extends TestCase {

	public static void testCreate() throws Exception 
	{
		File tempDir = File.createTempFile("hive", null);
		tempDir.delete();
		tempDir.mkdir();
		HiveIndex index = new HiveLuceneIndexImpl(tempDir.getAbsolutePath(), "test");
		index.createIndex();
		
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
