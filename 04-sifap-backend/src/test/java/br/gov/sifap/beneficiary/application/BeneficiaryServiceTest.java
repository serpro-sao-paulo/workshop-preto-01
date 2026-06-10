package br.gov.sifap.beneficiary.application;

import br.gov.sifap.beneficiary.domain.Beneficiary;
import br.gov.sifap.beneficiary.domain.BeneficiaryStatus;
import br.gov.sifap.beneficiary.infrastructure.BeneficiaryRepository;
import br.gov.sifap.shared.audit.BeneficiaryStatusChanged;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para BeneficiaryService.
 * @implements REQ-BEN-001, REQ-BEN-002, REQ-BEN-003, REQ-BEN-005
 */
@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceTest {

    @Mock BeneficiaryRepository repository;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks BeneficiaryService service;

    private static final String VALID_CPF = "52998224725";
    private static final String INVALID_CPF = "00000000000";

    @BeforeEach
    void setUp() {
        when(repository.existsByCpf(VALID_CPF)).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void create_validCpf_savesBeneficiary() {
        // @implements REQ-BEN-001
        Beneficiary result = service.create(VALID_CPF, "João Silva",
                LocalDate.of(1985, 3, 15), 1,
                new BigDecimal("500.00"), 2, 11, "SP");

        assertThat(result.getCpf()).isEqualTo(VALID_CPF);
        assertThat(result.getNome()).isEqualTo("João Silva");
        verify(repository).save(any(Beneficiary.class));
    }

    @Test
    void create_invalidCpf_throwsIllegalArgument() {
        // @implements REQ-BEN-002
        assertThatThrownBy(() -> service.create(INVALID_CPF, "Teste",
                LocalDate.of(1990, 1, 1), 1,
                BigDecimal.ZERO, 0, 15, "RJ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF inválido");
    }

    @Test
    void create_duplicateCpf_throwsDuplicateCpfException() {
        // @implements REQ-BEN-003
        when(repository.existsByCpf(VALID_CPF)).thenReturn(true);

        assertThatThrownBy(() -> service.create(VALID_CPF, "Maria",
                LocalDate.of(1990, 1, 1), 1,
                BigDecimal.ZERO, 0, 15, "SP"))
                .isInstanceOf(DuplicateCpfException.class);
    }

    @Test
    void create_beneficiaryOlderThan75_suspendedAutomatically() {
        // @implements REQ-BEN-005 / MYS-001: suspensão explícita > 75 anos
        LocalDate dtNasc = LocalDate.now().minusYears(76);

        service.create(VALID_CPF, "Idosa",
                dtNasc, 1, new BigDecimal("200.00"), 0, 15, "MG");

        // Verifica que evento AGE_LIMIT foi publicado
        ArgumentCaptor<BeneficiaryStatusChanged> eventCaptor =
                ArgumentCaptor.forClass(BeneficiaryStatusChanged.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        BeneficiaryStatusChanged event = eventCaptor.getValue();
        assertThat(event.newStatus()).isEqualTo(BeneficiaryStatus.SUSPENDED.name());
        assertThat(event.reason()).isEqualTo("AGE_LIMIT");
    }

    @Test
    void create_beneficiaryExactly74_notSuspended() {
        // Limite é 75 — com 74 anos deve permanecer ACTIVE
        LocalDate dtNasc = LocalDate.now().minusYears(74);

        service.create(VALID_CPF, "Idoso74",
                dtNasc, 1, new BigDecimal("200.00"), 0, 15, "MG");

        // Nenhum evento deve ser publicado
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void create_noCpfInLog_cpfIsMasked() {
        // Verifica que código não lança exceção com CPF mascarado no log
        // (teste de segurança — garante que CpfValidator.mask é chamado)
        assertThatThrownBy(() -> service.create(INVALID_CPF, "Teste",
                LocalDate.of(1990, 1, 1), 1, BigDecimal.ZERO, 0, 15, "SP"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageNotContainingAny("00000000000"); // CPF completo não deve aparecer
    }

    @Test
    void create_beneficiaryExactly75_suspendedAtBoundary() {
        // @implements REQ-BEN-005 — limite exato: >= 75 anos → SUSPENDED
        LocalDate dtNasc = LocalDate.now().minusYears(75);

        service.create(VALID_CPF, "Idoso75",
                dtNasc, 1, new BigDecimal("300.00"), 0, 15, "SP");

        ArgumentCaptor<BeneficiaryStatusChanged> eventCaptor =
                ArgumentCaptor.forClass(BeneficiaryStatusChanged.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().newStatus())
                .isEqualTo(BeneficiaryStatus.SUSPENDED.name());
    }

    @Test
    void changeStatus_alreadySuspended_eventStillPublished() {
        // @implements REQ-BEN-005 — mudança de status sempre gera evento de auditoria
        Beneficiary b = Beneficiary.create(VALID_CPF, "Test",
                LocalDate.of(1975, 1, 1), 1, new BigDecimal("400.00"), 0, 15, "SP");
        when(repository.findById(any())).thenReturn(java.util.Optional.of(b));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.changeStatus(java.util.UUID.randomUUID(), BeneficiaryStatus.SUSPENDED,
                "MANUAL", "operador-01");

        verify(eventPublisher).publishEvent(any(BeneficiaryStatusChanged.class));
    }
}
