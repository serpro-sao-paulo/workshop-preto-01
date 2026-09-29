// ============================================================================
// BenefitFactors.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Implementa os fatores multiplicadores do cálculo do benefício.
//
// Rastreabilidade:
//   - Regras legadas: BR-022 (familiar), BR-023 (renda), BR-024 (idade), BR-031 (regional)
//   - Programa Natural: CALCBENF.NSN#L185-L228
//   - Requisito moderno: REQ-PAY-003/004/005
// ============================================================================

package br.gov.client.sisdnit.payment.domain;

import java.math.BigDecimal;

/**
 * Cálculo puro dos quatro fatores multiplicadores do benefício mensal,
 * fiel ao CALCBENF. Sem dependência de framework — totalmente testável.
 */
public final class BenefitFactors {

    private BenefitFactors() {
    }

    /**
     * Fator Familiar por faixa de dependentes (BR-022 / CALCBENF.NSN#L187-L201).
     * 0 dep = 1,00; 1–2 = 1,00 + dep×0,05; 3–4 = 1,10 + (dep−2)×0,03; 5+ = 1,16 + (dep−4)×0,02.
     */
    public static BigDecimal family(int dependents) {
        if (dependents <= 0) {
            return new BigDecimal("1.0000");
        }
        if (dependents <= 2) {
            return new BigDecimal("1.0000")
                    .add(new BigDecimal(dependents).multiply(new BigDecimal("0.0500")));
        }
        if (dependents <= 4) {
            return new BigDecimal("1.1000")
                    .add(new BigDecimal(dependents - 2).multiply(new BigDecimal("0.0300")));
        }
        return new BigDecimal("1.1600")
                .add(new BigDecimal(dependents - 4).multiply(new BigDecimal("0.0200")));
    }

    /**
     * Fator Renda — primeira faixa cujo teto cobre a renda familiar
     * (BR-023 / CALCBENF.NSN#L203 subrotina DET-FAIXA-RENDA).
     * ≤300=1,00; ≤600=0,85; ≤1000=0,70; ≤1500=0,55; demais=0,40.
     */
    public static BigDecimal income(BigDecimal familyIncome) {
        if (familyIncome.compareTo(new BigDecimal("300.00")) <= 0) {
            return new BigDecimal("1.0000");
        }
        if (familyIncome.compareTo(new BigDecimal("600.00")) <= 0) {
            return new BigDecimal("0.8500");
        }
        if (familyIncome.compareTo(new BigDecimal("1000.00")) <= 0) {
            return new BigDecimal("0.7000");
        }
        if (familyIncome.compareTo(new BigDecimal("1500.00")) <= 0) {
            return new BigDecimal("0.5500");
        }
        return new BigDecimal("0.4000");
    }

    /**
     * Fator Idade (BR-024 / CALCBENF.NSN#L204-L218).
     * ≥65=1,15; ≥60=1,10; <18=1,05; demais=1,00.
     *
     * NOTA (MYS-002): no legado a idade é calculada apenas por diferença de anos
     * (ano da competência − ano de nascimento), ignorando mês/dia. Este método
     * recebe a idade já calculada; o serviço replica o comportamento legado.
     */
    public static BigDecimal age(int age) {
        if (age >= 65) {
            return new BigDecimal("1.1500");
        }
        if (age >= 60) {
            return new BigDecimal("1.1000");
        }
        if (age < 18) {
            return new BigDecimal("1.0500");
        }
        return new BigDecimal("1.0000");
    }
}
