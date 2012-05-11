package edu.unc.ils.mrc.hive.util;

import java.io.ByteArrayInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.io.PrintWriter;
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
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
	
	/* Used to include *html files during LC test collection generation  */
	private boolean saveHTML = false;
	private static String htmlForURL = "";

	/* Reads the following command line arguments:
	 *    *.xls input file with URLs and associated subject headings
	 *    output directory for generated files 
	 *    number of hops (optional, default is 0)
	 *    differencing enabled (default is no)
	 *    Example: -f c:\test\testdata.xls  -o c:\testout\  -n 3 -d
	 * Invokes the text crawler for each URL and generates associated 
	 *   *.txt, *.key, and *.html files
	 */
	public static void main(String[] args) throws ParseException {

		CommandLineParser parser = new BasicParser();
		Options options = getOptions();
		CommandLine commandLine = parser.parse(options, args);

		if (commandLine.hasOption("h")) {
			// Print the help message
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java edu.unc.ils.mrc.hive.util.SimpleTextCrawler",
					options);
		} else {
			String urlFileName = commandLine.getOptionValue("f");
			String outputDir = commandLine.getOptionValue("o");
			int numberOfHops = 0;
			boolean differencingEnabled = false;
			if (commandLine.hasOption("d"))
				differencingEnabled = true;
			try {
				File fileObject = new File(outputDir);
				if (!fileObject.exists()) {
					fileObject.mkdirs(); 
				}
				if (commandLine.hasOption("n")) {
					numberOfHops = Integer.parseInt(commandLine.getOptionValue("n"));
				}
				try {
					HSSFWorkbook wb = readFile(urlFileName);
					for (int k = 0; k < wb.getNumberOfSheets(); k++) {
						HSSFSheet sheet = wb.getSheetAt(k);
						int rows = sheet.getPhysicalNumberOfRows();
						
						System.out.println("Input file=" + urlFileName + " and has " + rows	+ " rows.");
						System.out.println("Output directory=" + outputDir + 
								           ", number of hops=" + numberOfHops +
								           ", differencing " + (differencingEnabled ? "enabled" : "disabled"));
						for (int r = 0; r < rows; r++) {
							HSSFRow row = sheet.getRow(r);
							if (row == null) {
								continue;
							}
							int cells = row.getPhysicalNumberOfCells();
							//System.out.println("\nROW " + row.getRowNum() + " has " + cells + " cell(s).");
							PrintWriter outtxt = null;
							PrintWriter outkey = null;
							PrintWriter outhtml = null;
							String txtFileName = null;
							String keyFileName = null;
							String htmlFileName = null;
							for (int c = 0; c < cells; c++) {
								HSSFCell cell = row.getCell(c);
								String value = null;
								String fileName = null;
								value = cell.getStringCellValue();
								try {
									//System.out.println("CELL col=" + cell.getColumnIndex() + " VALUE="+ value);
									SimpleTextCrawler sc = new SimpleTextCrawler();
									if (c == 0) {
										fileName = generateFileName(value);
									    txtFileName = fileName + ".txt";
                                        keyFileName = fileName + ".key";
                                        htmlFileName = fileName + ".html";
										System.out.println("txtFileName = "	+ txtFileName);
										outtxt = new PrintWriter(outputDir + txtFileName);
										outkey = new PrintWriter(outputDir + keyFileName);
										outhtml = new PrintWriter(outputDir + htmlFileName);
										URL url = new URL(cell.getStringCellValue());
										String text = sc.getTextAndHTML(url,numberOfHops,differencingEnabled);
										outtxt.print(text);
										outtxt.close();
										outhtml.print(htmlForURL);
										outhtml.close();
									}
									if (c > 0) {
										outkey.println(cell.getStringCellValue().toUpperCase());
									}
								} catch (FileNotFoundException e) {
									logger.error("Unable to create " + fileName+".txt" + " or " + fileName+".key"); 
									break;}
								  catch (SAXException e) {
									  e.printStackTrace(); }
								  catch (TikaException e) {
									  e.printStackTrace(); }
							}
							if (outkey != null) 
								outkey.close();
						}
					}
				}	 catch (IOException e) {
					logger.error("Unable to read file " + urlFileName);
				}
			} catch (NumberFormatException e) {
				logger.error("Number of hops must be an integer value. ");
			}
			catch (SecurityException e) {
				logger.error("Unable to create directory " + outputDir);
			}
		}
	}
	
	/**
	 * Returns the CLI options
	 * @return
	 */
	public static Options getOptions() {
		Options options = new Options();
		Option urlFile = new Option("f", true, "Input file of URLS to be crawled");
		urlFile.setRequired(true);
		options.addOption(urlFile);

		Option outputDir = new Option("o", true, "Output directory for *.txt and *.key files");
		outputDir.setRequired(true);
		options.addOption(outputDir);

		options.addOption("h", false, "Print this help message");
		options.addOption("n", true,
				"Number of hops. (Default=0, first page only)");
		options.addOption("d", false, "Enable differencing (Default=true)");
		return options;
	}
	
	/* Generate a file name from the domain name by removing trailing slash (if present)
	 * and replacing dots with underscores.
	 */
	private static String generateFileName(String urlString) {
		String fname = urlString.trim();
		if (fname.endsWith("/"))
			fname = fname.substring(0,fname.length() - 1);
		int pos = fname.lastIndexOf("/");
		if (pos > 0)
		    fname = fname.substring(pos+1);
		fname = fname.replace(".","_");
		return fname;
	}

	/**
	 * creates a HSSFWorkbook for the specified filename.
	 */
	private static HSSFWorkbook readFile(String filename) throws IOException {
		return new HSSFWorkbook(new FileInputStream(filename));
	}
	
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
	
	/* Same as getText(URL, int, boolean) above, but is used by the main method
	 * to generate LC test collection files: *.txt, *.key, and *.html 
	 */
	public String getTextAndHTML(URL url, int maxHops, boolean diff) throws ClientProtocolException, IOException, TikaException, SAXException {
		saveHTML = true;
		htmlForURL = "";
		
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

		if (saveHTML)
		   htmlForURL = htmlForURL + html;

		try
		{
			// Get the text of the current page
			String tmpText =  getTextFromHtml(html);

			
			String diffText = "";
			if (currentHop > 0 && baseText != null)
			{
				diffText = getDiff(tmpText, baseText);
			
				//diffText = diffText.replaceAll("\\s+", " ");
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
				diff += line + "\n";
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
			absoluteUrl = "";
			logger.warn(e);
		} catch (URISyntaxException e) {
			logger.warn(e);
		}
		return absoluteUrl;
	}
}
