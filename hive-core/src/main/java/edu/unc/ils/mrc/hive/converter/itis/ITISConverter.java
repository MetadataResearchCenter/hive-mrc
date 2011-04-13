package edu.unc.ils.mrc.hive.converter.itis;

import java.io.File;


import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

		// Creating an statement to execute queries
		Statement st = conexion.createStatement();
		Statement st2 = conexion.createStatement();
		Statement st3 = conexion.createStatement();
		Statement st4 = conexion.createStatement();
		Statement st5 = conexion.createStatement();

		// Firing queries and getting results in a Result set
		// Kingdoms information
		ResultSet rs = st.executeQuery("select * from longnames");
		String tsn;
		String longname;
		while (rs.next()) // printing results
		{
			// getting longnames for every taxon
			tsn = Integer.toString((Integer) rs.getObject("tsn"));
			Taxon taxon = new Taxon(tsn);
			longname = (String) rs.getObject("completename");
			taxon.setLongName(longname);

			// getting pareTt URI
			ResultSet rs2 = st2
					.executeQuery("select tsn,parent_tsn from taxonomic_units where tsn="
							+ tsn);
			String parent_tsn = "";
			while (rs2.next()) // printing results
			{
				parent_tsn = Integer.toString((Integer) rs2
						.getObject("parent_tsn"));
			}
			ResultSet rs3 = st3
					.executeQuery("select completename from longnames where tsn="
							+ parent_tsn);
			while (rs3.next()) // printing results
			{
				String parent_longname = (String) rs3.getObject("completename");
				taxon.setParent(parent_longname);
			}

			// getting children's URI
			ResultSet rs4 = st4
					.executeQuery("select * from hierarchy where hierarchy_string like \"%"
							+ tsn + "%\"");
			while (rs4.next()) // printing results
			{
				String hierarchy_string = (String) rs4
						.getObject("hierarchy_string");
				String subhierarchy = hierarchy_string.substring(
						hierarchy_string.indexOf(tsn), hierarchy_string
								.length());
				String[] elements = subhierarchy.split("-"); 
				if (elements.length == 2) {
					ResultSet rs5 = st5
							.executeQuery("select * from longnames where tsn="
									+ elements[1]);
					while (rs5.next()) // printing results
					{
						String child_longname = (String) rs5
								.getObject("completename");
						taxon.addchild(child_longname);
					}
				}
			}
			//System.out.println(taxon.toSKOS());
			taxonomy.putTaxon(tsn, taxon);
			System.out.println("TSN " + tsn + ", completename= " + longname);
		}

		pw.println(taxonomy.getHeader());
		for (Taxon t : taxonomy.getTaxons()) {
			pw.println(t.toSKOS());
		}
		pw.println(taxonomy.getFooter());

		// Closing database connections
		rs.close();
		st.close();
		conexion.close();

		pw.close();

	}

}
