package br.gov.sifap.beneficiary.infrastructure;

import br.gov.sifap.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para BeneficiaryController.
 * Usa Testcontainers (PostgreSQL 16) e MockMvc.
 * Flyway roda V1-V5 automaticamente no startup.
 *
 * @implements REQ-BEN-001, REQ-BEN-002, REQ-BEN-003, REQ-BEN-004
 */
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BeneficiaryControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    private static final String VALID_CPF   = "52998224725";
    private static final String VALID_CPF_2 = "71428793860";
    private static final String INVALID_CPF = "00000000000";

    private String createRequestBody(String cpf) {
        return """
                {
                  "cpf": "%s",
                  "nome": "João da Silva",
                  "dtNascimento": "1985-03-15",
                  "codPrograma": 1,
                  "rendaFamiliar": 450.00,
                  "numDependentes": 2,
                  "codRegiao": 11,
                  "uf": "SP"
                }
                """.formatted(cpf);
    }

    @Test
    @DisplayName("POST /beneficiaries — CPF válido cria beneficiário com status ACTIVE (REQ-BEN-001)")
    void create_validCpf_returns201WithActiveStatus() throws Exception {
        // @implements REQ-BEN-001
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody(VALID_CPF)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value(VALID_CPF))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @DisplayName("POST /beneficiaries — CPF duplicado retorna 409 Conflict (REQ-BEN-002)")
    void create_duplicateCpf_returns409() throws Exception {
        // @implements REQ-BEN-002
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody(VALID_CPF_2)))
                .andExpect(status().isCreated());

        // Segunda tentativa com mesmo CPF
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody(VALID_CPF_2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /beneficiaries — CPF inválido (DV errado) retorna 400 (REQ-BEN-001)")
    void create_invalidCpfDigit_returns400() throws Exception {
        // @implements REQ-BEN-001
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody("12345678900")))  // DV incorreto
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /beneficiaries — CPF de teste (all zeros) retorna 400 (REQ-BEN-004)")
    void create_testCpf_returns400() throws Exception {
        // @implements REQ-BEN-004
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody(INVALID_CPF)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /beneficiaries — campos obrigatórios ausentes retorna 400 (REQ-BEN-003)")
    void create_missingRequiredFields_returns400() throws Exception {
        // @implements REQ-BEN-003
        String bodyWithoutNome = """
                {
                  "cpf": "52998224725",
                  "dtNascimento": "1985-03-15",
                  "codPrograma": 1
                }
                """;
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithoutNome))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /beneficiaries/{id} — ID existente retorna 200 com dados corretos")
    void findById_existingBeneficiary_returns200() throws Exception {
        // @implements REQ-BEN-001
        String createResponse = mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody("11144477735")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extrai ID do response (simplificado — assume JSON linear)
        String id = createResponse.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/v1/beneficiaries/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /beneficiaries/{id} — ID inexistente retorna 404")
    void findById_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/beneficiaries/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /beneficiaries — beneficiário com data de nascimento > 75 anos fica SUSPENDED (REQ-BEN-005)")
    void create_beneficiaryOlderThan75_isImmediatelySuspended() throws Exception {
        // @implements REQ-BEN-005 — suspensão ao cadastro se já > 75 anos
        String bodyOldBeneficiary = """
                {
                  "cpf": "87748248800",
                  "nome": "Maria Idosa Santos",
                  "dtNascimento": "1940-01-01",
                  "codPrograma": 1,
                  "rendaFamiliar": 300.00,
                  "numDependentes": 0,
                  "codRegiao": 15,
                  "uf": "MG"
                }
                """;
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyOldBeneficiary))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }
}
