/*
 * sisdnit 2.0 — Par 4 · Qualidade (DBA + QA) · Estágio 3
 * ----------------------------------------------------------------------------
 * Testes BDD de elegibilidade (BR-043/BR-044) e do backdoor MYS-015.
 * Estilo Given/When/Then.
 */
package br.gov.client.sisdnit.eligibility.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.gov.client.sisdnit.eligibility.domain.EligibilityRequest;
import br.gov.client.sisdnit.eligibility.domain.ProgramType;
import br.gov.client.sisdnit.validation.domain.ValidationResult;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ElegibilidadeServiceTest {

    private final ElegibilidadeService service = new ElegibilidadeService();

    /** Beneficiário ativo, programa de trabalho (16-65), sem restrições adicionais. */
    private EligibilityRequest elegivelBase() {
        return new EligibilityRequest(
            1, 'A', 40, new BigDecimal("500.00"), 1, 12345678901L, 'S',
            ProgramType.TRABALHO, BigDecimal.ZERO, 0, 0, "");
    }

    @Test
    @DisplayName("BR-044: beneficiário ativo dentro das regras é elegível")
    void beneficiarioAtivoDentroDasRegrasEhElegivel() {
        ValidationResult result = service.evaluate(elegivelBase());

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("BR-044: beneficiário suspenso é inelegível")
    void beneficiarioSuspensoEhInelegivel() {
        EligibilityRequest req = new EligibilityRequest(
            1, 'S', 40, new BigDecimal("500.00"), 1, 12345678901L, 'S',
            ProgramType.TRABALHO, BigDecimal.ZERO, 0, 0, "");

        ValidationResult result = service.evaluate(req);

        assertFalse(result.isValid());
        assertTrue(result.errors().contains("BENEFICIARIO SUSPENSO"));
    }

    @Test
    @DisplayName("BR-044: programa previdenciário exige idade ≥ 60")
    void previdenciarioExigeIdadeMinima() {
        EligibilityRequest jovem = new EligibilityRequest(
            1, 'A', 59, new BigDecimal("500.00"), 1, 12345678901L, 'S',
            ProgramType.PREVIDENCIARIO, BigDecimal.ZERO, 0, 0, "");

        ValidationResult result = service.evaluate(jovem);

        assertTrue(result.errors().contains("PROG PREVIDENCIARIO: IDADE < 60"));
    }

    @Test
    @DisplayName("BR-044: assistencial com renda > 600 sem dependentes é inelegível")
    void assistencialRendaAltaSemDependentesEhInelegivel() {
        EligibilityRequest req = new EligibilityRequest(
            1, 'A', 40, new BigDecimal("700.00"), 0, 12345678901L, 'S',
            ProgramType.ASSISTENCIAL, BigDecimal.ZERO, 0, 0, "");

        ValidationResult result = service.evaluate(req);

        assertTrue(result.errors().contains("PROG ASSISTENCIAL: RENDA > 600 SEM DEPENDENTES"));
    }

    @Test
    @DisplayName("BR-044: renda acima do teto do programa reprova")
    void rendaAcimaDoTetoReprova() {
        EligibilityRequest req = new EligibilityRequest(
            1, 'A', 40, new BigDecimal("2000.00"), 1, 12345678901L, 'S',
            ProgramType.TRABALHO, new BigDecimal("1000.00"), 0, 0, "");

        ValidationResult result = service.evaluate(req);

        assertTrue(result.errors().contains("RENDA FAMILIAR ACIMA DO TETO DO PROGRAMA"));
    }

    @Test
    @DisplayName("BR-044: COD-ELEG 'R' exige NIS cadastrado")
    void codElegRExigeNis() {
        EligibilityRequest semNis = new EligibilityRequest(
            1, 'A', 40, new BigDecimal("500.00"), 1, 0L, 'S',
            ProgramType.TRABALHO, BigDecimal.ZERO, 0, 0, "R ");

        ValidationResult result = service.evaluate(semNis);

        assertTrue(result.errors().contains("NIS NAO CADASTRADO"));
    }

    @Test
    @DisplayName("MYS-015: região 99 é elegível no legado mas exige revisão no sistema novo")
    void regiao99DivergeEntreLegadoESistemaNovo() {
        // Beneficiário suspenso (inelegível pelas regras comuns) + região 99.
        EligibilityRequest req = new EligibilityRequest(
            99, 'S', 40, new BigDecimal("9999.00"), 0, 0L, 'N',
            ProgramType.ASSISTENCIAL, new BigDecimal("100.00"), 0, 0, "");

        ValidationResult legado = service.evaluateLegacyParity(req);
        ValidationResult moderno = service.evaluate(req);

        assertTrue(legado.isValid()); // backdoor: elegível sem checar nada
        assertFalse(moderno.isValid());
        assertTrue(moderno.errors().contains(
            "REGIAO 99 (ESPECIAL) REQUER REVISAO MANUAL DE ELEGIBILIDADE"));
    }
}
