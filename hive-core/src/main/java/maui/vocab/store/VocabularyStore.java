package maui.vocab.store;

import org.openrdf.elmo.sesame.SesameManager;

public interface VocabularyStore {
	
	public String getRDFFile();
	public SesameManager getSesameManager();
	public String getVocabularyName();
	public String getVocabularyFormat();
	public String getStore();

}
