package edu.unc.ils.mrc.hive.converter.itis.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class DBConnector {

	/**
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
		// Create a data source and set access data
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser("root");
		dataSource.setPassword("l3g10nar10");
		dataSource.setDatabaseName("ITIS");
		dataSource.setServerName("localhost");

		// Getting the connection
		Connection conexion = dataSource.getConnection();

		// Creating an statement to execute queries
		Statement st = conexion.createStatement();

		// Firing queries and getting results in a Result set
		// Kingdoms information
		ResultSet rs = st.executeQuery("select * from kingdoms");
		while (rs.next()) // printing results
		{
			System.out.println("Kingdom ID= " + rs.getObject("kingdom_id")
					+ ", Kingdom Name= " + rs.getObject("kingdom_name")
					+ ", Update date= " + rs.getObject("update_date"));
		}

		//taxons information (the most important)
		rs = st.executeQuery("select tsn,unit_name1,unit_name2,unit_name3 from " +
				"taxonomic_units where tsn=779880");
		while (rs.next()) // printing results
		{
			System.out.println("TSN " + rs.getObject("tsn") + ", name1= "
					+ rs.getObject("unit_name1") + ", name2= "
					+ rs.getObject("unit_name2") + ", name3= "
					+ rs.getObject("unit_name3"));
			// ", name4= "+rs.getObject("unit_name4"));
		}
		
		// Longnames information
		rs = st.executeQuery("select * from longnames where tsn=779880;");
		while (rs.next()) // printing results
		{
			System.out.println("TSN " + rs.getObject("tsn") + ", completename= "
					+ rs.getObject("completename"));
		}

		//Hierarchical information
		rs = st.executeQuery("select * from hierarchy where hierarchy_string like \"%779880\"");
		while (rs.next()) // printing results
		{
			System.out.println("hierarchy_string= "
					+ rs.getObject("hierarchy_string"));
		}
		
		// Taxonomic Rank information
		rs = st.executeQuery("select * from taxon_unit_types");
		while (rs.next()) // printing results
		{
			System.out.println("rank_name= "
					+ rs.getObject("rank_name"));
		}
		
		// Closing database connections
		rs.close();
		st.close();
		conexion.close();

	}

}
