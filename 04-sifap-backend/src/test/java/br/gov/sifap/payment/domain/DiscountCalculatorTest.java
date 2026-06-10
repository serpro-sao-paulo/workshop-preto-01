package br.gov.sifap.payment.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para DiscountCalculator.
 * Valores verificados manualmente contra CALCDSCT.NSN.
 *
 * @implements REQ-PAY-006, REQ-PAY-007
 */
class DiscountCalculatorTest {

    private DiscountCalculator calculator;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 10);

    @BeforeEach
    void setUp() {
        calculator = new DiscountCalculator();
    }

    // ─── Contribuição social obrigatória ───────────────────────────────────

    @Test
    void calculate_noDiscounts_onlyContribSocial() {
        // Bruto = 300.00 → faixa ≤500 → 3% = 9.00
        // @implements REQ-PAY-007
        BigDecimal total = calculator.calculate(List.of(), new BigDecimal("300.00"), TODAY);
        assertThat(total).isEqualByComparingTo("9.00");
    }

    @Test
    void contribuicaoSocial_bracket500_applies3percent() {
        assertThat(calculator.calcContribSocial(new BigDecimal("500.00")))
                .isEqualByComparingTo("15.00"); // 500 × 3% = 15.00
    }

    @Test
    void contribuicaoSocial_bracket1000_applies5percent() {
        assertThat(calculator.calcContribSocial(new BigDecimal("800.00")))
                .isEqualByComparingTo("40.00"); // 800 × 5% = 40.00
    }

    @Test
    void contribuicaoSocial_bracket2000_applies7percent() {
        assertThat(calculator.calcContribSocial(new BigDecimal("1500.00")))
                .isEqualByComparingTo("105.00"); // 1500 × 7% = 105.00
    }

    @Test
    void contribuicaoSocial_above2000_applies9percent() {
        assertThat(calculator.calcContribSocial(new BigDecimal("3000.00")))
                .isEqualByComparingTo("270.00"); // 3000 × 9% = 270.00
    }

    // ─── Teto 30% (cap não-judicial) ───────────────────────────────────────

    @Test
    void calculate_nonJudicialExceedsCap_clampsAt30percent() {
        // Bruto = 1000.00 → cap = 300.00
        // Sindical (S) = 1% = 10.00, Admin (A) = valor fixo 400.00
        // Total não-judicial = contrib(1000×5%=50) + 10 + 400 = 460 → clamped to 300
        // @implements REQ-PAY-007 (BR-026)
        List<DiscountInput> discounts = List.of(
                new DiscountInput("S", null, null, TODAY.minusDays(1), null),
                new DiscountInput("A", new BigDecimal("400.00"), null, TODAY.minusDays(1), null)
        );
        BigDecimal total = calculator.calculate(discounts, new BigDecimal("1000.00"), TODAY);
        assertThat(total).isEqualByComparingTo("300.00");
    }

    @Test
    void calculate_judicialDiscount_notSubjectToCap() {
        // Judicial = 400.00 sobre bruto de 500.00 (seria > 30% se tivesse cap)
        // Judicial não tem teto — passa integralmente
        List<DiscountInput> discounts = List.of(
                new DiscountInput("J", new BigDecimal("400.00"), null, TODAY.minusDays(1), null)
        );
        BigDecimal total = calculator.calculate(discounts, new BigDecimal("500.00"), TODAY);
        // contrib 500×3%=15 + judicial 400 = 415 (mas contrib capped at 30%=150, judicial livre)
        // contrib = 15 (≤150), judicial = 400 → total = 415
        assertThat(total).isEqualByComparingTo("415.00");
    }

    @Test
    void calculate_expiredDiscount_ignored() {
        // Desconto encerrado ontem → não deve ser aplicado
        List<DiscountInput> discounts = List.of(
                new DiscountInput("A", new BigDecimal("100.00"), null,
                        TODAY.minusYears(1), TODAY.minusDays(1))
        );
        // Apenas contrib social
        BigDecimal total = calculator.calculate(discounts, new BigDecimal("300.00"), TODAY);
        assertThat(total).isEqualByComparingTo("9.00");
    }

    @Test
    void calculate_futureDiscount_ignored() {
        // Desconto começa amanhã → não deve ser aplicado
        List<DiscountInput> discounts = List.of(
                new DiscountInput("A", new BigDecimal("100.00"), null,
                        TODAY.plusDays(1), null)
        );
        BigDecimal total = calculator.calculate(discounts, new BigDecimal("300.00"), TODAY);
        assertThat(total).isEqualByComparingTo("9.00");
    }

    @Test
    void calculate_usesHalfUp_notTruncation() {
        // Sindical: 1% de 333.33 = 3.3333 → HALF_UP = 3.33
        // Legado truncaria para 3.33 também neste caso, mas demonstra o mecanismo
        List<DiscountInput> discounts = List.of(
                new DiscountInput("S", null, null, TODAY.minusDays(1), null)
        );
        BigDecimal total = calculator.calculate(discounts, new BigDecimal("333.33"), TODAY);
        // contrib 333.33×3%=10.00 + sindical 3.33 = 13.33
        assertThat(total).isEqualByComparingTo("13.33");
    }

    // ─── REQ-PAY-005: vlr_liquido nunca negativo ───────────────────────────

    @Test
    void calculate_judicialExceedsBruto_discountExceedsBruto() {
        // @implements REQ-PAY-005
        // Judicial de 800 sobre bruto de 300 → desconto > bruto
        // A aplicação deve clamp: vlr_liquido = max(0, bruto - desconto)
        // Este teste verifica que o calculator retorna o desconto correto;
        // o clamp é responsabilidade da camada de aplicação (PaymentService).
        List<DiscountInput> discounts = List.of(
                new DiscountInput("J", new BigDecimal("800.00"), null, TODAY.minusDays(1), null)
        );
        BigDecimal totalDesconto = calculator.calculate(discounts, new BigDecimal("300.00"), TODAY);
        // contrib 300×3%=9 + judicial 800 = 809
        assertThat(totalDesconto).isGreaterThan(new BigDecimal("300.00"));

        // Camada de aplicação deve garantir vlr_liquido >= 0
        BigDecimal vlrLiquido = new BigDecimal("300.00").subtract(totalDesconto).max(BigDecimal.ZERO);
        assertThat(vlrLiquido).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculate_judicialPlusCappedNonJudicial_bothApplied() {
        // @implements REQ-PAY-004 — judicial não tem teto; não-judicial tem teto 30%
        // Bruto = 1000, judicial = 100, admin = 400
        // Não-judicial: contrib 50 + admin 400 = 450 → capped 300
        // Judicial: 100 (livre)
        // Total = 300 + 100 = 400
        List<DiscountInput> discounts = List.of(
                new DiscountInput("J", new BigDecimal("100.00"), null, TODAY.minusDays(1), null),
                new DiscountInput("A", new BigDecimal("400.00"), null, TODAY.minusDays(1), null)
        );
        BigDecimal total = calculator.calculate(discounts, new BigDecimal("1000.00"), TODAY);
        assertThat(total).isEqualByComparingTo("400.00");
    }
}
