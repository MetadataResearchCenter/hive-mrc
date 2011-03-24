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

import java.util.List;
import java.util.TreeMap;

import javax.xml.namespace.QName;

public interface SKOSConcept {

	public QName getQName();

	public String getPrefLabel();

	public List<String> getAltLabels();

	public TreeMap<String, QName> getBroaders();

	public TreeMap<String, QName> getRelated();

	public TreeMap<String, QName> getNarrowers();

	public List<String> getScopeNote();

	public List<String> getSchemes();

	public void setPrefLabel(String prefLabel);

	public void addBroader(String broader, QName uri);

	public void addRelated(String related, QName uri);

	public void addNarrower(String narrower, QName uri);

	public void addAltLabel(String altLabel);

	public void addScopeNote(String scopeNote);

	public void addScheme(String scheme);

	public String getSKOSFormat();

	public int getNumberOfChildren();

	public void setScore(double score);

	public double getScore();

}
