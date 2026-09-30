<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html-el"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-br">
	<head>
		<%@ include file="include/head.jsp"%>
		<style type="text/css">
			select#unidadeGestora, select#unidadeLotacao, select#unidadeLavratura { width: 45em; }
			select#unidadeGestora option, select#unidadeLotacao option, select#unidadeLavratura option { width: 44em }
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
			<div id="content">
				<h2>Alteração de Número de Contrato/Conv&ecirc;nio</h2>
				
				<a href="ajuda/manual_gecon.pdf" class="help" target="_blank" title="Ajuda">
					<span>Ajuda</span>
				</a>
				
				<div class="form-info">
					<p>Preencha os campos e selecione o botão "Alterar".</p>
					<p>* Campos obrigatórios.</p>
				</div>
				
				<html:form action="/jsp/gecon/alteracao_numero_contrato.do" method="post">
					<input type="hidden" name="tipoAcao" id="tipoAcao" value="alterar"/>
					<table>
						<tbody>
							<tr>
								<th colspan="2" width="30%">
									<label for="numeroSIAC">Número do Contrato/Conv&ecirc;nio*</label>
								</th>
								<td>
									<select name="numeroSIAC" 
										id="numeroSIAC" 
								 		objeto="ajaxNumeroContrato"
								 		metodo="buscarItensNumeroContrato"
								 		onchange="numeroContrato.carregarNumeroContrato(this.value);">
										<option inicial="true" value="">SELECIONE UM Número DE CONTRATO</option>
									</select>
								</td>
							</tr>
							
							<tr>
								<th colspan="2">
									<label for="unidadeLavratura">Unidade de Lavratura do Contrato*</label>
								</th>
								<td>
									<select name="unidadeLavratura" 
										id="unidadeLavratura" 
								 		objeto="ajaxNumeroContrato"
								 		metodo="buscarItensUnidade">
										<option inicial="true" value="">SELECIONE UMA UNIDADE</option>
									</select>
								</td>
							</tr>
							
							<tr>
								<th colspan="2">
									<label for="unidadeGestora">Unidade Gestora do Contrato*</label>
								</th>
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
								<th style="width: 15%" rowspan="3">Solicitante</th>
								<th style="width: 15%">
									<label for="unidadeLotacao">Unidade de Lotação*</label>
								</th>
								<td>
									<select name="unidadeLotacao" 
										id="unidadeLotacao" 
								 		objeto="ajaxNumeroContrato"
								 		metodo="buscarItensUnidade"
								 		funcao="numeroContrato.verificarCombosAlteracao();">
										<option inicial="true" value="">SELECIONE UMA UNIDADE</option>
									</select>
						 		</td>
							</tr>
							<tr>
								<th><label for="nome">Nome *</label></th>
								<td>
									<input name="nome" id="nome" tipo="texto" 
										type="text" class="text" 
										size="60" maxlength="60"
										value=""/>
								</td>
							</tr>
							<tr>
								<th><label for="ddd">Telefone</label></th>
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
								<th colspan="2">
									<label for="observacao">Observação</label>
								</th>
								<td>
									<textarea name="observacao" id="observacao" cols="60" 
										class="text" rows="4"
										onkeypress="limitarTexto(this, event, 2000); fkeypress_alfabetico(this, event);">
									</textarea>
								</td>
							</tr>
							
							<tr>
								<th colspan="2">Situação*</th>
								<td>
									<input type="radio" name="situacao" id="situacao_1" value="1"/>
									<label for="situacao_1">N&atilde;o utilizado no SIAC</label><br />
									<input type="radio" name="situacao" id="situacao_3" value="3"/>
									<label for="situacao_3">Número de Contrato/Conv&ecirc;nio Cancelado</label>
								</td>
							</tr>
						</tbody>
						<tfoot>
							<tr>
								<td colspan="3">
									<input type="button" name="alterar" value="Alterar" class="button" onclick="submeter(this.form, 'alterar', true);"/>
									<input name="reset" type="button" class="button" value="Limpar" onclick="numeroContrato.limparAlteracao();"/>
									<input type="button" value="Cancelar" class="button" onclick="cancelar('gecon.do?tipoAcao=manter_gerador&amp;codItemMenu=<%=
										UtilMenu.getIdItemMenu(request, "Manter Número de Contrato")%>')"/>
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