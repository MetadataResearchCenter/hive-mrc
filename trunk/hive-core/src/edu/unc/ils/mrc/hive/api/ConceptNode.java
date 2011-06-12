package edu.unc.ils.mrc.hive.api;

import java.util.ArrayList;
import java.util.List;

public class ConceptNode 
{
	private String uri;
	private String label;
	private List<ConceptNode> children = new ArrayList<ConceptNode>();
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public List<ConceptNode> getChildren() {
		return children;
	}
	
	public void setChildren(List<ConceptNode> children) {
		this.children = children;
	}
	
	public void addChild(ConceptNode child) {
		this.children.add(child);
	}
	
	public String toString() {
		return toString(this);
	}
	
	private String toString(ConceptNode node) {
		String str = node.getUri() + "," + node.getLabel();
		for (ConceptNode child: node.getChildren())
			str += "|" + toString(child);
		return str;
	}
}
