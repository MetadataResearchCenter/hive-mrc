package edu.unc.ils.mrc.hive.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;

public class ConceptTreeBuilder 
{
	Map<String, List<String>> conceptTree = new TreeMap<String,List<String>>();
	Map<String, String> concepts = new TreeMap<String, String>();
	List<String> topConcepts = new ArrayList<String>();
	
	public List<ConceptNode> getTree()
	{
		List<ConceptNode> cs = new ArrayList<ConceptNode>();
		for (String uri: topConcepts) 
		{
			ConceptNode node = new ConceptNode();
			String label = concepts.get(uri);
			node.setLabel(label);
			node.setUri(uri);
			getTree(node, uri);
			cs.add(node);
		}
		return cs;
	}
	
	private void getTree(ConceptNode node, String uri) {

		List<String> childURIs = conceptTree.get(uri);
		if (childURIs != null)
		{
			for (String childURI: childURIs) {
				String label = concepts.get(childURI);
				ConceptNode child = new ConceptNode();
				child.setLabel(label);
				child.setUri(childURI);
				node.addChild(child);
				getTree(child, childURI);
			}
		}
	}
	
	public void add(SKOSConcept concept, SKOSSearcher searcher) 
	{	
		String prefLabel = concept.getPrefLabel();
		String uri = concept.getQName().getNamespaceURI() + concept.getQName().getLocalPart();
		
		ConceptNode node = new ConceptNode();
		node.setLabel(prefLabel);
		node.setUri(uri);
		
		concepts.put(uri, prefLabel);
		
		TreeMap<String, QName> broaders = concept.getBroaders();
		if (broaders.size() > 0)
		{
			for (String key: broaders.keySet()) 
			{
				QName qname = broaders.get(key);

				SKOSConcept broader = searcher.searchConceptByURI(qname.getNamespaceURI(), 
						qname.getLocalPart());
				
				String broaderUri = broader.getQName().getNamespaceURI() + broader.getQName().getLocalPart();
				String broaderLabel = broader.getPrefLabel();
				concepts.put(broaderUri, broaderLabel);
				
				List<String> children = conceptTree.get(broaderUri);
				if (children == null) {
					children = new ArrayList<String>();
				}
				if (!children.contains(uri))
					children.add(uri);
				conceptTree.put(broaderUri, children);
				
				add(broader, searcher);
			}
		}
		else {
			if (!topConcepts.contains(uri))
				topConcepts.add(uri);
		}
	}
}
