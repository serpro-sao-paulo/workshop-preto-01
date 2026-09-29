// ============================================================================
// CalculoBeneficioServiceTest.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Testa a paridade do cálculo do benefício com o legado CALCBENF.
//
// Rastreabilidade: BR-021/022/023/024/025/031/032/033 · REQ-PAY-003/004/005
// ============================================================================

package br.gov.client.sisdnit.payment.application;

import br.gov.client.sisdnit.payment.domain.BenefitCalculationInput;
import br.gov.client.sisdnit.payment.domain.BenefitCalculationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculoBeneficioServiceTest {

    private final CalculoBeneficioService service = new CalculoBeneficioService();

    private BenefitCalculationInput input(int competence, int birthYear, int region,
                                          int deps, String income, String base,
                                          String reajuste, char type) {
        return new BenefitCalculationInput(competence, birthYear, region, deps,
                new BigDecimal(income), new BigDecimal(base), new BigDecimal(reajuste), type);
    }

    @Test
    @DisplayName("BR-021: benefício mensal = base × reg × fam × renda × idade × (1+reajuste), truncado")
    void monthlyBenefitMatchesLegacyFormula() {
        // SP (region 11 → 1.1000), 0 dep (1.0), renda 250 (faixa1 → 1.0),
        // idade 40 (1.0), base 1000, reajuste 0.10. mês 6 (sem 13º).
        // 1000 * 1.1000 * 1.0 * 1.0 * 1.0 = 1100 ; *1.10 = 1210.00
        BenefitCalculationInput in = input(202606, 1986, 11, 0, "250.00", "1000.00", "0.1000", 'N');

        BenefitCalculationResult r = service.calculate(in);

        assertEquals(new BigDecimal("1210.00"), r.grossAmount());
        assertEquals('N', r.paymentType());
        assertEquals(new BigDecimal("0.00"), r.thirteenth());
    }

    @Test
    @DisplayName("BR-032: trunca (não arredonda) as casas decimais excedentes")
    void truncatesInsteadOfRounding() {
        // base 100, region 19 (MS → 1.1500), demais 1.0, reajuste 0 → 115.00 exato;
        // usar renda que mantenha fator 1.0 e idade normal.
        // Para forçar dízima: base 100, region 1 (AC 1.3500) → 135.00 exato.
        // Usar 2 dep (1.10), region SP (1.10): 100*1.10*1.10 = 121.000... ok exato.
        // Forçar truncamento: renda faixa2 (0.85): 100*1.10*1.10*0.85 = 102.85 exato.
        // Use idade <18 (1.05): 100*1.10*1.10*0.85*1.05 = 107.9925 → trunca 107.99
        BenefitCalculationInput in = input(202607, 2015, 11, 2, "550.00", "100.00", "0.0000", 'N');

        BenefitCalculationResult r = service.calculate(in);

        assertEquals(new BigDecimal("107.99"), r.grossAmount());
    }

    @Test
    @DisplayName("BR-025/BR-033: dezembro paga 13º (base×reg×idade) e abono 15% para programa 'A'")
    void decemberAddsThirteenthAndBonusForAssistanceProgram() {
        // region 15 (REF → 1.0000), 0 dep, renda 200 (1.0), idade 40 (1.0),
        // base 1000, reajuste 0. mensal = 1000.00.
        // 13º = 1000 * 1.0 * 1.0 = 1000.00 ; abono = 1000 * 0.15 = 150.00
        // bruto = 1000 + 1000 + 150 = 2150.00
        BenefitCalculationInput in = input(202612, 1986, 15, 0, "200.00", "1000.00", "0.0000", 'A');

        BenefitCalculationResult r = service.calculate(in);

        assertEquals('D', r.paymentType());
        assertEquals(new BigDecimal("1000.00"), r.monthly());
        assertEquals(new BigDecimal("1000.00"), r.thirteenth());
        assertEquals(new BigDecimal("150.00"), r.bonus());
        assertEquals(new BigDecimal("2150.00"), r.grossAmount());
    }

    @Test
    @DisplayName("BR-025: programa não-'A' em dezembro recebe 13º mas NÃO recebe abono")
    void decemberNonAssistanceHasNoBonus() {
        BenefitCalculationInput in = input(202612, 1986, 15, 0, "200.00", "1000.00", "0.0000", 'N');

        BenefitCalculationResult r = service.calculate(in);

        assertEquals(new BigDecimal("0.00"), r.bonus());
        assertEquals(new BigDecimal("2000.00"), r.grossAmount());
    }

    @Test
    @DisplayName("BR-031: código de região fora de 1–25 usa fator regional 1,0")
    void regionOutOfRangeUsesDefaultFactor() {
        BenefitCalculationInput in = input(202606, 1986, 99, 0, "200.00", "1000.00", "0.0000", 'N');

        BenefitCalculationResult r = service.calculate(in);

        assertEquals(new BigDecimal("1000.00"), r.grossAmount());
    }
}
