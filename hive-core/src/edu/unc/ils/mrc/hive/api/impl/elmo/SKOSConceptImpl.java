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

import edu.unc.ils.mrc.hive.api.SKOSConcept;

public class SKOSConceptImpl implements SKOSConcept {

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

	public String getSKOSFormat(){
		
		String output = "<rdf:RDF>" + "\n";
		output = output + "\t<rdf:Description rdf:about=\"" + this.getQName().getNamespaceURI() + getQName().getLocalPart() + "\">\n";
		output = output + "\t<rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#Concept\"/>\n";
		output = output + "\t<skos:prefLabel>" + this.prefLabel + "</skos:prefLabel>" + "\n";
		for(String alt : this.altLabels) {
			output = output + "\t<skos:altLabel>" + alt + "</skos:altLabel>" + "\n";
		}
		for(String broader : this.broaders.keySet()){
			output = output + "\t<skos:broader rdf:resource=\"" + this.broaders.get(broader).getNamespaceURI() + this.broaders.get(broader).getLocalPart() + "/>" + "\n";
		}
		for(String narrower : this.narrowers.keySet()){
			output = output + "\t<skos:narrower rdf:resource=\"" + this.narrowers.get(narrower).getNamespaceURI() + this.narrowers.get(narrower).getLocalPart() + "/>" + "\n";
		}
		for(String related : this.relateds.keySet()){
			output = output + "\t<skos:related rdf:resource=\"" + this.relateds.get(related).getNamespaceURI() + this.relateds.get(related).getLocalPart() + "/>" + "\n";
		}
		output = output + "\t<skos:inScheme rdf:resource=\"" + this.getQName().getNamespaceURI() +"\"/>\n";
		for(String scopeNote : this.scopeNotes){
			output = output + "\t<skos:scopeNote>" + scopeNote + "</skos:scopeNote>" + "\n";
		}
		output = output + "</rdf:RDF>" ;
		
		return output;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}

}
