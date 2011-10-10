package org.unc.hive.client;

import java.util.TreeMap;

//jpb import com.claudiushauptmann.gwt.multipage.client.MultipageEntryPoint;
import org.gwtmultipage.client.MultipageEntryPoint;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

@MultipageEntryPoint(urlPattern = "/test.html")

public class TestVis implements EntryPoint {

	  public void onModuleLoad() {
		  
		  loadTermVisScript();
		  
		  TreeMap<String, Integer> testConcepts = new TreeMap<String, Integer>();
		  
		  testConcepts.put("abdomen", 5);
		  testConcepts.put("kitty", 30);
		  testConcepts.put("bamboo", 10);
		  testConcepts.put("garden", 1);
		  testConcepts.put("ShakeSpear", 50);
		  testConcepts.put("Paris", 20);
		  testConcepts.put("China", 50);
		  testConcepts.put("Fuzhou", 20);
		  testConcepts.put("School", 15);
		  testConcepts.put("City", 35);
		 
		  
		 
	
	  }
	  
	  private void loadTermVisScript()
	  {
		  ScriptElement TCscript = Document.get().createScriptElement();
		  TCscript.setPropertyString("language", "javascript");
		  TCscript.setPropertyString("src", "http://visapi-gadgets.googlecode.com/svn/trunk/termcloud/tc.js");
		  Document.get().getElementsByTagName("head").getItem(0).appendChild(TCscript);
		  
		  
		  ScriptElement script = Document.get().createScriptElement();
		  script.setPropertyString("language", "javascript");
		  script.setPropertyString("src", "http://www.google.com/jsapi");
		  Document.get().getElementsByTagName("head").getItem(0).appendChild(script);
		  
		  
		  LinkElement tcCss = Document.get().createLinkElement();
		  tcCss.setPropertyString("rel", "stylesheet");
		  tcCss.setPropertyString("type", "text/css");
		  tcCss.setPropertyString("href", "http://visapi-gadgets.googlecode.com/svn/trunk/termcloud/tc.css");
		  
		  
		  
	  }
	  
	  private native void createTermCloud(JsArrayString terms, JsArrayInteger frequency)
	  /*-{
	 
	  	
	  	 for (var i = 0; i < terms.length(); i++)
	  	 {
	  	 }
               
	  		  	
	  	
	   }-*/;
	   
	  
	  
	}

