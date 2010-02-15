package edu.unc.ils.mrc.hive.ir.tagging.dummy.tagger;

import java.util.ArrayList;
import java.util.List;

public class Documento {

	private List<Termino> terminos;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Documento() {
		this.terminos = new ArrayList<Termino>();
	}
	
	public void addTerm(String termino, double probabilidad, int frecuencia) {
		Termino term = new Termino(termino, probabilidad, frecuencia);
		term.setTf(frecuencia);
		this.terminos.add(term);
	}

	public List<Termino> getTerminos() {
		return terminos;
	}

}
