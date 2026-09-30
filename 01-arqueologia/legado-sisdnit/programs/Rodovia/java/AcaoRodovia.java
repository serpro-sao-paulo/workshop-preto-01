package br.gov.serpro.sunne.dnit.siac.acao;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import br.gov.serpro.sunne.dnit.infraestrutura.acao.Acao;
import br.gov.serpro.sunne.dnit.infraestrutura.util.AtributoSessao;
import br.gov.serpro.sunne.dnit.infraestrutura.util.ComparadorRodovia;
import br.gov.serpro.sunne.dnit.infraestrutura.util.Constantes;
import br.gov.serpro.sunne.dnit.negocio.bean.Rodovia;
import br.gov.serpro.sunne.dnit.negocio.fachada.FachadaManterCadastroRodovias;
import br.gov.serpro.sunne.dnit.siac.ajax.AjaxRodovia;

/**
 * [DESCRICAO]
 *
 * @author Endrigo G. Ferreira - endrigo.ferreira@serpro.gov.br
 * @date 09/08/2007
 */
public class AcaoRodovia extends Acao {
	private AtributoSessao<Long> ID_RODOVIA = new AtributoSessao<Long>("id");
	private AtributoSessao<List<Rodovia>> LISTA = new AtributoSessao<List<Rodovia>>("listaRodovias");
	private AtributoSessao<Integer> TAMANHO_PAGINA = new AtributoSessao<Integer>("tamanho");

	public ActionForward incluir(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) 
	throws Exception {

		DynaActionForm dados = (DynaActionForm)form;

		String sigla			= (String)dados.get("sigla");
		String descricao		= (String)dados.get("descricao"); 
		String[] ufs			= (String[])dados.get("ufs");
		int situacao 			= (Integer)dados.get("situacao");

		FachadaManterCadastroRodovias.processar(0L, sigla, descricao, ufs, situacao);

		return mapping.findForward("incluir");

	}

	public ActionForward consultar(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) 
	throws Exception {

		DynaActionForm dados = (DynaActionForm)form;

		String sigla			= (String)dados.get("sigla");
		String descricao		= (String)dados.get("descricao"); 
		String[] ufs			= (String[])dados.get("ufs");
		int situacao 			= (Integer)dados.get("situacao");
		List<Rodovia> bean = FachadaManterCadastroRodovias.consultar(sigla, descricao, ufs, situacao);
		if ((bean == null)||(bean.isEmpty()))
			return mapping.findForward("rodoviaNaoEncontrada");

		Collections.sort(bean, new ComparadorRodovia());
		LISTA.setConteudo(request.getSession(), bean);
		TAMANHO_PAGINA.setConteudo(request.getSession(), Constantes.TAMANHO_PAGINA);
		return mapping.findForward("consultar");

	}

	public ActionForward detalhar(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) 
	throws Exception {
		ID_RODOVIA.setConteudo(request.getSession(), null);

		//Recuperando dados do request
		Long id = Long.parseLong( request.getParameter("id") );

		Rodovia bean = FachadaManterCadastroRodovias.consultar(id);
		request.setAttribute("rodovias", bean);
		ID_RODOVIA.setConteudo(request.getSession(), bean.getId());
		return mapping.findForward("detalhar");
	}
	
	public ActionForward alterar(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) 
	throws Exception {

		DynaActionForm dados = (DynaActionForm)form;
		
		Long id = ID_RODOVIA.getConteudo(request.getSession());
//		Long id = Long.parseLong( request.getParameter("id") );
		String sigla			= (String)dados.get("sigla");
		String descricao		= (String)dados.get("descricao"); 
		String[] ufs			= (String[])dados.get("ufs");
		int situacao 			= (Integer)dados.get("situacao");

		FachadaManterCadastroRodovias.processar(id, sigla, descricao, ufs, situacao);
		
		ID_RODOVIA.setConteudo(request.getSession(), null); // limpa session

		return mapping.findForward("alterar");

	}

	public ActionForward voltarPrincipal(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) 
	throws Exception {
		request.getSession().setAttribute("rodovias", null);
		return mapping.findForward("voltarPrincipal");
	}
	

	public ActionForward reset(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) 
	throws Exception {
		CLASSE_AJAX.setConteudo(request.getSession(), AjaxRodovia.class.getName());
		ID_RODOVIA.setConteudo(request.getSession(), null);
		LISTA.setConteudo(request.getSession(), null);
		DynaActionForm dados = (DynaActionForm)form;
		if (dados != null) {
			dados.initialize(mapping);	
		}
		return mapping.findForward(FORWARD_RESET);
	}

	public ActionForward voltarMenu(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) 
				throws Exception {
		ID_RODOVIA.setConteudo(request.getSession(), null);
		LISTA.setConteudo(request.getSession(), null);
		DynaActionForm dados = (DynaActionForm)form;
		if (dados != null) {
			dados.initialize(mapping);
		}
		return mapping.findForward(FORWARD_MENU);
	}
}