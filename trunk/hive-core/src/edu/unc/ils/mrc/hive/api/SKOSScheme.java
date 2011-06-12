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

package edu.unc.ils.mrc.hive.api;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.openrdf.elmo.sesame.SesameManager;

import edu.unc.ils.mrc.hive.ir.lucene.search.AutocompleteTerm;
import edu.unc.ils.mrc.hive2.api.HiveVocabulary;

public interface SKOSScheme {
	
	public String getName();
	public Date getCreationDate();
	public Date getLastUpdateDate();
	
	public String getLastDate();

	public long getNumberOfConcepts();
	public long getNumberOfBroader();
	public long getNumberOfNarrower();
	public long getNumberOfRelated();
	public long getNumberOfRelations();
	public String getStoreDirectory();
	public SesameManager getManager();
	public String getIndexDirectory();
	public String getSchemaURI();
	
	public String getLongName();
	
	public String getStopwordsPath();
	public String getKEAtrainSetDir();
	public String getKEAtestSetDir();
	public String getKEAModelPath();
	public String getRdfPath();
	public String getAutoCompletePath();

	public String getAtomFeedURL();
	
	public String getLingpipeModel();
	
	public TreeMap<String,QName> getSubAlphaIndex(String startLetter);
	public List<SKOSConcept> getSubTopConceptIndex(String startLetter);
	
	public Map<String, QName> getAlphaIndex();
	public Map<String, QName> getTopConceptIndex();
	
	
	public void importConcepts(String path) throws Exception;
	public void importConcepts(String path, boolean doSesame, boolean doLucene, boolean doH2, boolean doH2KEA, boolean doAutocomplete) throws Exception;
	public void importConcept(QName qname, String path) throws Exception;
	public void importConcept(String uri) throws Exception;
	public void deleteConcept(String uri) throws Exception;
	public void deleteConcept(QName qname) throws Exception;
	public long getNumberOfTopConcepts() throws Exception;
	public void close() throws Exception;
	public List<AutocompleteTerm> suggestTermsFor(String str, int numTerms) throws Exception;
	public HiveVocabulary getHiveVocabulary();
	
}
