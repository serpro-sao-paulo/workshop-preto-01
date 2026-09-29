// ============================================================================
// CalculoBeneficioService.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Reproduz em Java o cálculo do benefício mensal do legado (CALCBENF.NSN).
//
// Rastreabilidade:
//   - Regras legadas: BR-021, BR-022, BR-023, BR-024, BR-025, BR-031, BR-032, BR-033
//   - Programa Natural: CALCBENF.NSN (cálculo principal #L221-L254)
//   - Requisitos modernos: REQ-PAY-003/004/005/006
//
// Commit:
//   feat(payment): port CALCBENF monthly benefit calc — Implements REQ-PAY-003
// ============================================================================

package br.gov.client.sisdnit.payment.application;

import br.gov.client.sisdnit.payment.domain.BenefitCalculationInput;
import br.gov.client.sisdnit.payment.domain.BenefitCalculationResult;
import br.gov.client.sisdnit.payment.domain.BenefitFactors;
import br.gov.client.sisdnit.payment.domain.MoneyRounding;
import br.gov.client.sisdnit.payment.domain.RegionalFactorTable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Serviço de cálculo do benefício mensal, fiel ao CALCBENF.
 *
 * <p>O método principal é puro (sem I/O) para máxima testabilidade e paridade
 * com o legado. A persistência (gravação em PAGAMENTO) fica na camada
 * infrastructure; aqui só a lógica financeira.</p>
 */
@Service
public class CalculoBeneficioService {

    private static final BigDecimal ONE = new BigDecimal("1");
    /** Percentual do abono natalino para programas tipo 'A' (BR-025 / CALCBENF.NSN#L251). */
    private static final BigDecimal CHRISTMAS_BONUS_RATE = new BigDecimal("0.15");
    private static final char ASSISTANCE_PROGRAM = 'A';

    /**
     * Calcula o benefício do mês (BR-021), incluindo 13º e abono em dezembro
     * (BR-025/BR-033). Trunca a cada etapa conforme BR-032.
     *
     * @param in dados do beneficiário, programa e competência
     * @return valores brutos calculados (mensal, 13º, abono, tipo)
     */
    public BenefitCalculationResult calculate(BenefitCalculationInput in) {
        // --- Fatores (BR-022/023/024/031) ---
        BigDecimal regional = RegionalFactorTable.factorFor(in.regionCode());
        BigDecimal family = BenefitFactors.family(in.dependents());
        BigDecimal income = BenefitFactors.income(in.familyIncome());
        BigDecimal age = BenefitFactors.age(in.legacyAge());

        // --- Benefício mensal: base * reg * fam * rnd * idade (BR-021) ---
        BigDecimal monthly = in.baseValue()
                .multiply(regional)
                .multiply(family)
                .multiply(income)
                .multiply(age);

        // Aplicar reajuste do programa: * (1 + fator) (CALCBENF.NSN#L227-L228).
        monthly = monthly.multiply(ONE.add(in.reajusteFactor()));

        // Truncar para 2 casas — padrão mainframe (BR-032).
        monthly = MoneyRounding.truncate(monthly);

        BigDecimal gross = monthly;
        BigDecimal thirteenth = BigDecimal.ZERO.setScale(2);
        BigDecimal bonus = BigDecimal.ZERO.setScale(2);
        char paymentType = 'N';

        // --- Dezembro: 13º + abono (BR-025/BR-033 / CALCBENF.NSN#L239-L255) ---
        if (in.month() == 12) {
            paymentType = 'D';

            // 13º = base * reg * idade (SEM fator familiar/renda — BR-033, ver MYS-013).
            thirteenth = MoneyRounding.truncate(
                    in.baseValue().multiply(regional).multiply(age));
            gross = monthly.add(thirteenth);

            // Abono natalino: 15% do benefício mensal, só programas tipo 'A' (BR-025).
            if (in.programType() == ASSISTANCE_PROGRAM) {
                bonus = MoneyRounding.truncate(monthly.multiply(CHRISTMAS_BONUS_RATE));
                gross = gross.add(bonus);
            }
        }

        return new BenefitCalculationResult(
                MoneyRounding.truncate(gross), monthly, thirteenth, bonus, paymentType);
    }
}
