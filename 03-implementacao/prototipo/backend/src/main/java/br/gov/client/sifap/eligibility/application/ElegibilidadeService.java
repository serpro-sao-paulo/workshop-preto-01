/*
 * sisdnit 2.0 — Par 4 · Qualidade (DBA + QA) · Estágio 3
 * ----------------------------------------------------------------------------
 * Serviço de validação de elegibilidade (porta VALELEG).
 *
 * Rastreabilidade:
 *   BR-043 / MYS-015 → VALELEG.NSN#L105-L111 (backdoor região 99)
 *   BR-044 → VALELEG.NSN#L116-L234 (status, idade, renda, tipo, COD-ELEG)
 *   REQ-VAL-ELEG-01
 *
 * Decisão de modernização (DBA/QA): em evaluate (sisdnit 2.0) a região 99 NÃO
 * concede elegibilidade automática — exige revisão manual (mantém os demais
 * controles). evaluateLegacyParity reproduz o backdoor para testes de paridade.
 */
package br.gov.client.sisdnit.eligibility.application;

import br.gov.client.sisdnit.eligibility.domain.EligibilityRequest;
import br.gov.client.sisdnit.validation.domain.ValidationResult;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class ElegibilidadeService {

    private static final int REGIAO_ESPECIAL = 99;
    private static final BigDecimal RENDA_LIMITE_ASSISTENCIAL = new BigDecimal("600.00");
    private static final int IDADE_MIN_PREVIDENCIARIO = 60;
    private static final int IDADE_MIN_TRABALHO = 16;
    private static final int IDADE_MAX_TRABALHO = 65;

    /** Avaliação segura (sisdnit 2.0): região 99 não burla os controles. */
    public ValidationResult evaluate(EligibilityRequest req) {
        ValidationResult result = new ValidationResult();
        if (req.codRegiao() == REGIAO_ESPECIAL) {
            result.addError("REGIAO 99 (ESPECIAL) REQUER REVISAO MANUAL DE ELEGIBILIDADE");
        }
        applyCommonRules(req, result);
        return result;
    }

    /** Reproduz o legado (paridade): região 99 → elegível imediatamente (MYS-015). */
    public ValidationResult evaluateLegacyParity(EligibilityRequest req) {
        ValidationResult result = new ValidationResult();
        if (req.codRegiao() == REGIAO_ESPECIAL) {
            return result; // ESCAPE ROUTINE — elegível sem checar nada
        }
        applyCommonRules(req, result);
        return result;
    }

    private void applyCommonRules(EligibilityRequest req, ValidationResult result) {
        verificarStatus(req, result);
        verificarFaixaEtaria(req, result);
        verificarRenda(req, result);
        verificarTipoPrograma(req, result);
        verificarElegibilidadeEspecifica(req, result);
    }

    private void verificarStatus(EligibilityRequest req, ValidationResult result) {
        char status = req.statusBeneficiario();
        if (status == 'A') {
            return;
        }
        switch (status) {
            case 'S' -> result.addError("BENEFICIARIO SUSPENSO");
            case 'C', 'D' -> result.addError("BENEFICIARIO CANCELADO/DESLIGADO");
            case 'I' -> result.addError("BENEFICIARIO INATIVO");
            default -> { /* outros valores não bloqueiam no legado */ }
        }
    }

    private void verificarFaixaEtaria(EligibilityRequest req, ValidationResult result) {
        if (req.idadeMin() > 0 && req.idade() < req.idadeMin()) {
            result.addError("IDADE INFERIOR AO MINIMO DO PROGRAMA");
        }
        if (req.idadeMax() > 0 && req.idade() > req.idadeMax()) {
            result.addError("IDADE SUPERIOR AO MAXIMO DO PROGRAMA");
        }
    }

    private void verificarRenda(EligibilityRequest req, ValidationResult result) {
        if (req.rendaMax() != null && req.rendaMax().signum() > 0
                && req.rendaFamiliar() != null
                && req.rendaFamiliar().compareTo(req.rendaMax()) > 0) {
            result.addError("RENDA FAMILIAR ACIMA DO TETO DO PROGRAMA");
        }
    }

    private void verificarTipoPrograma(EligibilityRequest req, ValidationResult result) {
        switch (req.tipoPrograma()) {
            case ASSISTENCIAL -> {
                if (req.rendaFamiliar() != null
                        && req.rendaFamiliar().compareTo(RENDA_LIMITE_ASSISTENCIAL) > 0
                        && req.numDependentes() < 1) {
                    result.addError("PROG ASSISTENCIAL: RENDA > 600 SEM DEPENDENTES");
                }
                if (req.documentosOk() != 'S') {
                    result.addError("DOCUMENTACAO INCOMPLETA");
                }
            }
            case PREVIDENCIARIO -> {
                if (req.idade() < IDADE_MIN_PREVIDENCIARIO) {
                    result.addError("PROG PREVIDENCIARIO: IDADE < 60");
                }
            }
            case TRABALHO -> {
                if (req.idade() < IDADE_MIN_TRABALHO || req.idade() > IDADE_MAX_TRABALHO) {
                    result.addError("PROG TRABALHO: IDADE FORA DA FAIXA 16-65");
                }
            }
            case DESCONHECIDO -> result.addError("TIPO PROGRAMA DESCONHECIDO");
        }
    }

    private void verificarElegibilidadeEspecifica(EligibilityRequest req, ValidationResult result) {
        String cod = req.codElegibilidade();
        if (cod == null || cod.trim().isEmpty()) {
            return;
        }
        if (cod.length() >= 1 && cod.charAt(0) == 'R' && req.nis() == 0) {
            result.addError("NIS NAO CADASTRADO");
        }
        if (cod.length() >= 2 && cod.charAt(1) == 'D' && req.numDependentes() == 0) {
            result.addError("PROGRAMA REQUER DEPENDENTES");
        }
    }
}
