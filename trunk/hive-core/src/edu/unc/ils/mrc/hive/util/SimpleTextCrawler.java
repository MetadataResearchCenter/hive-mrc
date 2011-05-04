package edu.unc.ils.mrc.hive.util;

import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This simple web crawler creates a textual representation of a website 
 * with all HTML, javascript, and CSS removed. The crawler starts with an 
 * initial URL and traverses links up to a maximum specific number of "hops".
 * The result is a text document that contains the contents of multiple web-pages
 * in the order traversed. 
 */
public class SimpleTextCrawler 
{	
	/* Logger */
	private static final Log logger = LogFactory.getLog(SimpleTextCrawler.class);
			
	/* Link extraction pattern */
	static final Pattern HREF_PATTERN = Pattern.compile("<a\\b[^>]*href=\"([^\"]*)\"[^>]*>");
	
	/* Map of crawled URLs */
	Map<String, String> retrievedURLs = new HashMap<String, String>();
	
	/**
	 * Returns a text representation of the website at the specified URL by crawling
	 * all links up to the maximum number of "hops" from the initial URL.
	 * 
	 * @param url 		Website to retrieve text for
	 * @param maxHops	Maximum number of hops to crawl
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String getText(URL url, int maxHops) throws ClientProtocolException, IOException {
		return getText(url.toString(), url.toString(), maxHops, 0);
	}
	
	/**
	 * Returns a text representation of the website at the specified URL by crawling
	 * all links up to the maximum number of "hops" from the initial URL limited 
	 * by the base URL. All links must match the base URL to be traversed.
	 * 
	 * @param url		Website to retrieve text for
	 * @param baseURL	Base URL used to filter links
	 * @param maxHops	Maximum number of hops to crawl
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String getText(URL url, String baseURL, int maxHops) throws ClientProtocolException, IOException {
		return getText(url.toString(), baseURL, maxHops, 0);
	}
	
	/**
	 * Internal method used to recursively traverse a website up to the maximum number of "hops"
	 * 
	 * @param url			Website to be crawled
	 * @param baseURL		Base URL used as a filter
	 * @param maxHops		Maximum number of hops to crawl
	 * @param currentHop	Current hop
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private String getText(String url, String baseURL, int maxHops, 
			int currentHop) throws ClientProtocolException, IOException
	{
		logger.debug("getText " + url + "(" + maxHops + "," + currentHop + ")");
		
		if (!url.startsWith(baseURL)) {
			logger.debug("Skipping " + url + ", not part of current site.");
			return "";
		}
		
		String text = "";
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
	
		HttpParams params = new BasicHttpParams();
		params.setParameter("http.protocol.handle-redirects", true);
		get.setParams(params);

		HttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		
		if (entity != null) {
			
			Header contentType = entity.getContentType();
			
			
			try
			{
				// Only process links of type text/html
				if (!contentType.getValue().contains("text/html"))
					return getTextFromURL(url);
			} catch (Exception e) {
				logger.error(e);
			}
			
			// Read the response
			InputStream is = entity.getContent();	
			StringWriter sw = new StringWriter();
			int c;
			while ((c = is.read()) != -1)
				sw.write(c);
			is.close();
			sw.close();
			
			// Get text of current page
			String html = sw.toString();
			try
			{
				// Get the text of the current page
				String tmp =  getTextFromHtml(html);
				tmp = tmp.replaceAll("\\s+", " ");
				text += tmp;
				// Add this URL to the list of processed URLs
				retrievedURLs.put(url, "1");
			} catch (Exception e) {
				logger.warn(e);
			}
			
			// Continue to process additional links
			if (currentHop < maxHops) 
			{
				// Get links from the current page
				List<String> links = getLinks(url, sw.getBuffer());
				for (String link : links) {
					// For each link, if not already processed, get text
					if (!retrievedURLs.containsKey(link)) {
						String tmp = getText(link, baseURL, maxHops, currentHop+1);
						text += tmp;
					}
				} 
			}
		}
		
		client.getConnectionManager().shutdown();
		return text;
	}
	
	/**
	 * Uses the Tika library to extract text from HTML
	 * 
	 * @param html HTML to process
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws TikaException
	 */
	protected String getTextFromHtml(String html) throws IOException, SAXException, TikaException 
	{
		InputStream is = new ByteArrayInputStream(html.getBytes());
		Metadata metadata = new Metadata();
		Parser parser = new AutoDetectParser();
		ContentHandler handler = new BodyContentHandler();
		parser.parse(is, handler, metadata);
		is.close();
		return handler.toString();
	}
	
	/**
	 * Uses the Tika library to extract text from a URL
	 * 
	 * @param path	URL to process
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws TikaException
	 */
	protected String getTextFromURL(String path) throws IOException, SAXException, TikaException 
	{
		URL url = new URL(path);

		InputStream is = url.openStream();
		Metadata metadata = new Metadata();
		
		int slash = path.lastIndexOf('/');
		String name = path.substring(slash + 1);
		if (name.length() > 0) {
			metadata.set(Metadata.RESOURCE_NAME_KEY, name);
		}		
		Parser parser = new AutoDetectParser();
		ContentHandler handler = new BodyContentHandler();
		parser.parse(is, handler, metadata);
		is.close();
		return handler.toString();
	}
	
	/**
	 * Returns a list of absolute URLs from the fetched page
	 * @param baseUrl	Base URL
	 * @param sb		Fetched HTML	
	 * @return
	 */
	protected List<String> getLinks(String baseUrl, StringBuffer html)
	{		
		List<String> links = new ArrayList<String>();

		Matcher tagmatch = HREF_PATTERN.matcher(html.toString());
		while (tagmatch.find()) 
		{
			String link = tagmatch.group(1);
			if (valid(link)) {
				links.add(makeAbsolute(baseUrl, link));
			}
		}	
		
		return links;
	}
	

	/**
	 * Returns true if the specified URL is a valid HTTP URL.
	 * @param s
	 * @return
	 */
	private boolean valid(String s) {
		if (s.matches("javascript:.*|mailto:.*")) {
			return false;
		}
		return true;
	}

	/**
	 * Create an absolute URL given a base URL and relative URL
	 * @param url  Base URL
	 * @param link Relative URL
	 * @return
	 */
	protected static String makeAbsolute(String baseUrl, String relativeUrl) 
	{
		String absoluteUrl = "";
		try {
			if (!baseUrl.endsWith("/"))
				baseUrl += "/";
			
			URI base = new URI(baseUrl);
			absoluteUrl = base.resolve(relativeUrl).toString();
			
		} catch (URISyntaxException e) {
			logger.warn(e);
		}
		return absoluteUrl;
	}
}
