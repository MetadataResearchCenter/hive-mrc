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

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSSchemeImpl;
import edu.unc.ils.mrc.hive.importers.Importer;
import edu.unc.ils.mrc.hive.importers.ImporterFactory;

/**
 * This class create Sesame Store, Lucene Index and AlphaIndex 
 * for a SKOS/RDF file
 * @author Jose R. Perez-Aguera
 */

public class AdminVocabularies {

	/**
	 * This method is a main to run HIVE importers
	 */
	
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
//		if (args[2].equals("train")) {
//			TaggerTrainer trainer = new TaggerTrainer(schema);
//			trainer.trainAutomaticIndexingModule();
// 		}
	}

}
