/*
 * sisdnit 2.0 — Par 4 · Qualidade (DBA + QA) · Estágio 3
 * ----------------------------------------------------------------------------
 * Resultado de validação reaproveitável pelos validadores de cadastro,
 * documento e elegibilidade. Reproduz a semântica do legado (#RESULTADO V/I +
 * lista de mensagens), mas com tipo seguro.
 *
 * Origem legada: VALBENEF.NSN / VALDOCS.NSN / VALELEG.NSN (campos #RESULTADO,
 * #MSG-ERRO, #QTD-ERROS).
 */
package br.gov.client.sisdnit.validation.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Acumula erros de validação e expõe o veredito final (V=válido / I=inválido). */
public final class ValidationResult {

    private final List<String> errors = new ArrayList<>();

    /** Registra uma falha de validação com a mensagem do legado. */
    public ValidationResult addError(String message) {
        errors.add(message);
        return this;
    }

    /** Verdadeiro quando nenhum erro foi registrado (equivale a #RESULTADO = 'V'). */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /** Letra de resultado do legado: 'V' válido, 'I' inválido. */
    public char legacyResultCode() {
        return isValid() ? 'V' : 'I';
    }

    public int errorCount() {
        return errors.size();
    }

    public List<String> errors() {
        return Collections.unmodifiableList(errors);
    }
}
