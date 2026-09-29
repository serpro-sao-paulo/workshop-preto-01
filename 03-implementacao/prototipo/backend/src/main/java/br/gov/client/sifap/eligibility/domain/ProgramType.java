/*
 * sisdnit 2.0 — Par 4 · Qualidade (DBA + QA) · Estágio 3
 * ----------------------------------------------------------------------------
 * Tipo de programa social.
 *
 * Rastreabilidade:
 *   BR-044 → VALELEG.NSN#L167-L201 (DECIDE ON #TIPO-PROG)
 *   DDM    → PROGRAMA-SOCIAL.TIPO
 */
package br.gov.client.sisdnit.eligibility.domain;

public enum ProgramType {
    ASSISTENCIAL('A'),
    PREVIDENCIARIO('P'),
    TRABALHO('T'),
    DESCONHECIDO(' ');

    private final char code;

    ProgramType(char code) {
        this.code = code;
    }

    public char code() {
        return code;
    }

    public static ProgramType fromCode(char code) {
        for (ProgramType t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        return DESCONHECIDO;
    }
}
