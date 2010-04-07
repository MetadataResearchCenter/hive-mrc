package edu.unc.ils.mrc.hive.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class TextManager {

	private Parser parser;
	private ContentHandler handler;
	private Metadata metadata;

	public TextManager() {
		this.parser = new AutoDetectParser();
		this.handler = new BodyContentHandler();
		this.metadata = new Metadata();
	}

	public String getPlainText(String inputPath) {
		InputStream is = null;
		try {
			File file = new File(inputPath);
			if (file.isFile()) {
				this.metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
				is = new FileInputStream(file);
			} else {
				URL url = new URL(inputPath);
				String path = url.getPath();
				int slash = path.lastIndexOf('/');
				String name = path.substring(slash + 1);
				if (name.length() > 0) {
					this.metadata.set(Metadata.RESOURCE_NAME_KEY, name);
				}
				is = url.openStream();
			}
			this.parser.parse(is, this.handler, this.metadata);
			return this.handler.toString();
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

		return this.handler.toString();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
