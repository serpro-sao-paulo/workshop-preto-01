/*
 * sisdnit 2.0 — Par 4 · Qualidade (DBA + QA) · Estágio 3
 * ----------------------------------------------------------------------------
 * Situação do beneficiário (domínio fechado).
 *
 * Rastreabilidade:
 *   BR-040 → VALBENEF.NSN#L164-L166 (STATUS aceita A/S/C/I/D)
 *   DDM    → BENEFICIARIO.SIT-BENEFICIARIO (CE)
 */
package br.gov.client.sisdnit.validation.domain;

public enum BeneficiaryStatus {
    ATIVO('A'),
    SUSPENSO('S'),
    CANCELADO('C'),
    INATIVO('I'),
    DESLIGADO('D');

    private final char code;

    BeneficiaryStatus(char code) {
        this.code = code;
    }

    public char code() {
        return code;
    }

    /** Verdadeiro se o caractere pertence ao domínio fechado A/S/C/I/D. */
    public static boolean isValidCode(char code) {
        for (BeneficiaryStatus s : values()) {
            if (s.code == code) {
                return true;
            }
        }
        return false;
    }
}
