package br.gov.sifap.shared;

import java.time.Instant;

/**
 * Base para todos os eventos de domínio do SIFAP 2.0.
 * Publicado via ApplicationEventPublisher; consumido por @EventListener.
 *
 * @param occurredAt timestamp do evento (UTC)
 * @param actor      identificador do usuário ou sistema que gerou o evento
 */
public record DomainEvent(Instant occurredAt, String actor) {

    public DomainEvent {
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt must not be null");
    }
}
