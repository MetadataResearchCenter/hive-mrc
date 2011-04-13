package edu.unc.ils.mrc.hive.converter.mesh.handlers;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a descriptor as parsed from the MeSH Descriptor element
 */
public class Descriptor {

	String descriptorId = null;
	

	List<Concept> concepts = new ArrayList<Concept>();;
	List<String> relatedDescriptors = new ArrayList<String>();
	List<String> treeNumbers = new ArrayList<String>();
	
	public Descriptor() 
	{
	}
	
	public Descriptor(String descriptorId)
	{
		this.descriptorId = descriptorId;
	}
	
	public String getDescriptorId() {
		return descriptorId;
	}

	public void setDescriptorId(String descriptorId) {
		this.descriptorId = descriptorId;
	}
	
	public void setConcepts(List<Concept> concepts) {
		this.concepts = concepts;
	}
	
	public List<Concept> getConcepts() {
		return concepts;
	}


	public List<String> getRelatedDescriptors() {
		return relatedDescriptors;
	}

	public void setRelatedDescriptors(List<String> relatedDescriptors) {
		this.relatedDescriptors = relatedDescriptors;
	}

	public void addTreeNumber(String treeNumber) {
		treeNumbers.add(treeNumber);
	}
	
	public List<String> getTreeNumbers() {
		return treeNumbers;
	}
}
