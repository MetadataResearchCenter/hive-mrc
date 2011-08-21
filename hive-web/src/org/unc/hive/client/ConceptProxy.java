package org.unc.hive.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ConceptProxy implements IsSerializable{
	private String preLabel;
	private String URI;
	private String origin;
	private String SKOSCode;
	private HashMap<String, String> narrower = null;
	private HashMap<String, String> broader = null;
	private HashMap<String, String> related = null;
	private List<String> altLabel = null;
	private List<String> scopeNotes = null;
	private boolean isleaf = false;
	private double score; 
	
	public ConceptProxy()
	{
		
	}
	
	public ConceptProxy(String prelabel, String uri)
	{
		this.preLabel = prelabel;
		this.URI = uri;
	}
	
	public ConceptProxy(String origin, String prelabel, String uri, boolean isleaf)
	{
		this.origin = origin;
		this.preLabel = prelabel;
		this.URI = uri;
		this.isleaf = isleaf;
	}
	
	public ConceptProxy(String origin, String prelabel, String uri)
	{
		this.origin = origin;
		this.preLabel = prelabel;
		this.URI = uri;
	}
	
	public ConceptProxy(String origin, String prelabel, String uri, double score)
	{
		this.origin = origin;
		this.preLabel = prelabel;
		this.URI = uri;
		this.score = score;
	}
	
	public ConceptProxy(String origin, String prelabel, String uri, String skosCode)
	{
		this.origin = origin;
		this.preLabel = prelabel;
		this.URI = uri;
		this.SKOSCode = skosCode;
	}
	
	public double getScore()
	{
		return this.score;
	}
	
	public String getSkosCode()
	{
		return this.SKOSCode;
	}
	
	public boolean getIsLeaf()
	{
		return this.isleaf;
	}
	
	public void setOrigin(String origin)
	{
		this.origin = origin;
	}
	
	public String getOrigin()
	{
		return this.origin;
	}
	
	public void setPreLabel(String prelabel)
	{
		preLabel = prelabel;
	}
	
	public String getPreLabel()
	{
		return this.preLabel;
	}
	
	public void setURI(String uri)
	{
		URI = uri;
	}
	
	public String getURI()
	{
		return URI;
	}
	
	public void setNarrower(Map<String, String> map)
	{
		this.narrower = new HashMap<String, String>(map);
	}
	
	
	public HashMap<String, String> getNarrower()
	{
		return this.narrower;
	}
	
	public void setBroader(Map<String, String> map)
	{
		this.broader = new HashMap<String, String>(map);
	}
	
	public HashMap<String, String> getBroader()
	{
		return this.broader;
	}
	
	public void setRelated(HashMap<String, String> map)
	{
		this.related = new HashMap<String, String>(map);
	}
	
	public HashMap<String, String> getRelated()
	{
		return this.related;
	}
	
	public void setAltLabel(List<String> altlabel)
	{
		this.altLabel = altlabel;
	}
	
	public List<String> getAltLabel()
	{
		return this.altLabel;
	}
	
	public void setScopeNotes(List<String> notes)
	{
		this.scopeNotes = notes;
	}
	public List<String> getScopeNotes()
	{
		return this.scopeNotes;
	}
	
	public void put(List<String> altlabel, HashMap<String, String> broader,  HashMap<String, String> narrower,  HashMap<String, String> related, List<String> scopeNote, String skosCode)
	{
		this.altLabel = altlabel;
		this.broader = broader;
		this.narrower = narrower;
		this.related = related;
		this.scopeNotes = scopeNote;
		this.SKOSCode = skosCode;
	}
}