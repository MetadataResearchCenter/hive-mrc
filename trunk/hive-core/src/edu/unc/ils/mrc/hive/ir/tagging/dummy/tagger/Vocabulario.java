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
