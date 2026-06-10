package br.gov.sifap.beneficiary.infrastructure;

import br.gov.sifap.beneficiary.domain.Beneficiary;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório JPA do beneficiário.
 * @implements REQ-BEN-001
 */
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {

    /** Busca leve: apenas dados do beneficiário, sem collections (LAZY). Use quando discount/dependents não são necessários. */
    Optional<Beneficiary> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    /**
     * Busca com JOIN FETCH em dependents e discounts.
     * Usar quando o cálculo de desconto precisar das duas coleções (evita N+1).
     * query-audit #001 — P2 action item.
     */
    @EntityGraph(attributePaths = {"dependents", "discounts"})
    Optional<Beneficiary> findWithDetailsByCpf(String cpf);
}
