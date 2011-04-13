package edu.unc.ils.mrc.hive.converter.mesh.handlers;

/**
 * Represents a term as parsed from the MeSH XML.
 */
public class Term {

	String termId = null;

	String termValue = null;
	boolean isPreferred = false;
	
	public Term() 
	{
		
	}
	
	public Term(String termId, String termValue, boolean isPreferred)
	{
		this.termId = termId;
		this.termValue = termValue;
		this.isPreferred = isPreferred;
	}
	
	public String getTermId() {
		return termId;
	}

	public void setTermId(String termId) {
		this.termId = termId;
	}

	public String getTermValue() {
		return termValue;
	}

	public void setTermValue(String termValue) {
		this.termValue = termValue;
	}

	public boolean isPreferred() {
		return isPreferred;
	}

	public void setPreferred(boolean isPreferred) {
		this.isPreferred = isPreferred;
	}

	
}
