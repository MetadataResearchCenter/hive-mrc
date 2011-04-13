/**
 * Copyright (c) 2010, UNC-Chapel Hill and Nescent
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided 
that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and 
 * the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the UNC-Chapel Hill or Nescent nor the names of its contributors may be used to endorse or promote 
 * products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

@author Jose R. Perez-Aguera
 */

package edu.unc.ils.mrc.hive.util;

import java.io.File;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


public class TextManager 
{
	
	/**
	 * Returns a plain-text document representation of a website. 
	 * @param url		URL to crawl
	 * @param maxHops	Maximum number of hops
	 * @return			Text representation of website
	 * @throws IOException
	 */
	public String getPlainText(URL url, int maxHops) throws IOException
	{
		SimpleTextCrawler sc = new SimpleTextCrawler();
		String text = sc.getText(url, maxHops);
		return text;
	}
	
	/**
	 * Returns plain-text given a local file path or URL.
	 * @param inputPath	Local path or URL
	 * @return
	 */
	public String getPlainText(String inputPath) {
		InputStream is = null;
		
		String text = "";
		try {
			Metadata metadata = new Metadata();
			File file = new File(inputPath);
			if (file.isFile()) {
				metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
				is = new FileInputStream(file);
			} else {
				URL url = new URL(inputPath);
				String path = url.getPath();
				int slash = path.lastIndexOf('/');
				String name = path.substring(slash + 1);
				if (name.length() > 0) {
					metadata.set(Metadata.RESOURCE_NAME_KEY, name);
				}
				is = url.openStream();
			}
			text = parse(is, metadata);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return text;
	}
	
	
	/**
	 * Returns plain-text representation of a file given an inputstream.
	 * @param is
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws TikaException
	 */
	public String getPlainText(InputStream is) throws IOException, SAXException, TikaException {
		Metadata metadata = new Metadata();
		return parse(is, metadata);
	}

	
	/**
	 * Parses contents of an input stream using Tikka content extractor.
	 * @param is
	 * @param metadata
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws TikaException
	 */
	protected String parse(InputStream is, Metadata metadata) throws IOException, SAXException, TikaException 
	{
		Parser parser = new AutoDetectParser();
		ContentHandler handler = new BodyContentHandler();
		parser.parse(is, handler, metadata);
		return handler.toString();
	}
	
	
	/**
	 * Simple command line to convert a directory of files to text.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException 
	{
		if (args.length != 1) {
			System.err.println("Usage: java " + TextManager.class.getName() + "[path to directory of files]");
		}
		
		String dir = args[0];
		File inputDir = new File(dir);
		if (inputDir.isDirectory())
		{
			File[] files = inputDir.listFiles();
			for (File file: files)
			{
				TextManager tm = new TextManager();
				String pdfPath = file.getAbsolutePath();
				String txtPath = pdfPath.substring(0, pdfPath.lastIndexOf('.')) + ".txt";
				String text = tm.getPlainText(pdfPath);
				FileWriter txtWriter = new FileWriter(txtPath);
				txtWriter.write(text);
				txtWriter.close();
			}
		}
		else 
			System.err.println("Error: A directory must be specified");
	}
}
