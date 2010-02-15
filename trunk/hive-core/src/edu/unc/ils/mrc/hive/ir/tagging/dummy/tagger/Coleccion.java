package edu.unc.ils.mrc.hive.ir.tagging.dummy.tagger;

import java.util.ArrayList;
import java.util.List;

public class Coleccion {

	private List<Documento> documentos;

	public Coleccion() {
		this.documentos = new ArrayList<Documento>();
	}

	public void addDocumento(Documento doc) {
		this.documentos.add(doc);
	}

	public List<Documento> getDocumentos() {
		return documentos;
	}

	//Remove divergence computation and using only freq
	public void calculaDivergencias(Vocabulario voc) {

		for (Documento doc : this.documentos) {
			for (Termino term : doc.getTerminos()) {
				double kld = term.getProbabilidad()
						* Math.log((term.getProbabilidad() / voc
								.getGlobalProbability(term.getTermino())));
				if(Double.isInfinite(kld))
					term.setDivergencia(0.0);
				else
					term.setDivergencia(kld);
			}
		}

	}

}
