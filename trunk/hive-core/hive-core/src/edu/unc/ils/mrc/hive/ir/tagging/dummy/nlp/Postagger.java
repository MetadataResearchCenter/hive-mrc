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
