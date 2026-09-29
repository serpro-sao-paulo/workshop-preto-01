<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html-el"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-br">
	<head>
<title>Alteração De Rodovia</title>
		<%@ include file="include/head.jsp"%>
		<html:javascript formName="RodoviaForm"/> 	 
		
		<script type="text/javascript">
			function selecionarTodasUfs(form) {
				// efetuando a seleção de todas as opções da combo de UFs para garantir o seu envio completo
				for (var i = 0; i < form.ufs.options.length; i++) {
					form.ufs.options[i].selected = true;
				}
			}
			
			function verificarSiglaExiste(obj) {
			
				var verificar = false;
				var valorAtual = trim(obj.value).toUpperCase();
						
				if(valorAtual!=obj.form.vBDSigla.value && valorAtual!="") {
					verificar = true;
				}		
				
				if (verificar) {
									
					obj.form.alterar.disabled = true;
						
					resultado = executarAjax('ajaxRodovia', 'verificarSiglaExiste', valorAtual, 1);
					if (resultado == true) {
						alert("Sigla da Rodovia já cadastrada.");
						obj.value=obj.form.vBDSigla.value;
						setFocus(obj);
					}
					
					obj.form.alterar.disabled = false;
				}
			}
			
		</script>
	</head>
	
	<body>
		<div id="container">
			
			<%@ include file="include/header.jsp" %>
		
			<div id="content" >
				<h2>Alteração de Rodovia</h2>
				
				<div class="form-info">
					<p>Preencha os campos e selecione o botão "Alterar". </p>
					<p>* Campos obrigatórios.</p>
				</div>
				
				<html:form action="/jsp/siac/alteracao_rodovia.do" method="post">
					<table>
						<tbody>
							<tr> 
								<th scope="row"><label for="sigla">Sigla *</label></th>
								<td >
									<input type="text" class="text" size="10" tipo="texto" maxlength="6" value="${rodovias.sigla}" disabled/>
									<input type="hidden" name="sigla" value="${rodovias.sigla}" />
								</td>
							</tr>
							
							<tr> 
								<th scope="row"> <label for="descricao">Descrição*</label></th>
								<td>
								 	<input type="text" id="descricao" name="descricao" tipo="texto" class="text" size="100" maxlength="80" value="${rodovias.descricao}"/>
								</td>
							</tr>
							
							<tr> 
								<th scope="row"><label for="intercambio">UF *</label></th>
								<td>
									<div class="interchange" tipo="interchange" id="intercambio" style="width: 515px;"
										fornecedor="objeto=ajaxRodovia, metodo=buscarItensUf, carregar=true, name=lista_ufs, id=lista_ufs, size=6" 
										consumidor="objeto=ajaxRodovia, metodo=buscarItensUf, carregar=false, name=ufs, id=ufs, size=6"
										botao1="style='width:30px;', class='button', id='adicionaUF'"
										botao2="style='width:30px;', class='button', id='removeUF'">
									</div>
									<script type="text/javascript">
										renderizarComponente("intercambio");
										carregarComboInterchange('intercambio', 'ufs', '${rodovias.id}');
									</script>
								</td>
							</tr>
							
							<tr> 
								<th scope="row">Situação*</th>
								<td>
									<c:set var="tipoSel" value="${(rodovias.situacao==0)?'0':'1'}" />
									<input type="radio" id="idTipoOperacao1" name="situacao" value="1" ${(tipoSel==1)?"checked='checked'":""} />
									<label for="idTipoOperacao1">ATIVO </label>
									<br /> 
									<input type="radio" id="idTipoOperacao0" name="situacao" value="0" ${(tipoSel==0)?"checked='checked'":""} />
									<label for="idTipoOperacao0">INATIVO</label>
								</td>
							</tr>
						</tbody>
						
						<tfoot>
							<tr> 
								<td colspan="2">
								 	<input type="hidden" name="tipoAcao"/>
									<input type="button" name="alterar" value="Alterar" class="button" onclick="selecionarTodasUfs(this.form);submeter(this.form, 'alterar', true)"/>
									<input name="reset" type="reset" class="button" value="Limpar" onclick="renderizarComponente('intercambio');carregarComboInterchange('intercambio', 'ufs', '${rodovias.id}');"/>
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