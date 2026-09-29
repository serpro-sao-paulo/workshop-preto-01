<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-br">
	<head>
<title>Resultado Da Consulta Para Alteração De Rodovia</title>
		<%@ include file="include/head.jsp"%>
	</head>
	
	<body>
		<div id="container">

			<%@ include file="include/header.jsp" %>
			
			<div id="content" >
				
				<h2>Resultado da Consulta para Alteração de Rodovia</h2>
				
				<div class="form-info">
					<p>Selecione a Rodovia.</p>
				</div>
				
				<html:form action="/jsp/siac/alteracao_rodovia.do" method="post">
					<input type="hidden" name="tipoAcao"/>
					<display:table list="${listaRodovias}" id="row" defaultsort="1" class="dataTable" export="true" pagesize="${tamanho}" sort="list" requestURI="alteracao_rodovia.do" >
						<display:column property="sigla" title="Sigla" sortable="true"
								href="alteracao_rodovia.do?tipoAcao=detalhar" paramId="id" paramProperty="id" group="1"/>
								
						<display:column property="descricao" title="Descrição" sortable="true"
								href="alteracao_rodovia.do?tipoAcao=detalhar" paramId="id" paramProperty="id" group="1"/>
								
						<display:column title="UF" sortable="true"
							href="alteracao_rodovia.do?tipoAcao=detalhar" paramId="id" paramProperty="id" group="1">
							<c:set var="nrUfs" value="${fn:length(row.ufs)}"/>
							<c:forEach var="uf" items="${row.ufs}" varStatus="status">
								${uf.nome}
								<c:if test="${status.count != nrUfs}">
									&nbsp;/&nbsp;
								</c:if>
							</c:forEach>
						</display:column>
						
						<display:column title="Descrição" sortable="true"
								href="alteracao_rodovia.do?tipoAcao=detalhar" paramId="id" paramProperty="id" group="1">${row.situacao==1?"ATIVO":"INATIVO"}</display:column>
						
						<display:footer>
							<tr> 
								<td colspan="2"> 
									<input type="button" name="Submit" value="Nova Consulta" class="button" onclick="cancelar('alteracao_rodovia.do?tipoAcao=reset')"/>
									<input type="button" value="Cancelar" class="button" onclick="cancelar('siac.do?tipoAcao=administracao_siac&amp;codItemMenu=<%=UtilMenu.getIdItemMenu(request, "Administração")%>')"/>
								</td>
							</tr>
						</display:footer>
														
						<display:setProperty name="export.excel.filename" value="consultaRodovia.xls" />
						<display:setProperty name="export.csv.filename" value="consultaRodovia.csv" />
						<display:setProperty name="export.xml" value="false" />
						
						<!-- alteração rodovia:  Inclui os parâmetros de configuração da paginação e tabela -->
						<%@ include file="include/pagination_properties.jsp" %>
					</display:table>
				</html:form>
				
				<%@ include file="include/quicknav_footer.jsp" %>
				
			</div>
			<!-- end of content -->
			
		</div>
		<!-- end of container -->
		
		<%@ include file="include/footer.jsp" %>
		
	</body>
</html>