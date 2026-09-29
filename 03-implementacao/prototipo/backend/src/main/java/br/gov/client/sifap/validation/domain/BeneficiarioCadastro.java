/*
 * sisdnit 2.0 — Par 4 · Qualidade (DBA + QA) · Estágio 3
 * ----------------------------------------------------------------------------
 * Dados cadastrais do beneficiário submetidos à validação (espelha a VIEW
 * BENEFICIARIO-V de VALBENEF/VALDOCS).
 *
 * Rastreabilidade: VALBENEF.NSN#L13-L20, VALDOCS.NSN#L13-L18
 * Campos DDM: NUM-CPF (AB), NOME-COMPLETO (AC), DT-NASCIMENTO (AF),
 *             UF (BG), SIT-BENEFICIARIO (CE), RG-NUMERO (AI)
 */
package br.gov.client.sisdnit.validation.domain;

public record BeneficiarioCadastro(
    String cpf,
    String nome,
    int dataNascimento,   // AAAAMMDD (N8)
    String uf,
    char status,
    String rg
) {
}
