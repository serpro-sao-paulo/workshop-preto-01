<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html-el"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-br">
	<head>
		<%@ include file="include/head.jsp"%>
		<html:javascript formName="NumeroContratoForm"/>
		<style type="text/css">
			table tbody th {
				width: 34%;
			}
			table tbody th.doublecol {
				width: 17%;
			}
		</style>
	</head>
		
	<body>
		<div id="container">
		
			<%@ include file="include/header.jsp"%>
			
			<div id="content" >
				<h2>Detalhamento de Consulta de Número de Contrato/Conv&ecirc;nio</h2>
						
				<table>
					<tbody>
						<tr>
							<th class="doublecol" rowspan="2">Número do Contrato/Convênio</th>
							<th class="doublecol">SIAC</th>
							<td>${NumeroContratoForm.map.numeroSIAC}</td>
						</tr>
						<tr>
							<th class="doublecol">SIASC</th>
							<td>${NumeroContratoForm.map.numeroSIASG}</td>
						</tr>
						
						<tr>
							<th colspan="2">Unidade de Lavratura do Contrato</th>
							<td>${NumeroContratoForm.map.unidadeLavratura}</td>
						</tr>
						
						<tr>
							<th colspan="2">Unidade Gestora do Contrato</th>
							<td>${NumeroContratoForm.map.unidadeGestora}</td>
						</tr>
						
						<tr>
							<th class="doublecol" rowspan="3">Solicitante</th>
							<th class="doublecol">Unidade de Lotação</th>
							<td>${NumeroContratoForm.map.unidadeLotacao}</td>
						</tr>
						<tr>
							<th class="doublecol">Nome</th>
							<td>${NumeroContratoForm.map.nome}</td>
						</tr>
						<tr>
							<th class="doublecol">Telefone</th>
							<td>${NumeroContratoForm.map.telefone}</td>
						</tr>
						
						<tr> 
							<th colspan="2">Situação</th>
							<td>${NumeroContratoForm.map.situacao}</td>
						</tr>
						
						<tr> 
							<th colspan="2">Observação</th>
							<td>${NumeroContratoForm.map.observacao}</td>
						</tr>
					</tbody>
					
					<tfoot>
						<tr>
							<td colspan="3">
								<input type="button" value="Nova Consulta" class="button" onclick="cancelar('consulta_numero_contrato.do')"/>
								<input name="button" type="button" class="button" onclick="cancelar('gecon.do?tipoAcao=manter_gerador&amp;codItemMenu=<%=UtilMenu.getIdItemMenu(request, "Manter Número de Contrato")%>')" value="Retornar ao Menu"/>
							</td>
						</tr>
					</tfoot>
				</table>
				
				<%@ include file="include/quicknav_footer.jsp" %>
			</div>
			<!-- end of content -->
			
		</div>
		<!-- end of container -->
		
		<%@ include file="include/footer.jsp" %>
		
	</body>
</html>
