// ============================================================================
// BenefitCalculationInput.java / BenefitCalculationResult.java
// Par 3 · Implementação (Estágio 3)
// ============================================================================
// Entrada e saída do cálculo do benefício mensal (CALCBENF).
//
// Rastreabilidade:
//   - Regras legadas: BR-021, BR-025, BR-032, BR-033 (business-rules-catalog.md)
//   - Programa Natural: CALCBENF.NSN
//   - Requisito moderno: REQ-PAY-003
// ============================================================================

package br.gov.client.sisdnit.payment.domain;

import java.math.BigDecimal;

/**
 * Dados de entrada do cálculo do benefício, agregando beneficiário + programa
 * + competência (espelha as views BENEFICIARIO/PROGRAMA-SOCIAL do CALCBENF).
 *
 * @param competence     competência AAAAMM (ex.: 202612)
 * @param birthYear      ano de nascimento (legado calcula idade só pelo ano — MYS-002)
 * @param regionCode     código de região (BR-031)
 * @param dependents     número de dependentes (BR-022)
 * @param familyIncome   renda familiar (BR-023)
 * @param baseValue      valor base do programa (PROGRAMA-SOCIAL.VLR-BASE)
 * @param reajusteFactor fator de reajuste do programa (FATOR-REAJUSTE)
 * @param programType    tipo do programa ('A' = assistencial, recebe abono — BR-025)
 */
public record BenefitCalculationInput(
        int competence,
        int birthYear,
        int regionCode,
        int dependents,
        BigDecimal familyIncome,
        BigDecimal baseValue,
        BigDecimal reajusteFactor,
        char programType) {

    public int year() {
        return competence / 100;
    }

    public int month() {
        return competence - (year() * 100);
    }

    /** Idade pela diferença de anos, fiel ao legado (CALCBENF.NSN#L205-L206 / MYS-002). */
    public int legacyAge() {
        return year() - birthYear;
    }
}
