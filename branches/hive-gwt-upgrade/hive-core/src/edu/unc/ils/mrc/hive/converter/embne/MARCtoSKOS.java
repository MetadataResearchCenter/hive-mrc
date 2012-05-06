package edu.unc.ils.mrc.hive.converter.embne;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

public class MARCtoSKOS {
	public  static String inputMARCfile = "C:\\hive\\HIVE-ES\\para_skos2.mrc";
	public  static String resultsFileName = "C:\\hive\\HIVE-ES\\marc21format2.txt";
	public  static PrintWriter outputStream = null;
	public  static String skosFileName = "C:\\hive\\HIVE-ES\\embne.rdf";
	public  static PrintWriter skosOutputStream = null;
	public  static MARCtoSKOSConverter conv = new MARCtoSKOSConverter();

	public static void main(String[] args) {
		createOutputFiles();
		conv.writeSKOSheader(skosOutputStream);
		conv.writeSKOSConceptSchemes(skosOutputStream);
		conv.readInputFile(inputMARCfile, skosOutputStream, outputStream);
		conv.writeSKOSfooter(skosOutputStream);
		skosOutputStream.close();
		outputStream.close();
	}

	public static void createOutputFiles() {
		try { // MARC21 format
			outputStream = new PrintWriter(resultsFileName);
		} catch (FileNotFoundException e) {
			System.out.println("Error opening file " + resultsFileName);
			System.exit(0);
		}
		try { // SKOS format
			skosOutputStream = new PrintWriter(skosFileName, "UTF-8");
		} catch (Exception e) {
			System.out.println("Error opening file " + skosFileName);
			System.exit(0);
		}
	}

}
