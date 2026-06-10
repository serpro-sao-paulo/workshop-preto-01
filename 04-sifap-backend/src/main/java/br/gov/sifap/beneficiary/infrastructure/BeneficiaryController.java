package br.gov.sifap.beneficiary.infrastructure;

import br.gov.sifap.beneficiary.application.BeneficiaryService;
import br.gov.sifap.beneficiary.domain.Beneficiary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * REST adapter para o contexto beneficiary.
 * @implements REQ-BEN-001, REQ-BEN-002, REQ-BEN-003
 */
@RestController
@RequestMapping("/api/v1/beneficiaries")
@RequiredArgsConstructor
@Tag(name = "Beneficiaries", description = "Cadastro e consulta de beneficiários")
public class BeneficiaryController {

    private final BeneficiaryService service;

    @Operation(summary = "Cadastra um novo beneficiário")
    @ApiResponse(responseCode = "201", description = "Beneficiário criado")
    @ApiResponse(responseCode = "400", description = "CPF inválido")
    @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
    @PostMapping
    public ResponseEntity<BeneficiaryResponse> create(@Valid @RequestBody CreateBeneficiaryRequest request) {
        Beneficiary created = service.create(
                request.cpf(), request.nome(), request.dtNascimento(),
                request.codPrograma(), request.rendaFamiliar(),
                request.numDependentes(), request.codRegiao(), request.uf());
        return ResponseEntity.status(HttpStatus.CREATED).body(BeneficiaryResponse.from(created));
    }

    @Operation(summary = "Busca beneficiário por ID")
    @ApiResponse(responseCode = "200", description = "Beneficiário encontrado")
    @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> findById(@PathVariable UUID id) {
        Beneficiary beneficiary = service.findByIdOrThrow(id);
        return ResponseEntity.ok(BeneficiaryResponse.from(beneficiary));
    }

    // ─── DTOs ────────────────────────────────────────────────────────────────

    public record CreateBeneficiaryRequest(
            @NotBlank String cpf,
            @NotBlank String nome,
            @NotNull LocalDate dtNascimento,
            @NotNull Integer codPrograma,
            BigDecimal rendaFamiliar,
            int numDependentes,
            int codRegiao,
            String uf
    ) {}

    public record BeneficiaryResponse(
            UUID id,
            String cpf,
            String nome,
            LocalDate dtNascimento,
            String status,
            Integer codPrograma
    ) {
        static BeneficiaryResponse from(Beneficiary b) {
            return new BeneficiaryResponse(
                    b.getId(), b.getCpf(), b.getNome(),
                    b.getDtNascimento(), b.getStatus(), b.getCodPrograma());
        }
    }
}
