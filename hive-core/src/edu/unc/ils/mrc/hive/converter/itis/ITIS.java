package edu.unc.ils.mrc.hive.converter.itis;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class ITIS {

	public static void main(String[] args) throws SQLException {
		ITISThesaurus thesaurus = new ITISThesaurus();

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

		ResultSet rs = st.executeQuery("select * from longnames");

		try {

			while (rs.next()) {

				Object tsn = rs.getObject("tsn");
				String termID = tsn.toString();

				Object completename = rs.getObject("completename");
				String termName = completename.toString();

				if (!thesaurus.contains(termID)) {
					ITISRecord record = new ITISRecord(termID);
					record.setTermName(termName);
					thesaurus.setRecord(record);
				}

			}

			System.out.println(thesaurus.size());

			rs = st.executeQuery("select * from hierarchy");

			while (rs.next()) {

				Object hierarchy_string = rs.getObject("hierarchy_string");
				String hs = hierarchy_string.toString();
				String[] ids = hs.split("-");

				if(ids.length==1)	
					System.out.println(hs);
				
				for (int i = 0; i <= ids.length - 1; i++) {
					if (i == 0 && ids.length > 1) {
						String na = thesaurus.getRecord(ids[i + 1]).getTermID();
						thesaurus.getRecord(ids[i]).setNarrowerTerm(na);
					} else if (i == ids.length - 1 && ids.length > 1) {
						String br = thesaurus.getRecord(ids[i - 1]).getTermID();
						thesaurus.getRecord(ids[i]).setBroaderTerm(br);
					} else if (ids.length > 1) {
						String br = thesaurus.getRecord(ids[i - 1]).getTermID();
						String na = thesaurus.getRecord(ids[i + 1]).getTermID();
						thesaurus.getRecord(ids[i]).setBroaderTerm(br);
						thesaurus.getRecord(ids[i]).setNarrowerTerm(na);
					}

				}

			}
			rs.close();
			st.close();
			conexion.close();
			thesaurus.printSKOS("/home/hive/itis.rdf");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			rs.close();
			st.close();
			conexion.close();
		}
	}

}
