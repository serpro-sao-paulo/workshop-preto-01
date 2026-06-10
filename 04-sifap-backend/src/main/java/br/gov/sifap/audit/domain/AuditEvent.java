package br.gov.sifap.audit.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de auditoria imutável — sem setters, sem update, sem delete.
 * Legado: AUDITORIA.ddm (FNR153). Corrige gap MYS-010 (ação 'EX' ocultada em BATCHCON.NSN).
 *
 * ADR-004: constraint de imutabilidade garantida por:
 *   1. Ausência de setters
 *   2. @Immutable — Hibernate não gera UPDATE
 *   3. CONSTITUTION: Flyway revoga UPDATE/DELETE do app_user (F5-T01 task)
 *
 * @implements REQ-AUD-001
 */
@Entity
@Table(name = "aud_audit_event")
@Immutable
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 60, updatable = false)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false, length = 60, updatable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100, updatable = false)
    private String aggregateId;

    @Column(nullable = false, columnDefinition = "jsonb", updatable = false)
    private String payload;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(length = 60, updatable = false)
    private String actor;

    protected AuditEvent() {}

    public static AuditEvent of(String eventType, String aggregateType,
                                String aggregateId, String payload,
                                Instant occurredAt, String actor) {
        AuditEvent e = new AuditEvent();
        e.eventType     = eventType;
        e.aggregateType = aggregateType;
        e.aggregateId   = aggregateId;
        e.payload       = payload;
        e.occurredAt    = occurredAt;
        e.actor         = actor;
        return e;
    }

    public UUID getId()            { return id; }
    public String getEventType()   { return eventType; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getPayload()     { return payload; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getActor()       { return actor; }
}
