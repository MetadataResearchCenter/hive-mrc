package edu.unc.ils.mrc.hive.ir.tagging;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Date;

import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

import edu.unc.ils.mrc.hive.HiveException;
import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSSchemeImpl;
import junit.framework.TestCase;

public class KEATaggerTest extends TestCase {
	
	public void testExtractKeyphrases() {
		String confPath = "/usr/local/hive/conf";
		String vocabularyName = "agrovoc";
		SKOSScheme scheme = null;
		try {
			scheme = new SKOSSchemeImpl(confPath, vocabularyName, false);
		} catch (HiveException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		KEATagger tagger = new KEATagger(scheme.getKEAtestSetDir(), scheme.getKEAModelPath(),
				scheme.getStopwordsPath(), scheme);
		
		// Copy file to test set dir
		File inFile = new File(scheme.getKEAtrainSetDir() + File.separator + "bostid_b02moe.txt");
		String path = scheme.getKEAtestSetDir();
		String fileName = path + File.separator + new Date().getTime();
		File outFile = new File(fileName + ".txt");
		try {
			FileReader r = new FileReader(inFile);
			FileWriter w = new FileWriter(outFile);
			int c;
			while ((c = r.read()) != -1) {
				w.write(c);
			}
			r.close();
			w.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		long start = System.currentTimeMillis();
		tagger.extractKeyphrases(10, 1);
		long end = System.currentTimeMillis();
		System.out.println("Duration: " + (end - start));

		//File keyFile = new File(fileName + ".key");
		//keyFile.delete();
		//outFile.delete();
	}
	
	public static void main(String[] args) {
		KEATaggerTest test = new KEATaggerTest();
		test.testExtractKeyphrases();
	}
}
