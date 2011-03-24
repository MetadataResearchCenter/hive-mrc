/**
 * Copyright (c) 2010, UNC-Chapel Hill and Nescent

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

@author Jose R. Perez-Aguera

 */

package edu.unc.ils.mrc.hive.admin;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.unc.ils.mrc.hive.HiveException;
import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSSchemeImpl;
import edu.unc.ils.mrc.hive.importers.Importer;
import edu.unc.ils.mrc.hive.importers.ImporterFactory;

/**
 * This class is used to administer the HIVE vocabularies. For the specified
 * vocabulary in SKOS RDF/XML format, create the Sesame store (NativeStore),
 * Lucene index, alphabetic and top-concept indexes (serialized TreeMaps). 
 * Optionally, create and train the KEA+ index.
 * 
 * This class expects the following:
 *  - SKOS vocabulary file in RDF/XML format
 *  - Valid HIVE vocabulary property file
 */
public class AdminVocabularies {

    private static final Log logger = LogFactory.getLog(AdminVocabularies.class);
	
	/**
	 * This method is a main to run HIVE importers
	 */	
	public static void main(String[] args) 
	{

		String configpath = args[0];
		String vocabularyName = args[1].toLowerCase();
		boolean doImport = (args.length==2) || (args.length==3 && !args[2].equals("train-only"));
		boolean doTrain = (args.length==3 && (args[2].equals("train") || args[2].equals("train-only")));
		
		logger.info("Starting import of vocabulary " + vocabularyName);
		ImporterFactory.selectImporter(ImporterFactory.SKOSIMPORTER);
		try
		{
			SKOSScheme schema = new SKOSSchemeImpl(configpath, vocabularyName, true);
			
			if (doImport)
			{
				Importer importer = ImporterFactory.getImporter(schema);
				importer.importThesaurustoDB();
				importer.importThesaurustoInvertedIndex();
				importer.close();
				logger.info("Vocabulary import complete");
			}
		
			if (doTrain) 
			{
				logger.info("Training KEA");
				TaggerTrainer trainer = new TaggerTrainer(schema);
				trainer.trainAutomaticIndexingModule();
				logger.info("KEA training complete");
	 		}
		} catch (HiveException e) {
			logger.error("Vocabulary import failed", e);
		}
	}
}
