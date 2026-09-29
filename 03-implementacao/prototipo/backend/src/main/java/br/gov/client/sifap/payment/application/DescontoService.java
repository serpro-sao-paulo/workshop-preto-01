// ============================================================================
// DescontoService.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Reproduz em Java o cálculo de descontos do legado (CALCDSCT.NSN).
//
// Rastreabilidade:
//   - Regras legadas: BR-013, BR-034 (contribuição social), BR-035 (tipos/teto)
//   - Programa Natural: CALCDSCT.NSN (#L88-L178)
//   - Mistério: MYS-010 (teto sobrescreve judicial dentro do loop)
//   - Requisitos modernos: REQ-PAY-001/002, REQ-PAY-DSCT-01
//
// Commit:
//   feat(payment): port CALCDSCT deductions, fix judicial cap clobber (MYS-010)
//     — Implements REQ-PAY-001
// ============================================================================

package br.gov.client.sisdnit.payment.application;

import br.gov.client.sisdnit.payment.domain.Deduction;
import br.gov.client.sisdnit.payment.domain.DeductionType;
import br.gov.client.sisdnit.payment.domain.MoneyRounding;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Serviço de cálculo de descontos, fiel ao CALCDSCT.
 *
 * <p>Expõe dois métodos: {@link #calculate} é o comportamento <b>corrigido</b>
 * do sisdnit 2.0 (judiciais nunca são sobrescritos pelo teto); {@link #calculateLegacyParity}
 * reproduz o comportamento legado <b>com o bug</b> documentado em MYS-010, para
 * testes de paridade e comparação.</p>
 */
@Service
public class DescontoService {

    /** Teto de desconto: 30% do bruto, exceto judicial (BR-013/BR-035 / CALCDSCT.NSN#L101). */
    private static final BigDecimal CAP_RATE = new BigDecimal("0.30");
    private static final BigDecimal UNION_RATE = new BigDecimal("0.01");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * Contribuição social obrigatória por faixa de valor bruto (BR-034 / CALCDSCT.NSN#L57-L66).
     * ≤500=3%; ≤1000=5%; ≤2000=7%; demais=9%.
     */
    public BigDecimal mandatoryContribution(BigDecimal gross) {
        BigDecimal rate;
        if (gross.compareTo(new BigDecimal("500.00")) <= 0) {
            rate = new BigDecimal("0.03");
        } else if (gross.compareTo(new BigDecimal("1000.00")) <= 0) {
            rate = new BigDecimal("0.05");
        } else if (gross.compareTo(new BigDecimal("2000.00")) <= 0) {
            rate = new BigDecimal("0.07");
        } else {
            rate = new BigDecimal("0.09");
        }
        return gross.multiply(rate);
    }

    /**
     * Cálculo de descontos do sisdnit 2.0 (comportamento corrigido).
     *
     * <p>Judiciais somam integralmente (BR-035) e o teto de 30% incide apenas
     * sobre o conjunto dos não-judiciais — evitando o clobber do legado (MYS-010).</p>
     *
     * @param deductions descontos cadastrados
     * @param gross      valor bruto do pagamento
     * @param reference  data de referência para vigência
     * @return total de descontos truncado em 2 casas (BR-032)
     */
    public BigDecimal calculate(List<Deduction> deductions, BigDecimal gross, LocalDate reference) {
        BigDecimal cap = MoneyRounding.truncate(gross.multiply(CAP_RATE));

        BigDecimal judicialTotal = BigDecimal.ZERO;
        BigDecimal otherTotal = mandatoryContribution(gross); // contribuição é não-judicial

        if (deductions != null) {
            for (Deduction d : deductions) {
                if (!d.isActiveOn(reference)) {
                    continue;
                }
                BigDecimal item = itemAmount(d, gross);
                if (d.type() == DeductionType.JUDICIAL) {
                    judicialTotal = judicialTotal.add(item);
                } else {
                    otherTotal = otherTotal.add(item);
                }
            }
        }

        BigDecimal cappedOther = otherTotal.min(cap);
        return MoneyRounding.truncate(cappedOther.add(judicialTotal));
    }

    /**
     * Reprodução FIEL do legado, incluindo o bug MYS-010: o teto é reavaliado
     * dentro do loop e pode sobrescrever descontos judiciais já somados quando um
     * desconto não-judicial seguinte dispara {@code total = teto}. Use apenas para
     * testes de paridade com o mainframe.
     */
    public BigDecimal calculateLegacyParity(List<Deduction> deductions, BigDecimal gross, LocalDate reference) {
        BigDecimal total = mandatoryContribution(gross);
        BigDecimal cap = MoneyRounding.truncate(gross.multiply(CAP_RATE));

        if (deductions != null) {
            for (Deduction d : deductions) {
                if (!d.isActiveOn(reference)) {
                    continue;
                }
                total = total.add(itemAmount(d, gross));
                // CALCDSCT.NSN#L164-L168 — teto reavaliado no loop (MYS-010).
                if (d.type() != DeductionType.JUDICIAL && total.compareTo(cap) > 0) {
                    total = cap;
                }
            }
        }
        return MoneyRounding.truncate(total);
    }

    /** Valor de um item de desconto por tipo (CALCDSCT.NSN#L120-L161). */
    private BigDecimal itemAmount(Deduction d, BigDecimal gross) {
        if (d.type() == DeductionType.UNION) {
            return gross.multiply(UNION_RATE); // sindical = 1% fixo
        }
        if (d.type() == DeductionType.INCOME_TAX) {
            return gross.multiply(d.percent()).divide(HUNDRED); // imposto sempre percentual
        }
        // J, P, A: valor fixo se > 0, senão percentual.
        return d.amountFor(gross);
    }
}
