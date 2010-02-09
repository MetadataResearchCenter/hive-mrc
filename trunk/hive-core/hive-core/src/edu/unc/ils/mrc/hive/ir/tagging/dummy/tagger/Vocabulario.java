package edu.unc.ils.mrc.hive.ir.tagging.dummy.tagger;

import java.util.ArrayList;
import java.util.List;

public class Vocabulario {
	
	private List<String> terminos;
	private List<Double> probabilidad;
	private List<Integer> frecuencia;

	public Vocabulario() {
		this.terminos = new ArrayList<String>();
		this.probabilidad = new ArrayList<Double>();
		this.frecuencia = new ArrayList<Integer>();
	}
	
	public void addTerm(String termino, int frecuencia) {
		if(terminos.contains(termino)) {
			int indice = terminos.indexOf(termino);
			int old_frecuencia = this.frecuencia.get(indice);
			this.frecuencia.set(indice, old_frecuencia + frecuencia);
		}
		else {
			this.terminos.add(termino);
			this.frecuencia.add(frecuencia);
			this.probabilidad.add(0.0);
		}
	}
	
	public double getGlobalProbability(String termino) {
		int indice = this.terminos.indexOf(termino);
		return this.probabilidad.get(indice);
	}
	
	public void calculaProbabilidades() {
		double total_terminos = 0;
		for(Integer f : this.frecuencia) {
			total_terminos = total_terminos + f;
		}
		for(String t : this.terminos) {
			int indice = this.terminos.indexOf(t);
			double freq = this.frecuencia.get(indice);
			this.probabilidad.set(indice, freq/total_terminos);
		}
	}
	
	public void print() {
		System.out.println("------------START VOCABULARIO-------------------");
		for(String t : this.terminos) {
			int indice = this.terminos.indexOf(t);
			int freq = this.frecuencia.get(indice);
			double prob = this.probabilidad.get(indice);
			System.out.println(t + " frecuencia: " + freq + " probabilidad en el documento: " + prob);
		}
		System.out.println("------------END VOCABULARIO-------------------");
	}

}
