/*
 * sisdnit 2.0 — Par 4 · Qualidade (DBA + QA) · Estágio 3
 * ----------------------------------------------------------------------------
 * Prefixos especiais de CPF do legado (VALDOCS).
 *
 * Rastreabilidade:
 *   BR-042  → VALDOCS.NSN#L49-L56 (carga) e #L168-L182 (CHECK-DOC-ESPECIAL)
 *   MYS-014 → bypass de segurança (zera erros se prefixo especial)
 *   MYS-017 → backdoor de CPF de teste prefixo 000
 *
 * Decisão de modernização: estes prefixos NÃO anulam a validação no sistema
 * novo. A classe existe só para identificar o caso e provar, em teste, que o
 * bypass legado foi removido.
 */
package br.gov.client.sisdnit.validation.domain;

import java.util.Set;

public final class SpecialCpfPrefixes {

    /** Os 8 prefixos que, no legado, anulavam toda a validação de documento. */
    public static final Set<String> PREFIXES =
        Set.of("000", "001", "002", "010", "011", "099", "100", "999");

    private SpecialCpfPrefixes() {
    }

    public static boolean isSpecial(Cpf cpf) {
        return PREFIXES.contains(cpf.prefix3());
    }
}
