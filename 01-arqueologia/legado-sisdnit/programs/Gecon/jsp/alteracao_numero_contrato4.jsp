<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-br">
	<head>
		<%@ include file="include/head.jsp"%>
	</head>
		
	<body>
		<div id="container">
		
			<%@ include file="include/header.jsp"%>
			
		  	<div id="content" >
				<h2>Alteração de Número de Contrato</h2>
				<div id="status-box">
					<h3 class="msg-sucesso">Alteração efetuada com sucesso!</h3>
					<p><a href="alteracao_numero_contrato.do" title="Clique aqui para alterar outro Número de Contrato">Alterar outro Número de Contrato</a></p>
					<p><a href="gecon.do?tipoAcao=manter_gerador&amp;codItemMenu=<%=UtilMenu.getIdItemMenu(request, "Manter Número de Contrato")%>" title="Clique aqui para retornar ao Menu">Retornar ao Menu</a></p>
				</div>
				<%@ include file="include/quicknav_footer.jsp" %>
			</div>
			<!-- end of content -->
			
		</div>
		<!-- end of container -->
		
		<%@ include file="include/footer.jsp" %>
		
	</body>
</html>