package edu.unc.ils.mrc.hive.api;

import edu.unc.ils.mrc.hive.HiveException;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSSchemeImpl;
import junit.framework.Assert;
import junit.framework.TestCase;


public class SKOSSchemeTest extends TestCase {

	public void testStats() {
		
		try {
			String configpath = "conf";
			SKOSScheme scheme = new SKOSSchemeImpl(configpath, "agrovoc", false);
			long numBroader = scheme.getNumberOfBroader(); 
			Assert.assertEquals(numBroader, 27688);
			
			long numConcepts = scheme.getNumberOfConcepts();
			Assert.assertEquals(numConcepts, 28174);
			
			long numNarrower = scheme.getNumberOfNarrower(); 
			Assert.assertEquals(numNarrower, 27686);
			
			long numRelated = scheme.getNumberOfRelated(); 
			Assert.assertEquals(numRelated, 27712);
			
			long totalRelations = scheme.getNumberOfRelations(); 
			Assert.assertEquals(totalRelations, 83086);
			scheme.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
