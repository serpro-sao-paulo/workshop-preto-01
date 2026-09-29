// ============================================================================
// RetroactiveCorrectionResult.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Resultado da correção retroativa de um pagamento (CALCCORR).
//
// Rastreabilidade:
//   - Regra legada: BR-036 (business-rules-catalog.md)
//   - Programa Natural: CALCCORR.NSN#L138-L168
//   - Mistério: MYS-011 (índice silenciosamente neutro fora de 2010–2012)
//   - Requisito moderno: REQ-PAY-001 (contexto Pagamentos)
// ============================================================================

package br.gov.client.sisdnit.payment.domain;

import java.math.BigDecimal;

/**
 * Saída da correção retroativa de um pagamento, espelhando os campos gravados
 * em PAGAMENTO ({@code VLR-CORRECAO}, {@code IND-CORRIGIDO}).
 *
 * @param correctedValue valor bruto corrigido pelo IPCA, truncado em 2 casas (BR-036/BR-032)
 * @param difference     diferença a pagar ({@code corrigido - original}); nunca negativa
 * @param applied        true quando a correção deve ser gravada (diferença &gt; 0 e ainda não corrigido)
 * @param indexCovered   false quando a competência está fora da tabela IPCA (MYS-011);
 *                       permite alertar o operador em vez de silenciar
 */
public record RetroactiveCorrectionResult(
        BigDecimal correctedValue,
        BigDecimal difference,
        boolean applied,
        boolean indexCovered) {
}
