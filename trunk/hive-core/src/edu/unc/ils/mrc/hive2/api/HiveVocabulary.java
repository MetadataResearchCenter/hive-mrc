/**
 * Copyright (c) 2011, UNC-Chapel Hill and Nescent

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
 */

package edu.unc.ils.mrc.hive2.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openrdf.elmo.sesame.SesameManager;

import edu.unc.ils.mrc.hive.ir.lucene.search.AutocompleteTerm;

/**
 * This interface represents a Hive vocabulary. A Hive vocabulary is
 * primarily backed by a Sesame repository and one or more underlying 
 * indexes.
 * 
 * @author craig.willis@unc.edu
 */
public interface HiveVocabulary 
{
	/**
	 * Returns the SesameManager for this vocabulary.
	 * @return
	 * @throws Exception
	 */
	public SesameManager getManager() throws Exception;
	
	/**
	 * Import concepts from the specified path to this Hive vocabulary
	 * @param path
	 * @throws Exception
	 */
	public void importConcepts(String path) throws Exception;
	
	/**
	 * Import concepts from the specified path to this Hive vocabulary,
	 * controlling the indexes that get created.
	 * @param path
	 * @param doSesame
	 * @param doLucene
	 * @param doH2
	 * @param doKEAH2
	 * @param doAutocomplete
	 * @throws Exception
	 */
	public void importConcepts(String path, boolean doSesame, boolean doLucene, boolean doH2, 
			boolean doKEAH2, boolean doAutocomplete) throws Exception;
	
	/**
	 * Import a single concept from the specified path into this Hive vocabulary
	 * @param qname
	 * @param path
	 * @throws Exception
	 */
	public void importConcept(QName qname, String path) throws Exception;
	
	/**
	 * Add a concept to this vocabulary
	 * @param concept
	 * @throws Exception
	 */
	public void addConcept(HiveConcept concept) throws Exception;
	
	/** 
	 * Update a concept in this vocabulary
	 * @param concept
	 * @throws Exception
	 */
	public void updateConcept(HiveConcept concept) throws Exception;
	
	/**
	 * Remove a concept from this vocabulary
	 * @param qname
	 * @throws Exception
	 */
	public void removeConcept(QName qname) throws Exception;
	
	/**
	 * Find a concept by Qname
	 * @param qname
	 * @return
	 * @throws Exception
	 */
	public HiveConcept findConcept(QName qname) throws Exception;
	
	/**
	 * Find concepts matching a certain pattern
	 * @param pattern
	 * @param topOnly
	 * @return
	 * @throws Exception
	 */
	public List<HiveConcept> findConcepts(String pattern, boolean topOnly) throws Exception;
	
	/**
	 * Returns the number of concepts in this vocabulary
	 * @return
	 * @throws Exception
	 */
	public long getNumConcepts() throws Exception;
	
	/** 
	 * Returns the number of top concepts in this vocabulary
	 * @return
	 * @throws Exception
	 */
	public long getNumTopConcepts() throws Exception;
	
	/** 
	 * Returns the last update date for this vocabulary
	 * @return
	 * @throws Exception
	 */
	public Date getLastUpdateDate() throws Exception;
	
	/**
	 * Close this vocabulary and the underlying indexes.
	 */
	public void close();
	
	/**
	 * Suggest terms given a string
	 */
	public List<AutocompleteTerm> suggestTermsFor(String str, int numTerms) throws Exception;
	
	/**
	 * Returns the number of concepts, broader, narrower and related concepts for this vocabulary
	 */
	public Map<String, Long> getStats() throws Exception;
	
	/**
	 * Returns a map of preflabel to QName for all concepts in the vocabulary
	 * @param topOnly
	 * @return
	 * @throws Exception
	 */
	public Map<String, QName> findAllConcepts(boolean topOnly) ;
}
