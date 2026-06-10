package br.gov.sifap.beneficiary.application;

import br.gov.sifap.beneficiary.domain.Beneficiary;
import br.gov.sifap.beneficiary.domain.BeneficiaryStatus;
import br.gov.sifap.beneficiary.infrastructure.BeneficiaryRepository;
import br.gov.sifap.shared.CpfValidator;
import br.gov.sifap.shared.audit.BeneficiaryStatusChanged;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço de domínio do beneficiário.
 * Toda lógica de negócio de cadastro e status reside aqui.
 * @Transactional somente nesta camada (ADR-004).
 *
 * @implements REQ-BEN-001, REQ-BEN-002, REQ-BEN-003, REQ-BEN-004, REQ-BEN-005
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BeneficiaryService {

    private static final int AGE_SUSPENSION_THRESHOLD = 75;

    private final BeneficiaryRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Cria um novo beneficiário após validar CPF e unicidade.
     * @throws IllegalArgumentException se CPF for inválido (REQ-BEN-002)
     * @throws DuplicateCpfException    se CPF já cadastrado (REQ-BEN-003)
     */
    @Transactional
    public Beneficiary create(String cpf, String nome, LocalDate dtNascimento,
                              Integer codPrograma, BigDecimal rendaFamiliar,
                              int numDependentes, int codRegiao, String uf) {
        if (!CpfValidator.isValid(cpf)) {
            log.warn("CPF inválido recebido: {}", CpfValidator.mask(cpf));
            throw new IllegalArgumentException("CPF inválido: " + CpfValidator.mask(cpf));
        }
        if (repository.existsByCpf(cpf)) {
            throw new DuplicateCpfException(cpf);
        }

        Beneficiary beneficiary = Beneficiary.create(
                cpf, nome, dtNascimento, codPrograma,
                rendaFamiliar, numDependentes, codRegiao, uf);

        Beneficiary saved = repository.save(beneficiary);

        // Verifica imediatamente se já deve ser suspenso por idade (REQ-BEN-005 / MYS-001)
        checkAgeSuspension(saved);

        log.info("Beneficiário criado: id={}", saved.getId());
        return saved;
    }

    /**
     * Altera o status do beneficiário com motivo explícito e publica evento de auditoria.
     * REQ-BEN-005, REQ-AUD-001.
     */
    @Transactional
    public Beneficiary changeStatus(UUID id, BeneficiaryStatus newStatus, String reason, String actor) {
        Beneficiary beneficiary = findByIdOrThrow(id);
        String previousStatus = beneficiary.getStatus();

        beneficiary.changeStatus(newStatus);
        Beneficiary saved = repository.save(beneficiary);

        eventPublisher.publishEvent(
                BeneficiaryStatusChanged.of(id, previousStatus, newStatus.name(), reason, actor));

        log.info("Status do beneficiário {} alterado: {} → {} (motivo: {})",
                id, previousStatus, newStatus, reason);
        return saved;
    }

    public Optional<Beneficiary> findByCpf(String cpf) {
        return repository.findByCpf(cpf);
    }

    public Beneficiary findByIdOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new BeneficiaryNotFoundException(id));
    }

    /**
     * REQ-BEN-005 / MYS-001: suspensão explícita para beneficiários com 75+ anos.
     * No legado (BR-006) era feita silenciosamente em VALBENEF.NSN — agora é auditável.
     */
    void checkAgeSuspension(Beneficiary beneficiary) {
        if (beneficiary.getStatusEnum() != BeneficiaryStatus.ACTIVE) return;
        int age = Period.between(beneficiary.getDtNascimento(), LocalDate.now()).getYears();
        if (age >= AGE_SUSPENSION_THRESHOLD) {
            beneficiary.changeStatus(BeneficiaryStatus.SUSPENDED);
            repository.save(beneficiary);
            eventPublisher.publishEvent(BeneficiaryStatusChanged.of(
                    beneficiary.getId(),
                    BeneficiaryStatus.ACTIVE.name(),
                    BeneficiaryStatus.SUSPENDED.name(),
                    "AGE_LIMIT",
                    "SYSTEM"));
            log.info("Beneficiário {} suspenso por limite de idade ({}a) — REQ-BEN-005",
                    beneficiary.getId(), age);
        }
    }
}
