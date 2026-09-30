package br.gov.serpro.sunne.dnit.negocio.bean;

import java.math.BigDecimal;

import br.gov.serpro.sunne.dnit.infraestrutura.util.Conversao;


/**
 * Classe bean que representa uma Rodovia
 *
 * @author Endrigo G. Ferreira - endrigo.ferreira@serpro.gov.br
 * @date 03/08/2007
 */
public class Rodovia extends Via {
	private String descricao;
	private BigDecimal nu_km_final_via;

	/**
	 * Cria um objeto do tipo Rodovia
	 *
	 */
	public Rodovia() {
		super();
		nu_km_final_via = BigDecimal.ZERO;
	}

	/**
	 * Cria um objeto do tipo Rodovia
	 *
	 * @param id
	 */
	public Rodovia(Long id) {
		super(id);
		nu_km_final_via = BigDecimal.ZERO;
	}

	/**
	 * Cria um objeto do tipo Rodovia
	 *
	 * @param descricao
	 */
	public Rodovia(String descricao) {
		super();
		this.descricao = descricao;
	}

	/**
	 * Retorna a/o descricao
	 *
	 * @return String
	 * @author Endrigo G. Ferreira - endrigo.ferreira@serpro.gov.br
	 * @since 03/08/2007
	 */
	public String getDescricao() {
		return this.descricao;
	}

	/**
	 * Define a/o descricao
	 *
	 * @param descricao String contendo a/o descricao
	 * @author Endrigo G. Ferreira - endrigo.ferreira@serpro.gov.br
	 * @since 03/08/2007
	 */
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public BigDecimal getNu_km_final_via() {
		return nu_km_final_via;
	}

	public void setNu_km_final_via(BigDecimal nu_km_final_via) {
		this.nu_km_final_via = nu_km_final_via;
	}
	
	public String getNu_km_final_viaToString(){
		return Conversao.bigDecimalToString(this.getNu_km_final_via(),2);
	}
	
	
}
