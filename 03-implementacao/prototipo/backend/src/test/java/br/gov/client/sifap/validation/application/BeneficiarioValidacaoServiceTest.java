/*
 * sisdnit 2.0 — Par 4 · Qualidade (DBA + QA) · Estágio 3
 * ----------------------------------------------------------------------------
 * Testes BDD de validação cadastral (BR-037..042) e dos mistérios MYS-014/016/017.
 * Estilo Given/When/Then. CPF válido de teste: 11144477735.
 */
package br.gov.client.sisdnit.validation.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.gov.client.sisdnit.validation.domain.BeneficiarioCadastro;
import br.gov.client.sisdnit.validation.domain.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BeneficiarioValidacaoServiceTest {

    private static final int ANO_ATUAL = 2026;
    private final BeneficiarioValidacaoService service = new BeneficiarioValidacaoService();

    private BeneficiarioCadastro cadastroValido() {
        // CPF válido, nome+sobrenome, data válida, UF válida, status A, RG ≥ 5.
        return new BeneficiarioCadastro("11144477735", "MARIA SILVA", 19800115, "SP", 'A', "1234567");
    }

    @Test
    @DisplayName("BR-037..041: cadastro completo e correto é válido")
    void cadastroValidoPassaEmTodasAsRegras() {
        ValidationResult result = service.validate(cadastroValido(), ANO_ATUAL);

        assertTrue(result.isValid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    @DisplayName("BR-037: CPF com dígito verificador errado é rejeitado")
    void cpfInvalidoEhRejeitado() {
        BeneficiarioCadastro dados = new BeneficiarioCadastro(
            "11144477700", "MARIA SILVA", 19800115, "SP", 'A', "1234567");

        ValidationResult result = service.validate(dados, ANO_ATUAL);

        assertFalse(result.isValid());
        assertTrue(result.errors().contains("CPF INVALIDO - DIGITO VERIFICADOR"));
    }

    @Test
    @DisplayName("BR-039: nome sem sobrenome é rejeitado")
    void nomeSemSobrenomeEhRejeitado() {
        BeneficiarioCadastro dados = new BeneficiarioCadastro(
            "11144477735", "MARIA", 19800115, "SP", 'A', "1234567");

        ValidationResult result = service.validate(dados, ANO_ATUAL);

        assertTrue(result.errors().contains("NOME INVALIDO - DEVE TER NOME E SOBRENOME"));
    }

    @Test
    @DisplayName("BR-040: UF fora das 27 unidades é rejeitada")
    void ufInvalidaEhRejeitada() {
        BeneficiarioCadastro dados = new BeneficiarioCadastro(
            "11144477735", "MARIA SILVA", 19800115, "XX", 'A', "1234567");

        ValidationResult result = service.validate(dados, ANO_ATUAL);

        assertTrue(result.errors().contains("UF INVALIDA"));
    }

    @Test
    @DisplayName("BR-040: STATUS fora de A/S/C/I/D é rejeitado")
    void statusInvalidoEhRejeitado() {
        BeneficiarioCadastro dados = new BeneficiarioCadastro(
            "11144477735", "MARIA SILVA", 19800115, "SP", 'Z', "1234567");

        ValidationResult result = service.validate(dados, ANO_ATUAL);

        assertTrue(result.errors().contains("STATUS INVALIDO"));
    }

    @Test
    @DisplayName("BR-041: RG com menos de 5 caracteres é rejeitado")
    void rgCurtoEhRejeitado() {
        BeneficiarioCadastro dados = new BeneficiarioCadastro(
            "11144477735", "MARIA SILVA", 19800115, "SP", 'A', "123");

        ValidationResult result = service.validate(dados, ANO_ATUAL);

        assertTrue(result.errors().contains("RG INVALIDO OU FORMATO INCORRETO"));
    }

    @Test
    @DisplayName("MYS-016: 29/02 em ano não bissexto — estrito rejeita, legado aceita")
    void fevereiro29DivergeEntreEstritoELegado() {
        // 1901 não é bissexto.
        BeneficiarioCadastro dados = new BeneficiarioCadastro(
            "11144477735", "MARIA SILVA", 19010229, "SP", 'A', "1234567");

        ValidationResult estrito = service.validate(dados, ANO_ATUAL);
        ValidationResult legado = service.validateLegacyParity(dados, ANO_ATUAL);

        assertTrue(estrito.errors().contains("DATA NASCIMENTO INVALIDA"));
        assertTrue(legado.isValid()); // legado aceita 29/02 sempre
    }

    @Test
    @DisplayName("MYS-014: prefixo especial de CPF burla a validação só no legado")
    void prefixoEspecialBurlaValidacaoApenasNoLegado() {
        // Prefixo 999 (especial), CPF inválido e nome ruim: estrito reprova, legado libera.
        BeneficiarioCadastro dados = new BeneficiarioCadastro(
            "99912345678", "X", 19800115, "SP", 'A', "1234567");

        ValidationResult estrito = service.validate(dados, ANO_ATUAL);
        ValidationResult legado = service.validateLegacyParity(dados, ANO_ATUAL);

        assertFalse(estrito.isValid());     // sistema novo continua validando
        assertTrue(legado.isValid());       // legado zera todos os erros (bypass)
    }

    @Test
    @DisplayName("MYS-017: CPF de teste 000... é inválido no sistema novo")
    void cpfDeTesteEhInvalidoNoSistemaNovo() {
        BeneficiarioCadastro dados = new BeneficiarioCadastro(
            "00000000000", "MARIA SILVA", 19800115, "SP", 'A', "1234567");

        ValidationResult estrito = service.validate(dados, ANO_ATUAL);

        assertTrue(estrito.errors().contains("CPF INVALIDO - DIGITO VERIFICADOR"));
    }
}
