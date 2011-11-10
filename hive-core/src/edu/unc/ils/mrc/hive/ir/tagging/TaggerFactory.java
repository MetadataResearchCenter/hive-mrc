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

package edu.unc.ils.mrc.hive.ir.tagging;

import org.apache.log4j.Logger;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSTaggerImpl;
import edu.unc.ils.mrc.hive.ir.lucene.search.ConceptMultiSearcher;
import edu.unc.ils.mrc.hive.ir.lucene.search.Searcher;
import edu.unc.ils.mrc.hive.ir.lucene.search.SearcherFactory;

public class TaggerFactory {
	
	private static Logger log = Logger.getLogger(TaggerFactory.class);
	public static final String DUMMYTAGGER = "dummy";
	public static final String KEATAGGER = "KEA";
	public static final String MAUITAGGER = "Maui";
	
	private static String tagger = "dummy";

	public static void selectTagger(String tagger) {
		TaggerFactory.tagger = tagger;
	}

	public static Tagger getTagger(String dirName, String modelName, String stopwordsPath,
			SKOSScheme schema) {
		if (tagger == DUMMYTAGGER)
			return new DummyTagger(dirName, modelName, stopwordsPath, schema);
		else if(tagger == KEATAGGER) {
			return new KEATagger(dirName, modelName, stopwordsPath, schema);
		}
		else if(tagger == MAUITAGGER) {
			return new MauiTagger(dirName, modelName, stopwordsPath, schema);
		}
		else
			return null;

	}

}
