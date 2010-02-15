package edu.unc.ils.mrc.hive.ir.lucene.indexing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;

public class IndexAdministrator {

	public static String getDate(String indexName) {
		try {
			Long date = IndexReader.lastModified(indexName);
			return new Date(date).toString();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static TreeMap<String, QName> getSubAlphaIndex(String startLetter,
			TreeMap<String, QName> index) {
		boolean finded = false;
		String start = "";
		String end = "";
		int j = 0;
		String ant = null;
		Set<String> keys = index.keySet();
		for (String s : keys) {
			String sl = s.toLowerCase();
			if (sl.startsWith(startLetter) && !finded) {
				start = s;
				finded = true;
			}
			if(finded)
				j++;
			if (!sl.startsWith(startLetter) && finded) {
				end = ant;
				break;
			}
			ant = s;
		}

		TreeMap<String, QName> tree = null;
		if (j > 1) {
			SortedMap<String, QName> l2 = index.subMap(start, end);
			System.out.println("Result size: " + l2.size());
			return tree = new TreeMap<String, QName>(l2);
		} else if (j == 1) {
			tree = new TreeMap<String, QName>();
			tree.put(start,index.get(start));
			System.out.println("Result size: " + tree.size());
			return tree;
		}

		return tree = new TreeMap<String, QName>();
	}

	public static TreeMap<String, QName> getTopConceptIndex(
			String topConceptFilePath) {
		File fichero = new File(topConceptFilePath);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new FileInputStream(fichero));
			TreeMap<String, QName> l = (TreeMap<String, QName>) ois
					.readObject();
			ois.close();
			System.err.println("Top Concpet Index loaded from "
					+ topConceptFilePath);
			return l;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static TreeMap<String, QName> getAlphaIndex(String alphaFilePath) {
		File fichero = new File(alphaFilePath);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new FileInputStream(fichero));
			TreeMap<String, QName> l = (TreeMap<String, QName>) ois
					.readObject();
			ois.close();
			System.err.println("AlphaIndex loaded from " + alphaFilePath);
			return l;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static int getNumconcepts(String indexName) {
		try {
			return IndexReader.open(indexName).numDocs();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public static int getNumBroader(String indexName) {
		IndexSearcher is;
		try {
			is = new IndexSearcher(indexName);
			IndexReader ir = is.getIndexReader();
			TermEnum te = ir.terms();
			int broader = 0;
			while (te.next()) {
				Term currTerm = te.term();
				if (currTerm.field().equals("broaderURI"))
					broader++;
			}
			te.close();
			is.close();
			return broader;
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public static int getNumNarrower(String indexName) {
		IndexSearcher is;
		try {
			is = new IndexSearcher(indexName);
			IndexReader ir = is.getIndexReader();
			TermEnum te = ir.terms();
			int narrower = 0;
			while (te.next()) {
				Term currTerm = te.term();
				if (currTerm.field().equals("narrowerURI"))
					narrower++;
			}
			te.close();
			is.close();
			return narrower;
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public static int getNumRelated(String indexName) {
		IndexSearcher is;
		try {
			is = new IndexSearcher(indexName);
			IndexReader ir = is.getIndexReader();
			TermEnum te = ir.terms();
			int related = 0;
			while (te.next()) {
				Term currTerm = te.term();
				if (currTerm.field().equals("relatedURI"))
					related++;
			}
			te.close();
			is.close();
			return related;
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public static int getNumRelationShips(String indexName) {
		IndexSearcher is;
		try {
			is = new IndexSearcher(indexName);
			IndexReader ir = is.getIndexReader();
			TermEnum te = ir.terms();
			int broader = 0;
			int narrower = 0;
			int related = 0;
			while (te.next()) {
				Term currTerm = te.term();
				if (currTerm.field().equals("broaderURI"))
					broader++;
				if (currTerm.field().equals("narrowerURI"))
					narrower++;
				if (currTerm.field().equals("relatedURI"))
					related++;
			}
			te.close();
			is.close();
			return broader + narrower + related;
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}

	public static void main(String[] args) throws CorruptIndexException,
			IOException {
		String indexName = "/home/hive/hive-data/mesh/meshIndex";
		int n = IndexAdministrator.getNumRelationShips(indexName);
		System.out.println("RelationShips: " + n);
		int c = IndexAdministrator.getNumconcepts(indexName);
		System.out.println("Concepts: " + c);
		String date = IndexAdministrator.getDate(indexName);
		System.out.println("Fecha: " + date);

	}

}
