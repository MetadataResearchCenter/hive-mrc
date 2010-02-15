package edu.unc.ils.mrc.hive.importers;

public interface Importer {
	
	public void importThesaurustoDB();
	public void importThesaurustoInvertedIndex();
	public void close();
}
