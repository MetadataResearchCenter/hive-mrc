package edu.unc.ils.mrc.hive.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.io.FileExtensionFilter;

import edu.unc.ils.mrc.hive.api.SKOSConcept;
import edu.unc.ils.mrc.hive.api.SKOSSearcher;
import edu.unc.ils.mrc.hive.api.SKOSServer;
import edu.unc.ils.mrc.hive.api.SKOSTagger;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSServerImpl;

/**
 * Simple command line tagger. Given a directory of PDF files, generates text files
 * with keyphrases from the specified vocabulary.
 */
public class BatchTagger 
{
	static final int NUM_TERMS = 20;
	public static void main(String[] args) 
	{
		if (args.length != 3) {
			System.err.println("Usage: java " + BatchTagger.class.getName() + "[path to hive.properties] [path to directory] [vocabulary]");
			return;
		}
		
		// Path to hive.properties
		String confPath = args[0];
		// Path to directory containing PDF files
		String inputPath = args[1];		
		// Vocabulary name
		String vocabulary = args[2];
		
		// Algorithm for indexing
		String algorithm = "maui";

		File inputDir = new File(inputPath);
		List<String> vocabularies = new ArrayList<String>();
		vocabularies.add(vocabulary);
		
		SKOSServer server = new SKOSServerImpl(confPath);
		SKOSTagger tagger = server.getSKOSTagger(algorithm);
		SKOSSearcher searcher = server.getSKOSSearcher();
		File[] files = inputDir.listFiles(new FileExtensionFilter("pdf"));
		for (File file: files) {
			try
			{
				String pdfName = file.getAbsolutePath();
				TextManager tm = new TextManager();
				String text = tm.getPlainText(new FileInputStream(file));
				String textFileName = pdfName.substring(0, pdfName.lastIndexOf('.')) + ".txt";

				String keyFileName = pdfName.substring(0, pdfName.lastIndexOf('.')) + ".key";
				FileWriter keyFileWriter = new FileWriter(keyFileName);

				List<SKOSConcept> concepts = tagger.getTags(file.getAbsolutePath(), vocabularies, searcher, NUM_TERMS, 2);
				for (SKOSConcept concept : concepts) {
					keyFileWriter.write(concept.getPrefLabel() + "\r\n");
				}
				keyFileWriter.close();
				
				FileWriter textFileWriter = new FileWriter(textFileName);
				textFileWriter.write(text);
				textFileWriter.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
