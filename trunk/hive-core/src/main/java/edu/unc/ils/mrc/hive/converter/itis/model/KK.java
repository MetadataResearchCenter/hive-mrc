package edu.unc.ils.mrc.hive.converter.itis.model;

public class KK {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String s = "202423-563954-696107-696109-696115-696174";
		System.out.println(s.substring(s.indexOf("696115"), s.length()));
	}

}
