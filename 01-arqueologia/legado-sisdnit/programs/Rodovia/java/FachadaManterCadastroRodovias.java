package br.gov.serpro.sunne.dnit.negocio.fachada;

import java.util.List;

import br.gov.serpro.sunne.dnit.infraestrutura.bd.RegraFactory;
import br.gov.serpro.sunne.dnit.infraestrutura.excecao.NegocioException;
import br.gov.serpro.sunne.dnit.negocio.IRegraManterCadastroRodovias;
import br.gov.serpro.sunne.dnit.negocio.IRegraManterCadastroUf;
import br.gov.serpro.sunne.dnit.negocio.bean.Rodovia;
import br.gov.serpro.sunne.dnit.negocio.sistema.Item;

/**
 * [DESCRICAO]
 *
 * @author Endrigo G. Ferreira - endrigo.ferreira@serpro.gov.br
 * @date 10/08/2007 
 */
public class FachadaManterCadastroRodovias {

	private static IRegraManterCadastroUf getRegraManterCadastroUf() {
		return (IRegraManterCadastroUf) RegraFactory.criar(IRegraManterCadastroUf.class);
	}

	private static IRegraManterCadastroRodovias getRegraManterCadastroRodovias() {
		return (IRegraManterCadastroRodovias) RegraFactory.criar(IRegraManterCadastroRodovias.class);
	}

	public static boolean verificarSigla(String sigla) throws NegocioException{
		return getRegraManterCadastroRodovias().verificarSigla(sigla);
	}
	
	public static List<Item> buscarItensUfs() throws NegocioException{
		return getRegraManterCadastroUf().buscarItensUF();
	}

	public static Rodovia consultar(Long id) throws NegocioException {
		return getRegraManterCadastroRodovias().consultar(id);
	}
	
	public static List<Rodovia> consultar(String sigla, String descricao, String[] ufs, Integer situacao) throws NegocioException {
		return getRegraManterCadastroRodovias().consultar(descricao, sigla, ufs, situacao);
	}
	
	public static Rodovia processar(Long id, String sigla, String descricao, String[] ufs, Integer situacao) throws NegocioException {
		return getRegraManterCadastroRodovias().processar(id, descricao, sigla, ufs, situacao);
	}
}
