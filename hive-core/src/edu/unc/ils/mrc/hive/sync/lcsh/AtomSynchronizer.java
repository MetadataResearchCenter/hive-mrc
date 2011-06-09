package edu.unc.ils.mrc.hive.sync.lcsh;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.icu.text.SimpleDateFormat;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSSchemeImpl;

/**
 * Synchronize the the LCSH vocabulary in HIVE using the id.loc.gov Atom feed.
 * @author craig.willis@unc.edu
 */
public class AtomSynchronizer 
{
	private static final Log logger = LogFactory.getLog(AtomSynchronizer.class);
			
	/* Current feed page being processed */
	protected int currentPage = 1;
	
	/* SKOSScheme representation of vocabulary*/
	protected SKOSScheme scheme;
	
	/* URL for atom feed */
	protected String feedUrl;
	
	/* Maximum number of pages to process */
	protected static int MAX_PAGES = 4000;
	
	/* List of updated concept URIs */
	protected List<String> updatedEntries = new ArrayList<String>();
	
	/* List of deleted concept URIs */
	protected List<String> deletedEntries = new ArrayList<String>();
	
	/** 
	 * Construct a sychronizer for the specified scheme.
	 * @param scheme
	 */
	public AtomSynchronizer(SKOSScheme scheme) 
	{
		this.scheme = scheme;
		this.feedUrl = scheme.getAtomFeedURL();
	}
	
	/**
	 * Process the feed
	 */
	public void processUpdates()
	{
		logger.trace("processUpdates()");
		
		try {
			// Get the date this vocabulary was last updated. If the 
			// last update date is empty, use the creation date from the 
			// configuration file.
			Date lastUpdate = (scheme.getLastUpdateDate() != null) ? 
					scheme.getLastUpdateDate() : scheme.getCreationDate();
			
			processUpdates(lastUpdate, null);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	/**
	 * Process the feed for the specified date range
	 * @param startDate
	 * @param endDate
	 */
	public void processUpdates(Date startDate, Date endDate)
	{
		logger.trace("processUpdates " + startDate + "," + endDate);
		
		try
		{
			// Read the updates and deletes frmo the feed
			readFeed(startDate, endDate);
			
			// Process updates
			for (String uri: updatedEntries) 
			{	
				QName qname = new QName(uri, "#concept");
				logger.debug("Updating " + qname);
				
				scheme.importConcept(qname, uri + ".rdf");
			}
			logger.info("Updated " + updatedEntries.size() + " concepts");
			
			// Process deletes
			for (String uri: deletedEntries) 
			{
				QName qname = new QName(uri, "#concept");
				logger.debug("Deleting " + qname);
				
				scheme.deleteConcept(qname);
			}
			logger.info("Deleted " + deletedEntries.size() + " concepts");

		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	/**
	 * Read the updates and deletes from the feed
	 * @param startDate
	 * @param endDate
	 * @throws Exception
	 */
	public void readFeed(Date startDate, Date endDate) throws Exception
	{
		logger.trace("readFeed " + startDate + "," + endDate);
		
		if (endDate == null)
			endDate = new Date();
		
		logger.debug("Using feed URL: " + feedUrl);
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		boolean done = false;
		
		for (int x=1; x<MAX_PAGES; x++) 
		{
			if (done)
				break;
			
			URL url = new URL(feedUrl + "page/" + x);
			logger.debug("Reading updates from feed URL " + url);
			
			Document document = db.parse(url.openStream());

			XPath xpath = XPathFactory.newInstance().newXPath();
			
			// Get all of the updates 
			String expression = "/feed/entry";
			NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
			for (int i=0; i<nodes.getLength(); i++) {
				Node node = nodes.item(i);
				Date updatedDate = null;
				String uri = null;
				
				NodeList children = node.getChildNodes();
				for (int j=0; j<children.getLength(); j++) {
					Node child = children.item(j);
					String name = child.getNodeName();
					if (name != null)
					{
						if (name.equals("updated")) {
							NodeList text = child.getChildNodes();
							String date = text.item(0).getNodeValue();
							if (date != null) {
								updatedDate = javax.xml.bind.DatatypeConverter.parseDateTime(date).getTime();
							}
						}
						else if (name.equals("link")) {
							NamedNodeMap attrMap = child.getAttributes();
							Node href = attrMap.getNamedItem("href");
							Node type = attrMap.getNamedItem("type");
							if (type == null)
								uri = href.getNodeValue();
						}
					}
				}
				if (updatedDate != null)
				{
					if (startDate != null && endDate != null)
					{
						if (updatedDate.after(startDate) && updatedDate.before(endDate))
							updatedEntries.add(uri);
						else if (updatedDate.before(startDate)) {
							done = true;
							break;
						}
					}
				}
			}
			
			expression = "/feed/deleted-entry";
			NodeList deletedNodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
			for (int i=0; i<deletedNodes.getLength(); i++) {
				Node node = deletedNodes.item(i);
				Date updatedDate = null;
				String uri = null;
				
				NodeList children = node.getChildNodes();
				for (int j=0; j<children.getLength(); j++) {
					Node child = children.item(j);
					if (child.getNodeName().equals("updated")) {
						NodeList text = child.getChildNodes();
						String date = text.item(0).getNodeValue();
						if (date != null)
							updatedDate = javax.xml.bind.DatatypeConverter.parseDateTime(date).getTime();
					}
					else if (child.getNodeName().equals("id")) {
						NodeList id = child.getChildNodes();
						String entryId = id.item(0).getNodeValue();
						uri = entryId.replace("info:lc/", "http://id.loc.gov/");
					}
				}
				if (updatedDate != null)
				{
					if (startDate != null && endDate != null)
					{
						if (updatedDate.after(startDate) && updatedDate.before(endDate))
							deletedEntries.add(uri);
						else if (updatedDate.before(startDate)) {
							done = true;
							break;
						}
					}
				}
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		
		String confPath = "/user/local/hive/conf/"; // args[0];
		
		SKOSScheme scheme = new SKOSSchemeImpl(confPath, "lcsh", true);
		AtomSynchronizer as = new AtomSynchronizer(scheme);

		//SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss aa");
		//Date d1 = df.parse("04-07-2011 12:00:00 PM");
		//Date d2 = df.parse("04-08-2011 09:00:00 AM");
		//as.processUpdates(d1, d2);
		as.processUpdates();
		scheme.close();
	}
}
