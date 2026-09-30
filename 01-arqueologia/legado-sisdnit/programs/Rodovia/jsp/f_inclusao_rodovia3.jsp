<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-br">
	<head>
<title>Inclusão De Rodovia</title>
		<%@ include file="include/head.jsp"%>
	</head>
		
	<body>
		<div id="container">	
			
			<%@ include file="include/header.jsp" %>
			
			<div id="content" >
				<h2>Inclusão de Rodovia</h2>
				<div id="status-box">
					<h3 class="msg-sucesso">Inclusão efetuada com sucesso!</h3>
					<p><a href="inclusao_rodovia.do" title="Clique aqui para incluir outra Rodovia">Incluir outra Rodovia</a></p>
					<p><a href="siac.do?tipoAcao=administracao_siac&amp;codItemMenu=<%=UtilMenu.getIdItemMenu(request, "Administração")%>" title="Clique aqui para retornar ao Menu">Retornar ao Menu</a></p>
				</div>
				<%@ include file="include/quicknav_footer.jsp" %>
			</div>
			<!-- end of content -->
			
		</div>
		<!-- end of container -->
		
		<%@ include file="include/footer.jsp" %>
		
	</body>
</html>