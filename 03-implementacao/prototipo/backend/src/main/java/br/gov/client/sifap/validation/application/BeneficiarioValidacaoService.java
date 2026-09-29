/*
 * sisdnit 2.0 — Par 4 · Qualidade (DBA + QA) · Estágio 3
 * ----------------------------------------------------------------------------
 * Serviço de validação cadastral do beneficiário (porta VALBENEF + VALDOCS).
 *
 * Rastreabilidade:
 *   BR-037 → VALBENEF.NSN#L188-L201  (CPF Módulo 11)
 *   BR-038 → VALBENEF.NSN#L242-L260  (data de nascimento)
 *   BR-039 → VALBENEF.NSN#L262-L273  (nome + sobrenome)
 *   BR-040 → VALBENEF.NSN#L143-L166  (UF e STATUS)
 *   BR-041 → VALDOCS.NSN#L146-L160   (RG ≥ 5 caracteres)
 *   BR-042 / MYS-014 → VALDOCS.NSN#L168-L182 (bypass por prefixo especial)
 *   REQ-VAL-CAD-01
 *
 * Decisão de modernização (DBA/QA): a versão padrão (validate) é segura —
 * usa validação estrita de data e NÃO aplica o bypass de prefixo especial.
 * validateLegacyParity reproduz o comportamento do legado para testes de
 * paridade e para documentar os mistérios MYS-014 e MYS-016.
 */
package br.gov.client.sisdnit.validation.application;

import br.gov.client.sisdnit.validation.domain.BeneficiarioCadastro;
import br.gov.client.sisdnit.validation.domain.BeneficiaryStatus;
import br.gov.client.sisdnit.validation.domain.BrazilianState;
import br.gov.client.sisdnit.validation.domain.Cpf;
import br.gov.client.sisdnit.validation.domain.LegacyDate;
import br.gov.client.sisdnit.validation.domain.SpecialCpfPrefixes;
import br.gov.client.sisdnit.validation.domain.ValidationResult;
import org.springframework.stereotype.Service;

@Service
public class BeneficiarioValidacaoService {

    private static final int MIN_RG_LENGTH = 5;

    /** Validação segura (sisdnit 2.0): data estrita, sem bypass de prefixo. */
    public ValidationResult validate(BeneficiarioCadastro dados, int anoAtual) {
        ValidationResult result = new ValidationResult();

        Cpf cpf = Cpf.of(dados.cpf());
        if (!cpf.isValid()) {
            result.addError("CPF INVALIDO - DIGITO VERIFICADOR");
        }
        if (!LegacyDate.isValidStrict(dados.dataNascimento(), anoAtual)) {
            result.addError("DATA NASCIMENTO INVALIDA");
        }
        if (!nomeValido(dados.nome())) {
            result.addError("NOME INVALIDO - DEVE TER NOME E SOBRENOME");
        }
        if (!ufValida(dados.uf())) {
            result.addError("UF INVALIDA");
        }
        if (!BeneficiaryStatus.isValidCode(dados.status())) {
            result.addError("STATUS INVALIDO");
        }
        if (!rgValido(dados.rg())) {
            result.addError("RG INVALIDO OU FORMATO INCORRETO");
        }
        return result;
    }

    /**
     * Reproduz o legado fielmente (paridade): data aceita 29/02 em qualquer ano
     * (MYS-016) e prefixos especiais de CPF zeram TODOS os erros (MYS-014).
     */
    public ValidationResult validateLegacyParity(BeneficiarioCadastro dados, int anoAtual) {
        ValidationResult result = new ValidationResult();

        Cpf cpf = Cpf.of(dados.cpf());
        boolean cpfValido = cpf.isValid() || cpf.isGovernmentTestCpf(); // MYS-017
        if (!cpfValido) {
            result.addError("CPF INVALIDO - DIGITO VERIFICADOR");
        }
        if (!LegacyDate.isValidLegacy(dados.dataNascimento(), anoAtual)) {
            result.addError("DATA NASCIMENTO INVALIDA");
        }
        if (!nomeValido(dados.nome())) {
            result.addError("NOME INVALIDO - DEVE TER NOME E SOBRENOME");
        }
        if (!ufValida(dados.uf())) {
            result.addError("UF INVALIDA");
        }
        if (!BeneficiaryStatus.isValidCode(dados.status())) {
            result.addError("STATUS INVALIDO");
        }
        if (!rgValido(dados.rg())) {
            result.addError("RG INVALIDO OU FORMATO INCORRETO");
        }

        // CHECK-DOC-ESPECIAL: prefixo especial anula tudo (MYS-014).
        if (SpecialCpfPrefixes.isSpecial(cpf)) {
            return new ValidationResult(); // zera erros — bypass do legado
        }
        return result;
    }

    /** BR-039: precisa de ao menos um espaço em posição > 1 (nome + sobrenome). */
    private boolean nomeValido(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }
        int pos = nome.indexOf(' ');
        return pos > 0; // posição 0-based > 0 equivale a #POS > 1 no legado (1-based)
    }

    private boolean ufValida(String uf) {
        // Legado só valida quando UF preenchida; em branco passa.
        if (uf == null || uf.trim().isEmpty()) {
            return true;
        }
        return BrazilianState.isValid(uf);
    }

    /** BR-041: RG preenchido e com pelo menos 5 caracteres. */
    private boolean rgValido(String rg) {
        if (rg == null) {
            return false;
        }
        String trimmed = rg.trim();
        return trimmed.length() >= MIN_RG_LENGTH;
    }
}
