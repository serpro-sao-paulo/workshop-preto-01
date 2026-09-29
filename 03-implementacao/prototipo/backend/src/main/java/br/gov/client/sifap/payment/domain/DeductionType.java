// ============================================================================
// Deduction.java / DeductionType.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Tipos de desconto do grupo periódico DESCONTOS (BR-035).
//
// Rastreabilidade:
//   - Regra legada: BR-035 (business-rules-catalog.md)
//   - Programa Natural: CALCDSCT.NSN#L107-L163 (DECIDE ON #TIPO-DSCT)
//   - DDM: BENEFICIARIO.DESCONTOS (PE)
//   - Requisito moderno: REQ-PAY-DSCT-01
// ============================================================================

package br.gov.client.sisdnit.payment.domain;

/**
 * Tipo de desconto, espelhando os códigos do legado (CALCDSCT.NSN).
 * Apenas {@link #JUDICIAL} é isento do teto de 30% (BR-013/BR-035).
 */
public enum DeductionType {
    CONTRIBUTION('C'),  // contribuição social (calculada à parte por faixa — BR-034)
    INCOME_TAX('I'),    // imposto retido (% sobre o bruto)
    JUDICIAL('J'),      // judicial — SEM teto
    UNION('S'),         // sindical — 1% fixo
    PENSION('P'),       // pensão alimentícia
    ADMINISTRATIVE('A');// administrativo

    private final char legacyCode;

    DeductionType(char legacyCode) {
        this.legacyCode = legacyCode;
    }

    public char legacyCode() {
        return legacyCode;
    }

    public static DeductionType fromLegacyCode(char code) {
        for (DeductionType t : values()) {
            if (t.legacyCode == code) {
                return t;
            }
        }
        throw new IllegalArgumentException("Código de desconto desconhecido: " + code);
    }
}
