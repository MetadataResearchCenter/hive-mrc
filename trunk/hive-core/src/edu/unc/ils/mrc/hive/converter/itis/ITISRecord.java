package edu.unc.ils.mrc.hive.converter.itis;

public class ITISRecord
{
	private String termID;
	private String termName;
	private String broaderTerm;
	private String narrowerTerm;
	
	public ITISRecord(String termID){
		this.termID= termID;
		this.termName = new String();
		this.broaderTerm = new String();
		this.narrowerTerm = new String();
	}
	
	
	public String getTermID(){
		return termID;
	}
	public String getTermName(){
		return termName;
	}
	public void setTermName(String tn){
		this.termName = tn;
	}
	public String getBroaderTerm(){
		return broaderTerm;
	}
	public void setBroaderTerm(String br){
		this.broaderTerm = br;
	
	}
	public String getNarrowerTerm(){
		return narrowerTerm;
	}
	public void setNarrowerTerm(String na){
		this.narrowerTerm = na;
	
	}

}
