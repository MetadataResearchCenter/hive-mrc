package maui.vocab;

import java.io.File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import maui.stemmers.Stemmer;
import maui.stopwords.Stopwords;

/**
 * Indexes the content of the controlled vocabulary into an embedded H2
 * database. Accepts vocabularies only as rdf files (SKOS format).

 * @author craig.willis
 */

public class VocabularyH2 implements Vocabulary
{
	
	/** Document language */
	private String language = "en";

	/** Document encoding */
	private String encoding = "UTF-8";

	/** Default stemmer to be used */
	private Stemmer stemmer;

	/** List of stopwords to be used */
	private Stopwords stopwords;

	/** Normalization to lower case - defaulte no */
	private boolean toLowerCase = true;

	/** Normalization via alphabetic reordering - default true*/
	private boolean reorder = true;
	
	private boolean debugMode = false;

	private String vocabularyName = "";

	/** Vocabulary constructor. 
	 * 
	 * Given the name of the vocabulary and the format, it first checks whether
	 * the data/vocabularies directory contains the specified files:<br>
	 * - vocabularyName.rdf if skos format is selected<br>
	 * - or a set of 3 flat txt files starting with vocabularyName and with extensions<br>
	 * <li>.en (id term)
	 * <li>.use (non-descriptor \t descriptor)
	 * <li>.rel (id \t related_id1 related_id2 ...)
	 * If the required files exist, the vocabulary index is built.
	 * 
	 * @param vocabularyName The name of the vocabulary file (before extension).
	 * @param vocabularyFormat The format of the vocabulary (skos or text).
	 * @throws Exception 
	 * */
	public VocabularyH2(String vocabularyName, String vocabularyDirectory, String h2Path)
			throws Exception {

	    this.vocabularyName = vocabularyName;
		
        // Initialize an H2 connection pool
        String uri = "jdbc:h2:" + h2Path + File.separator + vocabularyName;
        Class.forName("org.h2.Driver");

        ObjectPool connectionPool = new GenericObjectPool(null);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(uri, "", "");
        new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);

        Class.forName("org.apache.commons.dbcp.PoolingDriver");
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:"); 
        driver.registerPool(vocabularyName, connectionPool);
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setLowerCase(boolean toLowerCase) {
		this.toLowerCase = toLowerCase;
	}

	public void setReorder(boolean reorder) {
		this.reorder = reorder;
	}

	public void setStemmer(Stemmer stemmer) {
		this.stemmer = stemmer;
	}
	
	public void setDebug(boolean debugMode) {
		this.debugMode = debugMode;
	}

	/**
	 * Starts initialization of the vocabulary.
	 * @throws Exception 
	 *
	 */
	public void initialize() throws Exception {

	}

	/**
	 * Set the stopwords class.
	 * @param stopwords 
	 */
	public void setStopwords(Stopwords stopwords) {
		this.stopwords = stopwords;
	}


    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:apache:commons:dbcp:" + vocabularyName);
    }    
    
	/**
	 * Returns the id of the given term
	 * @param phrase
	 * @return term id
	 */
    public String getID(String phrase) 
    {
        String id = null;
        if (phrase != null) {
            Connection con = null;
            PreparedStatement ps = null;
            PreparedStatement ps2 = null;
            try {
            	String sql = "select value from vocabulary_en where id = ?";
                
                con = getConnection();
                ps = con.prepareStatement(sql);
                ps.setString(1, phrase.toLowerCase());
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    id = rs.getString(1);
                
                if (id == null)
                {
                	String sql2 = "select value from vocabulary_use where id = ?";
    
                    ps2 = con.prepareStatement(sql2);
                    ps2.setString(1, phrase);
                    ResultSet rs2 = ps2.executeQuery();
                    while (rs2.next())
                    {
                        id = rs2.getString(1);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try
                {
                    if (con != null) 
                        con.close();
                    if (ps != null)
                        ps.close();
                    if (ps2 != null)
                        ps2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return id;
    }
    
    public String getIDFromPrefLabel(String prefLabel) 
    {
        String id = null;
        if (prefLabel != null) {
            Connection con = null;
            PreparedStatement ps = null;
            PreparedStatement ps2 = null;
            try {
            	String sql = "select id from vocabulary_enrev where value = ?";
                
                con = getConnection();
                ps = con.prepareStatement(sql);
                ps.setString(1, prefLabel);
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    id = rs.getString(1);
                rs.close();
                
            	String sql2 = "select value from vocabulary_use where id = ?";

                ps2 = con.prepareStatement(sql2);
                ps2.setString(1, id);
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next())
                    id = rs2.getString(1);
                rs2.close();
                
        
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try
                {
                    if (con != null) 
                        con.close();
                    if (ps != null)
                        ps.close();
                    if (ps2 != null)
                        ps2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return id;
    }

    /**
     * Returns the id of the given term
     * @param phrase
     * @return term id
     */
    public Vector<String> getIDs(String normalized) 
    {
        Vector<String> ids = new Vector<String>();

        
        if (normalized != null) {
            Connection con = null;
            PreparedStatement ps = null;
            PreparedStatement ps2 = null;
            try {
                String sql = "select value from vocabulary_en where id = ?";
                
                con = getConnection();
                ps = con.prepareStatement(sql);
                ps.setString(1, normalized);
                ResultSet rs = ps.executeQuery();
                List<String> tmp = new ArrayList<String>();
                while (rs.next()) {
                    String id = rs.getString(1);
                    tmp.add(id);
                }
                
                String sql2 = "select value from vocabulary_use where id = ?";
                ps2 = con.prepareStatement(sql2);
                for (String id: tmp)
                {
                    ps2.setString(1, id);
                    ResultSet rs2 = ps2.executeQuery();
                    if (rs2.next())
                    {
                        String id2 = rs2.getString(1);
                        ids.add(id2);
                    }
                    else
                    {
                    	ids.add(id);
                    }
                }


            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try
                {
                    if (con != null) 
                        con.close();
                    if (ps != null)
                        ps.close();
                    if (ps2 != null)
                        ps2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return ids;
    }


	/**
	 * Checks whether a normalized phrase 
	 * is a valid vocabulary term.
	 * @param phrase
	 * @return true if phrase is in the vocabulary
	 */
	public boolean containsNormalizedEntry(String phrase) {
		return getIDs(normalizePhrase(phrase)).size() > 0;
	}

	/**
	 * Returns true if a phrase has more than one senses
	 * @param phrase
	 * @return false if a phrase has only one sense
	 */
	public boolean isAmbiguous(String phrase) {
	    return getIDs(normalizePhrase(phrase)).size() > 1;
	}

	/**
	 * Retrieves all possible descriptors for a given phrase
	 * @param phrase
	 * @return a vector list of all senses of a given term
	 */
	public Vector<String> getSenses(String phrase) 
	{
		String normalized = normalizePhrase(phrase);

		return getIDs(normalized);
	}
	
 
	
	/** 
	 * Generates the preudo phrase from a string.
	 * A pseudo phrase is a version of a phrase
	 * that only contains non-stopwords,
	 * which are stemmed and sorted into alphabetical order. 
	 */
	public String normalizePhrase(String phrase) {		
		
		if (toLowerCase) {
			phrase = phrase.toLowerCase();
		}
		
		if (toLowerCase) {
			phrase = phrase.toLowerCase();
		}
		StringBuffer result = new StringBuffer();
		char prev = ' ';
		int i = 0;
		while (i < phrase.length()) {
			char c = phrase.charAt(i);
			
			// we ignore everything after the "/" symbol and everything in brackets
			// e.g. Monocytes/*immunology/microbiology -> monocytes
			// e.g. Vanilla (Spice) -> vanilla
			if (c == '/' || c == '(') 
				break;
			
			if (c == '-' ||  c == '&' || c == '.' || c == '.') 
				c = ' ';
				
			if (c == '*' || c == ':') {
				prev = c;
				i++;
				continue;
			}
			
			if (c != ' ' || prev != ' ')
				result.append(c);
			
			prev = c;
			i++;
		}

	
		phrase = result.toString().trim();
		
		if (reorder || stopwords != null || stemmer != null) {
			phrase = pseudoPhrase(phrase);
		} 
		if (phrase.equals("")) {
			// to prevent cases where the term is a stop word (e.g. Back).
			return result.toString(); 
		} else {
			return phrase;
		}
	}

	/** 
	 * Generates the preudo phrase from a string.
	 * A pseudo phrase is a version of a phrase
	 * that only contains non-stopwords,
	 * which are stemmed and sorted into alphabetical order. 
	 */
	public String pseudoPhrase(String str) {
		String result = "";
		String[] words = str.split(" ");
		if (reorder) {
			Arrays.sort(words);
		}
		for (String word : words) {

			if (stopwords != null) {
				if (stopwords.isStopword(word)) {
					continue;
				}
			}

			int apostr = word.indexOf('\'');
			if (apostr != -1) {
				word = word.substring(0, apostr);
			}

			if (stemmer != null) {
				word = stemmer.stem(word);
			}
			result += word + " ";
		}
		return result.trim();
	}
    
    /**
     * Returns the term for the given id
     * @param id - id of some phrase in the vocabulary
     * @return phrase, i.e. the full form listed in the vocabulary
     */
    public String getTerm(String id) 
    {
        String orig = null;

        Connection con = null;
        PreparedStatement ps = null;
        try {
        	String sql = "select value from vocabulary_enrev where id = ?";
            
            con = getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                orig = rs.getString(1);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try
            {
                if (con != null) 
                    con.close();
                if (ps != null)
                    ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
            
        return orig;
    }


    public Vector<String> getRelated(String id) 
    {
        
        Vector<String> related = new Vector<String>();

        Connection con = null;
        PreparedStatement ps = null;
        try {
        	String sql = "select value from vocabulary_rel where id = ? and relation = 'related'";
            
            con = getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                related.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try
            {
                if (con != null) 
                    con.close();
                if (ps != null)
                    ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return related;
        
    }

    /**
     * Given an ID of a term gets the list of all IDs of terms
     * that are semantically related to the given term
     * with a specific relation
     * @param id - id of some term in the vocabulary
     * @param relation - a given semantic relation
     * @return a vector with ids related to the input id by a specified relation
     */
    public Vector<String> getRelated(String id, String relation) 
    {
        Vector<String> related = new Vector<String>();

        Connection con = null;
        PreparedStatement ps = null;
        try {
        	String sql = "select value from vocabulary_rel where id = ? and relation = ?";
            
            con = getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, id);
            ps.setString(2, relation);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                related.add(rs.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try
            {
                if (con != null) 
                    con.close();
                if (ps != null)
                    ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return related;
    }
 
}
