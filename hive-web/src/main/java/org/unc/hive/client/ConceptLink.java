package org.unc.hive.client;

import com.google.gwt.user.client.ui.Hyperlink;

public class ConceptLink extends Hyperlink {
	
	private String namespaceURI;
	private String localPart;
	private String origin = "";
	public ConceptLink(String uri, String lp, String preLabel, String history)
	{
		super(preLabel, history);
		this.namespaceURI = uri;
		this.localPart = lp;
	}
	
	public ConceptLink(String origin, String uri, String lp, String preLabel, String history)
	{
		super(preLabel, history);
		this.origin= origin;
		this.namespaceURI = uri;
		this.localPart = lp;
	}
	
	public String getNamespaceURI()
	{
		return this.namespaceURI;
	}
	public String getlocalPart()
	{
		return this.localPart;
	}
	public String getOrigin()
	{
		return this.origin;
	}
}
