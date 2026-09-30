package br.gov.serpro.sunne.dnit.gecon.acao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import br.gov.serpro.sunne.dnit.infraestrutura.acao.Acao;

public class AcaoGecon extends Acao {

	public ActionForward gecon(ActionMapping mapping, ActionForm form, 	
			HttpServletRequest request, HttpServletResponse response) 
				throws Exception {
		return mapping.findForward("gecon");
	}
	
	public ActionForward prazimetro(ActionMapping mapping, ActionForm form, 	
			HttpServletRequest request, HttpServletResponse response) 
				throws Exception {
		return mapping.findForward("prazimetro");
	}
	
	public ActionForward manter_gerador(ActionMapping mapping, ActionForm form, 	
			HttpServletRequest request, HttpServletResponse response) 
				throws Exception {
		return mapping.findForward("manter_gerador");
	}
	
	public ActionForward ajuda(ActionMapping mapping, ActionForm form, 	
			HttpServletRequest request, HttpServletResponse response) 
				throws Exception {
		return mapping.findForward("ajuda");
	}
	
	public ActionForward acessibilidade(ActionMapping mapping, ActionForm form, 	
			HttpServletRequest request, HttpServletResponse response) 
				throws Exception {
		return mapping.findForward("acessibilidade");
	}

}
