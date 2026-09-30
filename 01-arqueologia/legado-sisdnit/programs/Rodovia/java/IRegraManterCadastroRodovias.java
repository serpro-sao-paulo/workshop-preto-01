package br.gov.serpro.sunne.dnit.negocio;

import java.util.List;

import br.gov.serpro.sunne.dnit.infraestrutura.excecao.NegocioException;
import br.gov.serpro.sunne.dnit.negocio.bean.Rodovia;

public interface IRegraManterCadastroRodovias {

	public boolean verificarSigla(String sigla) throws NegocioException;

	public Rodovia processar(Long id, String descricao, String sigla, String[] ufs, Integer situacao) throws NegocioException;
	
	public List<Rodovia> filtrar(Rodovia bean) throws NegocioException;
	
	public List<Rodovia> consultar(Integer situacao) throws NegocioException;

	public List<Rodovia> consultar(String descricao, String sigla, String[] ufs, Integer situacao) throws NegocioException;

	public Rodovia consultar(Long id) throws NegocioException;

	public List<Rodovia> filtrarBEM(long idUf) throws NegocioException;
}