package kea.vocab;

import java.io.File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.openrdf.concepts.skos.core.Concept;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

import kea.stemmers.PorterStemmer;
import kea.stopwords.Stopwords;
import kea.stopwords.StopwordsEnglish;

public class VocabularyH2 extends Vocabulary {

	private static final long serialVersionUID = 1563256111040433036L;

	private SesameManager manager;

	public VocabularyH2(String h2path, String documentLanguage, SesameManager manager) 
		throws ClassNotFoundException, SQLException 
	{
		super(documentLanguage);
		this.manager = manager;
		
		String uri = "jdbc:h2:" + h2path;
		Class.forName("org.h2.Driver");
		
		initializeH2(h2path, uri);
		
		ObjectPool connectionPool = new GenericObjectPool(null); 
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(uri, "", "");
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true); 
		Class.forName("org.apache.commons.dbcp.PoolingDriver"); 
		PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:"); 
		driver.registerPool("hive", connectionPool); 	
	}
	

	private void initializeH2(String h2path, String uri) throws SQLException {
		File h2store = new File(h2path + ".h2.db");
		if (!h2store.exists())
		{
			Connection con = DriverManager.getConnection(uri);
			Statement s = con.createStatement();

			s.execute("CREATE TABLE vocabulary_en (id varchar(512) , value varchar(1024));");
			//s.execute("CREATE INDEX idx1 on vocabulary_en(id);");
			s.execute("CREATE TABLE vocabulary_enrev (id varchar(512) , value varchar(1024));");
			//s.execute("CREATE INDEX idx2 on vocabulary_enrev(id);");
			s.execute("CREATE TABLE vocabulary_rel (id varchar(512) , value varchar(1024) , relation varchar(20));");
			//s.execute("CREATE INDEX idx3 on vocabulary_rel(id);");
			s.execute("CREATE TABLE vocabulary_use ( id varchar(512) , value varchar(1024));");
			//s.execute("CREATE INDEX idx4 on vocabulary_use(id);");
			s.close();
		}
	}
	
	private void initializeIndexes() throws Exception {

		Connection con = getConnection();
		Statement s = con.createStatement();

		s.execute("CREATE INDEX idx1 on vocabulary_en(id);");
		s.execute("CREATE INDEX idx2 on vocabulary_enrev(id);");
		s.execute("CREATE INDEX idx3 on vocabulary_rel(id);");
		s.execute("CREATE INDEX idx4 on vocabulary_use(id);");
		s.close();
	}
	
	/**
	 * Starts initialization of the vocabulary.
	 * 
	 */
	public void initialize() {
	
	}


	protected Connection getConnection() throws Exception {
		return DriverManager.getConnection("jdbc:apache:commons:dbcp:hive");
	}

	/**
	 * Builds the vocabulary indexes from SKOS file.
	 */
	public void buildSKOS() throws Exception {

		Connection con = null;
		
		try {

			con = getConnection();
			int count = 1;

			for (Concept concept : manager.findAll(Concept.class)) {

				// id of the concept (Resource), e.g. "c_4828"
				String uri = concept.getQName().getNamespaceURI()
						+ concept.getQName().getLocalPart();

				// value of the property, e.g. c_4828 has narrower term "c_4829"
				//String val = concept.getSkosPrefLabel();

				/*
				 * For prefLabels
				 */
				String preferredLabel = concept.getSkosPrefLabel();
				String pseudoPhrase = pseudoPhrase(preferredLabel);
				if (pseudoPhrase == null) {
					pseudoPhrase = preferredLabel;
				}

				if (pseudoPhrase.length() > 1) {
					addConcept(con, uri, pseudoPhrase, preferredLabel);
				}

				/*
				 * For altLabels
				 */
				Set<String> altLabels = concept.getSkosAltLabels();
				for (String altLabel : altLabels) {
					addNonDescriptor(con, count, uri, altLabel);
					count++;
				}

				/*
				 * For broader terms
				 */
				String uriBroader;
				Set<Concept> broaders = concept.getSkosBroaders();
				for (Concept b : broaders) {
					uriBroader = b.getQName().getNamespaceURI()
							+ b.getQName().getLocalPart();

					addBroader(con, uri, uriBroader);
				}

				/*
				 * For narrower terms
				 */
				String uriNarrower;
				Set<Concept> narrowers = concept.getSkosNarrowers();
				for (Concept n : narrowers) {
					uriNarrower = n.getQName().getNamespaceURI()
							+ n.getQName().getLocalPart();
					addNarrower(con, uri, uriNarrower);
				}

				/*
				 * For related terms
				 */
				String uriRelated;
				Set<Concept> related = concept.getSkosRelated();
				for (Concept r : related) {
					uriRelated = r.getQName().getNamespaceURI()
							+ r.getQName().getLocalPart();
					addRelated(con, uri, uriRelated);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) { }
			}
		}
		initializeIndexes();
	}

	
	private void addConcept(Connection con, String uri, String pseudoPhrase, String preferredLabel) {
		try {
			String sql1 = "insert into vocabulary_en values (?,?)";
			String sql2 = "insert into vocabulary_enrev values (?,?)";
			
			PreparedStatement ps1 = con.prepareStatement(sql1);
			ps1.setString(1, pseudoPhrase);
			ps1.setString(2, uri);
			ps1.execute();
			ps1.close();
			
			PreparedStatement ps2 = con.prepareStatement(sql2);
			ps2.setString(1, uri);
			ps2.setString(2, preferredLabel);
			ps2.execute();
			ps1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private void addBroader (Connection con, String uri, String uriBroader) 
	{
		// insert into into related (uri, uri, 'broader');
		try {
			String sql = "insert into vocabulary_rel values (?,?, 'broader')";
			
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, uri);
			ps.setString(2, uriBroader);
			ps.execute();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void addNarrower (Connection con, String uri, String uriNarrower) {
		
		// insert into related (uri, uri, 'narrower');
		try {
			String sql = "insert into vocabulary_rel values (?,?,'narrower')";

			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, uri);
			ps.setString(2, uriNarrower);
			ps.execute();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void addRelated (Connection con, String uri, String uriRelated) {
		
		// insert into related (uri, uri, 'related')
		try {
			String sql = "insert into vocabulary_rel values (?,?,'related')";
			
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, uri);
			ps.setString(2, uriRelated);
			ps.execute();
			
			ps.setString(1, uriRelated);
			ps.setString(2, uri);
			ps.execute();			
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void addNonDescriptor(Connection con, int count, String uri, String altLabel) {

		String id_non_descriptor = "d_" + count;
		String avterm = pseudoPhrase(altLabel);
		
		try {
			String sql1 = "insert into vocabulary_en values (?, ?)";
			String sql2 = "insert into vocabulary_enrev values (?, ?)";
			String sql3 = "insert into vocabulary_use values (?, ?)";
			
			if (avterm.length() > 2) {
				PreparedStatement ps1 = con.prepareStatement(sql1);
				ps1.setString(1, avterm);
				ps1.setString(2, id_non_descriptor);
				ps1.execute();
				ps1.close();
				
				PreparedStatement ps2 = con.prepareStatement(sql2);
				ps2.setString(1, id_non_descriptor);
				ps2.setString(2, altLabel);
				ps2.execute();
				ps2.close();	
			}
			
			PreparedStatement ps3 = con.prepareStatement(sql3);
			ps3.setString(1, id_non_descriptor);
			ps3.setString(2, uri);
			ps3.execute();
			ps3.close();			

		} catch (Exception e) {
			e.printStackTrace();
		}		
	}


	/**
	 * Checks whether a normalized version of a phrase (pseudo phrase) is a
	 * valid vocabulary term.
	 * 
	 * @param phrase
	 * @return true if phrase is in the vocabulary
	 */
	public boolean containsEntry(String phrase) {
		
		// return VocabularyEN.containsKey(phrase);
		
		boolean found = false;
		try {
			String sql = "select value from vocabulary_en where id = ?";
			
			Connection con = getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, phrase);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				found = true;
			ps.close();
			//con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return found;
	}

	public String getID(String phrase) {
		String pseudo = pseudoPhrase(phrase);
		String id = null;
		if (pseudo != null) {
			try {
				String sql = "select value from vocabulary_en where id = ?";
				
				Connection con = getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setString(1, pseudo);
				ResultSet rs = ps.executeQuery();
				while (rs.next())
					id = rs.getString(1);
				ps.close();


				String sql2 = "select value from vocabulary_use where id = ?";

				PreparedStatement ps2 = con.prepareStatement(sql2);
				ps2.setString(1, id);
				ResultSet rs2 = ps2.executeQuery();
				// TODO: To replicate KEA behavior, use the last entry, sadly
				while (rs2.next())
					id = rs2.getString(1);
				ps2.close();
				//con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return id;
	}

	/* (non-Javadoc)
	 * @see kea.vocab.IVocabulary#getOrig(java.lang.String)
	 */
	@Override
	public String getOrig(String id) {

		
		String orig = null;
		// select preferredLabel from concept where id = id
		try {
			try {
				String sql = "select value from vocabulary_enrev where id = ?";
				
				Connection con = getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setString(1, id);
				ResultSet rs = ps.executeQuery();
				if (rs.next())
					orig = rs.getString(1);
				ps.close();

				//con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return orig;
	}

	/* (non-Javadoc)
	 * @see kea.vocab.IVocabulary#getDescriptor(java.lang.String)
	 */
	@Override
	public String getDescriptor(String id) {
		
		// return (String) VocabularyUSE.get(id);
		
		String desc = null;
		try {
			String sql = "select value from vocabulary_use where id = ?";
			
			Connection con = getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				desc = rs.getString(1);
			ps.close();
			//con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return desc;
	}

	/* (non-Javadoc)
	 * @see kea.vocab.IVocabulary#getRelated(java.lang.String)
	 */
	@Override
	public Vector<String> getRelated(String id) {
		//return (Vector) VocabularyREL.get(id);
		
		Vector<String> related = new Vector<String>();

		try {
			String sql = "select value from vocabulary_rel where id = ? and relation = 'related'";
			
			Connection con = getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				related.add(rs.getString(1));
			}
			ps.close();
			//con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return related;
		
	}

	@Override
	public Vector<String> getRelated(String id, String relation) {
		Vector<String> related = new Vector<String>();

		try {
			String sql = "select value from vocabulary_rel where id = ? and relation = ?";
			
			Connection con = getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, id);
			ps.setString(2, relation);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				related.add(rs.getString(1));
			}
			ps.close();
			//con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return related;

	}

	public static void main(String[] args) throws Exception {
		
		NativeStore store = new NativeStore(new File("/Users/cwillis/dev/hive/hive-data/agrovoc/agrovocStore"));
		//NativeStore store = new NativeStore(new File("/Users/cwillis/dev/hive/hive-data/lcsh/lcshStore"));
		Repository repository = new SailRepository(store);
        repository.initialize();            
        ElmoModule module = new ElmoModule();           
        SesameManagerFactory factory = new SesameManagerFactory(module, repository);         
        // Create a new ElmoManager with default locale
        SesameManager manager = factory.createElmoManager(); 
        String h2path = "/Users/cwillis/dev/hive/hive-data/agrovoc/agrovocH2/agrovoc";
        //String h2path = "/Users/cwillis/dev/hive/hive-data/lcsh/lcshH2/lcsh";
		VocabularyH2 voc = new VocabularyH2(h2path, "en", manager);
		Stopwords sw = new StopwordsEnglish("/Users/cwillis/dev/hive/hive-data/agrovoc//agrovocKEA/data/stopwords/stopwords_en.txt");
		voc.setStopwords(sw);
		voc.setStemmer(new PorterStemmer());
		voc.buildSKOS();
		
		
	}

	@Override
	public void build() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void buildUSE() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void buildREL() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void buildRT() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
