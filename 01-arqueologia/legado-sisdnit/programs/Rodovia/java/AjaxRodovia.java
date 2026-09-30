package br.gov.serpro.sunne.dnit.siac.ajax;

import java.util.ArrayList;
import java.util.List;

import br.gov.serpro.sunne.dnit.infraestrutura.ajax.Ajax;
import br.gov.serpro.sunne.dnit.infraestrutura.excecao.NegocioException;
import br.gov.serpro.sunne.dnit.negocio.bean.Uf;
import br.gov.serpro.sunne.dnit.negocio.fachada.FachadaManterCadastroRodovias;
import br.gov.serpro.sunne.dnit.negocio.sistema.Item;

public class AjaxRodovia extends Ajax {

	public static List<Item> buscarItensUf() throws NegocioException {
		return FachadaManterCadastroRodovias.buscarItensUfs();
	}

	public static boolean verificarSiglaExiste(String sigla) throws NegocioException {
		return FachadaManterCadastroRodovias.verificarSigla(sigla);
	}

	public static List<Item> buscarItensUf(Long id) throws NegocioException {
		List<Item> lista = new ArrayList<Item>();
		List<Uf> ufs = FachadaManterCadastroRodovias.consultar(id).getUfs();
		for (int i=0; i < ufs.size(); i++) {
			lista.add(new Item(ufs.get(i).getId(), ufs.get(i).getNome()));
		}
		return lista;
	}
}
