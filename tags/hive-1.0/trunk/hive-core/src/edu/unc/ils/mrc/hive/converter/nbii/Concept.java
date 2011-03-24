package edu.unc.ils.mrc.hive.converter.nbii;

import java.util.List;

public interface Concept {

	public void setUri(String uri);
	
	public String getUri();

	public String getPrefLabel();

	public void setPrefLabel(String prefLabel);
	
	public String getScopeNote();

	public void setScopeNote(String scopeNote);

	public List<String> getAltLabel();

	public void setAltLabel(String altLabel);

	public List<String> getHiddenLabel();

	public void setHiddenLabel(String hiddenLabel);
	
	public List<String> getNarrower();
	
	public void setNarrower(String narrower);

	public List<String> getBroader();

	public void setBroader(String broader);
	
	public List<String> getRelated();

	public void setRelated(String broader);
	
	public List<String> getNarrowerURI();
	
	public void setNarrowerURI(String narrowerURI);
	
	public List<String> getBroaderURI();
	
	public void setBroaderURI(String broaderURI);
	
	public List<String> getRelatedURI();
	
	public void setRelatedURI(String relatedURI);
}
