package br.gov.sifap.shared.audit;

import br.gov.sifap.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado quando o status de um beneficiário muda.
 * Legado: BR-002 a BR-010 (transições de status). MYS-001 (suspensão >75 anos — agora explícita).
 * Cobre: REQ-BEN-005, REQ-AUD-001
 */
public record BeneficiaryStatusChanged(
        UUID beneficiaryId,
        String previousStatus,
        String newStatus,
        String reason,
        Instant occurredAt,
        String actor
) implements java.io.Serializable {

    public static BeneficiaryStatusChanged of(UUID id, String from, String to, String reason, String actor) {
        return new BeneficiaryStatusChanged(id, from, to, reason, Instant.now(), actor);
    }
}
