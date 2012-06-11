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

import java.net.URL;
import java.util.List;

import org.apache.commons.configuration.Configuration;

public interface SKOSTagger {
	
	/**
	 * Returns a list of SKOSConcept objects for the specified file
	 * using the specified vocabularies and SKOSSearcher implementation.
	 * 
	 * @param path			Path to the file
	 * @param vocabularies	List of vocabularies
	 * @param searcher		Searcher implementation
	 * @param maxTerms		Maximum number of terms
	 * @param minOccur		Minimum number of times a phrase/term must occur
	 * @return
	 */
	public List<SKOSConcept> getTags(String path, List<String> vocabularies, 
			SKOSSearcher searcher, int maxTerms, int minOccur);

	/**
	 * Returns a list of SKOSConcept objects for the specified URL
	 * using the specified vocabularies and SKOSSearcher implementation. 
	 * The maximum number of hops indicates the number of levels of links
	 * to be crawled/traversed when indexing the site.
	 * 
	 * @param url			URL of desired web site
	 * @param vocabularies  List of vocabularies
	 * @param searcher		Searcher implementation
	 * @param maxHops		Maximum number of links to be traversed (hops)
	 * @param maxTerms		Maximum number of terms
	 * @param diff			Index only the differences between base page and subsequent pages
	 * @param minOccur		Minimum number of times a phrase/term must occur
	 * @return
	 */
	public List<SKOSConcept> getTags(URL url, List<String> vocabularies, 
			SKOSSearcher searcher, int maxHops, int maxTerms, boolean diff, int minOccur);
	
	public void setConfig(Configuration config);
	
	public List<SKOSConcept> getTagsFromText(String text, List<String> vocabularies, 
			SKOSSearcher searcher,  int maxTerms, int minOccur);
	
	public List<ConceptNode> getTagsAsTree(String text, List<String> vocabularies, 
			SKOSSearcher searcher,  int maxTerms, int minOccur);
}
