package maui.vocab;

import maui.vocab.store.VocabularyStore;

public class VocabularyFactory {
	
	public static final int JENA = 1;
	public static final int SESAME = 2;
	public static final int TEXT = 3;

	private static int vocabulary = 1;

	public static void selectVocabulary(int vocabulary) {
		VocabularyFactory.vocabulary = vocabulary;
	}

	public static Vocabulary getVocabulary(VocabularyStore store) {
		if (vocabulary == JENA)
			try {
				return new VocabularyJena(store);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else if(vocabulary == SESAME) {
			try {
				return new VocabularySesame(store);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if(vocabulary == TEXT) {
			try {
				return new VocabularyText(store);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			return null;
		return null;

	}

}
