package edu.unc.ils.mrc.hive.util;

import java.net.URL;

import junit.framework.TestCase;

public class SimpleCrawlerTest extends TestCase {

	public static void testCrawl()
	{
		SimpleTextCrawler sc = new SimpleTextCrawler();
		try
		{
			
			URL url = new URL("http://ils.unc.edu/mrc/");
			String text = sc.getText(url, 1, true);
			System.out.println(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
