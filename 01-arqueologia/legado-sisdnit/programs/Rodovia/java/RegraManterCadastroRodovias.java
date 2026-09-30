package br.gov.serpro.sunne.dnit.negocio;

import java.util.ArrayList;
import java.util.List;

import br.gov.serpro.sunne.dnit.infraestrutura.excecao.NegocioException;
import br.gov.serpro.sunne.dnit.negocio.bean.Rodovia;
import br.gov.serpro.sunne.dnit.negocio.bean.Uf;
import br.gov.serpro.sunne.dnit.negocio.persistencia.RepositorioRodovia;

/**
 * [DESCRICAO]
 *
 * @author Endrigo G. Ferreira - endrigo.ferreira@serpro.gov.br
 * @date 09/08/2007
 */
public class RegraManterCadastroRodovias implements IRegraManterCadastroRodovias {
	private static IRegraManterCadastroRodovias singleton;
	
	public synchronized static IRegraManterCadastroRodovias getInstancia(){
		if (singleton == null){
			singleton = new RegraManterCadastroRodovias();			
		}
		return singleton;
	}
	
	public boolean verificarSigla(String sigla) throws NegocioException {
		Rodovia via = new Rodovia();
		via.setSigla(sigla);
	
		return RepositorioRodovia.getInstancia().verificaSiglaExiste(via);
	}
	
	public List<Rodovia> filtrar(Rodovia bean) throws NegocioException {
		return RepositorioRodovia.getInstancia().filtrar(bean);
	}
	
	public Rodovia processar(
			Long id, 
			String descricao, 
			String sigla, 
			String[] ufs, 
			Integer situacao
			) throws NegocioException {
		Rodovia bean = new Rodovia();
		bean.setId(id);
		bean.setDescricao(descricao);
		bean.setSigla(sigla);
		List<Uf> listaUf = new ArrayList<Uf>();
		for( String str : ufs ){
			listaUf.add(new Uf(Long.parseLong(str)));
		}
		bean.setUfs(listaUf);
		bean.setSituacao(situacao);
		return RepositorioRodovia.getInstancia().processar(bean);
	}
	
	public List<Rodovia> consultar(String descricao, String sigla, String[] ufs, Integer situacao) throws NegocioException {
		Rodovia bean = new Rodovia();
		bean.setDescricao(descricao);
		bean.setSigla(sigla);
		List<Uf> listaUf = new ArrayList<Uf>();
		if (ufs != null) {
			for( String str : ufs ){
				listaUf.add(new Uf(Long.parseLong(str)));
			}	
		}
		bean.setUfs(listaUf);
		bean.setSituacao(situacao);
		return RepositorioRodovia.getInstancia().filtrar(bean);
	}
	
	public Rodovia consultar(Long id) throws NegocioException {
		return RepositorioRodovia.getInstancia().buscaID(id);
	}
	
	public List<Rodovia> consultar(Integer situacao) throws NegocioException {
		Rodovia bean = new Rodovia();
		bean.setSituacao(situacao);
		return RepositorioRodovia.getInstancia().buscaSgVia(bean);
	}
	
	public List<Rodovia> filtrarBEM(long idUf) throws NegocioException {
		return RepositorioRodovia.getInstancia().filtrarBEM(idUf);
	}
}
