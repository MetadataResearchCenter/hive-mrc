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

import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.openrdf.elmo.sesame.SesameManager;

public interface SKOSScheme {
	
	public String getName();
	public String getLastDate();
	public int getNumberOfConcepts();
	public int getNumberOfBroader();
	public int getNumberOfNarrower();
	public int getNumberOfRelated();
	public int getNumberOfRelations();
	public String getStoreDirectory();
	public SesameManager getManager();
	public void setManager(SesameManager manager);
	public String getIndexDirectory();
	public String getSchemaURI();
	
	public String getLongName();
	
	public String getStopwordsPath();
	public String getKEAtrainSetDir();
	public String getKEAtestSetDir();
	public String getKEAModelPath();
	public String getRdfPath();
	public String getTopConceptIndexPath();
	public String getAlphaFilePath();

	public String getLingpipeModel();
	
	public TreeMap<String,QName> getAlphaIndex();
	public TreeMap<String,QName> getSubAlphaIndex(String startLetter);
	public TreeMap<String, QName> getTopConceptIndex();
	public TreeMap<String, QName> getSubTopConceptIndex(String startLetter);
}
