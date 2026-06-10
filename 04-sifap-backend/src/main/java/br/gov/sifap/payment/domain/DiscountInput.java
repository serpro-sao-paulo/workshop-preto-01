package br.gov.sifap.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representação de um desconto para cálculo.
 * Projetado a partir de BeneficiaryDiscount — desacoplado do domínio beneficiary.
 */
public record DiscountInput(
        String tipoDsct,
        BigDecimal vlrDsct,
        BigDecimal pctDsct,
        LocalDate dtInicio,
        LocalDate dtFim
) {
    public boolean isActiveOn(LocalDate date) {
        if (dtInicio.isAfter(date)) return false;
        if (dtFim != null && dtFim.isBefore(date)) return false;
        return true;
    }
}
