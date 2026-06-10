package br.gov.sifap.payment.domain;

/**
 * Exceção lançada quando um beneficiário não é elegível para pagamento.
 * Mapeia para HTTP 422 no controller.
 * REQ-ELI-001 a REQ-ELI-005.
 */
public class EligibilityException extends RuntimeException {

    public enum Reason {
        INACTIVE_STATUS,
        INCOME_EXCEEDED,
        REGION_EXCEPTION_REQUIRED,
        PROGRAM_INACTIVE
    }

    private final Reason reason;

    public EligibilityException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() { return reason; }
}
