package br.gov.sifap.shared.audit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado quando um pagamento é calculado com sucesso.
 * Cobre: REQ-PAY-001, REQ-AUD-001
 */
public record PaymentCalculated(
        UUID paymentId,
        UUID beneficiaryId,
        String competencia,
        BigDecimal vlrBruto,
        BigDecimal vlrDesconto,
        BigDecimal vlrLiquido,
        String tipoPgto,
        Instant occurredAt,
        String actor
) implements java.io.Serializable {

    public static PaymentCalculated of(UUID paymentId, UUID beneficiaryId,
                                       String competencia, BigDecimal bruto,
                                       BigDecimal desconto, BigDecimal liquido,
                                       String tipoPgto, String actor) {
        return new PaymentCalculated(paymentId, beneficiaryId, competencia,
                bruto, desconto, liquido, tipoPgto, Instant.now(), actor);
    }
}
