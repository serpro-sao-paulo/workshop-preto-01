// ============================================================================
// RegionalFactorTable.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Implementa BR-031 (Fator Regional por tabela de 27 posições).
//
// Rastreabilidade:
//   - Regra legada: BR-031 (business-rules-catalog.md)
//   - Programa Natural: CALCBENF.NSN#L93-L120 e #L179-L184
//   - Mistério resolvido: MYS-008 (tabela duplicada entre BATCHPGT e CALCBENF).
//     Esta classe é a FONTE ÚNICA do fator regional no sisdnit 2.0 (ADR-002).
//   - Requisito moderno: REQ-PAY-003
// ============================================================================

package br.gov.client.sisdnit.payment.domain;

import java.math.BigDecimal;

/**
 * Fonte única do Fator Regional (BR-031), substituindo as duas cópias hardcoded
 * do legado (CALCBENF e BATCHPGT — MYS-008). Índice 1..25 = fator específico,
 * 26..27 = reserva (1,00), fora do intervalo = 1,00 (fallback do legado).
 */
public final class RegionalFactorTable {

    private static final BigDecimal DEFAULT_FACTOR = new BigDecimal("1.0000");

    // Posições 1..27 (índice 0 não usado, espelha #TAB-REG(1..27) do Natural).
    private static final BigDecimal[] FACTORS = {
            null,                       // 0 — não usado
            new BigDecimal("1.3500"),   // 1  AC
            new BigDecimal("1.3200"),   // 2  AM
            new BigDecimal("1.3000"),   // 3  AP
            new BigDecimal("1.2800"),   // 4  PA
            new BigDecimal("1.3100"),   // 5  RO
            new BigDecimal("1.4000"),   // 6  MA
            new BigDecimal("1.3800"),   // 7  PI
            new BigDecimal("1.3500"),   // 8  CE
            new BigDecimal("1.3200"),   // 9  BA
            new BigDecimal("1.3600"),   // 10 PE
            new BigDecimal("1.1000"),   // 11 SP
            new BigDecimal("1.1200"),   // 12 RJ
            new BigDecimal("1.0800"),   // 13 MG
            new BigDecimal("1.0500"),   // 14 ES
            new BigDecimal("1.0000"),   // 15 REF
            new BigDecimal("1.0500"),   // 16 PR
            new BigDecimal("1.0700"),   // 17 SC
            new BigDecimal("1.0300"),   // 18 RS
            new BigDecimal("1.1500"),   // 19 MS
            new BigDecimal("1.2000"),   // 20 MT
            new BigDecimal("1.1800"),   // 21 GO
            new BigDecimal("1.2500"),   // 22 TO
            new BigDecimal("1.1000"),   // 23 DF
            new BigDecimal("1.2200"),   // 24 RR
            new BigDecimal("1.3300"),   // 25 SE
            new BigDecimal("1.0000"),   // 26 RESERVA
            new BigDecimal("1.0000"),   // 27 RESERVA
    };

    private RegionalFactorTable() {
    }

    /**
     * Retorna o fator regional para o código de região (BR-031).
     * Códigos 1..25 retornam o fator específico; demais retornam 1,0000
     * (fallback idêntico ao {@code ELSE MOVE 1.0000} do CALCBENF.NSN#L181).
     */
    public static BigDecimal factorFor(int regionCode) {
        if (regionCode >= 1 && regionCode <= 25) {
            return FACTORS[regionCode];
        }
        return DEFAULT_FACTOR;
    }
}
