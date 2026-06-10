package br.gov.sifap.payment.domain;

import br.gov.sifap.shared.MoneyRounding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Domain service puro — calcula descontos do beneficiário.
 * Sem Spring, sem JPA. Testável como POJO.
 *
 * Tradução de CALCDSCT.NSN.
 * REQ-PAY-007, REQ-PAY-006 (half-up substitui truncamento legado — MYS-005).
 */
public class DiscountCalculator {

    /**
     * Teto de desconto não-judicial: 30% do bruto.
     * CALCDSCT.NSN#L100: COMPUTE #VLR-MAX-DSCT = #VLR-BRUTO * 0.30
     * BR-026, REQ-PAY-007.
     */
    private static final BigDecimal NON_JUDICIAL_CAP = new BigDecimal("0.30");

    /**
     * Alíquotas de contribuição social por faixa de renda (CALCDSCT.NSN#L55-L68).
     * tipo 'C' — Contribuição compulsória.
     */
    private static final BigDecimal[] CONTRIB_FAIXA = {
            new BigDecimal("500.00"),
            new BigDecimal("1000.00"),
            new BigDecimal("2000.00"),
            new BigDecimal("9999.99")
    };
    private static final BigDecimal[] CONTRIB_ALIQ = {
            new BigDecimal("0.03"),
            new BigDecimal("0.05"),
            new BigDecimal("0.07"),
            new BigDecimal("0.09")
    };

    /**
     * Calcula o total de descontos a aplicar sobre o valor bruto.
     *
     * Regras (CALCDSCT.NSN):
     * - Judicial ('J'): sem teto — valor fixo ou percentual
     * - Pensão ('P'): sem teto — valor fixo ou percentual
     * - Imposto ('I'): percentual do bruto
     * - Sindical ('S'): 1% do bruto
     * - Admin ('A'): valor fixo ou percentual
     * - Contrib ('C'): alíquota progressiva
     * - Teto 30% aplica-se ao total não-judicial acumulado (BR-026)
     *
     * @param discounts   descontos cadastrados (vigentes na data de referência)
     * @param vlrBruto    valor bruto do pagamento
     * @param referenceDate data de referência para vigência
     * @return total de desconto efetivo (com teto aplicado)
     */
    public BigDecimal calculate(List<DiscountInput> discounts, BigDecimal vlrBruto, LocalDate referenceDate) {
        if (discounts == null || discounts.isEmpty()) {
            return calcContribSocial(vlrBruto);
        }

        BigDecimal maxNonJudicial = MoneyRounding.halfUp(vlrBruto.multiply(NON_JUDICIAL_CAP));
        BigDecimal judicialTotal  = BigDecimal.ZERO;
        BigDecimal nonJudicialTotal = BigDecimal.ZERO;

        // Contribuição social obrigatória é sempre somada primeiro
        nonJudicialTotal = calcContribSocial(vlrBruto);

        for (DiscountInput d : discounts) {
            if (!d.isActiveOn(referenceDate)) continue;

            BigDecimal item = calcItem(d, vlrBruto);

            if ("J".equals(d.tipoDsct()) || "P".equals(d.tipoDsct())) {
                // Judicial e Pensão: sem teto (CALCDSCT.NSN comentário "JUDICIAL NAO TEM TETO")
                judicialTotal = judicialTotal.add(item);
            } else {
                nonJudicialTotal = nonJudicialTotal.add(item);
            }
        }

        // Aplica teto nos não-judiciais (BR-026, REQ-PAY-007)
        BigDecimal cappedNonJudicial = nonJudicialTotal.min(maxNonJudicial);

        return MoneyRounding.halfUp(judicialTotal.add(cappedNonJudicial));
    }

    private BigDecimal calcItem(DiscountInput d, BigDecimal vlrBruto) {
        return switch (d.tipoDsct()) {
            case "J", "P", "A" -> d.vlrDsct() != null && d.vlrDsct().compareTo(BigDecimal.ZERO) > 0
                    ? d.vlrDsct()
                    : vlrBruto.multiply(d.pctDsct().divide(new BigDecimal("100")));
            case "I" -> vlrBruto.multiply(d.pctDsct().divide(new BigDecimal("100")));
            case "S" -> vlrBruto.multiply(new BigDecimal("0.01")); // 1% fixo (CALCDSCT.NSN#L154)
            case "C" -> calcContribSocial(vlrBruto);
            default  -> BigDecimal.ZERO;
        };
    }

    /**
     * Contribuição social progressiva por faixa (CALCDSCT.NSN#L55-L68).
     * Faixas: ≤500→3%, ≤1000→5%, ≤2000→7%, >2000→9%.
     */
    BigDecimal calcContribSocial(BigDecimal vlrBruto) {
        for (int i = 0; i < CONTRIB_FAIXA.length; i++) {
            if (vlrBruto.compareTo(CONTRIB_FAIXA[i]) <= 0) {
                return MoneyRounding.halfUp(vlrBruto.multiply(CONTRIB_ALIQ[i]));
            }
        }
        return MoneyRounding.halfUp(vlrBruto.multiply(CONTRIB_ALIQ[CONTRIB_ALIQ.length - 1]));
    }
}
