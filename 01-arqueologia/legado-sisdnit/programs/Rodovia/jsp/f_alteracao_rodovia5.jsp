<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html-el"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-br">
	<head>
<title>Resultado Da Consulta De Ferrovias</title>
		<%@ include file="include/head.jsp"%>
	</head>
		
	<body>
		<div id="container">
			
			<%@ include file="include/header.jsp" %>
			
			<div id="content" >
				<h2>Resultado da Consulta de Ferrovias</h2>
				<html:form action="/jsp/siac/consulta_ferrovia.do" method="post">
					<input type="hidden" id="tipoAcao" name="tipoAcao" value=""/>
					<div id="status-box">
						<h3 class="msg-erro">Rodovia n&atilde;o encontrada.</h3>
						<input type="button" value="Nova Consulta" class="button" onclick="cancelar('alteracao_rodovia.do')"/>
						<input type="button" value="Cancelar" class="button" onclick="cancelar('siac.do?tipoAcao=administracao_siac&amp;codItemMenu=<%=UtilMenu.getIdItemMenu(request, "Administração")%>')" name="button"/>
					</div>
				</html:form>
				<%@ include file="include/quicknav_footer.jsp" %>
			</div>
			<!-- end of content -->
			
		</div>
		<!-- end of container -->
		
		<%@ include file="include/footer.jsp" %>
		
	</body>
</html>
