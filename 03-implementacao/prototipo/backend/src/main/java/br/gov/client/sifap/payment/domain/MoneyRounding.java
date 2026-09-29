// ============================================================================
// MoneyRounding.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Implementa BR-032 (truncamento monetário padrão mainframe).
//
// Rastreabilidade:
//   - Regra legada: BR-032 (business-rules-catalog.md)
//   - Programa Natural: CALCBENF.NSN#L231-L234 (COMPUTE temp = vlr*100; vlr = temp/100)
//   - Requisito moderno: REQ-PAY-003 (SPECIFICATION.md)
//
// IMPORTANTE: o legado TRUNCA (descarta casas), não arredonda. Usar
// RoundingMode.DOWN para garantir paridade bit-a-bit com o mainframe.
// ============================================================================

package br.gov.client.sisdnit.payment.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilitário de truncamento monetário fiel ao legado (BR-032).
 * O Natural calcula {@code temp = INT(valor * 100)} e depois {@code valor = temp / 100},
 * o que equivale a truncar para 2 casas decimais (sem arredondamento).
 */
public final class MoneyRounding {

    public static final int SCALE = 2;

    private MoneyRounding() {
    }

    /**
     * Trunca para 2 casas decimais, descartando o excedente (BR-032 / CALCBENF.NSN#L231-L234).
     *
     * @param value valor a truncar (não nulo)
     * @return valor truncado com escala 2
     */
    public static BigDecimal truncate(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("value não pode ser nulo");
        }
        return value.setScale(SCALE, RoundingMode.DOWN);
    }
}
