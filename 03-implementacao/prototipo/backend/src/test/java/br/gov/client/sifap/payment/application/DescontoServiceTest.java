// ============================================================================
// DescontoServiceTest.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Testa descontos (CALCDSCT) e demonstra o mistério MYS-010.
//
// Rastreabilidade: BR-013/034/035 · REQ-PAY-001/002 · MYS-010
// ============================================================================

package br.gov.client.sisdnit.payment.application;

import br.gov.client.sisdnit.payment.domain.Deduction;
import br.gov.client.sisdnit.payment.domain.DeductionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DescontoServiceTest {

    private final DescontoService service = new DescontoService();
    private final LocalDate today = LocalDate.of(2026, 6, 10);
    private final BigDecimal gross = new BigDecimal("1000.00");

    private Deduction fixed(DeductionType type, String amount) {
        return new Deduction(type, new BigDecimal(amount), BigDecimal.ZERO, null, null);
    }

    private Deduction percent(DeductionType type, String pct) {
        return new Deduction(type, BigDecimal.ZERO, new BigDecimal(pct), null, null);
    }

    @Test
    @DisplayName("BR-034: contribuição social por faixa (≤1000 → 5%)")
    void mandatoryContributionByBand() {
        assertEquals(new BigDecimal("15.00"),
                service.mandatoryContribution(new BigDecimal("500.00")).setScale(2)); // 3%
        assertEquals(new BigDecimal("50.00"),
                service.mandatoryContribution(new BigDecimal("1000.00")).setScale(2)); // 5%
        assertEquals(new BigDecimal("140.00"),
                service.mandatoryContribution(new BigDecimal("2000.00")).setScale(2)); // 7%
        assertEquals(new BigDecimal("270.00"),
                service.mandatoryContribution(new BigDecimal("3000.00")).setScale(2)); // 9%
    }

    @Test
    @DisplayName("BR-013/035: teto de 30% incide nos não-judiciais, judicial soma integral")
    void correctedCapsNonJudicialKeepsJudicial() {
        // contribuição 50 + imposto 40% (400) = 450 não-judicial → capado em 300.
        // judicial fixo 500 → soma integral. total = 300 + 500 = 800.
        List<Deduction> deductions = List.of(
                percent(DeductionType.INCOME_TAX, "40.00"),
                fixed(DeductionType.JUDICIAL, "500.00"));

        assertEquals(new BigDecimal("800.00"), service.calculate(deductions, gross, today));
    }

    @Test
    @DisplayName("BR-035: descontos fora de vigência são ignorados")
    void inactiveDeductionsAreIgnored() {
        Deduction expired = new Deduction(DeductionType.ADMINISTRATIVE,
                new BigDecimal("100.00"), BigDecimal.ZERO,
                LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1));

        // Só sobra a contribuição obrigatória (50.00).
        assertEquals(new BigDecimal("50.00"), service.calculate(List.of(expired), gross, today));
    }

    @Test
    @DisplayName("MYS-010: o legado sobrescreve (clobber) o judicial conforme a ORDEM dos descontos")
    void legacyParityIsOrderDependent() {
        Deduction judicial = fixed(DeductionType.JUDICIAL, "500.00");
        Deduction tax = percent(DeductionType.INCOME_TAX, "40.00");

        // Ordem [judicial, imposto]: judicial somado e depois clobbered pelo teto → 300.
        BigDecimal judicialFirst = service.calculateLegacyParity(List.of(judicial, tax), gross, today);
        // Ordem [imposto, judicial]: teto aplicado antes, judicial soma depois → 800.
        BigDecimal taxFirst = service.calculateLegacyParity(List.of(tax, judicial), gross, today);

        assertEquals(new BigDecimal("300.00"), judicialFirst);
        assertEquals(new BigDecimal("800.00"), taxFirst);

        // A versão corrigida do sisdnit 2.0 é estável (800 em qualquer ordem).
        assertEquals(new BigDecimal("800.00"), service.calculate(List.of(judicial, tax), gross, today));
        assertEquals(new BigDecimal("800.00"), service.calculate(List.of(tax, judicial), gross, today));
    }
}
