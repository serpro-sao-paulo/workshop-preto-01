// ============================================================================
// CorrecaoRetroativaServiceTest.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Testa a correção retroativa (CALCCORR) e demonstra os mistérios MYS-011/012.
//
// Rastreabilidade: BR-036 · REQ-PAY-001 · MYS-011 · MYS-012
// ============================================================================

package br.gov.client.sisdnit.payment.application;

import br.gov.client.sisdnit.payment.domain.RetroactiveCorrectionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorrecaoRetroativaServiceTest {

    private final CorrecaoRetroativaService service = new CorrecaoRetroativaService();
    private final BigDecimal gross = new BigDecimal("1000.00");

    @Test
    @DisplayName("BR-036: aplica IPCA da competência e marca para gravação quando há diferença")
    void appliesIpcaWhenDifferencePositive() {
        // NOV/2010 → IPCA 0,0083 → fator 1,0083 → 1000,00 * 1,0083 = 1008,30.
        RetroactiveCorrectionResult result = service.correct(gross, 201011, false);

        assertEquals(new BigDecimal("1008.30"), result.correctedValue());
        assertEquals(new BigDecimal("8.30"), result.difference());
        assertTrue(result.applied());
        assertTrue(result.indexCovered());
    }

    @Test
    @DisplayName("BR-036: não corrige quando o pagamento já foi corrigido (idempotência)")
    void doesNotReapplyWhenAlreadyCorrected() {
        RetroactiveCorrectionResult result = service.correct(gross, 201011, true);

        assertFalse(result.applied());
        // o valor calculado existe, mas não deve ser gravado novamente.
        assertEquals(new BigDecimal("8.30"), result.difference());
    }

    @Test
    @DisplayName("BR-036: diferença zero (IPCA do mês = 0) não gera correção")
    void doesNotApplyWhenDifferenceIsZero() {
        // JUN/2010 → IPCA 0,0000 → fator 1,0000 → sem diferença.
        RetroactiveCorrectionResult result = service.correct(gross, 201006, false);

        assertEquals(new BigDecimal("1000.00"), result.correctedValue());
        assertEquals(new BigDecimal("0.00"), result.difference());
        assertFalse(result.applied());
        assertTrue(result.indexCovered());
    }

    @Test
    @DisplayName("BR-032: valor corrigido é truncado em 2 casas (não arredondado)")
    void truncatesCorrectedValue() {
        // JAN/2010 → IPCA 0,0075 → 1234,56 * 1,0075 = 1243,8192 → trunca para 1243,81.
        RetroactiveCorrectionResult result =
                service.correct(new BigDecimal("1234.56"), 201001, false);

        assertEquals(new BigDecimal("1243.81"), result.correctedValue());
        assertEquals(new BigDecimal("9.25"), result.difference());
    }

    @Test
    @DisplayName("MYS-011: competência fora de 2010–2012 não tem índice e não corrige (mas sinaliza)")
    void uncoveredCompetenceIsFlagged() {
        // 2005 não está na tabela IPCA carregada (MYS-011) → fator neutro 1,0.
        RetroactiveCorrectionResult result = service.correct(gross, 200501, false);

        assertEquals(new BigDecimal("1000.00"), result.correctedValue());
        assertEquals(new BigDecimal("0.00"), result.difference());
        assertFalse(result.applied());
        assertFalse(result.indexCovered()); // sisdnit 2.0 expõe o gap em vez de silenciar
    }

    @Test
    @DisplayName("CALCCORR: período inválido quando competência inicial > final")
    void rejectsInvalidPeriod() {
        assertTrue(service.isValidPeriod(201001, 201012));
        assertFalse(service.isValidPeriod(201012, 201001));
    }
}
