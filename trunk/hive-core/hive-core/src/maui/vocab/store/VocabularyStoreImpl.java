package maui.vocab.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

public class VocabularyStoreImpl implements VocabularyStore {

	private String RDFFile;
	private SesameManager manager;
	private String format;
	private String name;
	private String sesameStore;

	public VocabularyStoreImpl(String propertiesFile) {
		Properties properties = new Properties();
		try {
			FileInputStream fis = new FileInputStream(propertiesFile);
			properties.load(fis);
			this.RDFFile = properties.getProperty("rdf");
			this.sesameStore = properties.getProperty("sesameStore");
			this.format = properties.getProperty("format");
			this.name = properties.getProperty("name");
			if(this.sesameStore!=null)
				this.initSesameRepository();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initSesameRepository() {
		NativeStore store = new NativeStore(new File(this.sesameStore));
		Repository repository = new SailRepository(store);
		try {
			repository.initialize();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ElmoModule module = new ElmoModule();
		SesameManagerFactory factory = new SesameManagerFactory(module, repository);
		this.manager = factory.createElmoManager();
	}

	@Override
	public String getRDFFile() {
		// TODO Auto-generated method stub
		return this.RDFFile;
	}

	@Override
	public SesameManager getSesameManager() {
		// TODO Auto-generated method stub
		return this.manager;
	}

	@Override
	public String getVocabularyFormat() {
		// TODO Auto-generated method stub
		return this.format;
	}

	@Override
	public String getVocabularyName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public String getStore() {
		return this.sesameStore;
	}

}
