package edu.unc.ils.mrc.hive.admin;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSSchemeImpl;
import edu.unc.ils.mrc.hive.importers.Importer;
import edu.unc.ils.mrc.hive.importers.ImporterFactory;

/*
 * This class create Sesame Store, Lucene Index and AlphaIndex 
 * for a SKOS/RDF file
 * 
 */
public class AdminVocabularies {

	public static void main(String[] args) {

		String configpath = args[0];
		String vocabularyName = args[1].toLowerCase();

		SKOSScheme schema = new SKOSSchemeImpl(configpath, vocabularyName, true);

		ImporterFactory.selectImporter(ImporterFactory.SKOSIMPORTER);
		Importer importer = ImporterFactory.getImporter(schema);
		importer.importThesaurustoDB();
		importer.importThesaurustoInvertedIndex();
		importer.close();
		System.out.println("Import finished");
		if (args[2].equals("train")) {
			TaggerTrainer trainer = new TaggerTrainer(schema);
			trainer.trainAutomaticIndexingModule();
 		}
	}

}
