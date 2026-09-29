// ============================================================================
// BenefitCalculationResult.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Resultado do cálculo do benefício mensal (CALCBENF).
//
// Rastreabilidade:
//   - Regras legadas: BR-021, BR-025, BR-033 (business-rules-catalog.md)
//   - Programa Natural: CALCBENF.NSN#L257-L300
//   - Requisito moderno: REQ-PAY-003
// ============================================================================

package br.gov.client.sisdnit.payment.domain;

import java.math.BigDecimal;

/**
 * Saída do cálculo do benefício, espelhando os campos gravados em PAGAMENTO.
 *
 * @param grossAmount  valor bruto (benefício mensal + 13º + abono, quando dezembro)
 * @param monthly      benefício mensal calculado (antes de 13º/abono)
 * @param thirteenth   13º salário (zero fora de dezembro — BR-033)
 * @param bonus        abono natalino (zero se programa não 'A' ou fora de dezembro — BR-025)
 * @param paymentType  'N' normal, 'D' dezembro (13º)
 */
public record BenefitCalculationResult(
        BigDecimal grossAmount,
        BigDecimal monthly,
        BigDecimal thirteenth,
        BigDecimal bonus,
        char paymentType) {
}
