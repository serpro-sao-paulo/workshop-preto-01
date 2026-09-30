package br.gov.serpro.sunne.dnit.infraestrutura.util;

import java.util.Comparator;

import br.gov.serpro.sunne.dnit.negocio.bean.Rodovia;

public class ComparadorRodovia implements Comparator<Rodovia> {

	public int compare(Rodovia r1, Rodovia r2) {
		return r1.getSigla().compareTo(r2.getSigla());
	}

}
