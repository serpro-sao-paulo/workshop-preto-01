<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-br">
	<head>
<title>Detalhamento Da Consulta De Rodovia</title>
		<%@ include file="include/head.jsp"%>
	</head>
		
	<body>
		<div id="container">
			
			<%@ include file="include/header.jsp" %>
			
			<div id="content" >
				<h2>Detalhamento da Consulta de Rodovia</h2>
				<table>
					<tbody>
						<tr> 
							<th scope="row">Sigla</th>
							<td>${rodovias.sigla}</td>
						</tr>
						
						<tr> 
							<th scope="row">Descrição</th>
							<td>${rodovias.descricao}</td>
						</tr>
						
						<tr> 
							<th scope="row">UF</th>
							<td><c:forEach var="uf" items="${rodovias.ufs}">${uf.nome}<br /></c:forEach></td>
						</tr>
						
						<tr> 
							<th scope="row">Situação</th>
							<td>${(rodovias.situacao==1)?"ATIVO":"INATIVO"}</td>
						</tr>
					</tbody>
					
					<tfoot>
						<tr> 
							<td colspan="2"> 
								<input type="button" value="Nova Consulta" class="button" onclick="cancelar('consulta_rodovia.do')" />
								<input type="button" value="Retornar ao Menu" class="button" onclick="cancelar('siac.do?tipoAcao=administracao_siac&amp;codItemMenu=<%=UtilMenu.getIdItemMenu(request, "Administração")%>')"/>
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