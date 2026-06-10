package br.gov.sifap.payment.domain;

import br.gov.sifap.beneficiary.domain.BeneficiaryStatus;

import java.math.BigDecimal;

/**
 * Domain service puro — verifica elegibilidade antes do cálculo do pagamento.
 * Legado: VALELEG.NSN. REQ-ELI-001 a REQ-ELI-005.
 */
public class EligibilityChecker {

    /**
     * Limite de renda familiar para elegibilidade.
     * REQ-ELI-003: renda acima de R$ 3.000 → inelegível.
     */
    private static final BigDecimal INCOME_LIMIT = new BigDecimal("3000.00");

    /**
     * Verifica se o beneficiário é elegível para pagamento.
     *
     * @throws EligibilityException com motivo se não elegível
     */
    public void check(EligibilityParams params) {
        checkStatus(params.status());
        checkIncome(params.rendaFamiliar());
        checkRegion(params.codRegiao(), params.isRegionException());
        checkProgramActive(params.statusPrograma());
    }

    /**
     * REQ-ELI-001: somente beneficiários com status ACTIVE são elegíveis.
     */
    private void checkStatus(String status) {
        if (!BeneficiaryStatus.ACTIVE.name().equals(status)) {
            throw new EligibilityException(
                    "Beneficiário não ativo: status=" + status,
                    EligibilityException.Reason.INACTIVE_STATUS);
        }
    }

    /**
     * REQ-ELI-003: renda familiar acima do limite → inelegível.
     */
    private void checkIncome(BigDecimal rendaFamiliar) {
        if (rendaFamiliar.compareTo(INCOME_LIMIT) > 0) {
            throw new EligibilityException(
                    "Renda familiar acima do limite permitido",
                    EligibilityException.Reason.INCOME_EXCEEDED);
        }
    }

    /**
     * REQ-ELI-005: região 99 era bypass silencioso (MYS-008 / VALELEG.NSN#L72-L85).
     * Agora exige autorização explícita — só prossegue se isRegionException = true
     * (concedida por autoridade competente e registrada em auditoria).
     */
    private void checkRegion(int codRegiao, boolean isRegionException) {
        if (codRegiao == 99 && !isRegionException) {
            throw new EligibilityException(
                    "Região 99 requer autorização de exceção controlada",
                    EligibilityException.Reason.REGION_EXCEPTION_REQUIRED);
        }
    }

    /**
     * REQ-ELI-004: programa social deve estar ativo.
     */
    private void checkProgramActive(String statusPrograma) {
        if (!"A".equals(statusPrograma)) {
            throw new EligibilityException(
                    "Programa social inativo: status=" + statusPrograma,
                    EligibilityException.Reason.PROGRAM_INACTIVE);
        }
    }
}
