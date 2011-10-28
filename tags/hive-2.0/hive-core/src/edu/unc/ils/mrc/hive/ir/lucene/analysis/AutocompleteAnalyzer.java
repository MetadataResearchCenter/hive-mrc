package edu.unc.ils.mrc.hive.ir.lucene.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter.Side;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;



public class AutocompleteAnalyzer extends Analyzer
{
	private static final String[] ENGLISH_STOP_WORDS = {
	    "a", "an", "and", "are", "as", "at", "be", "but", "by",
	    "for", "i", "if", "in", "into", "is",
	    "no", "not", "of", "on", "or", "s", "such",
	    "t", "that", "the", "their", "then", "there", "these",
	    "they", "this", "to", "was", "will", "with"
	    };

	public TokenStream tokenStream(String fieldName,
			Reader reader) 
	{
		TokenStream result = new StandardTokenizer(reader);

		result = new StandardFilter(result);
		result = new LowerCaseFilter(result);
		result = new ISOLatin1AccentFilter(result);
		result = new StopFilter(result,
			ENGLISH_STOP_WORDS);
		result = new EdgeNGramTokenFilter(
			result, Side.FRONT,1, 20);

		return result;
	}
}
