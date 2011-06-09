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

package edu.unc.ils.mrc.hive.api.impl.elmo;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.unc.ils.mrc.hive.api.SKOSConcept;

public class SKOSConceptImpl implements SKOSConcept {

    private static final Log logger = LogFactory.getLog(SKOSConceptImpl.class);
            
	private QName qname;
	private String prefLabel;
	private TreeMap<String, QName> broaders;
	private TreeMap<String, QName> relateds;
	private TreeMap<String, QName> narrowers;
	private List<String> altLabels;
	private List<String> scopeNotes;
	private List<String> schemes;
	private double score;

	public SKOSConceptImpl(QName uri) {
		this.qname = uri;
		this.broaders = new TreeMap<String, QName>();
		this.narrowers = new TreeMap<String, QName>();
		this.relateds = new TreeMap<String, QName>();
		this.altLabels = new ArrayList<String>();
		this.schemes = new ArrayList<String>();
		this.scopeNotes = new ArrayList<String>();
	}
	
	public int getNumberOfChildren() {
		return this.narrowers.size();
	}

	@Override
	public List<String> getAltLabels() {
		return this.altLabels;
	}

	@Override
	public TreeMap<String, QName> getBroaders() {
		return this.broaders;
	}

	@Override
	public TreeMap<String, QName> getNarrowers() {
		return this.narrowers;
	}

	@Override
	public String getPrefLabel() {
		return this.prefLabel;
	}

	@Override
	public void setPrefLabel(String prefLabel) {
		this.prefLabel = prefLabel;
	}

	@Override
	public TreeMap<String, QName> getRelated() {
		return this.relateds;
	}

	@Override
	public List<String> getSchemes() {
		return this.schemes;
	}

	@Override
	public List<String> getScopeNote() {
		return this.scopeNotes;
	}

	@Override
	public QName getQName() {
		return this.qname;
	}

	@Override
	public void addAltLabel(String altLabel) {
		this.altLabels.add(altLabel);

	}

	@Override
	public void addBroader(String broader, QName uri) {
		this.broaders.put(broader, uri);
	}

	@Override
	public void addNarrower(String narrower, QName uri) {
		this.narrowers.put(narrower, uri);
	}

	@Override
	public void addRelated(String related, QName uri) {
		this.relateds.put(related, uri);
	}

	@Override
	public void addScheme(String scheme) {
		this.schemes.add(scheme);
	}

	@Override
	public void addScopeNote(String scopeNote) {
		this.scopeNotes.add(scopeNote);
	}

	public String getSKOSFormat()
	{
	    logger.trace("getSKOSFormat");
	    
	    StringBuffer skos =  new StringBuffer();
	    
	    skos.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
		                     "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">" + "\n");
		
		skos.append("  <rdf:Description rdf:about=\"");
		skos.append(this.getQName().getNamespaceURI());
		skos.append(getQName().getLocalPart());
		skos.append("\">\n");
		
		skos.append("    <rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#Concept\"/>\n");
		
		skos.append("    <skos:prefLabel>");
		skos.append(this.prefLabel);
		skos.append("</skos:prefLabel>\n");
		
		for(String alt : this.altLabels) {
			skos.append("    <skos:altLabel>");
			skos.append(alt);
			skos.append("</skos:altLabel>\n");
		}
		
		for(String broader : this.broaders.keySet()){
			skos.append("    <skos:broader rdf:resource=\"");
			skos.append(this.broaders.get(broader).getNamespaceURI());
			skos.append(this.broaders.get(broader).getLocalPart());
			skos.append("\"/>\n");
		}
		for(String narrower : this.narrowers.keySet()){
			skos.append("    <skos:narrower rdf:resource=\"");
			skos.append(this.narrowers.get(narrower).getNamespaceURI());
			skos.append(this.narrowers.get(narrower).getLocalPart());
			skos.append("\"/>\n");
		}
		for(String related : this.relateds.keySet()){
			skos.append("    <skos:related rdf:resource=\"");
			skos.append(this.relateds.get(related).getNamespaceURI());
			skos.append(this.relateds.get(related).getLocalPart());
			skos.append("\"/>\n");
		}
		
		skos.append("    <skos:inScheme rdf:resource=\"");
		skos.append(this.getQName().getNamespaceURI());
		skos.append("\"/>\n");
		
		for(String scopeNote : this.scopeNotes){
			skos.append("    <skos:scopeNote>");
			skos.append(scopeNote);
			skos.append("</skos:scopeNote>\n");
		}
		skos.append("  </rdf:Description>\n");
		skos.append("</rdf:RDF>");
		
		return skos.toString();
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}

}
