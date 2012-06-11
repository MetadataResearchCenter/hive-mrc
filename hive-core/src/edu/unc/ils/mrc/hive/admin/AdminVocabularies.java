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

import kea.vocab.VocabularyH2;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.unc.ils.mrc.hive.HiveException;
import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSSchemeImpl;

/**
 * This class is used to administer HIVE vocabularies. For the specified vocabulary
 * in SKOS RDF/XML format, this class creates
 *   - SSesame store (NativeStore)
 *   - Lucene index
 *   - H2 tables and indexes
 *   - KEA++ model (based on training set)
 *   - Maui model (based on same training set)
 * 
 * This class expects the following:
 *  - SKOS vocabulary file in RDF/XML format
 *  - Valid HIVE vocabulary property file
 */
public class AdminVocabularies {

    private static final Log logger = LogFactory.getLog(AdminVocabularies.class);
	
    /**
     * Returns the CLI options
     * @return
     */
    public static Options getOptions()
    {
    	Options options = new Options();
    	Option config = new Option("c", true, "Path to directory that contains hive.properties");
    	config.setRequired(true);
    	options.addOption(config);
    	
    	Option vocab = new Option("v", true, "Name of the vocabulary to be initialized");
    	vocab.setRequired(true);
    	options.addOption(vocab);
    	
    	options.addOption("h", false, "Print this help message");
    	options.addOption("a", false, "Initialize everything");
    	options.addOption("s", false, "Initialize Sesame");
    	options.addOption("l", false, "Initialize Lucene");
    	options.addOption("d", false, "Initialize H2");
    	options.addOption("k", false, "Initialize H2 (KEA)");
    	options.addOption("t", false, "Train KEA");
    	options.addOption("m", false, "Train Maui");
    	options.addOption("x", false, "Initialize autocomplete index");
    	options.addOption("mo", true, "Minimum phrase occurrence");
    	return options;
    }
    
	/**
	 * This method is a main to run HIVE importers
	 * @throws ParseException 
	 */	
	public static void main(String[] args) throws ParseException 
	{

		// Get the options specified
		CommandLineParser parser = new BasicParser( );

		Options options = getOptions();
		CommandLine commandLine = parser.parse( options, args );

		if (commandLine.hasOption("h")) 
		{
			// Print the help message
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "java edu.unc.ils.mrc.hive.admin.AdminVocabularies", options );
		}
		else
		{

			String configpath = commandLine.getOptionValue("c");
			String vocabularyName = commandLine.getOptionValue("v");
			
			boolean doAll = commandLine.hasOption("a");
			boolean doSesame = commandLine.hasOption("s");
			boolean doLucene = commandLine.hasOption("l");
			boolean doH2 = commandLine.hasOption("d");
			boolean doKEAH2 = commandLine.hasOption("k");
			boolean doTrainKEA = commandLine.hasOption("t");
			boolean doTrainMaui = commandLine.hasOption("m");
			boolean doAutocomplete = commandLine.hasOption("x");
			
			String moStr = commandLine.getOptionValue("mo");
			int minOccur = 2;
			if (moStr != null)
				minOccur = Integer.parseInt(moStr);
			
			if (doAll)
				doSesame = doLucene = doH2 = doKEAH2 = doTrainKEA = doTrainMaui = doAutocomplete =true;
			
			logger.info("Starting import of vocabulary " + vocabularyName);
			try
			{
				SKOSScheme scheme = new SKOSSchemeImpl(configpath, vocabularyName, true);
				
				try
				{
					scheme.importConcepts(scheme.getRdfPath(), doSesame, doLucene, doH2, doKEAH2, doAutocomplete);
				} catch (Exception e) {
					logger.error(e);
				}
			
				if (doKEAH2) 
				{
					logger.info("Initializing KEA H2 index");
					try
					{
						VocabularyH2 keaH2 = new VocabularyH2(scheme, "en");
						keaH2.initialize();		
					} catch (Exception e) {
						logger.error(e);
					}
				} 
				else
					logger.info("Skipping KEA H2 initialization");
				
				if (doTrainKEA) 
				{
					TaggerTrainer trainer = new TaggerTrainer(scheme);
					trainer.setMinOccur(minOccur);
					logger.info("Starting KEA training");
					trainer.trainKEAAutomaticIndexingModule();
					logger.info("KEA training complete");
		 		} 
				else
					logger.info("Skipping KEA training");
				
				if (doTrainMaui) 
				{
					TaggerTrainer trainer = new TaggerTrainer(scheme);
					trainer.setMinOccur(minOccur);
					logger.info("Starting Maui training");
					trainer.trainMauiAutomaticIndexingModule();
					logger.info("Maui training complete");
		 		} 
				else
					logger.info("Skipping Maui training");
				
				try {
					scheme.close();
				} catch (Exception e) {
					logger.error(e);
				}
			} catch (HiveException e) {
				logger.error("Vocabulary import failed", e);
			}
		}
	}
}
