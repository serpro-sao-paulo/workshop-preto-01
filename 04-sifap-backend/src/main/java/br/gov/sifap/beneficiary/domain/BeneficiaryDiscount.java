package br.gov.sifap.beneficiary.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Desconto cadastrado para o beneficiário.
 * Legado: PE DESCONTOS no BENEFICIARIO.ddm (FNR 150).
 * REQ-BEN-007.
 */
@Entity
@Table(name = "ben_discount")
@Getter
@NoArgsConstructor
public class BeneficiaryDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    /** C=Contrib I=Imposto J=Judicial S=Sindical P=Pensao A=Admin */
    @Column(name = "tipo_dsct", nullable = false, length = 1)
    private String tipoDsct;

    /** Valor fixo do desconto (exclusivo com pctDsct). */
    @Column(name = "vlr_dsct", precision = 9, scale = 2)
    private BigDecimal vlrDsct;

    /** Percentual do desconto (exclusivo com vlrDsct). */
    @Column(name = "pct_dsct", precision = 5, scale = 2)
    private BigDecimal pctDsct;

    @Column(name = "dt_inicio", nullable = false)
    private LocalDate dtInicio;

    @Column(name = "dt_fim")
    private LocalDate dtFim;

    @Column(name = "num_processo", length = 20)
    private String numProcesso;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static BeneficiaryDiscount create(Beneficiary beneficiary,
                                             String tipoDsct, BigDecimal vlrDsct,
                                             BigDecimal pctDsct, LocalDate dtInicio,
                                             LocalDate dtFim, String numProcesso) {
        BeneficiaryDiscount d = new BeneficiaryDiscount();
        d.beneficiary = beneficiary;
        d.tipoDsct = tipoDsct;
        d.vlrDsct = vlrDsct;
        d.pctDsct = pctDsct;
        d.dtInicio = dtInicio;
        d.dtFim = dtFim;
        d.numProcesso = numProcesso;
        return d;
    }

    public boolean isActiveOn(LocalDate date) {
        if (dtInicio.isAfter(date)) return false;
        if (dtFim != null && dtFim.isBefore(date)) return false;
        return true;
    }
}
