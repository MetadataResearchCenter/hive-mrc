package edu.unc.ils.mrc.hive.api;

import java.util.TreeMap;

import javax.xml.namespace.QName;

public interface SKOSServer {
	
	public TreeMap<String, SKOSScheme> getSKOSSchemas();
	public SKOSSearcher getSKOSSearcher();
	public SKOSTagger getSKOSTagger();
	public String getOrigin(QName uri);
	public void close();

}
