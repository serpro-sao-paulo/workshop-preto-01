package br.gov.sifap.program.infrastructure;

import br.gov.sifap.program.domain.SocialProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de programas sociais — leitura apenas para outros contextos.
 * @implements REQ-PRG-001
 */
public interface ProgramRepository extends JpaRepository<SocialProgram, UUID> {
    Optional<SocialProgram> findByCodPrograma(Integer codPrograma);
}
