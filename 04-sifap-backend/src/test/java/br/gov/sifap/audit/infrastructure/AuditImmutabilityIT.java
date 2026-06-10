package br.gov.sifap.audit.infrastructure;

import br.gov.sifap.AbstractIntegrationTest;
import br.gov.sifap.audit.domain.AuditEvent;
import br.gov.sifap.beneficiary.application.BeneficiaryService;
import br.gov.sifap.beneficiary.domain.Beneficiary;
import br.gov.sifap.beneficiary.domain.BeneficiaryStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de integração para imutabilidade da trilha de auditoria.
 *
 * Valida:
 * 1. Eventos de auditoria são criados quando o status do beneficiário muda.
 * 2. UPDATE em aud_audit_event é rejeitado pelo trigger trg_aud_no_update (V5).
 * 3. DELETE em aud_audit_event é rejeitado pelo trigger trg_aud_no_delete (V5).
 * 4. Consulta paginada funciona (não carrega 10 anos de log para heap).
 *
 * Mandato legal: IN-TCU 63/2010 + Art. 14 Lei 8.159 (retenção 10 anos).
 *
 * @implements REQ-AUD-001
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuditImmutabilityIT extends AbstractIntegrationTest {

    @Autowired
    BeneficiaryService beneficiaryService;

    @Autowired
    AuditEventRepository auditEventRepository;

    @Autowired
    JdbcTemplate jdbc;

    private static final String CPF_AUDIT_TEST = "71428793860";

    private Beneficiary createTestBeneficiary(String cpf) {
        return beneficiaryService.create(
                cpf, "Auditoria Teste IT",
                LocalDate.of(1975, 6, 10),
                1,
                new BigDecimal("600.00"),
                1, 15, "RJ");
    }

    @Test
    @DisplayName("Mudança de status publica evento de auditoria no banco (REQ-AUD-001)")
    void changeStatus_publishesAuditEventToDB() {
        // @implements REQ-AUD-001
        Beneficiary b = createTestBeneficiary(CPF_AUDIT_TEST);
        String beneficiaryId = b.getId().toString();

        // Act: muda status para SUSPENDED
        beneficiaryService.changeStatus(b.getId(), BeneficiaryStatus.SUSPENDED,
                "MANUAL_REVIEW", "operador-teste");

        // Assert: deve existir ao menos 1 evento de auditoria para este beneficiário
        Page<AuditEvent> events = auditEventRepository.findByBeneficiaryId(
                beneficiaryId,
                PageRequest.of(0, 10, Sort.by("occurredAt").descending()));

        assertThat(events.getContent()).isNotEmpty();
        AuditEvent latest = events.getContent().get(0);
        assertThat(latest.getAggregateType()).isEqualTo("Beneficiary");
        assertThat(latest.getAggregateId()).isEqualTo(beneficiaryId);
    }

    @Test
    @DisplayName("UPDATE em aud_audit_event é bloqueado pelo trigger (REQ-AUD-001 — imutabilidade)")
    void updateAuditEvent_isRejectedByTrigger() {
        // @implements REQ-AUD-001 — teste do trigger trg_aud_no_update (V5)
        Beneficiary b = createTestBeneficiary("11144477735");
        beneficiaryService.changeStatus(b.getId(), BeneficiaryStatus.INACTIVE,
                "TEST", "system");

        // Pega o ID de um evento existente
        Page<AuditEvent> events = auditEventRepository.findByBeneficiaryId(
                b.getId().toString(),
                PageRequest.of(0, 1));

        assertThat(events.getContent()).isNotEmpty();
        UUID eventId = events.getContent().get(0).getId();

        // Attempt UPDATE directly via JDBC — must be rejected by trigger
        assertThatThrownBy(() ->
                jdbc.update(
                        "UPDATE aud_audit_event SET event_type = 'TAMPERED' WHERE id = ?",
                        eventId))
                .hasMessageContaining("immutable");
    }

    @Test
    @DisplayName("DELETE em aud_audit_event é bloqueado pelo trigger (REQ-AUD-001 — imutabilidade)")
    void deleteAuditEvent_isRejectedByTrigger() {
        // @implements REQ-AUD-001 — teste do trigger trg_aud_no_delete (V5)
        Beneficiary b = createTestBeneficiary("87748248800");
        beneficiaryService.changeStatus(b.getId(), BeneficiaryStatus.CANCELLED,
                "TEST_DELETE", "system");

        Page<AuditEvent> events = auditEventRepository.findByBeneficiaryId(
                b.getId().toString(),
                PageRequest.of(0, 1));

        assertThat(events.getContent()).isNotEmpty();
        UUID eventId = events.getContent().get(0).getId();

        // Attempt DELETE directly via JDBC — must be rejected by trigger
        assertThatThrownBy(() ->
                jdbc.update("DELETE FROM aud_audit_event WHERE id = ?", eventId))
                .hasMessageContaining("immutable");
    }

    @Test
    @DisplayName("Consulta paginada de eventos retorna Page — não carrega tudo em memória (REQ-AUD-001)")
    void findByBeneficiaryId_returnsPage_notList() {
        // @implements REQ-AUD-001 — query-audit #003 fix: Pageable obrigatório
        Beneficiary b = createTestBeneficiary("52998224725");

        // Gera múltiplos eventos
        beneficiaryService.changeStatus(b.getId(), BeneficiaryStatus.SUSPENDED,
                "REASON_1", "system");
        beneficiaryService.changeStatus(b.getId(), BeneficiaryStatus.ACTIVE,
                "REASON_2", "system");

        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by("occurredAt").descending());
        Page<AuditEvent> page = auditEventRepository.findByBeneficiaryId(
                b.getId().toString(), pageRequest);

        assertThat(page.getContent()).hasSize(1);       // page size respeitado
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2); // há mais eventos
        assertThat(page.hasNext()).isTrue();             // paginação funciona
    }

    @Test
    @DisplayName("CPF alterado após cadastro é bloqueado pelo trigger ben_prevent_cpf_change (REQ-BEN-002)")
    void updateCpf_isRejectedByTrigger() {
        // @implements REQ-BEN-002 — trigger V5
        Beneficiary b = createTestBeneficiary("85893612600");

        assertThatThrownBy(() ->
                jdbc.update(
                        "UPDATE ben_beneficiary SET cpf = '99999999999' WHERE id = ?",
                        b.getId()))
                .hasMessageContaining("CPF cannot be changed");
    }
}
