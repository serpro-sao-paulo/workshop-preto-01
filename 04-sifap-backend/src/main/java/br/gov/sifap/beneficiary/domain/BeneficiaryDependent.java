package br.gov.sifap.beneficiary.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dependente do beneficiário.
 * Legado: PE DEPENDENTES no BENEFICIARIO.ddm (FNR 150).
 * REQ-BEN-006.
 */
@Entity
@Table(name = "ben_dependent")
@Getter
@NoArgsConstructor
public class BeneficiaryDependent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(name = "dt_nascimento", nullable = false)
    private LocalDate dtNascimento;

    @Column(length = 30)
    private String parentesco;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static BeneficiaryDependent create(Beneficiary beneficiary,
                                              String nome, LocalDate dtNascimento,
                                              String parentesco) {
        BeneficiaryDependent d = new BeneficiaryDependent();
        d.beneficiary = beneficiary;
        d.nome = nome;
        d.dtNascimento = dtNascimento;
        d.parentesco = parentesco;
        return d;
    }
}
