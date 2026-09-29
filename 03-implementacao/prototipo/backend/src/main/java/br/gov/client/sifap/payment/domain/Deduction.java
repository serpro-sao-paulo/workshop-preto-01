// ============================================================================
// Deduction.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Representa um desconto cadastrado no grupo periódico DESCONTOS (BR-035).
//
// Rastreabilidade:
//   - Regra legada: BR-035 (business-rules-catalog.md)
//   - Programa Natural: CALCDSCT.NSN#L20-L31 (PE DESCONTOS) e #L107-L163
//   - Requisito moderno: REQ-PAY-DSCT-01
// ============================================================================

package br.gov.client.sisdnit.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Desconto vigente de um beneficiário. O valor efetivo usa {@code fixedAmount}
 * quando &gt; 0; caso contrário aplica {@code percent} sobre o valor bruto
 * (mesma regra do legado: {@code IF VLR-DSCT > 0 ... ELSE bruto*PCT/100}).
 *
 * @param type        tipo do desconto
 * @param fixedAmount valor fixo (use ZERO para usar percentual)
 * @param percent     percentual (ex.: 10.00 = 10%)
 * @param startDate   início da vigência (DT-INICIO-DSCT)
 * @param endDate     fim da vigência (DT-FIM-DSCT); {@code null} = sem fim
 */
public record Deduction(
        DeductionType type,
        BigDecimal fixedAmount,
        BigDecimal percent,
        LocalDate startDate,
        LocalDate endDate) {

    /**
     * Vigência do desconto na data de referência (BR-035 / CALCDSCT.NSN#L111-L118).
     * Fora da vigência o desconto é ignorado (ESCAPE TOP no legado).
     */
    public boolean isActiveOn(LocalDate reference) {
        if (startDate != null && startDate.isAfter(reference)) {
            return false;
        }
        return endDate == null || !endDate.isBefore(reference);
    }

    /**
     * Valor do desconto: fixo se &gt; 0, senão percentual sobre o bruto
     * (CALCDSCT.NSN#L122-L161).
     */
    public BigDecimal amountFor(BigDecimal grossAmount) {
        if (fixedAmount != null && fixedAmount.signum() > 0) {
            return fixedAmount;
        }
        if (percent == null) {
            return BigDecimal.ZERO;
        }
        return grossAmount.multiply(percent).divide(new BigDecimal("100"));
    }
}
