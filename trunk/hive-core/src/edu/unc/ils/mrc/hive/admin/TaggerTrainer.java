package edu.unc.ils.mrc.hive.admin;

import java.io.File;

import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.ir.tagging.KEAModelGenerator;

/*
 * This class train the tagger from a SKOS/RDF file and some domain oriented 
 * documents which are used like trainning set for KEA algorithm
 */

public class TaggerTrainer {
	
	private SKOSScheme schema;

	public TaggerTrainer(SKOSScheme schema) {
		this.schema = schema;

	}

	public void trainAutomaticIndexingModule() {
		NativeStore store = new NativeStore(
				new File(schema.getStoreDirectory()));
		Repository repository = new SailRepository(store);
		try {
			repository.initialize();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ElmoModule module = new ElmoModule();
		SesameManagerFactory factory = new SesameManagerFactory(module,
				repository);
		SesameManager manager = factory.createElmoManager();

		this.schema.setManager(manager);
		KEAModelGenerator generator = new KEAModelGenerator(this.schema);
		generator.createModel(this.schema.getStopwordsPath());
		System.out.println("Model created");
		manager.close();
		factory.close();
		try {
			repository.shutDown();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @throws RepositoryException
	 */
	public static void main(String[] args) throws RepositoryException {
		// String trainDir = "/home/hive/hive-data/mesh/meshKEA/train";
		// String modelPath = "/home/hive/hive-data/mesh/meshKEA/mesh";
		// String vocabularyPath = "/home/hive/hive-data/mesh/mesh2.rdf";
		// String stopwordsPath =
		// "/home/hive/hive-data/mesh/meshKEA/data/stopwords/stopwords_en.txt";
		//		
		// String confPath = "/home/hive/workspace/hive-core/conf/";
		// String vocabularyName = "mesh";

//		String trainDir = "/home/hive/hive-data/nbii/nbiiKEA/train";
//		String modelPath = "/home/hive/hive-data/nbii/nbiiKEA/nbii";
//		String vocabularyPath = "/home/hive/hive-data/nbii/nbii3.rdf";
//		String stopwordsPath = "/home/hive/hive-data/nbii/nbiiKEA/data/stopwords/stopwords_en.txt";
//
//		String confPath = "/home/hive/workspace/hive-core/conf/";
//		String vocabularyName = "nbii";

		// String trainDir = "/home/hive/hive-data/lcsh/lcshKEA/train";
		// String modelPath = "/home/hive/hive-data/lcsh/lcshKEA/lcsh";
		// String vocabularyPath = "/home/hive/hive-data/lcsh/lcsh.rdf";
		// String stopwordsPath =
		// "/home/hive/hive-data/lcsh/lcshKEA/data/stopwords/stopwords_en.txt";
		//		
		// String confPath = "/home/hive/workspace/hive-core/conf/";
		// String vocabularyName = "lcsh";

		// String trainDir = "/home/hive/hive-data/agrovoc/agrovocKEA/train";
		// String modelPath = "/home/hive/hive-data/agrovoc/agrovocKEA/agrovoc";
		// String vocabularyPath = "/home/hive/hive-data/agrovoc/agrovoc.rdf";
		// String stopwordsPath =
		// "/home/hive/hive-data/agrovoc/agrovocKEA/data/stopwords/stopwords_en.txt";
		//		
		// String confPath = "/home/hive/workspace/hive-core/conf/";
		// String vocabularyName = "agrovoc";

//		SKOSScheme schema = new SKOSSchemeImpl(confPath, vocabularyName);
//
//		NativeStore store = new NativeStore(
//				new File(schema.getStoreDirectory()));
//		Repository repository = new SailRepository(store);
//		repository.initialize();
//		ElmoModule module = new ElmoModule();
//		SesameManagerFactory factory = new SesameManagerFactory(module,
//				repository);
//		SesameManager manager = factory.createElmoManager();
//
//		schema.setManager(manager);
//
//		KEAModelGenerator generator = new KEAModelGenerator(trainDir,
//				modelPath, vocabularyPath, stopwordsPath, schema);
//		generator.createModel(stopwordsPath);
//		System.out.println("Model created");
//		manager.close();
//		factory.close();
//		repository.shutDown();
	}

}
