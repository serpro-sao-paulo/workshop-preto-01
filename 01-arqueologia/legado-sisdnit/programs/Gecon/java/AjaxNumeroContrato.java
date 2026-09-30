package br.gov.serpro.sunne.dnit.gecon.ajax;

import static br.gov.serpro.sunne.dnit.infraestrutura.util.Conversao.stringToLong;

import java.util.ArrayList;
import java.util.List;

import br.gov.serpro.sunne.dnit.infraestrutura.ajax.Ajax;
import br.gov.serpro.sunne.dnit.infraestrutura.excecao.NegocioException;
import br.gov.serpro.sunne.dnit.infraestrutura.util.AtributoSessao;
import br.gov.serpro.sunne.dnit.infraestrutura.util.Constantes;
import br.gov.serpro.sunne.dnit.infraestrutura.util.SessaoGlobal;
import br.gov.serpro.sunne.dnit.negocio.bean.EnumSubtipoUnidade;
import br.gov.serpro.sunne.dnit.negocio.bean.EnumTipoUnidade;
import br.gov.serpro.sunne.dnit.negocio.bean.NumeroContrato;
import br.gov.serpro.sunne.dnit.negocio.bean.Unidade;
import br.gov.serpro.sunne.dnit.negocio.bean.Usuario;
import br.gov.serpro.sunne.dnit.negocio.fachada.FachadaGerarNumeroContrato;
import br.gov.serpro.sunne.dnit.negocio.sistema.Item;

public class AjaxNumeroContrato extends Ajax {
	
	private AtributoSessao<NumeroContrato> NUMERO = new AtributoSessao<NumeroContrato>("numero");
	
	/**
	 * Retorna uma lista de Item de Unidade de acordo com a RNG
	 * Método utilizado somente na INCLUSÃO
	 * @return
	 * @throws NegocioException
	 * @author Helio Y. Mine - helio.mine@serpro.gov.br
	 * @since 06/12/2007
	 */
	public List<Item> buscarItensLavraturaContrato() throws NegocioException {		
		Usuario usuario = (Usuario)SessaoGlobal.USUARIO_LOGADO.getConteudo(getSession());

		List<Item> itensUnidade = new ArrayList<Item>();
		
		// caso o usuario seja unidade da sede exibe todas as sup reg + a sede
		if (usuario.getUnidade().getSubtipoUnidade().getTipoUnidade().getId().equals(EnumTipoUnidade.UNIDADE_DA_SEDE.getValue())) {
			itensUnidade = FachadaGerarNumeroContrato.buscarItensUnidadeLavratura(EnumSubtipoUnidade.SUPERINTENDENCIA_REGIONAL.getValue());
			itensUnidade.addAll(FachadaGerarNumeroContrato.buscarItensUnidadeLavratura(EnumSubtipoUnidade.ADMINISTRACAO_HIDROVIARIA.getValue()));
			itensUnidade.add(0, new Item(Constantes.SEDE,"SEDE - SEDE"));
		} else { 
			// caso o usuario seja do subtipo sup regional traz somente a sua unidade
			if (usuario.getUnidade().getSubtipoUnidade().getId().equals(EnumSubtipoUnidade.SUPERINTENDENCIA_REGIONAL.getValue())) {
				Unidade unidadeAux = FachadaGerarNumeroContrato.buscarUnidadeID(usuario.getUnidade().getId());
				Item itemAux = new Item(unidadeAux.getId(), unidadeAux.getSigla() + " - " + unidadeAux.getNome());
				itensUnidade.add(itemAux);
			}
			
			// caso o usuario seja do subtipo adm hidro traz somente a sua unidade
			if (usuario.getUnidade().getSubtipoUnidade().getId().equals(EnumSubtipoUnidade.ADMINISTRACAO_HIDROVIARIA.getValue())) {
				Unidade unidadeAux = FachadaGerarNumeroContrato.buscarUnidadeID(usuario.getUnidade().getId());
				Item itemAux = new Item(unidadeAux.getId(), unidadeAux.getSigla() + " - " + unidadeAux.getNome());
				itensUnidade.add(itemAux);
			}
		}
		
		return itensUnidade;
	}
	
	/**
	 * Retorna todos os Item Unidade cadastrados no sistema
	 * Método utilizado na INCLUSÃO, ALTERAÇÃO e CONSULTA
	 * @return
	 * @throws NegocioException
	 * @author Helio Y. Mine - helio.mine@serpro.gov.br
	 * @since 06/12/2007
	 */
	public List<Item> buscarItensUnidade() throws NegocioException {
		return FachadaGerarNumeroContrato.buscarItensUnidade();
	}
	
	/**
	 * Retorna os Item Unidade de acordo com a RNG
	 * Método utilizado na CONSULTA
	 * @return
	 * @throws NegocioException
	 * @author Helio Y. Mine - helio.mine@serpro.gov.br
	 * @since 06/12/2007
	 */
	public List<Item> buscarItensLavraturaContratoConsulta() throws NegocioException {
		List<Item> itensUnidade = FachadaGerarNumeroContrato.buscarItensUnidadeLavratura(4L);
		itensUnidade.add(0, new Item(Constantes.SEDE,"SEDE - SEDE"));
		
		return itensUnidade;
	}
	
	/**
	 * Busca lista de anos que foram gerados números de contratos
	 * @author Rafael Binde - rafael.binde@serpro.gov.br
	 * @throws NegocioException 
	 * @date 02/02/2007
	 */
	public static List<Item> buscarItensAnos() throws NegocioException {				
		List<Item> itens = FachadaGerarNumeroContrato.buscarAnos();
		return itens;
	}
	
	/**
	 * Retorna a lista de Item NumeroContrato que não sendo utilizados por nenhum contrato,
	 * ou seja, nas situações nao utilizados e inutilizados.
	 * Método utilizado na ALTERAÇÃO
	 * @return
	 * @throws NegocioException
	 * @author Helio Y. Mine - helio.mine@serpro.gov.br
	 * @since 06/12/2007
	 */
	public List<Item> buscarItensNumeroContrato() throws NegocioException {
		// OBS... VALE A PENA COLOCAR A LISTA NA SESSAO? VERIFICAR
		// Nao vale a pena, pois eh utilizado apenas uma vez.
		Usuario usuario = (Usuario)SessaoGlobal.USUARIO_LOGADO.getConteudo(getSession());
			
		return FachadaGerarNumeroContrato.buscarNaoUtilizados(usuario.getUnidade().getId());
	}
	
	/**
	 * Retorna o NumeroContrato com o parâmetro e o coloca na sessão
	 * Método utilizado pela ALTERAÇÃO
	 * @param strNumeroContrato
	 * @return
	 * @throws NegocioException
	 * @author Helio Y. Mine - helio.mine@serpro.gov.br
	 * @since 06/12/2007
	 */
	public NumeroContrato buscarNumeroContrato(String strNumeroContrato) throws NegocioException {
		if (!strNumeroContrato.isEmpty()) {
			NumeroContrato numero = FachadaGerarNumeroContrato.buscarID(stringToLong(strNumeroContrato)); 
			
			// Incluindo o numero contrato na sessão
			NUMERO.setConteudo(getSession(), numero);
			
			return numero;
		}
		
		return null;
	}
}
