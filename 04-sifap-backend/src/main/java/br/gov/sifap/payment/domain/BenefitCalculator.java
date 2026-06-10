package br.gov.sifap.payment.domain;

import br.gov.sifap.shared.MoneyRounding;

import java.math.BigDecimal;

/**
 * Domain service puro — calcula o valor bruto do benefício.
 * Sem Spring, sem JPA. Testável como POJO.
 *
 * Tradução direta de CALCBENF.NSN.
 * REQ-PAY-001, REQ-PAY-002, REQ-PAY-003, REQ-PAY-004, REQ-PAY-005.
 */
public class BenefitCalculator {

    /**
     * Fatores regionais indexados por cod_regiao (1-based).
     * Fonte: CALCBENF.NSN#L92-L120 (tabela #TAB-REG).
     * Índice 0 não usado. Índice 26-27 = reserva (1.0).
     */
    private static final BigDecimal[] REGIONAL_FACTOR = {
        BigDecimal.ZERO,        // [0] não usado
        new BigDecimal("1.3500"), // [1]  AC
        new BigDecimal("1.3200"), // [2]  AM
        new BigDecimal("1.3000"), // [3]  AP
        new BigDecimal("1.2800"), // [4]  PA
        new BigDecimal("1.3100"), // [5]  RO
        new BigDecimal("1.4000"), // [6]  MA
        new BigDecimal("1.3800"), // [7]  PI
        new BigDecimal("1.3500"), // [8]  CE
        new BigDecimal("1.3200"), // [9]  BA
        new BigDecimal("1.3600"), // [10] PE
        new BigDecimal("1.1000"), // [11] SP
        new BigDecimal("1.1200"), // [12] RJ
        new BigDecimal("1.0800"), // [13] MG
        new BigDecimal("1.0500"), // [14] ES
        new BigDecimal("1.0000"), // [15] REF
        new BigDecimal("1.0500"), // [16] PR
        new BigDecimal("1.0700"), // [17] SC
        new BigDecimal("1.0300"), // [18] RS
        new BigDecimal("1.1500"), // [19] MS
        new BigDecimal("1.2000"), // [20] MT
        new BigDecimal("1.1800"), // [21] GO
        new BigDecimal("1.2500"), // [22] TO
        new BigDecimal("1.1000"), // [23] DF
        new BigDecimal("1.2200"), // [24] RR
        new BigDecimal("1.3300"), // [25] SE
    };

    /**
     * Calcula o valor bruto do benefício mensal.
     *
     * Fórmula (CALCBENF.NSN#L222-L228):
     *   VLR = VLR_BASE × FATOR_REG × FATOR_FAM × FATOR_RND × FATOR_IDADE × (1 + FATOR_REAJ)
     *
     * Legado truncava para 2 casas. Aqui usamos HALF_UP (REQ-PAY-006 / MYS-005).
     *
     * @param params parâmetros de cálculo
     * @return CalculationResult com vlrBruto, tipoPgto, vlrAbono
     */
    public CalculationResult calculate(CalculationParams params) {
        BigDecimal fatorReg    = regionalFactor(params.codRegiao());
        BigDecimal fatorFam    = familyFactor(params.numDependentes());
        BigDecimal fatorRnd    = incomeFactor(params.rendaFamiliar());
        BigDecimal fatorIdade  = ageFactor(params.idadeAnos());

        BigDecimal vlrBenf = params.vlrBase()
                .multiply(fatorReg)
                .multiply(fatorFam)
                .multiply(fatorRnd)
                .multiply(fatorIdade)
                .multiply(BigDecimal.ONE.add(params.fatorReajuste()));

        vlrBenf = MoneyRounding.halfUp(vlrBenf);

        // Dezembro — 13º e abono (CALCBENF.NSN#L237-L260)
        if (params.mes() == 12) {
            return decemberBonus(params, vlrBenf, fatorReg, fatorIdade);
        }

        return new CalculationResult(vlrBenf, "N", BigDecimal.ZERO);
    }

    /**
     * Cálculo diferenciado para dezembro: 13º + abono natalino.
     * CALCBENF.NSN#L237-L260 — BR-023/024, REQ-PAY-004, REQ-PAY-005.
     *
     * VLR_13 = VLR_BASE × FATOR_REG × FATOR_IDADE
     * ABONO  = VLR_BENF × 0.15  (somente programas tipo 'A')
     */
    private CalculationResult decemberBonus(CalculationParams params, BigDecimal vlrBenf,
                                            BigDecimal fatorReg, BigDecimal fatorIdade) {
        BigDecimal vlr13 = MoneyRounding.halfUp(
                params.vlrBase().multiply(fatorReg).multiply(fatorIdade));

        BigDecimal bruto = vlrBenf.add(vlr13);
        BigDecimal abono = BigDecimal.ZERO;

        if ("A".equals(params.tipoProg())) {
            abono = MoneyRounding.halfUp(vlrBenf.multiply(new BigDecimal("0.15")));
            bruto = bruto.add(abono);
        }

        return new CalculationResult(MoneyRounding.halfUp(bruto), "D", abono);
    }

    /**
     * Fator regional (CALCBENF.NSN#L177-L181).
     * Região 1-25 → tabela; fora do intervalo → 1.0.
     * Região 99 é tratada pelo EligibilityChecker antes de chegar aqui (REQ-ELI-005).
     */
    static BigDecimal regionalFactor(int codRegiao) {
        if (codRegiao >= 1 && codRegiao <= 25) {
            return REGIONAL_FACTOR[codRegiao];
        }
        return BigDecimal.ONE;
    }

    /**
     * Fator familiar por número de dependentes (CALCBENF.NSN#L183-L199).
     * 0 dep: 1.00
     * 1-2:   1.00 + n × 0.05
     * 3-4:   1.10 + (n-2) × 0.03
     * 5+:    1.16 + (n-4) × 0.02
     */
    static BigDecimal familyFactor(int numDependentes) {
        if (numDependentes == 0) return BigDecimal.ONE;
        if (numDependentes <= 2) {
            return new BigDecimal("1.0000")
                    .add(new BigDecimal("0.0500").multiply(BigDecimal.valueOf(numDependentes)));
        }
        if (numDependentes <= 4) {
            return new BigDecimal("1.1000")
                    .add(new BigDecimal("0.0300").multiply(BigDecimal.valueOf(numDependentes - 2)));
        }
        return new BigDecimal("1.1600")
                .add(new BigDecimal("0.0200").multiply(BigDecimal.valueOf(numDependentes - 4)));
    }

    /**
     * Fator renda (CALCBENF.NSN subroutine DET-FAIXA-RENDA).
     * ≤300: 1.00 | ≤600: 0.85 | ≤1000: 0.70 | ≤1500: 0.55 | >1500: 0.40
     */
    static BigDecimal incomeFactor(BigDecimal rendaFamiliar) {
        if (rendaFamiliar.compareTo(new BigDecimal("300.00")) <= 0) return new BigDecimal("1.0000");
        if (rendaFamiliar.compareTo(new BigDecimal("600.00")) <= 0) return new BigDecimal("0.8500");
        if (rendaFamiliar.compareTo(new BigDecimal("1000.00")) <= 0) return new BigDecimal("0.7000");
        if (rendaFamiliar.compareTo(new BigDecimal("1500.00")) <= 0) return new BigDecimal("0.5500");
        return new BigDecimal("0.4000");
    }

    /**
     * Fator idade (CALCBENF.NSN#L202-L215).
     * ≥65: 1.15 | ≥60: 1.10 | <18: 1.05 | resto: 1.00
     */
    static BigDecimal ageFactor(int idadeAnos) {
        if (idadeAnos >= 65) return new BigDecimal("1.1500");
        if (idadeAnos >= 60) return new BigDecimal("1.1000");
        if (idadeAnos < 18)  return new BigDecimal("1.0500");
        return BigDecimal.ONE;
    }

    /** Resultado do cálculo do benefício bruto. */
    public record CalculationResult(BigDecimal vlrBruto, String tipoPgto, BigDecimal vlrAbono) {}
}
