package edu.unc.ils.mrc.hive.converter.itis;

import java.io.File;


import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import edu.unc.ils.mrc.hive.converter.itis.model.Taxon;
import edu.unc.ils.mrc.hive.converter.itis.model.Taxonomy;


public class ITISConverter {

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws SQLException,
			FileNotFoundException {

		File out = new File("/tmp/itis.rdf");
		PrintWriter pw = new PrintWriter(out);

		Taxonomy taxonomy = new Taxonomy();

		// Create a data source and set access data
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser("root");
		//dataSource.setPassword("l3g10nar10");
		dataSource.setDatabaseName("ITIS");
		dataSource.setServerName("localhost");

		// Getting the connection
		Connection conexion = dataSource.getConnection();
		
		Statement st = conexion.createStatement();
		ResultSet rs = st.executeQuery("select * from longnames");
		Map<String, String> tsnMap = new HashMap<String, String>();
		while (rs.next()) // printing results
		{
			String tsn = rs.getString("tsn");
			String name = rs.getString("completename");
			tsnMap.put(tsn, name);
		}
		st.close();
		rs.close();
		
		Statement st2 = conexion.createStatement();
		ResultSet rs2 = st2.executeQuery("select tsn,parent_tsn, name_usage from taxonomic_units");
		Map<String, String> parentMap = new HashMap<String, String>();
		Map<String, Boolean> acceptedMap = new HashMap<String, Boolean>();
		HashMap<String, ArrayList<String>> childMap = new HashMap<String, ArrayList<String>>();
		while (rs2.next()) {
			String tsn = rs2.getString("tsn");
			String parentTsn = rs2.getString("parent_tsn");
			parentMap.put(tsn, parentTsn);
			ArrayList<String> children = childMap.get(parentTsn);
			if (children == null) {
				children = new ArrayList<String>();
				children.add(tsn);
				childMap.put(parentTsn, children);
			} else {
				children.add(tsn);
				childMap.put(parentTsn, children);
			}
			
			String nameUsage = rs2.getString("name_usage");
			boolean accepted = false;
			if (nameUsage.equals("accepted") || nameUsage.equals("valid"))
				accepted = true;
				
			acceptedMap.put(tsn, accepted);
		}
		st2.close();
		rs2.close();

		// getting parent URI
		Statement st3 = conexion.createStatement();
		ResultSet rs3 = st3.executeQuery("select tsn, vernacular_name from vernaculars");
		Map<String, List<String>> commonNames = new HashMap<String, List<String>>();
		while (rs3.next()) {
			String tsn = rs3.getString("tsn");
			String name = rs3.getString("vernacular_name");
			
			List<String> names = commonNames.get(tsn);
			if (names == null) {
				names = new ArrayList<String>();
				names.add(name);
				commonNames.put(tsn, names);
			} else {
				names.add(name);
				commonNames.put(tsn, names);
			}
		}
		st3.close();
		rs3.close();

		Statement st4 = conexion.createStatement();
		ResultSet rs4 = st4.executeQuery("select tsn, tsn_accepted from synonym_links");
		Map<String, List<String>> synonyms = new HashMap<String, List<String>>();
		while (rs4.next()) {
			String tsnSynonym = rs4.getString("tsn");
			String tsnAccepted = rs4.getString("tsn_accepted");
			String name = tsnMap.get(tsnSynonym);
			
			List<String> tsns = synonyms.get(tsnAccepted);
			if (tsns == null) {
				tsns = new ArrayList<String>();
				tsns.add(name);
				synonyms.put(tsnAccepted, tsns);
			} else {
				
				tsns.add(name);
				synonyms.put(tsnAccepted, tsns);
			}
		}
		st4.close();
		rs4.close();
		
		for (String tsn: tsnMap.keySet()) {
			if (acceptedMap.get(tsn) == null || !acceptedMap.get(tsn))
				continue;
			
			String parentTsn = parentMap.get(tsn);
			List<String> syns = new ArrayList<String>();
			
			List<String> children = childMap.get(tsn);
			
			List<String> vernaculars = commonNames.get(tsn);
			List<String> s = synonyms.get(tsn);
			
			String name = tsnMap.get(tsn);
			
			if (s != null)
				syns.addAll(s);
			
			if (vernaculars != null)
				syns.addAll(vernaculars);
			
			Taxon taxon = new Taxon(tsn);
			taxon.setLongName(name);
			if (!parentTsn.equals("0"))
				taxon.setParent(parentTsn);
			taxon.setChilds(children);
			taxon.setSynonyms(syns);
			taxonomy.putTaxon(tsn, taxon);
			System.out.println("TSN " + tsn + ", completename= " + name);
		}
		
		pw.println(taxonomy.getHeader());
		for (Taxon t : taxonomy.getTaxons()) {
			pw.println(t.toSKOS());
		}
		pw.println(taxonomy.getFooter());

		// Closing database connections
		conexion.close();

		pw.close();

	}

}
