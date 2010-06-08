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
 */

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
