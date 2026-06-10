package br.gov.sifap.beneficiary.domain;

/**
 * Máquina de estados do beneficiário.
 * Legado: campo STATUS (A1) em BENEFICIARIO.ddm.
 * A=Active I=Inactive S=Suspended C=Cancelled D=Discharged
 *
 * REQ-BEN-005: suspensão por idade >75 agora é estado explícito com motivo.
 */
public enum BeneficiaryStatus {
    /** Beneficiário ativo — elegível para pagamento. Legado: 'A' */
    ACTIVE,
    /** Suspenso temporariamente (incluindo >75 anos — MYS-001). Legado: 'S' */
    SUSPENDED,
    /** Cancelado definitivamente. Legado: 'C' */
    CANCELLED,
    /** Inativo (desligado voluntariamente ou por prazo). Legado: 'I' */
    INACTIVE,
    /** Desligado (fallecimento ou saída definitiva). Legado: 'D' */
    DISCHARGED
}
