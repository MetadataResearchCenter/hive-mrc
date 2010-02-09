package edu.unc.ils.mrc.hive.importers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.openrdf.concepts.skos.core.Concept;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.nativerdf.NativeStore;

import edu.unc.ils.mrc.hive.ir.lucene.indexing.ConceptIndexer;
import edu.unc.ils.mrc.hive.ir.lucene.indexing.Indexer;

import edu.unc.ils.mrc.hive.admin.*;
import edu.unc.ils.mrc.hive.api.SKOSScheme;

public class SKOSImporter implements Importer {

	private Indexer indexer;
	private File SKOSfile;
	private File dbDirectory;
	private String indexDirectory;
	private NativeStore store;
	private SesameManager manager;
	private Repository repository;
	private SesameManagerFactory factory;
	private TreeMap<String, QName> alphaIndex;
	private TreeMap<String, QName> topConceptIndex;
	private String alphaIndexFile;
	private String topConceptIndexFile;
	private String vname;

//	public SKOSImporter(String SKOSfile, String dbDirectory,
//			String indexDirectory, String alphaIndexFile,
//			String topConceptIndexFile) {
	public SKOSImporter(SKOSScheme scheme) {
		this.topConceptIndexFile = scheme.getTopConceptIndexPath();
		this.topConceptIndex = new TreeMap<String, QName>();
		this.alphaIndexFile = scheme.getAlphaFilePath();
		this.alphaIndex = new TreeMap<String, QName>();
		this.indexDirectory = scheme.getIndexDirectory();
		this.SKOSfile = new File(scheme.getRdfPath());
		this.dbDirectory = new File(scheme.getStoreDirectory());
		this.indexer = new ConceptIndexer(this.indexDirectory, true);
		this.store = new NativeStore(this.dbDirectory);
		this.repository = new SailRepository(store);
		try {
			repository.initialize();
			ElmoModule module = new ElmoModule();
			factory = new SesameManagerFactory(module, repository);
			manager = factory.createElmoManager();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void importThesaurustoDB() {
		System.out.println("Indexing thesaurus in Dababase");
		try {
			manager.getConnection().add(this.SKOSfile, "", RDFFormat.RDFXML);
			for (Concept concept : manager.findAll(Concept.class)) {
				System.out.print("uri: ");
				System.out.println(concept.getQName().getNamespaceURI());
			}
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

	}

	public void importThesaurustoInvertedIndex() {
		System.out.println("Indexing Thesaurus in inverted index");
		for (Concept concept : this.manager.findAll(Concept.class)) {
			System.out.println("Indexing " + concept.getSkosPrefLabel()
					+ " concept");
			this.indexer.indexConcept(concept);
			this.alphaIndex.put(concept.getSkosPrefLabel(), concept.getQName());
			if (concept.getSkosBroaders().size() == 0
					&& concept.getSkosNarrowers().size() > 0) {
				this.topConceptIndex.put(concept.getSkosPrefLabel(), concept
						.getQName());
			}
		}
		this.createVocabularyMetadata();
		this.createAlphabeticIndex();
		this.createTopConceptIndex();
	}

	private void createVocabularyMetadata() {
		this.vname = this.SKOSfile.getName().replaceAll(".rdf", "");
	}

	private void createAlphabeticIndex() {
		File fichero = new File(this.alphaIndexFile);
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(fichero));
			oos.writeObject(this.alphaIndex);
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createTopConceptIndex() {
		File fichero = new File(this.topConceptIndexFile);
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(fichero));
			oos.writeObject(this.topConceptIndex);
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			this.manager.close();
			this.factory.close();
			this.repository.shutDown();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.indexer.close();
	}

}
