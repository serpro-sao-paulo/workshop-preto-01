package br.gov.sifap.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para CpfValidator.
 * @implements REQ-BEN-002
 */
class CpfValidatorTest {

    // --- CPFs válidos (valores reais de teste, módulo 11 correto) ---

    @Test
    void isValid_givenValidCpf_returnsTrue() {
        // CPF válido sintético: 529.982.247-25
        assertThat(CpfValidator.isValid("52998224725")).isTrue();
    }

    @Test
    void isValid_givenValidCpfWithMask_returnsTrue() {
        assertThat(CpfValidator.isValid("529.982.247-25")).isTrue();
    }

    // --- CPFs inválidos ---

    @ParameterizedTest
    @ValueSource(strings = {"00000000000", "11111111111", "22222222222", "99999999999"})
    void isValid_givenAllSameDigits_returnsFalse(String cpf) {
        // REQ-BEN-004: backdoors como 000...0 devem ser rejeitados como inválidos
        assertThat(CpfValidator.isValid(cpf)).isFalse();
    }

    @Test
    void isValid_givenWrongCheckDigit_returnsFalse() {
        assertThat(CpfValidator.isValid("52998224726")).isFalse();
    }

    @Test
    void isValid_givenShortCpf_returnsFalse() {
        assertThat(CpfValidator.isValid("1234567890")).isFalse();
    }

    @Test
    void isValid_givenNull_returnsFalse() {
        assertThat(CpfValidator.isValid(null)).isFalse();
    }

    @Test
    void isValid_givenEmpty_returnsFalse() {
        assertThat(CpfValidator.isValid("")).isFalse();
    }

    // --- Mascaramento ---

    @Test
    void mask_givenValidCpf_returnsMasked() {
        String masked = CpfValidator.mask("52998224725");
        assertThat(masked).isEqualTo("529.***.***25");
        assertThat(masked).doesNotContain("982247"); // dígitos centrais mascarados
    }

    @Test
    void mask_givenNull_returnsStars() {
        assertThat(CpfValidator.mask(null)).isEqualTo("***");
    }
}
