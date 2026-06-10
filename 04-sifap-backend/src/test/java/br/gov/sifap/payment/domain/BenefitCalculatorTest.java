package br.gov.sifap.payment.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para BenefitCalculator.
 * Todos os valores esperados foram calculados manualmente a partir da fórmula
 * em CALCBENF.NSN e verificados linha a linha.
 *
 * @implements REQ-PAY-001, REQ-PAY-002, REQ-PAY-003, REQ-PAY-004, REQ-PAY-005
 */
class BenefitCalculatorTest {

    private BenefitCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new BenefitCalculator();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private CalculationParams params(BigDecimal vlrBase, BigDecimal fatorReajuste,
                                     String tipoProg, int codRegiao, int numDep,
                                     BigDecimal renda, int idade, int mes) {
        return new CalculationParams(vlrBase, fatorReajuste, tipoProg,
                codRegiao, numDep, renda, idade, mes);
    }

    // ─── Cálculo mensal normal ──────────────────────────────────────────────

    @Test
    void calculate_basicScenario_returnsCorrectGross() {
        // Given: vlrBase=347.22, sem reajuste, tipo N, região 15 (fator=1.00),
        //        0 dependentes (fator=1.00), renda=300 (fator=1.00), idade=30 (fator=1.00)
        // Expected: 347.22 × 1.00 × 1.00 × 1.00 × 1.00 × (1+0) = 347.22
        // @implements REQ-PAY-001
        CalculationParams p = params(
                new BigDecimal("347.22"), BigDecimal.ZERO, "B",
                15, 0, new BigDecimal("300.00"), 30, 6);

        BenefitCalculator.CalculationResult result = calculator.calculate(p);

        assertThat(result.vlrBruto()).isEqualByComparingTo("347.22");
        assertThat(result.tipoPgto()).isEqualTo("N");
        assertThat(result.vlrAbono()).isEqualByComparingTo("0.00");
    }

    @Test
    void calculate_withRegionalFactor_north_multipliesCorrectly() {
        // Região 6 = MA: fator 1.40 (CALCBENF.NSN#L99)
        // 100.00 × 1.40 = 140.00
        // @implements REQ-PAY-002
        CalculationParams p = params(
                new BigDecimal("100.00"), BigDecimal.ZERO, "B",
                6, 0, new BigDecimal("300.00"), 30, 6);

        BenefitCalculator.CalculationResult result = calculator.calculate(p);

        assertThat(result.vlrBruto()).isEqualByComparingTo("140.00");
    }

    @Test
    void calculate_withFamilyFactor_twoDependents() {
        // 2 dependentes: fator = 1.00 + 2×0.05 = 1.10
        // 100.00 × 1.10 = 110.00
        // @implements REQ-PAY-003
        CalculationParams p = params(
                new BigDecimal("100.00"), BigDecimal.ZERO, "B",
                15, 2, new BigDecimal("300.00"), 30, 6);

        BenefitCalculator.CalculationResult result = calculator.calculate(p);

        assertThat(result.vlrBruto()).isEqualByComparingTo("110.00");
    }

    @ParameterizedTest(name = "dependentes={0} fatorEsperado={1}")
    @CsvSource({
        "0,  1.0000",
        "1,  1.0500",
        "2,  1.1000",
        "3,  1.1300",
        "4,  1.1600",
        "5,  1.1800",
        "6,  1.2000"
    })
    void familyFactor_variousDependents(int numDep, BigDecimal expectedFactor) {
        assertThat(BenefitCalculator.familyFactor(numDep))
                .isEqualByComparingTo(expectedFactor);
    }

    @ParameterizedTest(name = "renda={0} fatorEsperado={1}")
    @CsvSource({
        "100.00,  1.0000",
        "300.00,  1.0000",
        "300.01,  0.8500",
        "600.00,  0.8500",
        "600.01,  0.7000",
        "1000.00, 0.7000",
        "1000.01, 0.5500",
        "1500.00, 0.5500",
        "1500.01, 0.4000",
        "5000.00, 0.4000"
    })
    void incomeFactor_incomeThresholds(BigDecimal renda, BigDecimal expectedFactor) {
        assertThat(BenefitCalculator.incomeFactor(renda))
                .isEqualByComparingTo(expectedFactor);
    }

    @ParameterizedTest(name = "idade={0} fatorEsperado={1}")
    @CsvSource({
        "10, 1.0500",
        "17, 1.0500",
        "18, 1.0000",
        "30, 1.0000",
        "59, 1.0000",
        "60, 1.1000",
        "64, 1.1000",
        "65, 1.1500",
        "80, 1.1500"
    })
    void ageFactor_ageThresholds(int idade, BigDecimal expectedFactor) {
        assertThat(BenefitCalculator.ageFactor(idade))
                .isEqualByComparingTo(expectedFactor);
    }

    @Test
    void calculate_withReajuste_appliesToResult() {
        // fatorReajuste = 0.025 (2.5%) sobre 100.00 = 102.50
        CalculationParams p = params(
                new BigDecimal("100.00"), new BigDecimal("0.0250"), "B",
                15, 0, new BigDecimal("300.00"), 30, 6);

        BenefitCalculator.CalculationResult result = calculator.calculate(p);

        assertThat(result.vlrBruto()).isEqualByComparingTo("102.50");
    }

    // ─── Dezembro — 13º e abono ─────────────────────────────────────────────

    @Test
    void calculate_december_programTypeB_addsBonusOnly13() {
        // Dezembro, tipo B: vlrBruto + vlr13 (sem abono)
        // vlrBenf = 100.00, vlr13 = 100.00×1.00×1.00 = 100.00
        // bruto = 100.00 + 100.00 = 200.00, abono=0
        // @implements REQ-PAY-004
        CalculationParams p = params(
                new BigDecimal("100.00"), BigDecimal.ZERO, "B",
                15, 0, new BigDecimal("300.00"), 30, 12);

        BenefitCalculator.CalculationResult result = calculator.calculate(p);

        assertThat(result.vlrBruto()).isEqualByComparingTo("200.00");
        assertThat(result.tipoPgto()).isEqualTo("D");
        assertThat(result.vlrAbono()).isEqualByComparingTo("0.00");
    }

    @Test
    void calculate_december_programTypeA_addsBonus13AndAbono() {
        // Dezembro, tipo A: vlrBruto + vlr13 + abono(15% do bruto mensal)
        // vlrBenf = 100.00, vlr13 = 100.00, abono = 100.00 × 0.15 = 15.00
        // bruto = 100.00 + 100.00 + 15.00 = 215.00
        // @implements REQ-PAY-005
        CalculationParams p = params(
                new BigDecimal("100.00"), BigDecimal.ZERO, "A",
                15, 0, new BigDecimal("300.00"), 30, 12);

        BenefitCalculator.CalculationResult result = calculator.calculate(p);

        assertThat(result.vlrBruto()).isEqualByComparingTo("215.00");
        assertThat(result.tipoPgto()).isEqualTo("D");
        assertThat(result.vlrAbono()).isEqualByComparingTo("15.00");
    }

    @Test
    void calculate_resultUsesHalfUpRounding_notTruncation() {
        // Demonstra diferença em relação ao legado (REQ-PAY-006 / MYS-005)
        // 0.347215 × 1.00 × ... = 0.347215 → arredonda para 0.35, legado truncaria para 0.34
        CalculationParams p = params(
                new BigDecimal("0.347215"), BigDecimal.ZERO, "B",
                15, 0, new BigDecimal("300.00"), 30, 6);

        BenefitCalculator.CalculationResult result = calculator.calculate(p);

        // HALF_UP: 0.347215 → 0.35 (não 0.34 como no legado)
        assertThat(result.vlrBruto()).isEqualByComparingTo("0.35");
    }

    // ─── Fatores regionais ──────────────────────────────────────────────────

    @ParameterizedTest(name = "regiao={0} fator={1}")
    @CsvSource({
        "1,  1.3500", // AC
        "6,  1.4000", // MA
        "10, 1.3600", // PE
        "15, 1.0000", // REF
        "25, 1.3300", // SE
        "26, 1.0000", // fora do range → default 1.0
        "0,  1.0000"  // inválido → default 1.0
    })
    void regionalFactor_knownRegions(int regiao, BigDecimal expectedFactor) {
        assertThat(BenefitCalculator.regionalFactor(regiao))
                .isEqualByComparingTo(expectedFactor);
    }

    // ─── REQ-PAY-002: meses que NÃO geram 13º ──────────────────────────────

    @Test
    void calculate_november_typeA_noDecemberBonus() {
        // @implements REQ-PAY-002 — teste de limite negativo: novembro NÃO gera abono
        CalculationParams november = params(
                new BigDecimal("100.00"), BigDecimal.ZERO, "A",
                15, 0, new BigDecimal("300.00"), 30, 11);
        CalculationParams june = params(
                new BigDecimal("100.00"), BigDecimal.ZERO, "A",
                15, 0, new BigDecimal("300.00"), 30, 6);

        BenefitCalculator.CalculationResult resNov  = calculator.calculate(november);
        BenefitCalculator.CalculationResult resJune = calculator.calculate(june);

        // Novembro == Junho: sem 13º, sem abono
        assertThat(resNov.vlrBruto()).isEqualByComparingTo(resJune.vlrBruto());
        assertThat(resNov.tipoPgto()).isEqualTo("N");
        assertThat(resNov.vlrAbono()).isEqualByComparingTo("0.00");
    }

    @Test
    void calculate_december_typeB_noAbono_onlyDecimo() {
        // @implements REQ-PAY-002 — programa tipo B em dezembro: apenas 13º, sem abono
        CalculationParams p = params(
                new BigDecimal("100.00"), BigDecimal.ZERO, "B",
                15, 0, new BigDecimal("300.00"), 30, 12);

        BenefitCalculator.CalculationResult result = calculator.calculate(p);

        assertThat(result.tipoPgto()).isEqualTo("D");
        assertThat(result.vlrAbono()).isEqualByComparingTo("0.00"); // tipo B não tem abono
        assertThat(result.vlrBruto()).isGreaterThan(new BigDecimal("100.00")); // tem 13º
    }
}
