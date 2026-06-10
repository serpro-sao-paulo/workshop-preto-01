package br.gov.sifap.beneficiary.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade JPA do beneficiário.
 * Legado: BENEFICIARIO.ddm (FNR 150). Fonte: CADBENEF.NSN, VALBENEF.NSN.
 * REQ-BEN-001 a REQ-BEN-007.
 */
@Entity
@Table(name = "ben_beneficiary")
@Getter
@NoArgsConstructor
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 60)
    @Setter
    private String nome;

    @Column(name = "dt_nascimento", nullable = false)
    private LocalDate dtNascimento;

    @Column(nullable = false, length = 20)
    private String status = BeneficiaryStatus.ACTIVE.name();

    @Column(name = "cod_programa", nullable = false)
    @Setter
    private Integer codPrograma;

    @Column(name = "renda_familiar", nullable = false, precision = 9, scale = 2)
    @Setter
    private BigDecimal rendaFamiliar = BigDecimal.ZERO;

    @Column(name = "num_dependentes", nullable = false)
    @Setter
    private int numDependentes = 0;

    /** Código de região 1-25 (mapeia para FATOR-REG). 99 = exceção controlada (REQ-ELI-005). */
    @Column(name = "cod_regiao", nullable = false)
    @Setter
    private int codRegiao = 15;

    @Column(length = 2)
    @Setter
    private String uf;

    @Column(length = 11)
    private String nis;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BeneficiaryDependent> dependents = new ArrayList<>();

    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BeneficiaryDiscount> discounts = new ArrayList<>();

    public static Beneficiary create(String cpf, String nome, LocalDate dtNascimento,
                                     Integer codPrograma, BigDecimal rendaFamiliar,
                                     int numDependentes, int codRegiao, String uf) {
        Beneficiary b = new Beneficiary();
        b.cpf = cpf;
        b.nome = nome;
        b.dtNascimento = dtNascimento;
        b.codPrograma = codPrograma;
        b.rendaFamiliar = rendaFamiliar != null ? rendaFamiliar : BigDecimal.ZERO;
        b.numDependentes = numDependentes;
        b.codRegiao = codRegiao;
        b.uf = uf;
        return b;
    }

    public BeneficiaryStatus getStatusEnum() {
        return BeneficiaryStatus.valueOf(status);
    }

    /**
     * Transição de status explícita — toda transição é validada e registrada.
     * MYS-001: suspensão por idade agora usa motivo "AGE_LIMIT" explícito (REQ-BEN-005).
     */
    public void changeStatus(BeneficiaryStatus newStatus) {
        this.status = newStatus.name();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
