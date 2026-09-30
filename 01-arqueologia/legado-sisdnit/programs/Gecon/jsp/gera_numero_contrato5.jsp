<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-br">
	<head>
		<%@ include file="include/head.jsp"%>
	</head>
		
	<body>
		<div id="container">
			<%@ include file="include/header.jsp"%>
			
			<div id="content" >
				<h2>Inclusão de Número de Contrato/Conv&ecirc;nio </h2>
				<table>
					<tbody>
						<tr>
							<th>Número do Contrato no SIAC</th>
							<td>${numeroContratoSIAC}</td>
						</tr>
						<tr>
							<th>Número do Contrato no SIASG </th>
							<td>${numeroContratoSIASG}</td>
						</tr>
						<tr>
							<th>Data e Hora de Impress&atilde;o:</th>
							<td>${data}&nbsp;-&nbsp;${hora}</td>
						</tr>
					</tbody>
				</table>
			
				<%@ include file="include/quicknav_footer.jsp" %>
			</div>
			<!-- end of content -->
			
		</div>
		<!-- end of container -->
		
		<%@ include file="include/footer.jsp" %>
		
	</body>
</html>