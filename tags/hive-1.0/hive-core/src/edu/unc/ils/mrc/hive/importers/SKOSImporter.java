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

package edu.unc.ils.mrc.hive.importers;

import java.io.File;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.openrdf.concepts.skos.core.Concept;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.nativerdf.NativeStore;
import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;

import edu.unc.ils.mrc.hive.HiveException;
import edu.unc.ils.mrc.hive.ir.lucene.indexing.ConceptIndexer;
import edu.unc.ils.mrc.hive.ir.lucene.indexing.Indexer;

import edu.unc.ils.mrc.hive.api.SKOSScheme;

/**
 * Imports a SKOS vocabulary in RDF/XML format into HIVE. 
 * 
 * Each HIVE vocabulary consists of a Sesame store, Lucene index, and 
 * two serialized TreeMaps representing the alphabetic and top-concept
 * indexes. 
 */
public class SKOSImporter implements Importer 
{
    private static final Logger logger = Logger.getLogger(SKOSImporter.class);
            
    /* Vocabulary name */
    private String vocabularyName;
    
    /* SKOS RDF/XML file */
    private File SKOSfile;
    
    /* Lucene index */
    private Indexer indexer;    
    private String indexDirectory;
    
    /* Sesame store */
    private File dbDirectory;   
    private NativeStore store;
    private SesameManager manager;
    private Repository repository;
    private SesameManagerFactory factory;
    
    /* Alphabetical index */
    private String alphaIndexFile;
    private TreeMap<String, QName> alphaIndex;
        
    /* Top concept index */
    private String topConceptIndexFile;
    private TreeMap<String, QName> topConceptIndex;
    
    /**
     * Constructs a <code>SKOSImporter</code> for the specified scheme
     */
    public SKOSImporter(SKOSScheme scheme) throws HiveException 
    {
        this.vocabularyName = scheme.getName();
        this.topConceptIndexFile = scheme.getTopConceptIndexPath();
        this.topConceptIndex = new TreeMap<String, QName>();
        this.alphaIndexFile = scheme.getAlphaFilePath();
        this.alphaIndex = new TreeMap<String, QName>();
        this.indexDirectory = scheme.getIndexDirectory();
        this.SKOSfile = new File(scheme.getRdfPath());
        this.dbDirectory = new File(scheme.getStoreDirectory());
        this.indexer = new ConceptIndexer(this.indexDirectory, true);
        this.store = new NativeStore(this.dbDirectory);
        this.repository = new SailRepository(store);
        
        // Initialize the Sesame store
        try
        {
            repository.initialize();            
            ElmoModule module = new ElmoModule();           
            factory = new SesameManagerFactory(module, repository);         
            // Create a new ElmoManager with default locale
            manager = factory.createElmoManager();          
        } catch (RepositoryException e) {
            throw new HiveException("Failed to initialize Sesame database", e);
        }
    }

    /**
     * Import the SKOS RDF/XML file into the Sesame store
     */
    public void importThesaurustoDB() throws HiveException
    {
        logger.info("Importing thesaurus " + this.SKOSfile.getPath() + " to Sesame store");
        
        StopWatch stopWatch = new Log4JStopWatch();
        try {
            long size = manager.getConnection().size();
            logger.info("Sesame store size before import: " + size);
                                
            // Adds RDF data from the specified file to the repository
            manager.getConnection().add(this.SKOSfile, "", RDFFormat.RDFXML);

            size = manager.getConnection().size();
            logger.info("Sesame store size after import: " + size);
            
        } catch (RepositoryException e) {
            throw new HiveException("Failed to initialize Sesame database", e);
        } catch (RDFParseException e) {
            throw new HiveException("Import to Sesame database failed", e);         
        } catch (IOException e) {
            throw new HiveException("Import to Sesame database failed", e); 
        }
        stopWatch.lap(vocabularyName + " Sesame create");
        logger.info("Sesame import complete");
    }

    /**
     * Iterates through all concepts in the Sesame DB and adds concepts to Lucene index
     * as well as alphabetic and top-concept maps. 
     */
    public void importThesaurustoInvertedIndex() {
        logger.info("Creating inverted index for thesaurus " + this.SKOSfile.getPath());
        
        StopWatch stopWatch = new Log4JStopWatch();
        
        int count = 0;
        for (Concept concept : this.manager.findAll(Concept.class)) {
            
            // Add concept to Lucene index
            this.indexer.indexConcept(concept);
            
            // Add concept to alphabetical index TreeMap
            this.alphaIndex.put(concept.getSkosPrefLabel(), concept.getQName());
            
            // If this is a top-level concept, add it to the top concept serialized TreeMap
            if (concept.getSkosBroaders().size() == 0
                    && concept.getSkosNarrowers().size() > 0) {
                this.topConceptIndex.put(concept.getSkosPrefLabel(), concept
                        .getQName());
            }
            count++;            
        }
        logger.info(count + " concepts added to Lucene index");
        logger.info(alphaIndex.size() + " concepts added to alphabetic index");
        logger.info(topConceptIndex.size() + " concepts added to top concept index");
        
        stopWatch.lap(vocabularyName + " Lucene create");   
                
        // Write the alphabetic index TreeMap to disk
        this.createAlphabeticIndex();
        stopWatch.lap(vocabularyName + " alpha create");    

        // Write the top concept index TreeMap to disk
        this.createTopConceptIndex();
        stopWatch.lap(vocabularyName + " top create");
    }

    /**
     * Serializes the alphabetic index (TreeMap) to disk.
     */
    private void createAlphabeticIndex() {
        logger.info("Creating alpha index " + this.alphaIndexFile);
        
        File fichero = new File(this.alphaIndexFile);
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(fichero));
            oos.writeObject(this.alphaIndex);
            oos.close();
        } catch (FileNotFoundException e) {
            logger.error("Error writing alpha index", e);
        } catch (IOException e) {
            logger.error("Error writing alpha index", e);
        }
        logger.info("Alpha index created");
    }

    /**
     * Serializes the top-concept index (TreeMap) to disk.
     */
    private void createTopConceptIndex() {
        logger.info("Creating top concept index " + this.topConceptIndexFile);
        
        File fichero = new File(this.topConceptIndexFile);
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(fichero));
            oos.writeObject(this.topConceptIndex);
            oos.close();
        } catch (FileNotFoundException e) {
            logger.error("Error writing top concept index", e);
        } catch (IOException e) {
            logger.error("Error writing top concept index", e);
        }
        logger.info("Top concept index created");
    }

    public void close() {
        try {
            this.manager.close();
            this.factory.close();
            this.repository.shutDown();
        } catch (RepositoryException e) {
            logger.error(e);
        }
        this.indexer.close();
    }

}
