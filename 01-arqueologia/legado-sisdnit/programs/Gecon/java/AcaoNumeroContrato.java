package br.gov.serpro.sunne.dnit.gecon.acao;

import static br.gov.serpro.sunne.dnit.infraestrutura.util.Conversao.stringToInteger;
import static br.gov.serpro.sunne.dnit.infraestrutura.util.Conversao.stringToLong;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import br.gov.serpro.sunne.dnit.gecon.ajax.AjaxNumeroContrato;
import br.gov.serpro.sunne.dnit.infraestrutura.acao.Acao;
import br.gov.serpro.sunne.dnit.infraestrutura.excecao.NegocioException;
import br.gov.serpro.sunne.dnit.infraestrutura.util.AtributoSessao;
import br.gov.serpro.sunne.dnit.infraestrutura.util.Conversao;
import br.gov.serpro.sunne.dnit.infraestrutura.util.Propriedades;
import br.gov.serpro.sunne.dnit.infraestrutura.util.UtilString;
import br.gov.serpro.sunne.dnit.negocio.bean.EnumSituacaoNumeroContrato;
import br.gov.serpro.sunne.dnit.negocio.bean.NumeroContrato;
import br.gov.serpro.sunne.dnit.negocio.fachada.FachadaGerarNumeroContrato;

public class AcaoNumeroContrato extends Acao {
	private static final int ALTERACAO = 1;
	private static final int CONSULTA = 2;
	private static final int INCLUSAO = 3;
	private static final int INCLUSAO_MANUAL = 4;
	
	private AtributoSessao<List<NumeroContrato>> LISTA_NUMEROS = new AtributoSessao<List<NumeroContrato>>("listaNumeros");
	private AtributoSessao<NumeroContrato> NUMERO = new AtributoSessao<NumeroContrato>("numero");
	
	public ActionForward alterar(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		return processar(mapping, form, request, response, ALTERACAO);
	}
	
	public ActionForward incluir(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		return processar(mapping, form, request, response, INCLUSAO);
	}
	
	public ActionForward incluirManual(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		return processar(mapping, form, request, response, INCLUSAO_MANUAL);
	}
	
	@SuppressWarnings("rawtypes")
	private ActionForward processar(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response, int operacao) throws NegocioException {
		Map mapa = ((DynaActionForm)form).getMap();
		String forward = FORWARD_FRACASSO_PROCESSAMENTO;
		NumeroContrato numero = mapToNumeroContrato(mapa, operacao);		
			
		// IMPLEMENTAR:
		// 1 - VERIIFCAR SE O ATOR PODE OU NAO ALTERAR O NUMERO CONTRATO EM QUESTAO.
		// ACHO Q ESSA VERIFICACAO DEVE FICAR NA REGRA QUE ENVIA UMA LISTA DE MENSAGENS PARA A ACAO
		// VER CASO DE USO DO LOGIN
		
		// CAMPOS NAO EDITAVEIS NA ALTERACAO:
		// 1 - ID DO NUMERO CONTRATO
		// 2 - UNIDADE LAVRATURA
		// 3 - ANO
		if (operacao == ALTERACAO) { // ALTERACAO
			NumeroContrato numeroSessao = NUMERO.getConteudo(request.getSession());
			if (numeroSessao == null) {
				//Provavelmente a sessão onde a IPG foi salva expirou.
				throw new NegocioException(NegocioException.SISTEMA_FRACASSO, "operação. Por favor, reinicie o processo");
			}
			
			numero.setId(numeroSessao.getId());
//			numero.getUnidadeLavraturaContrato().setId(numeroSessao.getUnidadeLavraturaContrato().getId());
			numero.setAnoContrato(numeroSessao.getAnoContrato());
		} else { // INCLUSAO
			numero.setSituacaoNumeroContrato((EnumSituacaoNumeroContrato.NAO_UTILIZADO.getValue()));
			
			if(operacao == INCLUSAO) {
				GregorianCalendar data = new GregorianCalendar();
				numero.setAnoContrato(data.get(GregorianCalendar.YEAR));
			}
		}

		numero = FachadaGerarNumeroContrato.processar(numero);
				
		request.setAttribute("data",  Conversao.gregorianCalendarToString(numero.getDataOperacao()));
		request.setAttribute("hora",  Conversao.gregorianCalendarToStringHora(numero.getDataOperacao()));
		request.setAttribute("numeroContratoSIAC",  numero.getNumeroContratoFormatadoSIAC());
		request.setAttribute("numeroContratoSIASG", numero.getNumeroContratoFormatadoSIASG());
		
		forward = FORWARD_SUCESSO_PROCESSAMENTO;
		return mapping.findForward(forward);
	}
	
	@SuppressWarnings("rawtypes")
	public ActionForward consultar(ActionMapping mapping, ActionForm form, 
									HttpServletRequest request,	HttpServletResponse response) 
									throws Exception {
		// Recuperando atributos do request(form)
		Map mapa = ((DynaActionForm)form).getMap();
		
		NumeroContrato numero = mapToNumeroContrato(mapa, CONSULTA);
		
		List<NumeroContrato> listaNumeros = FachadaGerarNumeroContrato.filtrar(numero);
		
		if (listaNumeros.size() > 0) {
			LISTA_NUMEROS.setConteudo(request.getSession(), listaNumeros);
			return mapping.findForward(FORWARD_SUCESSO_CONSULTA);
		} else {			
			MENSAGEM.setConteudo(request, Propriedades.getInstance().obterMensagem("persistencia.naoEncontrado", "Número de Contrato"));
			return mapping.findForward(FORWARD_FRACASSO_CONSULTA);
		}
	}
	
	@SuppressWarnings("unchecked")
	public ActionForward detalharObjeto(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) 
				throws Exception {
		List<NumeroContrato> listaNumeros = LISTA_NUMEROS.getConteudo(request.getSession());
		if (listaNumeros == null) {
			//Provavelmente a sessão onde a IPG foi salva expirou.
			throw new NegocioException(NegocioException.SISTEMA_FRACASSO, "operação. Por favor, reinicie o processo");
		}
		
		Long idNumeroContrato = Conversao.stringToLong(request.getParameter("idNumeroContrato"));
		NumeroContrato numero = null;
		for (NumeroContrato item : listaNumeros) {
			if (item.getId().equals(idNumeroContrato)) {
				numero = (NumeroContrato)FachadaGerarNumeroContrato.buscarID(item.getId());
				break;
			}
		}
				
		if (numero != null) {
			// colocando o numero do contrato na sessao
			NUMERO.setConteudo(request.getSession(), numero);
			
			Map<String, Object> mapaRetorno = numeroContratoToMap(numero);
			((DynaActionForm)form).getMap().putAll(mapaRetorno);
			
			return mapping.findForward(FORWARD_SUCESSO_DETALHAR);
		} else {
			throw new NegocioException(NegocioException.ERRO_SEGURANCA);
		}		
	}
	
	/**
	 * Recebe o map(form) que veio do requeste e transforma num NumeroContrato 
	 * @param mapa
	 * @param operacao 
	 * @return
	 * @author Helio Y. Mine - helio.mine@serpro.gov.br
	 * @since 06/12/2007
	 * 
	 * @update	29/09/2009
	 * 			inclusao da checagem para a inclusao de um numero de contrato manualmente.
	 * @author 	Danilo Nagase - danilo.nagase@serpro.gov.br
	 */
	@SuppressWarnings("rawtypes")
	private NumeroContrato mapToNumeroContrato(Map mapa, int operacao) {
		NumeroContrato numero = new NumeroContrato();
		
		long lavratura = stringToLong((String)mapa.get("unidadeLavratura"));
		
		if (operacao == ALTERACAO) {
//			lavratura = 0L;
			numero.setInclusaoManual(false);
		} else {
			
			numero.setInclusaoManual(false);
			
			// verifica se a inclusao do numero eh manual
			if(operacao == INCLUSAO_MANUAL) {
				numero.setInclusaoManual(true);
				numero.setAnoContrato(Integer.valueOf((String)mapa.get("ano")));
				numero.setNumeroContrato(Integer.valueOf((String)mapa.get("numeroContratoConvenio")));
			}
		}
		
		numero.getUnidadeLavraturaContrato().setId(lavratura);
		numero.getUnidadeGestoraContrato().setId(stringToLong((String)mapa.get("unidadeGestora")));
		numero.getUsuarioSolicitante().getUnidade().setId(stringToLong((String)mapa.get("unidadeLotacao")));
		numero.setAnoContrato(stringToInteger((String)mapa.get("ano")));
		numero.setSituacaoNumeroContrato(stringToInteger((String)mapa.get("situacao")));
		String numSiac = (String)mapa.get("numeroSIAC");
		
		if (numSiac.length() > 0 && operacao == CONSULTA) {
			int indice = numSiac.indexOf("/");
			StringBuffer sb = new StringBuffer();
			sb.append(UtilString.lpad(numSiac.substring(0, indice), "0", 5)).append(numSiac.substring(indice));
			numero.setNumeroContratoFormatadoSIAC(sb.toString()); 
		}
		
		numero.setNumeroContratoFormatadoSIASG((String)mapa.get("numeroSIASG"));
		numero.getUsuarioSolicitante().setNome((String)mapa.get("nome"));
		numero.getUsuarioSolicitante().setDdd((String)mapa.get("ddd"));
		numero.getUsuarioSolicitante().setTelefone((String)mapa.get("telefone"));
		numero.setObservacao((String)mapa.get("observacao"));
		
		return numero;
	}	
	
	/**
	 * Transforma um NumeroContrato num map(form)
	 * @param numero
	 * @return
	 * @author Helio Y. Mine - helio.mine@serpro.gov.br
	 * @since 06/12/2007
	 */
	private Map<String, Object> numeroContratoToMap(NumeroContrato numero){
		Map<String, Object> mapaRetorno = new HashMap<String, Object>();
		
		mapaRetorno.put("numeroSIAC", numero.getNumeroContratoFormatadoSIAC());
		mapaRetorno.put("numeroSIASG", numero.getNumeroContratoFormatadoSIASG());
		mapaRetorno.put("unidadeLavratura", numero.getUnidadeLavraturaContrato().getSigla() + " - " +  numero.getUnidadeLavraturaContrato().getNome());
		mapaRetorno.put("unidadeGestora", numero.getUnidadeGestoraContrato().getSigla() + " - " +  numero.getUnidadeGestoraContrato().getNome());
		mapaRetorno.put("unidadeLotacao", numero.getUsuarioSolicitante().getUnidade().getSigla() + " - " + numero.getUsuarioSolicitante().getUnidade().getNome());
		mapaRetorno.put("nome", numero.getUsuarioSolicitante().getNome());
		
		// montando o numero telefone (ddd + numero)
		StringBuffer sb = new StringBuffer();
		String ddd = numero.getUsuarioSolicitante().getDdd();
		if (ddd != null) {
			sb.append("(");
			sb.append(ddd);
			sb.append(")");
		}
		
		String telefone = numero.getUsuarioSolicitante().getTelefone();
		if (telefone != null) {
			sb.append(" ");
			if (telefone.length() > 4) {
				sb.append(telefone.substring(0, 4));
				sb.append("-");
				sb.append(telefone.substring(4));
			} else {
				sb.append(telefone);
			}
		} 
		mapaRetorno.put("telefone", sb.toString());
		mapaRetorno.put("situacao", EnumSituacaoNumeroContrato.getSituacaoNumeroContrato(numero.getSituacaoNumeroContrato()).getNome());
		mapaRetorno.put("observacao", numero.getObservacao());
		
		return mapaRetorno;
	}
	
	public ActionForward reset(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) 
				throws Exception {
		CLASSE_AJAX.setConteudo(request.getSession(), AjaxNumeroContrato.class.getName());
		LISTA_NUMEROS.setConteudo(request.getSession(), null);
		NUMERO.setConteudo(request.getSession(), null);
		DynaActionForm dados = (DynaActionForm)form;
		if (dados != null) {
			dados.initialize(mapping);	
		}
		return mapping.findForward(FORWARD_RESET);
	}
	
	public ActionForward voltarMenu(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) 
				throws Exception {
		LISTA_NUMEROS.setConteudo(request.getSession(), null);
		NUMERO.setConteudo(request.getSession(), null);
		DynaActionForm dados = (DynaActionForm)form;
		if (dados != null) {
			dados.initialize(mapping);
		}
		return mapping.findForward(FORWARD_MENU);
	}
}
