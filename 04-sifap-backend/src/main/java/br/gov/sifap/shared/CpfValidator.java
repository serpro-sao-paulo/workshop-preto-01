package br.gov.sifap.shared;

/**
 * Validação de CPF pelo algoritmo módulo 11.
 * Centraliza a lógica que estava duplicada em VALBENEF.NSN, VALDOCS.NSN e VALELEG.NSN.
 * Não usa Spring — testável como POJO puro.
 *
 * Cobre: REQ-BEN-002, REQ-BEN-003
 */
public final class CpfValidator {

    private CpfValidator() {}

    /**
     * Verifica se o CPF é válido pelo módulo 11.
     * Rejeita sequências triviais (000...000, 111...111 etc.).
     *
     * @param cpf string de exatamente 11 dígitos (sem máscara)
     * @return true se válido
     */
    public static boolean isValid(String cpf) {
        if (cpf == null) return false;
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() != 11) return false;
        if (digits.chars().distinct().count() == 1) return false; // 000...0, 111...1 etc.

        int d1 = calculateDigit(digits, 10);
        int d2 = calculateDigit(digits, 11);

        return digits.charAt(9) - '0' == d1
            && digits.charAt(10) - '0' == d2;
    }

    private static int calculateDigit(String digits, int weight) {
        int sum = 0;
        for (int i = 0; i < weight - 1; i++) {
            sum += (digits.charAt(i) - '0') * (weight - i);
        }
        int remainder = 11 - (sum % 11);
        return remainder >= 10 ? 0 : remainder;
    }

    /**
     * Retorna o CPF mascarado para uso em logs (nunca logar CPF completo — CONSTITUTION).
     * Ex: "12345678901" → "123.***.***01"
     */
    public static String mask(String cpf) {
        if (cpf == null || cpf.length() < 11) return "***";
        return cpf.substring(0, 3) + ".***.***" + cpf.substring(9);
    }
}
