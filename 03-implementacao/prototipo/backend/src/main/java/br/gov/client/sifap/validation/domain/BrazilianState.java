/*
 * sisdnit 2.0 — Par 4 · Qualidade (DBA + QA) · Estágio 3
 * ----------------------------------------------------------------------------
 * Tabela das 27 Unidades Federativas válidas.
 *
 * Rastreabilidade:
 *   BR-040 → VALBENEF.NSN#L66-L92 (#UF-TAB) e #L143-L160 (validação)
 *   DDM    → BENEFICIARIO.UF (BG)
 */
package br.gov.client.sisdnit.validation.domain;

import java.util.Set;

public final class BrazilianState {

    public static final Set<String> CODES = Set.of(
        "AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO",
        "MA", "MG", "MS", "MT", "PA", "PB", "PE", "PI", "PR",
        "RJ", "RN", "RO", "RR", "RS", "SC", "SE", "SP", "TO"
    );

    private BrazilianState() {
    }

    public static boolean isValid(String uf) {
        return uf != null && CODES.contains(uf.trim().toUpperCase());
    }
}
