package br.gov.sifap.audit.application;

import br.gov.sifap.audit.domain.AuditEvent;
import br.gov.sifap.audit.infrastructure.AuditEventRepository;
import br.gov.sifap.shared.audit.BeneficiaryStatusChanged;
import br.gov.sifap.shared.audit.PaymentCalculated;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Ouve eventos de domínio e persiste na trilha de auditoria imutável.
 * Corrige gap do legado: BATCHCON.NSN ocultava ação 'EX' (MYS-010).
 * @implements REQ-AUD-001
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditEventRepository repository;
    private final ObjectMapper objectMapper;

    @EventListener
    public void onBeneficiaryStatusChanged(BeneficiaryStatusChanged event) {
        persist("BeneficiaryStatusChanged", "Beneficiary",
                event.beneficiaryId().toString(), event, event.actor());
    }

    @EventListener
    public void onPaymentCalculated(PaymentCalculated event) {
        persist("PaymentCalculated", "Payment",
                event.paymentId().toString(), event, event.actor());
    }

    private void persist(String eventType, String aggregateType,
                         String aggregateId, Object payload, String actor) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            AuditEvent auditEvent = AuditEvent.of(
                    eventType, aggregateType, aggregateId,
                    json, java.time.Instant.now(), actor);
            repository.save(auditEvent);
        } catch (Exception e) {
            log.error("Falha ao persistir evento de auditoria: type={} aggregateId={}",
                    eventType, aggregateId, e);
        }
    }
}
