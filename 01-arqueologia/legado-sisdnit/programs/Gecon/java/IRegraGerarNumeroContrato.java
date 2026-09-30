package br.gov.serpro.sunne.dnit.negocio;

import java.util.List;

import br.gov.serpro.sunne.dnit.infraestrutura.excecao.NegocioException;
import br.gov.serpro.sunne.dnit.negocio.bean.NumeroContrato;
import br.gov.serpro.sunne.dnit.negocio.sistema.Item;

public interface IRegraGerarNumeroContrato {

	public abstract List<Item> buscarAnos() throws NegocioException;
	
	public abstract NumeroContrato buscarID(Long id) throws NegocioException;
	
	public abstract List<Item> buscarNaoUtilizados(Long idLavratura) throws NegocioException;
	
	public abstract List<Item> buscarNaoUtilizados(Long idGestora, Long idLavratura) throws NegocioException;
	
	public abstract List<NumeroContrato> filtrar(NumeroContrato bean) throws NegocioException;
	
	public abstract List<Item> filtrarItens(NumeroContrato bean) throws NegocioException;
	
	public abstract NumeroContrato processar(NumeroContrato bean) throws NegocioException;

	public abstract boolean isNumeroContratoUtilizado(Long idNumeroContrato) throws NegocioException;
}