package br.gov.sifap.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Arredondamento monetário padrão do SIFAP 2.0.
 *
 * O legado usava truncamento (CALCBENF.NSN#L228-L232, CALCDSCT.NSN):
 *   COMPUTE #VLR-TEMP = #VLR-BENF * 100
 *   COMPUTE #VLR-BENF = #VLR-TEMP / 100   ← divisão inteira = truncamento
 *
 * Por decisão de negócio (REQ-PAY-006, MYS-005), substituímos por HALF_UP
 * para consistência com normas contábeis brasileiras.
 */
public final class MoneyRounding {

    private MoneyRounding() {}

    /**
     * Arredonda para 2 casas decimais com RoundingMode.HALF_UP.
     *
     * @param value valor a arredondar
     * @return valor com 2 casas decimais, nunca null
     * @throws IllegalArgumentException se value for null
     */
    public static BigDecimal halfUp(BigDecimal value) {
        if (value == null) throw new IllegalArgumentException("value must not be null");
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
