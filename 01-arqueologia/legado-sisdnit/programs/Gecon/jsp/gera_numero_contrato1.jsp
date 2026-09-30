<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html-el"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-br">
	<head>
		<%@ include file="include/head.jsp"%>
		<style type="text/css">
			table tbody th {
				width: 34%;
			}
			table tbody th.doublecol {
				width: 17%;
			}
		</style>
		
		<html:javascript formName="NumeroContratoForm"/>
		<jsp:useBean id="usuarioLogado" scope="session"	class="br.gov.serpro.sunne.dnit.negocio.bean.Usuario" />
		<script type="text/javascript" src="../js/form/mascaras.js"></script>
	
		<script type="text/javascript">
			var objBean = new Object();
			objBean.idLogado = '${usuarioLogado.id}';
			objBean.idUnidade = '${usuarioLogado.unidade.id}';
			objBean.idUnidadePai = '${usuarioLogado.unidade.unidadePai.id}';
			objBean.idTipoUnidade = "${usuarioLogado.unidade.subtipoUnidade.tipoUnidade.id}";
			objBean.idSubtipoUnidade = "${usuarioLogado.unidade.subtipoUnidade.id}";
		</script>
	</head>
	<body>
		<div id="container">
			<%@ include file="include/header.jsp"%>
			<script src="js/NumeroContrato.js"></script>
			
			<div id="content" >
				<h2>Inclusão de Número de Contrato/Conv&ecirc;nio </h2>
				
				<a href="ajuda/manual_gecon.pdf" class="help" target="_blank" title="Ajuda">
					<span>Ajuda</span>
				</a>
				
				<div class="form-info">
					<p>Preencha os campos e selecione o botão &quot;Gerar&quot;.</p>
					<p>* Campos obrigatórios.</p>
				</div>
				
				<html:form action="/jsp/gecon/gera_numero_contrato.do" method="post">
					<input type="hidden" name="tipoAcao" id="tipoAcao" value="incluir"/>
					<table>
						<tbody>
							<tr>
								<th colspan="2"><label for="unidadeLavratura">Unidade de Lavratura do Contrato*</label></th>
								<td>						
									<select name="unidadeLavratura" 
											id="unidadeLavratura" 
									 		objeto="ajaxNumeroContrato"
									 		metodo="buscarItensLavraturaContrato"
									 		funcao="numeroContrato.verificarComboUnidadeLavratura();">
										<option inicial="true" value="">SELECIONE UMA UNIDADE</option>
									</select>		 	
								</td>
							</tr>
									 
							<tr>
								<th colspan="2"><label for="unidadeGestora">Unidade Gestora do Contrato*</label></th>
								<td>
									<select name="unidadeGestora" 
											id="unidadeGestora" 
									 		objeto="ajaxNumeroContrato"
									 		metodo="buscarItensUnidade">
										<option inicial="true" value="">SELECIONE UMA UNIDADE</option>
									</select>									
								</td>
							</tr>
							
							<tr>
								<th class="doublecol" rowspan="3">Solicitante</th>
								<th class="doublecol"><label for="unidadeLotacao">Unidade de Lotação*</label></th>
								<td>
									<select name="unidadeLotacao" 
											id="unidadeLotacao" 
									 		objeto="ajaxNumeroContrato"
									 		metodo="buscarItensUnidade">
										<option inicial="true" value="">SELECIONE UMA UNIDADE</option>
									</select>									
								</td>
							</tr>
							<tr>
								<th class="doublecol"><label for="nome">Nome *</label></th>
								<td>
									<input name="nome" id="nome" tipo="texto" 
											type="text" class="text" 
											size="60" maxlength="60"
											value=""/>
								</td>
							</tr>
							<tr>
								<th class="doublecol"><label for="ddd">Telefone</label></th>
								<td>
									<input name="ddd" id="ddd"
											tipo="numerico" type="text" class="text"
											size="2" maxlength="2"
											value="" 
											onfocus="ffocus(this, event)"/>
									<input name="telefone" id="telefone"
											tipo="numerico" type="text" class="text" 
											size="8" maxlength="8" 
											onfocus="ffocus(this, event)" 
											value=""/>
								</td>
							</tr>
							
							<tr> 
								<th colspan="2"><label for="observacao">Observação</label></th>
								<td colspan="3">
									<textarea name="observacao" id="observacao" cols="60" 
											class="text" rows="4" onkeypress="limitarTexto(this, event, 2000); fkeypress_alfabetico(this, event);"></textarea>
								</td>
							</tr>
						</tbody>
						
						<tfoot>
							<tr>
								<td colspan="3">
									<input type="button" name="gerar" value="Gerar" class="button" onclick="submeter(this.form, 'incluir', true);"/>
									<input name="reset" type="reset" class="button" value="Limpar"/>
									<input type="button" value="Cancelar" class="button" onclick="cancelar('gecon.do?tipoAcao=manter_gerador&amp;codItemMenu=<%=UtilMenu.getIdItemMenu(request, "Manter Número de Contrato")%>')"/>
								</td>
							</tr>
						</tfoot>
					</table>
				</html:form>
				
				<%@ include file="include/quicknav_footer.jsp" %>
			</div>
			<!-- end of content -->
			
		</div>
		<!-- end of container -->
		
		<%@ include file="include/footer.jsp" %>
		
	</body>
</html>
