import java.net.URL;
import java.io.*;

/**
   Simple demonstration of how to read content from a HIVE-RS web service.
**/


public class SampleHiveRS extends Object {

    public static final String RS_BASE = "http://hive.nescent.org/hive-rs/schemes/";
    
    public static void main(String[] args) throws Exception {
	URL vocabListURL = new URL(RS_BASE+"nbii/concepts/prefLabels/a");

	BufferedReader theReader;
	theReader = new BufferedReader(
		       new InputStreamReader(vocabListURL.openStream()));

	String aLine = theReader.readLine();
		
	while(aLine != null) {
	    System.out.println(aLine);
	    aLine = theReader.readLine();
	}
	
	theReader.close();
	
    }
}
