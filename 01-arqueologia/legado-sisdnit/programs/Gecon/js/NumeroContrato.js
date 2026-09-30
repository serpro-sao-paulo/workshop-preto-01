include("utils/UtilDhtml.js");
include("utils/UtilCampo.js");
include("utils/UtilEvento.js");
include("utils/UtilLayer.js");
include("utils/UtilAjax.js");
include("utils/UtilFormulario.js");
include("utils/UtilCombo.js");
include("htmlDom/ModeloDom.js");

function NumeroContrato() {
	this.form = document.forms[0];
	this.ajax = UtilAjax.obterObjetoAjax("ajaxNumeroContrato");
		
	this.iniciarCarga = function(objBean) {
		this.bean = objBean;
		UtilCombo.carregarCombo(null, 'unidadeLavratura');
		UtilCombo.carregarCombo(null, 'unidadeGestora');
		UtilCombo.carregarCombo(null, 'unidadeLotacao');
		this.form.nome.value = '';
		this.form.ddd.value = '';
		this.form.telefone.value = '';
		this.form.observacao.value = '';
	};
	
//	var objBean = new Object();
//	objBean.idLogado = '${usuarioLogado.id}';
//	objBean.idUnidade = '${usuarioLogado.unidade.id}';
//	objBean.idUnidadePai = '${usuarioLogado.unidade.unidadePai.id}';
//	objBean.idTipoUnidade = "${usuarioLogado.unidade.subtipoUnidade.tipoUnidade.id}";
//	objBean.idSubtipoUnidade = "${usuarioLogado.unidade.subtipoUnidade.id}";
	
	// verifica se o usuario deve vir com o combo pre selecionado
	this.verificarComboUnidadeLavratura = function() {
		var combo = UtilDhtml.findElementById(document, 'unidadeLavratura');
		
		if (combo.options.length == 2) {
			UtilCombo.selecionarItemCombo(combo, this.bean.idUnidade);
			UtilCampo.bloquearEscrita(combo);	
		}
	};
	
	this.iniciarCargaConsulta = function(objBean) {
		//alert('CONSULTA');
		this.bean = objBean;
		UtilCombo.carregarCombo(null, 'unidadeLavratura');
		UtilCombo.carregarCombo(null, 'unidadeGestora');
		UtilCombo.carregarCombo(null, 'unidadeLotacao');
		UtilCombo.carregarCombo(null, 'ano');
	};
	
	this.validarConsulta = function() {
		var msg = this.validarNumeroSIAC2();
		msg += this.validarNumeroSIASG();
		
		if(msg == '') {
			submeter(this.form, 'consultar', true);
		} else {
			alert(msg);
		}
		
	};
	
	this.validarNumeroSIAC2 = function() {
		// SIAC
		// 1234567890
		// NNNNN/YYYY
		var tam = this.form.numeroSIAC.value.length;
		
		if (tam > 0 && tam < 6) {
			return 'O formato do campo Número do Contrato/Convênio no SIAC é inválido. O formato correto é NNNNN/YYYY\n';
		} 
		return '';
	};
	
	this.validarNumeroSIAC = function() {
		// SIAC
		// 1234567890
		// NNNNN/YYYY
		var tam = this.form.numeroSIAC.value.length;
		
		if (tam > 0 && tam != 10) {
			return 'O formato do campo Número do Contrato/Convênio no SIAC é inválido. O formato correto é NNNNN/YYYY\n';
		} 
		return '';
	};
	
	this.validarNumeroSIASG = function() {
		// SIASG
		// 12345678901234567
		// XXXXXX NNNNN/YYYY
		var tam = this.form.numeroSIASG.value.length;
		
		if (tam > 0 && tam != 17) {
			return 'O formato do campo Número do Contrato/Convênio no SIASG é inválido. O formato correto é XXXXXX NNNNN/YYYY\n';
		} 
		return '';
	};
	
		
	// ******************************************************
	// ******************* ALTERACAO ************************
	// ******************************************************	
	this.iniciarCargaAlteracao = function(objBean) {
		this.bean = objBean;
		UtilCombo.carregarCombo(null, 'numeroSIAC');
	};
	
	this.carregarNumeroContrato = function(idContrato) {
		this.numContrato = this.ajax.buscarNumeroContrato(idContrato);
		
		if (this.numContrato != null) {
			UtilCombo.carregarCombo(null, 'unidadeLavratura');
			UtilCombo.carregarCombo(null, 'unidadeGestora');
			UtilCombo.carregarCombo(null, 'unidadeLotacao');
			
			this.form.nome.value = this.numContrato.usuarioSolicitante.nome;
			this.form.ddd.value = this.numContrato.usuarioSolicitante.ddd;
			this.form.telefone.value = this.numContrato.usuarioSolicitante.telefone;
			this.form.observacao.value = this.numContrato.observacao;
			var aux = this.numContrato.situacaoNumeroContrato;
			UtilDhtml.findElementById(document,"situacao_"+aux).checked = true;
		} else {
			this.form.nome.value = '';
			this.form.ddd.value = '';
			this.form.telefone.value = '';
			this.form.observacao.value = '';
			UtilDhtml.findElementById(document,"situacao_1").checked = false;
			UtilDhtml.findElementById(document,"situacao_3").checked = false;
		}
	};
	
	this.verificarCombosAlteracao = function() {
		UtilCombo.selecionarItemCombo("unidadeLavratura", this.numContrato.unidadeLavraturaContrato.id);
		
//		if(this.numContrato.situacaoNumeroContrato != 2) {
//			UtilCampo.liberarEscrita(this.form.unidadeLavratura);
//		}else {
//			UtilCampo.bloquearEscrita(this.form.unidadeLavratura);
//		}
		
		UtilCombo.selecionarItemCombo("unidadeGestora", this.numContrato.unidadeGestoraContrato.id);
		UtilCombo.selecionarItemCombo("unidadeLotacao", this.numContrato.usuarioSolicitante.unidade.id);
	};
	
	this.limparAlteracao = function() {
		UtilCombo.selecionarItemCombo("numeroSIAC", "");
		UtilCampo.liberarEscrita(document.getElementById("unidadeLavratura"));
		document.getElementById("unidadeLavratura").value = "";
		UtilCombo.limparCombo(document.getElementById("unidadeGestora"));
		UtilCombo.limparCombo(document.getElementById("unidadeLotacao"));
		document.getElementById("nome").value = "";
		document.getElementById("ddd").value = "";
		document.getElementById("telefone").value = "";
		document.getElementById("observacao").value = "";
		
		if(document.getElementById("situacao_1").checked) {
			document.getElementById("situacao_1").checked = false;
		}
		
		if(document.getElementById("situacao_3").checked) {
			document.getElementById("situacao_3").checked = false;
		}
	};
}

var numeroContrato;

UtilEvento.adicionarTratadorEvento(window, "load",
	function() {
		var tipoAcao = UtilDhtml.findElementById(document, 'tipoAcao');
		
		if((tipoAcao.value == "incluir") || (tipoAcao.value == "incluirManual")) { // INCLUSAO
			numeroContrato = new NumeroContrato();
			numeroContrato.iniciarCarga(objBean);
		} else {
			if(tipoAcao.value == "consultar") { // CONSULTA
				//onload="Mascaras.carregar();"
				Mascaras.carregar();
				numeroContrato = new NumeroContrato();
				numeroContrato.iniciarCargaConsulta(objBean);
			} else { // ALTERACAO
				Mascaras.carregar();
				numeroContrato = new NumeroContrato();
				numeroContrato.iniciarCargaAlteracao(objBean);
			}
		}
	}
);
