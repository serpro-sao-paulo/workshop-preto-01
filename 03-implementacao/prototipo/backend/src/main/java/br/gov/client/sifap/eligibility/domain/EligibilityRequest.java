/*
 * sisdnit 2.0 — Par 4 · Qualidade (DBA + QA) · Estágio 3
 * ----------------------------------------------------------------------------
 * Entrada da avaliação de elegibilidade (combina BENEFICIARIO-V + PROGRAMA-V
 * de VALELEG).
 *
 * Rastreabilidade: VALELEG.NSN#L13-L37
 * Campos DDM: COD-REGIAO (BJ), SIT-BENEFICIARIO (CE), VLR-RENDA-FAMILIAR (CH),
 *             QTD-MEMBROS-FAMILIA (CI), NIS; PROGRAMA-SOCIAL.{TIPO, RENDA-MAX,
 *             IDADE-MIN, IDADE-MAX, COD-ELEGIBILIDADE}
 */
package br.gov.client.sisdnit.eligibility.domain;

import java.math.BigDecimal;

public record EligibilityRequest(
    int codRegiao,
    char statusBeneficiario,
    int idade,
    BigDecimal rendaFamiliar,
    int numDependentes,
    long nis,
    char documentosOk,        // 'S' quando documentação completa
    ProgramType tipoPrograma,
    BigDecimal rendaMax,      // 0 = sem teto
    int idadeMin,             // 0 = sem mínimo
    int idadeMax,             // 0 = sem máximo
    String codElegibilidade   // posição 1='R' exige NIS, posição 2='D' exige dependentes
) {
}
