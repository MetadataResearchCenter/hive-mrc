package edu.unc.ils.mrc.hive.ir.tagging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.nlp.Dictionary;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.nlp.Postagger;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.ranking.Rankeable;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.ranking.Ranking;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.tagger.Coleccion;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.tagger.Documento;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.tagger.Termino;
import edu.unc.ils.mrc.hive.ir.tagging.dummy.tagger.Vocabulario;

public class DummyTagger implements Tagger {
	
	private String model;
	private Postagger postagger;

	
	public DummyTagger(String dirName, String modelName, String stopwordsPath, SKOSScheme schema) {
		this.model = modelName;
		try {
			postagger = new Postagger(this.model);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<String> extractKeyphrases(String text) {
		List<String> keywords = new ArrayList<String>();
		// TODO Auto-generated method stub
		Analyzer analyzer = new StandardAnalyzer();
		Directory index = new RAMDirectory();
		IndexWriter w;
		try {
			w = new IndexWriter(index, analyzer, true,
					IndexWriter.MaxFieldLength.UNLIMITED);
			Document doc = new Document();
			doc.add(new Field("text", text, Field.Store.YES,
					Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
			w.addDocument(doc);
			text = text.trim();
			
			Dictionary dic = this.postagger.tagText(text);

			w.close();

			IndexSearcher searcher = new IndexSearcher(index);
			IndexReader reader = searcher.getIndexReader();
			//System.out.println("Number of Documents: " + reader.maxDoc());
			Coleccion collec = new Coleccion();
			Vocabulario vocabulario = new Vocabulario();
			for (int n = 0; n <= reader.maxDoc() - 1; n++) {
				Document Ldoc = reader.document(n);
				// System.out.println("----------NEW DOCUMENT----"+ n +
				// "-------------");
				TermFreqVector vector = reader.getTermFreqVector(n, "text");
				// System.out.println(vector);
				int[] freq = vector.getTermFrequencies();
				String[] terms = vector.getTerms();
				Documento documento = new Documento();
				documento.setName(Ldoc.get("title"));
				for (int i = 0; i < freq.length; i++) {
					double f = freq[i];
					double t = freq.length;
					double prob = f / t;
					documento.addTerm(terms[i], prob, freq[i]);
					vocabulario.addTerm(terms[i], freq[i]);
				}
				collec.addDocumento(documento);
			}
			vocabulario.calculaProbabilidades();
			collec.calculaDivergencias(vocabulario);
			Ranking ranking = new Ranking();
			for (Documento d : collec.getDocumentos()) {
				for (Termino t : d.getTerminos()) {
					if (t.getTf() > 0.1
							&& dic.isAllowed(t.getTermino())
							&& t.getTermino().length() > 1) {
						ranking.addValor(t);
					}
				}
				for(Rankeable term : ranking.getRanking()) {
					Termino te = (Termino) term;
					keywords.add(te.getTermino());
				}
				//ranking = new Ranking();
			}
			index.close();
			reader.close();
			searcher.close();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return keywords;
	}

	@Override
	public String getVocabulary() {
		return "Dummytagger";
	}

	@Override
	public void extractKeyphrases() {
		// TODO Auto-generated method stub
		
	}
	
}
