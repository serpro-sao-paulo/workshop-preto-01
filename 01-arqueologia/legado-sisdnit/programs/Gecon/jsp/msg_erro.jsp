<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dnit" uri="/WEB-INF/tld/Sisdnit.tld"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-br">
	<head>
		<%@ include file="include/head.jsp"%>
	</head>
		
	<body>
		<div id="container">
		
			<%@ include file="include/header.jsp"%>
			
			<div id="content" >
     			<h2>Problema Encontrado</h2>
				<div id="status-box">
					<dnit:out escapeXml="false">
						<h3 class="msg-erro">${msg_erro}</h3>
					</dnit:out>
				</div>
				<%@ include file="include/quicknav_footer.jsp" %>
				
			</div>
			<!-- end of content -->
			
		</div>
		<!-- end of container -->
		
		<%@ include file="include/footer.jsp" %>
	
	</body>
</html>