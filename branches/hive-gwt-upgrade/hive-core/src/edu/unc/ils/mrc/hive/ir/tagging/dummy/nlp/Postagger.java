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

package edu.unc.ils.mrc.hive.ir.tagging.dummy.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Streams;

// Determiners & Numerals
// ABN, ABX, AP, AP$, AT, CD, CD$, DT, DT$, DTI, DTS, DTX, OD

// Adjectives
// JJ, JJ$, JJR, JJS, JJT

// Nouns
// NN, NN$, NNS, NNS$, NP, NP$, NPS, NPS$

// Adverbs
// RB, RB$, RBR, RBT, RN (not RP, the particle adverb)

// Pronoun
// PN, PN$, PP$, PP$$, PPL, PPLS, PPO, PPS, PPSS

// Verbs
// VB, VBD, VBG, VBN, VBZ

// Auxiliaries
// MD, BE, BED, BEDZ, BEG, BEM, BEN, BER, BEZ

// Adverbs
// RB, RB$, RBR, RBT, RN (not RP, the particle adverb)

// Punctuation
// ', ``, '', ., (, ), *, --, :, ,

public class Postagger {

	private TokenizerFactory TOKENIZER_FACTORY = new RegExTokenizerFactory(
			"(-|'|\\d|\\p{L})+|\\S");

	private HmmDecoder decoder;

	public Postagger(String model) throws IOException, ClassNotFoundException {
		System.out.println("Reading model from file=" + model);
		FileInputStream fileIn = new FileInputStream(model);
		ObjectInputStream objIn = new ObjectInputStream(fileIn);
		HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
		Streams.closeInputStream(objIn);
		this.decoder = new HmmDecoder(hmm);
	}

	public Dictionary tagText(String text) throws ClassNotFoundException,
			IOException {
		Dictionary dic = new Dictionary();
		char[] cs = text.toCharArray();

		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(cs, 0, cs.length);
		String[] tokens = tokenizer.tokenize();

		String[] tags = decoder.firstBest(tokens);

		dic.setTags(tags);
		dic.setWords(tokens);

		return dic;
	}

}
