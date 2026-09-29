// ============================================================================
// BenefitFactorsTest.java — Par 4 · Qualidade (QA) · Estágio 3
// ============================================================================
// Testes de VALOR-LIMITE dos fatores do benefício (BR-022/023/024), garantindo
// paridade com as faixas do CALCBENF. Cada faixa é testada no seu limite
// superior e no primeiro valor da faixa seguinte (off-by-one que o legado
// esconde). Estilo orientado a tabela — quebra no primeiro bug de fronteira.
//
// Rastreabilidade: BR-022/BR-023/BR-024 · CALCBENF.NSN#L185-L228 · REQ-PAY-003/004/005
// ============================================================================

package br.gov.client.sisdnit.payment.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BenefitFactorsTest {

    // ------------------------------------------------------------------------
    // Fator Familiar (BR-022): 0=1,00; 1–2=+0,05/dep; 3–4=1,10+(dep−2)×0,03; 5+=1,16+(dep−4)×0,02
    // ------------------------------------------------------------------------
    @ParameterizedTest(name = "{0} dependentes → fator {1}")
    @CsvSource({
            "0, 1.0000",   // sem dependentes
            "1, 1.0500",   // faixa 1–2
            "2, 1.1000",   // limite superior da faixa 1–2
            "3, 1.1300",   // primeira da faixa 3–4 (1,10 + 1×0,03)
            "4, 1.1600",   // limite superior da faixa 3–4
            "5, 1.1800",   // primeira da faixa 5+ (1,16 + 1×0,02)
            "10, 1.2800"   // faixa 5+ estendida (1,16 + 6×0,02)
    })
    @DisplayName("BR-022: fator familiar nos limites de cada faixa de dependentes")
    void familyFactorBoundaries(int dependents, String expected) {
        assertEquals(new BigDecimal(expected), BenefitFactors.family(dependents));
    }

    @Test
    @DisplayName("BR-022: número negativo de dependentes é tratado como zero (fator 1,00)")
    void familyFactorNegativeIsTreatedAsZero() {
        assertEquals(new BigDecimal("1.0000"), BenefitFactors.family(-3));
    }

    // ------------------------------------------------------------------------
    // Fator Renda (BR-023): ≤300=1,00; ≤600=0,85; ≤1000=0,70; ≤1500=0,55; >1500=0,40
    // Testa o teto da faixa (inclusivo) e o primeiro centavo da faixa seguinte.
    // ------------------------------------------------------------------------
    @ParameterizedTest(name = "renda {0} → fator {1}")
    @CsvSource({
            "300.00, 1.0000",   // teto da faixa 1 (inclusivo)
            "300.01, 0.8500",   // primeiro centavo da faixa 2
            "600.00, 0.8500",   // teto da faixa 2
            "600.01, 0.7000",   // primeiro centavo da faixa 3
            "1000.00, 0.7000",  // teto da faixa 3
            "1000.01, 0.5500",  // primeiro centavo da faixa 4
            "1500.00, 0.5500",  // teto da faixa 4
            "1500.01, 0.4000",  // primeiro centavo da faixa 5
            "0.00, 1.0000"      // renda zero → faixa 1
    })
    @DisplayName("BR-023: fator renda nos limites de faixa (teto inclusivo)")
    void incomeFactorBoundaries(String income, String expected) {
        assertEquals(new BigDecimal(expected), BenefitFactors.income(new BigDecimal(income)));
    }

    // ------------------------------------------------------------------------
    // Fator Idade (BR-024): ≥65=1,15; ≥60=1,10; <18=1,05; demais=1,00
    // ------------------------------------------------------------------------
    @ParameterizedTest(name = "idade {0} → fator {1}")
    @CsvSource({
            "17, 1.0500",   // limite superior de <18
            "18, 1.0000",   // primeira idade da faixa adulta normal
            "59, 1.0000",   // limite inferior antes de 60
            "60, 1.1000",   // entra na faixa ≥60
            "64, 1.1000",   // limite superior de ≥60 antes de 65
            "65, 1.1500",   // entra na faixa ≥65
            "80, 1.1500"    // faixa ≥65 estendida
    })
    @DisplayName("BR-024: fator idade nos limites das faixas etárias")
    void ageFactorBoundaries(int age, String expected) {
        assertEquals(new BigDecimal(expected), BenefitFactors.age(age));
    }
}
