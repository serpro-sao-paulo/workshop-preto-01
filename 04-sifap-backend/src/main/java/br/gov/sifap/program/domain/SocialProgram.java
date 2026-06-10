package br.gov.sifap.program.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Programa social — parâmetros de cálculo.
 * Legado: PROGRAMA-SOCIAL.ddm (FNR 151). Fonte: CADPROG.NSN.
 *
 * vlrBase substitui a constante 0.347215 hardcoded em CALCBENF.NSN (BR-016, REQ-PRG-001).
 * @implements REQ-PRG-001
 */
@Entity
@Table(name = "prg_social_program")
@Getter
@NoArgsConstructor
public class SocialProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cod_programa", nullable = false, unique = true)
    private Integer codPrograma;

    @Column(nullable = false, length = 100)
    private String nome;

    /** A=elegível para abono natalino; B/C/D=outros tipos (CALCBENF.NSN#L249). */
    @Column(nullable = false, length = 1)
    private String tipo;

    /**
     * Valor-base do programa. Substitui constante 0.347215 de CALCBENF.NSN#L12 (BR-016).
     * REQ-PRG-001: deve ser configurável por programa.
     */
    @Column(name = "vlr_base", nullable = false, precision = 9, scale = 2)
    private BigDecimal vlrBase;

    /** Fator de reajuste. Ex: 0.0250 = 2.5%. CALCBENF.NSN#L230. */
    @Column(name = "fator_reajuste", nullable = false, precision = 7, scale = 4)
    private BigDecimal fatorReajuste = BigDecimal.ZERO;

    @Column(name = "status_prog", nullable = false, length = 1)
    private String statusProg = "A";

    @Column(name = "dt_vigencia_ini", nullable = false)
    private LocalDate dtVigenciaIni;

    @Column(name = "dt_vigencia_fim")
    private LocalDate dtVigenciaFim;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public boolean isActive() {
        return "A".equals(statusProg);
    }
}
