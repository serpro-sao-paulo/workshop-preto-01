package br.gov.sifap.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para MoneyRounding.
 * @implements REQ-PAY-006
 */
class MoneyRoundingTest {

    @Test
    void halfUp_givenExactTwoDecimals_returnsUnchanged() {
        assertThat(MoneyRounding.halfUp(new BigDecimal("100.25")))
                .isEqualByComparingTo("100.25");
    }

    @Test
    void halfUp_givenThirdDecimalFive_roundsUp() {
        // 100.125 → 100.13 com HALF_UP (legado truncaria para 100.12 — MYS-005)
        assertThat(MoneyRounding.halfUp(new BigDecimal("100.125")))
                .isEqualByComparingTo("100.13");
    }

    @Test
    void halfUp_givenThirdDecimalFour_roundsDown() {
        assertThat(MoneyRounding.halfUp(new BigDecimal("100.124")))
                .isEqualByComparingTo("100.12");
    }

    @Test
    void halfUp_givenNegative_worksCorrectly() {
        assertThat(MoneyRounding.halfUp(new BigDecimal("-10.555")))
                .isEqualByComparingTo("-10.56");
    }

    @Test
    void halfUp_givenNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> MoneyRounding.halfUp(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void halfUp_legacyTruncationDifference_demonstratesChange() {
        // Demonstra diferença em relação ao legado (MYS-005 / REQ-PAY-006)
        BigDecimal value = new BigDecimal("347.215"); // vlr típico legado
        BigDecimal legacyResult = new BigDecimal("347.21"); // truncamento mainframe
        BigDecimal modernResult = MoneyRounding.halfUp(value);
        assertThat(modernResult).isEqualByComparingTo("347.22");
        assertThat(modernResult).isNotEqualByComparingTo(legacyResult);
    }
}
