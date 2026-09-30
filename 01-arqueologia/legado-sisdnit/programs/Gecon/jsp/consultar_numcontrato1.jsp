<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html-el"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-br">
	<head>
		<%@ include file="include/head.jsp"%>
		
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
		
		<style type="text/css">
			table tbody th {
				width: 30%;
				min-width: 25em;
				_width: 25em; /* IE6 hack */
			}
		</style>
	</head>
	
	<body>
		<div id="container">
			
			<%@ include file="include/header.jsp"%>
			
			<script src="js/NumeroContrato.js"></script>
			<div id="content" >
				<h2>Consulta de Número de Contrato/Conv&ecirc;nio</h2>

				<a href="ajuda/manual_gecon.pdf" class="help" target="_blank" title="Ajuda">
					<span>Ajuda</span>
				</a>
				
				<div class="form-info">
					<p>Preencha os campos e selecione o botão "Consultar".</p>
					<p>** Selecione pelo menos um dos campos.</p>
				</div>
				
				<html:form action="/jsp/gecon/consulta_numero_contrato.do" method="post">
					<input type="hidden" name="tipoAcao" id="tipoAcao" value="consultar"/>
					<table>
						<tbody>
							<tr> 
								<th scope="row"><label for="numeroSIAC" >Número do Contrato/Convênio no SIAC**</label></th>
								<td>
									<input name="numeroSIAC" id="numeroSIAC" type="text" class="text"
											size="11" maxlength="10" 
											value="" 
											formato="numerico" mascara="#####/####"/>
								</td>
							</tr>
							
							<tr> 
								<th scope="row"><label for="numeroSIASG">Número do Contrato/Convênio no SIASG**</label></th>
								<td>
									<input name="numeroSIASG" id="numeroSIASG" type="text" class="text"
											size="18" maxlength="17" 
											value="" 
											formato="numerico" mascara="###### #####/####"/>
								</td>
							</tr>
							
							<tr>
								<th scope="row"><label for="unidadeLavratura">Unidade de Lavratura do Contrato**</label></th>
								<td>						
						 			<select name="unidadeLavratura" 
											id="unidadeLavratura" 
									 		objeto="ajaxNumeroContrato"
									 		metodo="buscarItensLavraturaContratoConsulta">
											<option inicial="true" value="">SELECIONE UMA UNIDADE</option>
									</select>
								</td>
							</tr>
													 
							<tr>
								<th scope="row"><label for="unidadeGestora">Unidade Gestora do Contrato**</label></th>
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
								<th scope="row"><label for="unidadeLotacao">Unidade Solicitante**</label></th>
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
								<th scope="row"><label for="ano">Ano**</label></th>
								<td>
									<select name="ano" 
											id="ano" 
									 		objeto="ajaxNumeroContrato"
									 		metodo="buscarItensAnos">
											<option inicial="true" value="">SELECIONE O ANO</option>
									</select>
								</td>
							</tr>
							
							<tr> 
								<th scope="row">Situação**</th>
								<td>
									<input type="radio" name="situacao" id="situacao_1" value="1"/><label for="situacao_1">Não utilizado no SIAC</label><br />
									<input type="radio" name="situacao" id="situacao_2" value="2"/><label for="situacao_2">Utilizado no SIAC</label><br />
									<input type="radio" name="situacao" id="situacao_3" value="3"/><label for="situacao_3">Número de Contrato/Convênio Cancelado</label>
								</td>
					 		</tr>
				 		</tbody>
				 		
				 		<tfoot>
					 		<tr>
								<td colspan="2">
									<input type="button" value="Consultar" class="button" onclick="numeroContrato.validarConsulta()"/>
									<input type="reset" value="Limpar" class="button"/>
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
