package edu.unc.ils.mrc.hive.api;

import java.util.HashMap;
import java.util.List;

public interface SPARQLEngine {
	
	public String runSPARQL(String qs, String format);
	public List<HashMap> runSPARQL(String qs);

}
