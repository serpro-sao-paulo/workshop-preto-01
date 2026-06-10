package br.gov.sifap.payment.domain;

import java.math.BigDecimal;

/**
 * Parâmetros de elegibilidade para verificação antes do cálculo.
 *
 * @param status           status do beneficiário (ex: "ACTIVE")
 * @param rendaFamiliar    renda familiar declarada
 * @param codRegiao        código de região (99 = exceção)
 * @param isRegionException true se a exceção de região foi concedida explicitamente
 * @param statusPrograma   status do programa social ('A' = ativo)
 */
public record EligibilityParams(
        String status,
        BigDecimal rendaFamiliar,
        int codRegiao,
        boolean isRegionException,
        String statusPrograma
) {}
