package br.gov.sifap.payment.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para EligibilityChecker.
 * @implements REQ-ELI-001, REQ-ELI-002, REQ-ELI-003, REQ-ELI-004, REQ-ELI-005
 */
class EligibilityCheckerTest {

    private EligibilityChecker checker;

    @BeforeEach
    void setUp() {
        checker = new EligibilityChecker();
    }

    private EligibilityParams activeEligible() {
        return new EligibilityParams("ACTIVE", new BigDecimal("500.00"), 15, false, "A");
    }

    @Test
    void check_eligibleBeneficiary_doesNotThrow() {
        // REQ-ELI-001: ativo + renda dentro + programa ativo = elegível
        assertThatNoException().isThrownBy(() -> checker.check(activeEligible()));
    }

    @Test
    void check_inactiveBeneficiary_throwsInactiveStatus() {
        // REQ-ELI-001: status SUSPENDED → inelegível
        EligibilityParams params = new EligibilityParams(
                "SUSPENDED", new BigDecimal("500.00"), 15, false, "A");
        assertThatThrownBy(() -> checker.check(params))
                .isInstanceOf(EligibilityException.class)
                .satisfies(e -> assertThat(((EligibilityException) e).getReason())
                        .isEqualTo(EligibilityException.Reason.INACTIVE_STATUS));
    }

    @Test
    void check_cancelledBeneficiary_throwsInactiveStatus() {
        // REQ-ELI-001
        EligibilityParams params = new EligibilityParams(
                "CANCELLED", new BigDecimal("500.00"), 15, false, "A");
        assertThatThrownBy(() -> checker.check(params))
                .isInstanceOf(EligibilityException.class)
                .satisfies(e -> assertThat(((EligibilityException) e).getReason())
                        .isEqualTo(EligibilityException.Reason.INACTIVE_STATUS));
    }

    @Test
    void check_incomeAboveLimit_throwsIncomeExceeded() {
        // REQ-ELI-003: renda > 3000 → inelegível
        EligibilityParams params = new EligibilityParams(
                "ACTIVE", new BigDecimal("3000.01"), 15, false, "A");
        assertThatThrownBy(() -> checker.check(params))
                .isInstanceOf(EligibilityException.class)
                .satisfies(e -> assertThat(((EligibilityException) e).getReason())
                        .isEqualTo(EligibilityException.Reason.INCOME_EXCEEDED));
    }

    @Test
    void check_incomeExactlyAtLimit_doesNotThrow() {
        // REQ-ELI-003: renda = 3000.00 → elegível
        EligibilityParams params = new EligibilityParams(
                "ACTIVE", new BigDecimal("3000.00"), 15, false, "A");
        assertThatNoException().isThrownBy(() -> checker.check(params));
    }

    @Test
    void check_region99WithoutException_throwsRegionExceptionRequired() {
        // REQ-ELI-005: região 99 sem autorização → inelegível
        // No legado: VALELEG.NSN#L72-L85 pulava silenciosamente (MYS-008)
        EligibilityParams params = new EligibilityParams(
                "ACTIVE", new BigDecimal("500.00"), 99, false, "A");
        assertThatThrownBy(() -> checker.check(params))
                .isInstanceOf(EligibilityException.class)
                .satisfies(e -> assertThat(((EligibilityException) e).getReason())
                        .isEqualTo(EligibilityException.Reason.REGION_EXCEPTION_REQUIRED));
    }

    @Test
    void check_region99WithExplicitException_doesNotThrow() {
        // REQ-ELI-005: região 99 com autorização explícita → elegível
        EligibilityParams params = new EligibilityParams(
                "ACTIVE", new BigDecimal("500.00"), 99, true, "A");
        assertThatNoException().isThrownBy(() -> checker.check(params));
    }

    @Test
    void check_inactiveProgramme_throwsProgramInactive() {
        // REQ-ELI-004: programa inativo → inelegível
        EligibilityParams params = new EligibilityParams(
                "ACTIVE", new BigDecimal("500.00"), 15, false, "I");
        assertThatThrownBy(() -> checker.check(params))
                .isInstanceOf(EligibilityException.class)
                .satisfies(e -> assertThat(((EligibilityException) e).getReason())
                        .isEqualTo(EligibilityException.Reason.PROGRAM_INACTIVE));
    }
}
