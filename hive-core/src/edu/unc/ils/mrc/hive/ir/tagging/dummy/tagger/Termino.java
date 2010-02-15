package edu.unc.ils.mrc.hive.ir.tagging.dummy.tagger;

import edu.unc.ils.mrc.hive.ir.tagging.dummy.ranking.Rankeable;

public class Termino implements Rankeable{
	
	private static final double K1 = 1.2;
	
	private String termino;
	private double probabilidad;
	private double divergencia;
	private int frecuencia;
	private double tf;

	public Termino(String termino, double probabilidad, int frecuencia) {
		this.termino = termino;
		this.probabilidad = probabilidad;
		this.frecuencia = frecuencia;
	}
	
	public double getTf() {
		return tf;
	}

	public void setTf(double tf) {
		this.tf = tf / (K1 + tf);
	}
	
	public void setDivergencia(double divergencia) {
		this.divergencia = divergencia;
	}
	
	public double getDivergencia() {
		return this.divergencia;
	}

	public String getTermino() {
		return termino;
	}

	public void setTermino(String termino) {
		this.termino = termino;
	}

	public double getProbabilidad() {
		return probabilidad;
	}

	public void setProbabilidad(double probabilidad) {
		this.probabilidad = probabilidad;
	}

	public int getFrecuencia() {
		return frecuencia;
	}

	public void setFrecuencia(int frecuencia) {
		this.frecuencia = frecuencia;
	}

	@Override
	public Double getRankingValue() {
		return this.getTf();
	}

}
