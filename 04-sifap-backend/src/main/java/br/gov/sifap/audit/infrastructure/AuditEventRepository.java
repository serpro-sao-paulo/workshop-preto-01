package br.gov.sifap.audit.infrastructure;

import br.gov.sifap.audit.domain.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Repositório de auditoria — somente leitura e insert.
 * Nunca expõe delete() ou update methods.
 * @implements REQ-AUD-001
 */
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    // Paginação obrigatória — evita OOM em consultas de retenção de 10 anos (query-audit #003)
    Page<AuditEvent> findByAggregateTypeAndAggregateIdOrderByOccurredAtDesc(
            String aggregateType, String aggregateId, Pageable pageable);

    Page<AuditEvent> findByEventTypeOrderByOccurredAtDesc(String eventType, Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.aggregateType = 'Beneficiary' " +
           "AND a.aggregateId = :beneficiaryId ORDER BY a.occurredAt DESC")
    Page<AuditEvent> findByBeneficiaryId(@Param("beneficiaryId") String beneficiaryId, Pageable pageable);
}
