package br.gov.sifap.payment.domain;

import java.math.BigDecimal;

/**
 * Parâmetros de entrada para cálculo do benefício.
 * Snapshot imutável — os dados do beneficiário e programa no momento do cálculo.
 *
 * @param vlrBase        valor-base do programa (substitui constante 0.347215 — BR-016, REQ-PRG-001)
 * @param fatorReajuste  fator de reajuste do programa (ex: 0.0250 = 2.5%)
 * @param tipoProg       tipo do programa ('A' = elegível para abono natalino)
 * @param codRegiao      código de região 1-25 (99 = exceção controlada)
 * @param numDependentes número de dependentes do beneficiário
 * @param rendaFamiliar  renda familiar declarada
 * @param idadeAnos      idade do beneficiário em anos completos
 * @param mes            mês de competência (1-12)
 */
public record CalculationParams(
        BigDecimal vlrBase,
        BigDecimal fatorReajuste,
        String tipoProg,
        int codRegiao,
        int numDependentes,
        BigDecimal rendaFamiliar,
        int idadeAnos,
        int mes
) {}
