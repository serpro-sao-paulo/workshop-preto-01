// ============================================================================
// CorrecaoRetroativaService.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Reproduz em Java a correção retroativa de pagamentos do legado (CALCCORR.NSN).
//
// Rastreabilidade:
//   - Regra legada: BR-036 (business-rules-catalog.md)
//   - Programa Natural: CALCCORR.NSN (correção principal #L138-L168, período #L130-L133)
//   - Mistérios: MYS-011 (tabela IPCA só 2010–2012, índice neutro silencioso)
//                MYS-012 (bloco morto "PLANO VERAO" — NÃO migrado, ver nota abaixo)
//   - Requisito moderno: REQ-PAY-001 (contexto Pagamentos)
//
// Commit:
//   feat(payment): port CALCCORR retroactive IPCA correction, surface MYS-011
//     — Implements REQ-PAY-001
// ============================================================================

package br.gov.client.sisdnit.payment.application;

import br.gov.client.sisdnit.payment.domain.IpcaIndexTable;
import br.gov.client.sisdnit.payment.domain.MoneyRounding;
import br.gov.client.sisdnit.payment.domain.RetroactiveCorrectionResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Serviço de correção retroativa, fiel ao CALCCORR.
 *
 * <p>O método principal é puro (sem I/O) para paridade com o legado; a leitura
 * e gravação em PAGAMENTO ficam na camada infrastructure.</p>
 *
 * <p><b>MYS-012:</b> o bloco comentado "PLANO VERAO" (CALCCORR.NSN#L98-L110),
 * com os fatores mágicos 2,7500 e 1,4289 da transição Cruzado→Cruzeiro
 * (1989–1991), é código morto sem procedência documentada e <b>não</b> foi
 * migrado.</p>
 */
@Service
public class CorrecaoRetroativaService {

    /**
     * Valida o período informado (CALCCORR.NSN#L130-L133): competência inicial
     * não pode ser maior que a final.
     *
     * @param competenceStart competência inicial (AAAAMM)
     * @param competenceEnd   competência final (AAAAMM)
     * @return true se o período é válido
     */
    public boolean isValidPeriod(int competenceStart, int competenceEnd) {
        return competenceStart <= competenceEnd;
    }

    /**
     * Calcula a correção retroativa de um pagamento (BR-036).
     *
     * <p>Aplica o fator IPCA da competência sobre o valor bruto, trunca em 2
     * casas (BR-032) e só marca como aplicável quando a diferença é positiva e
     * o pagamento ainda não foi corrigido — nunca reduz valores e é idempotente
     * pela flag de correção.</p>
     *
     * @param originalGross    valor bruto original do pagamento (não nulo)
     * @param competence       competência do pagamento (AAAAMM)
     * @param alreadyCorrected true se {@code IND-CORRIGIDO = 'S'} (já corrigido)
     * @return resultado da correção (valor corrigido, diferença, se aplica, se há índice)
     */
    public RetroactiveCorrectionResult correct(BigDecimal originalGross,
                                               int competence,
                                               boolean alreadyCorrected) {
        if (originalGross == null) {
            throw new IllegalArgumentException("originalGross não pode ser nulo");
        }

        boolean indexCovered = IpcaIndexTable.isCovered(competence);
        BigDecimal factor = IpcaIndexTable.correctionFactor(competence);

        // VLR-CORR = VLR-ORIG * IND-ACUM, truncado em 2 casas (CALCCORR.NSN#L150-L156).
        BigDecimal corrected = MoneyRounding.truncate(originalGross.multiply(factor));
        BigDecimal difference = corrected.subtract(originalGross);

        // Só corrige pagamentos não corrigidos e com diferença positiva (BR-036).
        boolean applied = !alreadyCorrected && difference.signum() > 0;

        return new RetroactiveCorrectionResult(corrected, difference, applied, indexCovered);
    }
}
