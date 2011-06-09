/**
 * Copyright (c) 2011, UNC-Chapel Hill and Nescent

   All rights reserved.

   Redistribution and use in source and binary forms, with or without modification, are permitted provided 
   that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and 
    * the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
    * following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of the UNC-Chapel Hill or Nescent nor the names of its contributors may be used to endorse or promote 
    * products derived from this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
   STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
   ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.unc.ils.mrc.hive2.api.impl;

import java.io.File;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import edu.unc.ils.mrc.hive2.api.HiveConcept;
import edu.unc.ils.mrc.hive2.api.HiveIndex;

/**
 * Implements a Hive concept index using an embedded H2 database. The H2 store
 * supplements the primary triple store to provide faster access to concepts 
 * using traditional SQL.
 * 
 * @author craig.willis@unc.edu
 */
public class HiveH2IndexImpl implements HiveIndex
{
	private static final Log logger = LogFactory.getLog(HiveH2IndexImpl.class);
	
	/* Vocabulary name */
	String name;
	
	/* Base path for H2 store */
	String h2path;
	
	/* Full path for H2 store */
	String h2db;
	
	boolean inTransaction = false;
	
	/**
	 * Constructs a HiveH2IndexImpl for the specified vocabulary at the specified 
	 * path.
	 * 
	 * @param h2Path	Base path for H2 database for this vocabulary
	 * @param name		Vocabulary name
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public HiveH2IndexImpl(String h2path, String name) 
		throws ClassNotFoundException, SQLException 
	{
		this.name = name;
		
		// Construct H2 index paths
		//this.h2path = basePath + File.separator + "h2" + File.separator + name;
		this.h2path = h2path + File.separator + name.toLowerCase();
		this.h2db = h2path + File.separator + name.toLowerCase() + ".h2.db";
		
		init();
	}
	
	/**
	 * Initialize the H2 store
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected void init () throws ClassNotFoundException, SQLException 
	{
		logger.trace("init()");
		
		// Initialize an H2 connection pool
		String uri = "jdbc:h2:" + h2path;
		Class.forName("org.h2.Driver");	
		
		logger.debug("JDBC URI: " + uri);
		
		ObjectPool connectionPool = new GenericObjectPool(null); 
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(uri, "", "");
		new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true); 
		
		Class.forName("org.apache.commons.dbcp.PoolingDriver"); 
		PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:"); 
		driver.registerPool(name, connectionPool);
		
		// If the H2 store doesn't exist, create it.
		if (!exists())
			createIndex();
	}
	
	public boolean exists() {

		if (!new File(h2db).exists())
			return false;
		else
		{
			Connection con = null;
			try {
				con = getConnection();
				DatabaseMetaData dm = con.getMetaData();
				ResultSet rs = dm.getTables(null, null, "SETTINGS", null);
				if (rs.next())
					return true;
				else
					return false;
			} catch (SQLException e) {
				logger.error(e);
			} finally {
				if (con != null) {
					try {
						con.close();
					} catch (Exception e) { 
						logger.error(e);
					}
				}
			}
		}
		return false;
			
	}
	
	/**
	 * Create the H2 database structure
	 */
	public void createIndex() throws SQLException 
	{
		logger.trace("createIndex()" + h2path);
		
		Connection con = null;
		Statement s = null;
		PreparedStatement ps = null;
		try
		{
			con = getConnection();
			s = con.createStatement();
						
			// Create vocabulary settings

			s.execute("CREATE TABLE settings (" + 
						" name varchar(100)," + 
						" last_update timestamp," + 
						" created timestamp)"
						);
			
			// Create concept table 
			s.execute ("CREATE TABLE concept (" + 
						" id int identity, " + 
						" pref_label varchar(1000), " + 
						" pref_label_lower varchar(1000), " + 
						" uri varchar(1000), " + 
						" local_part varchar(1000), " +
						" is_top_concept int, " + 
						" is_leaf int )"
						);

			s.execute("CREATE INDEX idx_alpha_1 on concept (uri, local_part)");
			s.execute("CREATE INDEX idx_alpha_2 on concept (pref_label_lower)");
			
			Timestamp now = new Timestamp(System.currentTimeMillis());
			ps = con.prepareStatement("insert into settings values (?,?,?)");
			ps.setString(1, name);
			ps.setTimestamp(2, now);
			ps.setTimestamp(3, now);
			ps.execute();

			
		} finally {
			try {
				if (con != null) {
					con.close();
				}
				if (s != null) {
					s.close();
				}
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				logger.error(e);
			}
		}
	}
	

	
	/**
	 * Returns the date this HIVE index was last updated
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Date getLastUpdate() throws SQLException
	{
		logger.trace("getLastUpdate()");
		
		Connection con = null;
		Statement s = null;
		Date lastUpdate = null;
		try
		{
			con = getConnection();
			s = con.createStatement();
			ResultSet rs = s.executeQuery("select last_update from settings");
			if (rs.next())
				lastUpdate = new Date(rs.getTimestamp(1).getTime());
		} finally {
			try {
				if (con != null)
					con.close();
				if (s != null)
					s.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return lastUpdate;
	}
	
	
	/**
	 * Returns the date this HIVE index was created.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Date getCreated() throws SQLException
	{
		logger.trace("getCreated()");
		
		Connection con = null;
		Statement s = null;
		Date created = null;
		try
		{
			con = getConnection();
			s = con.createStatement();
			ResultSet rs = s.executeQuery("select created from settings");
			if (rs.next())
				created = new Date(rs.getTimestamp(1).getTime());
		} finally {
			try {
				if (con != null)
					con.close();
				if (s != null)
					s.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return created;
	}


	/**
	 * Adds the specified concept to the CONCEPT table
	 */
	public void addConcept(HiveConcept concept) throws SQLException 
	{
		logger.trace("addConcept: " + concept.getQName());
		
		Connection con = null;
		try
		{
			con = getConnection();
			
			String prefLabel = concept.getPrefLabel();
			if (prefLabel != null) 
			{
				QName qname = concept.getQName();
				boolean isTopConcept = false;
				boolean isLeafConcept = false;
				
				if (concept.getBroaderConcepts().size() == 0) 
					isTopConcept = true;
				
				if (concept.getNarrowerConcepts().size() == 0)
					isLeafConcept = true;
				
				insertConcept(con, concept.getPrefLabel(), qname.getNamespaceURI(), 
						qname.getLocalPart(), isTopConcept, isLeafConcept);    
	    	} else {
	    		logger.warn("Concept " + concept.getQName() + " missing prefLabel. Skipping.");
	    	}
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
	}
	
	

	/**
	 * Updates a record in the CONCEPT table
	 */
	public void updateConcept(HiveConcept concept) throws SQLException 
	{
		logger.trace("updateConcept: " + concept.getQName());
		
		Connection con = null;
		try
		{
			con = getConnection();
		
			String prefLabel = concept.getPrefLabel();
			if (prefLabel != null) 
			{
				QName qname = concept.getQName();
				boolean isTopConcept = false;
				boolean isLeafConcept = false;
				
				if (concept.getBroaderConcepts().size() == 0) 
					isTopConcept = true;
				
				if (concept.getNarrowerConcepts().size() == 0)
					isLeafConcept = true;
				
				insertConcept(con, concept.getPrefLabel(), qname.getNamespaceURI(), 
						qname.getLocalPart(), isTopConcept, isLeafConcept);    
	    	} else {
	    		logger.warn("Concept " + concept.getQName() + " missing prefLabel. Skipping.");
	    	}
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
	}

	/**
	 * Removes a record from the CONCEPT table
	 */
	public void removeConcept(QName qname) throws SQLException 
	{
		logger.trace("removeConcept: " + qname);
		
		Connection con = null;
		
		try
		{
			con = getConnection();
			
			deleteConcept(con, qname.getNamespaceURI(), 
					qname.getLocalPart());
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}

	}
	
	public List<HiveConcept> findConceptsByName(String name, boolean topOnly) throws SQLException {
		logger.trace("findConceptsByName()");
		
		List<HiveConcept> concepts = new ArrayList<HiveConcept>();
		
		Connection con = null;
		PreparedStatement ps = null;
		try
		{	
			con = getConnection();
			
			if (topOnly)
				ps = con.prepareStatement("select * from concept where pref_label_lower like ? and is_top_concept = 1 and is_leaf = 0 order by pref_label_lower");
			else
				ps = con.prepareStatement("select * from concept where pref_label_lower like ? order by pref_label_lower");
			ps.setString(1, name.toLowerCase());
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				String prefLabel = rs.getString("pref_label");
				String uri = rs.getString("uri");
				String localPart = rs.getString("local_part");
				boolean isTopConcept = rs.getBoolean("is_top_concept");
				boolean isLeaf = rs.getBoolean("is_leaf");
				
				HiveConcept hc = new HiveConcept();
				hc.setPrefLabel(prefLabel);
				hc.setQName(new QName(uri, localPart));
				hc.setTopConcept(isTopConcept);
				hc.setLeaf(isLeaf);
				concepts.add(hc);
		
			}
		} finally {
			try {
				if (con != null)
					con.close();
				if (ps != null)
					ps.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return concepts;
	}


	/**
	 * Returns the total number of concepts in this vocabulary
	 */
	public long getNumConcepts() throws Exception 
	{
		logger.trace("getNumConcepts()");
		
		long numConcepts = 0;
		Connection con = null;
		Statement s = null;
		try
		{	
			con = getConnection();
			s = con.createStatement();
			ResultSet rs = s.executeQuery("select count(*) from concept");
			if (rs.next())
				numConcepts = rs.getLong(1);
		} finally {
			try {
				if (con != null)
					con.close();
				if (s != null)
					s.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return numConcepts;
	}
	
	/**
	 * Returns the total number of top concepts in this vocabulary
	 * @return
	 * @throws Exception
	 */
	public long getNumTopConcepts() throws Exception 
	{
		logger.trace("getNumTopConcepts()");
		
		long numConcepts = 0;
		Connection con = null;
		Statement s = null;
		try
		{	
			con = getConnection();
			s = con.createStatement();
			ResultSet rs = 
				s.executeQuery("select count(*) from concept where is_top_concept = 1");
			if (rs.next())
				numConcepts = rs.getLong(1);
		} finally {
			try {
				if (con != null)
					con.close();
				if (s != null)
					s.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return numConcepts;
	}
	
	/**
	 * Get a connection from the pool
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		logger.trace("getConnection()");
		return DriverManager.getConnection("jdbc:apache:commons:dbcp:" + name);
	}
	
	/**
	 * Insert a record into the CONCEPT table
	 * @param con
	 * @param prefLabel
	 * @param uri
	 * @param localPart
	 * @throws SQLException
	 */
	protected void insertConcept(Connection con, String prefLabel, String uri, String localPart, 
			boolean isTopConcept, boolean isLeafConcept) throws SQLException
	{
		logger.trace("insertConcept: " + uri + localPart);
		
		// Delete existing concept record.
		if (!inTransaction)
			deleteConcept(con, uri, localPart);
		
		PreparedStatement ps = con.prepareStatement("insert into concept " + 
				"(pref_label, pref_label_lower, uri, local_part, is_top_concept, is_leaf) " + 
				" values (?, ?, ?, ?, ?, ?)");
		ps.setString(1, prefLabel);
		ps.setString(2, prefLabel.toLowerCase());
		ps.setString(3, uri);
		ps.setString(4, localPart);
		ps.setBoolean(5, isTopConcept);
		ps.setBoolean(6, isLeafConcept);
		ps.executeUpdate();
		ps.close();
		
		if (!inTransaction)
			setLastUpdate(con);
	}
	
	/**
	 * Delete a record from the CONCEPT table
	 * @param con
	 * @param uri
	 * @param localPart
	 * @throws SQLException
	 */
	protected void deleteConcept(Connection con, String uri, String localPart) throws SQLException
	{
		logger.trace("deleteConcept: " + uri + localPart);
		
		PreparedStatement ps = con.prepareStatement("delete from concept where uri=? and local_part = ?");
		ps.setString(1, uri);
		ps.setString(2, localPart);
		ps.execute();
		ps.close();
		
		setLastUpdate(con);
	}

	
	/**
	 * Sets the last update datetime to the current time
	 * @throws SQLException
	 */
	protected void setLastUpdate(Connection con) throws SQLException
	{
		
		PreparedStatement ps = null;
		try
		{
			ps = con.prepareStatement("update settings set last_update = ?");
			ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			ps.executeUpdate();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
	}

	@Override
	public void startTransaction() {
		this.inTransaction = true;
		
	}


	@Override
	public void commit() throws Exception {
		Connection con = null;
		try
		{
			con = getConnection();
			setLastUpdate(con);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			if (con != null) {
				try
				{
					con.close();
				} catch (SQLException e) {
					logger.error(e);
				}
			}		
		}		
	}
	
	public static void main (String[] args) {
		try {
			HiveH2IndexImpl h2 = new HiveH2IndexImpl("/Users/cwillis/dev/hive/hive-data/lcsh/lcshH2", "lcsh");
			h2.createIndex();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
