<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html-el"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-br">
	<head>
<title>Alteração De Rodovia</title>
		<%@ include file="include/head.jsp"%>
		<html:javascript formName="RodoviaForm"/>
		<script type="text/javascript" src="../js/form/mascaras.js"></script>
		<script type="text/javascript">
			function selecionarTodasUfs(form) {
				// efetuando a seleção de todas as opções da combo de UFs para garantir o seu envio completo
				for (var i = 0; i < form.ufs.options.length; i++) {
					form.ufs.options[i].selected = true;
				}
			}
			
			function limparUfs(form) {
				selecionarTodasUfs(form);
				eval((findElementById(document,'removeUF').getAttribute('onclick')).split('void')[1]);
			}
		</script>
	</head>
	
	<body onload="Mascaras.carregar();">
		<div id="container">
			
			<%@ include file="include/header.jsp" %>
			
			<div id="content" >
				<h2>Alteração de Rodovia</h2>
				
				<a href="ajuda/manual_siac.pdf" class="help" target="_blank" title="Ajuda">
					<span>Ajuda</span>
				</a>
				
				<div class="form-info">
					<p>Preencha os campos e selecione o botão "Consultar".</p>
					<p>** Selecione pelo menos um dos campos.</p>
				</div>
				
				<html:form action="/jsp/siac/alteracao_rodovia.do" method="post">
					<input type="hidden" name="tipoAcao"/>
					<table>
						<tbody>
							<tr> 
								<th scope="row"><label for="sigla">Sigla**</label></th>
								<td>
									<input type="text" id="sigla" name="sigla" class="text" size="10" maxlength="6" formato="alfabetico" mascara="##-###"/>
								</td>
							</tr>
							
							<tr> 
								<th scope="row"> <label for="descricao">Descrição**</label></th>
								<td><input type="text" id="descricao" name="descricao" class="text" size="100" maxlength="80" /></td>
							</tr>
							
							<tr> 
								<th scope="row"><label for="lista_ufs">UF**</label></th>
								<td>				
									 <div class="interchange" tipo="interchange" id="intercambio" style="width: 500px;"
										fornecedor="objeto=ajaxRodovia, metodo=buscarItensUf, carregar=true, name=lista_ufs, id=lista_ufs, size=6" 
										consumidor="carregar=false, name=ufs, id=ufs, size=6"
										botao1="style='width:30px;', class='button', id='adicionaUF'"
										botao2="style='width:30px;', class='button', id='removeUF'" >
									</div>
									 <script type="text/javascript">
											renderizarComponente("intercambio");
											document.getElementById("lista_ufs").style.fontSize = "10px";
											document.getElementById("ufs").style.fontSize = "10px";
									</script>
								</td>	
							</tr>
					
							<tr> 
								<th scope="row">Situação**</th>
								<td>
									<input type="radio" name="situacao" id="situacao1" value="1" checked="checked"/>
									<label for="situacao1">ATIVO</label>
									<br /> 
									<input type="radio" name="situacao" id="situacao0" value="0" />
									<label for="situacao0">INATIVO</label>
								</td>
							</tr>
						</tbody>
						
						<tfoot>
							<tr> 
								<td colspan="2">
									<input type="button" name="Submit" value="Consultar" class="button" onclick="selecionarTodasUfs(this.form);submeter(this.form, 'consultar', true);"/> 
									<input name="reset" type="reset" class="button" value="Limpar" onclick="limparUfs(this.form);"/>
									<input name="button" type="button" class="button" onclick="cancelar('siac.do?tipoAcao=administracao_siac&amp;codItemMenu=<%=UtilMenu.getIdItemMenu(request, "Administração")%>')" value="Cancelar" /> 
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