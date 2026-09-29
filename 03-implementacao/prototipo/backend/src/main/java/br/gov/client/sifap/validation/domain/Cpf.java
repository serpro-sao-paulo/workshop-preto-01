/*
 * sisdnit 2.0 — Par 4 · Qualidade (DBA + QA) · Estágio 3
 * ----------------------------------------------------------------------------
 * Value object de CPF com validação Módulo 11.
 *
 * Rastreabilidade:
 *   BR-037  → VALBENEF.NSN#L188-L201 (Módulo 11 + dígitos iguais)
 *   BR-003  → CADBENEF.NSN (mesmo algoritmo, reuso)
 *   MYS-017 → VALBENEF.NSN#L196-L200 (backdoor de CPF de teste prefixo 000)
 *   REQ-VAL-CPF-01
 *
 * Decisão de modernização (DBA/QA): o sistema novo NÃO aceita automaticamente
 * CPFs de teste (prefixo 000) — o backdoor do legado fica isolado em
 * isGovernmentTestCpf(), para ser liberado apenas por configuração de ambiente
 * de testes, nunca em produção (ver MYS-017).
 */
package br.gov.client.sisdnit.validation.domain;

import java.util.regex.Pattern;

/** CPF normalizado (11 dígitos) com validação de dígito verificador (Módulo 11). */
public final class Cpf {

    private static final Pattern ONLY_DIGITS = Pattern.compile("\\d{11}");

    private final String digits;

    private Cpf(String digits) {
        this.digits = digits;
    }

    /** Cria a partir de 11 dígitos (string ou numérico já com zeros à esquerda). */
    public static Cpf of(String raw) {
        String normalized = normalize(raw);
        return new Cpf(normalized);
    }

    /** Normaliza para 11 dígitos preservando zeros à esquerda (campo N11 do Adabas). */
    public static String normalize(String raw) {
        if (raw == null) {
            return "00000000000";
        }
        String onlyDigits = raw.replaceAll("\\D", "");
        if (onlyDigits.length() > 11) {
            onlyDigits = onlyDigits.substring(onlyDigits.length() - 11);
        }
        return String.format("%011d", Long.parseLong(onlyDigits.isEmpty() ? "0" : onlyDigits));
    }

    public String digits() {
        return digits;
    }

    /** Prefixo de 3 dígitos — usado pela regra de prefixos especiais (BR-042). */
    public String prefix3() {
        return digits.substring(0, 3);
    }

    /**
     * Validação Módulo 11 (BR-037). Rejeita CPF com todos os dígitos iguais,
     * inclusive 00000000000 — sem o backdoor do legado.
     */
    public boolean isValid() {
        if (!ONLY_DIGITS.matcher(digits).matches()) {
            return false;
        }
        if (allDigitsEqual()) {
            return false;
        }
        int dv1 = checkDigit(9, 10);
        if (dv1 != digitAt(9)) {
            return false;
        }
        int dv2 = checkDigit(10, 11);
        return dv2 == digitAt(10);
    }

    /**
     * Reproduz o backdoor do legado (MYS-017): CPF iniciado com 000 é "válido".
     * Existe apenas para rastrear o comportamento legado em testes de paridade.
     */
    public boolean isGovernmentTestCpf() {
        return digits.startsWith("000");
    }

    private boolean allDigitsEqual() {
        char first = digits.charAt(0);
        for (int i = 1; i < 11; i++) {
            if (digits.charAt(i) != first) {
                return false;
            }
        }
        return true;
    }

    private int digitAt(int index) {
        return digits.charAt(index) - '0';
    }

    /** Cálculo do dígito verificador: soma ponderada (peso decrescente) e resto. */
    private int checkDigit(int upTo, int startWeight) {
        int sum = 0;
        int weight = startWeight;
        for (int i = 0; i < upTo; i++) {
            sum += digitAt(i) * weight;
            weight--;
        }
        int rest = sum % 11;
        return rest < 2 ? 0 : 11 - rest;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Cpf other && digits.equals(other.digits);
    }

    @Override
    public int hashCode() {
        return digits.hashCode();
    }

    @Override
    public String toString() {
        return digits;
    }
}
