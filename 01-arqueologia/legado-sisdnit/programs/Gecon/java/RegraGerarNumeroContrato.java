package br.gov.serpro.sunne.dnit.negocio;

import java.util.List;

import br.gov.serpro.sunne.dnit.infraestrutura.excecao.NegocioException;
import br.gov.serpro.sunne.dnit.negocio.bean.NumeroContrato;
import br.gov.serpro.sunne.dnit.negocio.persistencia.RepositorioNumeroContrato;
import br.gov.serpro.sunne.dnit.negocio.sistema.Item;

/**
 * @author Rafael Binde - rafael.binde@serpro.gov.br
 * @author Helio Mine - helio.mine@serpro.gov.br
 * @date 29/01/2007
 */
public class RegraGerarNumeroContrato implements IRegraGerarNumeroContrato {
	
	private static IRegraGerarNumeroContrato singleton;

	public static synchronized IRegraGerarNumeroContrato getInstancia() {
		if (singleton == null) {
			singleton = new RegraGerarNumeroContrato();
		}
		return singleton;
	}
	
	public List<Item> buscarAnos() throws NegocioException {
		return RepositorioNumeroContrato.getInstancia().buscarAnos();
	}

	public NumeroContrato buscarID(Long id) throws NegocioException {
		return RepositorioNumeroContrato.getInstancia().buscarID(id);
	}

	public List<Item> buscarNaoUtilizados(Long idLavratura) throws NegocioException {
		return RepositorioNumeroContrato.getInstancia().buscarNaoUtilizados(idLavratura);
	}
	
	public List<Item> buscarNaoUtilizados(Long idGestora, Long idLavratura) throws NegocioException {
		return RepositorioNumeroContrato.getInstancia().buscarNaoUtilizados(idGestora, idLavratura);
	}
	
	public List<NumeroContrato> filtrar(NumeroContrato bean) throws NegocioException {
		return RepositorioNumeroContrato.getInstancia().filtrar(bean);
	}

	public List<Item> filtrarItens(NumeroContrato bean) throws NegocioException {
		return RepositorioNumeroContrato.getInstancia().filtrarItens(bean);
	}

	// Obs. Acho que aqui tenho que colocar uma verificação se usuario pode ou nao
	// incluir/alterar o numeroContrato, mas para isso teria que passar por parametro
	// o numeroContrato da sessao e o usuario logado. Verificar
	public NumeroContrato processar(NumeroContrato bean) throws NegocioException {
		return RepositorioNumeroContrato.getInstancia().processar(bean);
	}

	@Override
	public boolean isNumeroContratoUtilizado(Long idNumeroContrato) throws NegocioException {
		return RepositorioNumeroContrato.getInstancia().isNumeroContratoUtilizado(idNumeroContrato);
	}
}
