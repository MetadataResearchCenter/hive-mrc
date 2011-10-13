package edu.unc.ils.mrc.hive.util;

import java.io.ByteArrayInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
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

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

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
	Map<String, Integer> retrievedURLs = new HashMap<String, Integer>();
	
	private String proxyHost = null;
	private int proxyPort = -1;
	private List<Pattern> ignorePatterns = new ArrayList<Pattern>();
	
	public void setProxy(String host, int port) {
		this.proxyHost = host;
		this.proxyPort = port;
	}
	
	public void setIgnorePrefixes(List<String> prefixes) {
		if (prefixes != null)
		{
			for (String pattern: prefixes)
			{
				Pattern p = Pattern.compile("(" + pattern + ").*?");
				ignorePatterns.add(p);
			}
		}
	}
	
	/**
	 * Returns a text representation of the website at the specified URL by crawling
	 * all links up to the maximum number of "hops" from the initial URL.
	 * 
	 * @param url 		Website to retrieve text for
	 * @param maxHops	Maximum number of hops to crawl
	 * @param diff		Only extract differences between base page and subsequent pages
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String getText(URL url, int maxHops, boolean diff) throws ClientProtocolException, IOException, TikaException, SAXException {
		String baseText = null;
		if (diff) {
			String baseHTML = getHtml(url.toString());
			baseText =  getTextFromHtml(baseHTML);
		}
		return getText(url.toString(), url.toString(), maxHops, 0, baseText);
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
	public String getText(URL url, String baseURL, int maxHops, boolean diff) throws ClientProtocolException, IOException, TikaException, SAXException {
		String baseText = null;
		if (diff) {
			String baseHTML = getHtml(url.toString());
			baseText =  getTextFromHtml(baseHTML);
		}
		return getText(url.toString(), baseURL, maxHops, 0, baseText);
	}
	
	public boolean isPartOf(String url, String baseUrl)
	{
		String newBaseUrl = stripPrefix(baseUrl);
		String newUrl = stripPrefix(url);
		return newUrl.startsWith(newBaseUrl);
		
		/*
		if (baseUrl.startsWith("http://webarchive.loc.gov"))
		{
			String archiveUrl = url.substring(url.lastIndexOf("http://"), url.length());
			String archiveBase = baseUrl.substring(baseUrl.lastIndexOf("http://"), baseUrl.length());
			return archiveUrl.startsWith(archiveBase);
		}
		else
			return url.startsWith(baseUrl);
	*/
		
	}
	
	private String stripPrefix(String url)
	{
		String newUrl = url;
		for (Pattern p: ignorePatterns)
		{
			Matcher m = p.matcher(url);
			if (m.matches())
			{
				String prefix = m.group(1);
				// Ignore this prefix
				newUrl = url.substring(prefix.length(), url.length());
				break;
			}
		}
		return newUrl;
	}
	
	public String stripLocPrefix(String url)
	{
		if (url.startsWith("http://webarchive.loc.gov"))
			return url.substring(url.lastIndexOf("http://"), url.length());
		else
			return url;
	}
	
	private String getText(String url, String baseURL, int maxHops, 
			int currentHop) throws ClientProtocolException, IOException
	{
		String baseHTML = getHtml(url);
		return getText(url, baseURL, maxHops, currentHop, baseHTML);
	}
	
	private String getHtml(String url) throws IOException
	{
		String html = "";
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
	
		HttpParams params = new BasicHttpParams();
		params.setParameter("http.protocol.handle-redirects", true);
		get.setParams(params);

		if (proxyHost != null)
		{
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		
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
			html = sw.toString();
		}
		client.getConnectionManager().shutdown();
		return html;
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
			int currentHop, String baseText) throws ClientProtocolException, IOException
	{
		logger.debug("getText " + url + "(" + maxHops + "," + currentHop + ")");
		
		// Add this URL to the list of processed URLs
		String tmpUrl = stripLocPrefix(url);
		Integer tmpHop = retrievedURLs.get(tmpUrl);
		if (tmpHop != null && tmpHop <= currentHop) {
			logger.debug("Skipping " + tmpUrl + ", already seen");
			return "";
		}
		if (!isPartOf(url, baseURL)) {
			logger.debug("Skipping " + url + ", not part of current site.");
			return "";
		}
		
		String html = getHtml(url);
		String text = "";


		try
		{
			// Get the text of the current page
			String tmpText =  getTextFromHtml(html);

			
			String diffText = "";
			if (currentHop > 0 && baseText != null)
			{
				diffText = getDiff(tmpText, baseText);
			
				diffText = diffText.replaceAll("\\s+", " ");
				diffText = diffText.replaceAll(tmpUrl.toLowerCase(), "");
				text += diffText;
			}
			else
				text = tmpText;
			
			// Add this URL to the list of processed URLs
			if (tmpHop == null || tmpHop > currentHop)
				retrievedURLs.put(tmpUrl, currentHop);
		} catch (Exception e) {
			logger.warn(e);
		}
		
		// Continue to process additional links
		if (currentHop < maxHops) 
		{
			logger.debug("Getting links from " + tmpUrl);
			// Get links from the current page
			List<String> links = getLinks(url, new StringBuffer(html));
			for (String link : links) {
				String tmpLink = stripLocPrefix(link);
				// For each link, if not already processed, get text
				tmpHop = retrievedURLs.get(tmpLink);
				if (tmpHop == null || tmpHop > currentHop)
				{
				    //if (!retrievedURLs.containsKey(tmpLink)) {
					String tmp = getText(link, baseURL, maxHops, currentHop+1, baseText);
					text += tmp;
				} 
			} 
		}
		

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
		ContentHandler handler = new BodyContentHandler(-1);
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
		ContentHandler handler = new BodyContentHandler(-1);
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
	
	private String getDiff(String base, String current)
	{
		List<String> baseRows = Arrays.asList(base.split("\n"));
		List<String> currentRows = Arrays.asList(current.split("\n"));
		Patch patch = DiffUtils.diff(baseRows, currentRows);
		List<Delta> deltas = patch.getDeltas();
		String diff = "";
		for (Delta delta: deltas) {
			Chunk c= delta.getOriginal();
			List<String> lines = (List<String>) c.getLines();
			for (String line : lines)
				diff += line;
		}
		return diff;
	}

	/**
	 * Returns true if the specified URL is a valid HTTP URL.
	 * @param s
	 * @return
	 */
	private boolean valid(String s) {
		if (s.matches("javascript:.*|mailto:.*") || s.equals("#")) {
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
		} catch (IllegalArgumentException e) {
			logger.warn(e);
		} catch (URISyntaxException e) {
			logger.warn(e);
		}
		return absoluteUrl;
	}
	
	public String readFile(File file) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		String text ="";
		while ((line = br.readLine()) != null)
			text += line + "\n";
		
		br.close();
		return text;
	}
	public static void main(String[] args) throws Exception
	{
		
		SimpleTextCrawler stc = new SimpleTextCrawler();
		List<String> ignore = new ArrayList<String>();
		ignore.add("http://webarchive.loc.gov/lcwa[^/]*/[^/]*/");
		ignore.add("http://loc-wm.archive.org/all/[^/]*/");
		//stc.setProxy("wayback.archive-it.org", 9194);
		//stc.setIgnorePrefixes(ignore);
		
		URL url = new URL("http://webarchive.loc.gov/lcwa0010/20050422184603/http://www.hcef.org/hcef/");
		//URL url = new URL("http://www.ncadfp.org/");
		String text = stc.getText(url, 1, true);
		
		System.out.println(text);
		/*
		String source = stc.readFile(new File("/Users/cwillis/Desktop/hcef1.htm"));
		String target = stc.readFile(new File("/Users/cwillis/Desktop/hcef2.cfm"));
		String srcTxt = stc.getTextFromHtml(source);
		String targetTxt = stc.getTextFromHtml(target);
		String diff = stc.getDiff(srcTxt, targetTxt);
		diff = diff.replaceAll("\\s+", " ");
		System.out.println(diff);
		*/

	}
}
