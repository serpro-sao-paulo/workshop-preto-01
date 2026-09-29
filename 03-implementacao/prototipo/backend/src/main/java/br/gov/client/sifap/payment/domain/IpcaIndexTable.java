// ============================================================================
// IpcaIndexTable.java — Par 3 · Implementação (Estágio 3)
// ============================================================================
// Tabela de índices IPCA mensais usada na correção retroativa (CALCCORR).
//
// Rastreabilidade:
//   - Regra legada: BR-036 (business-rules-catalog.md)
//   - Programa Natural: CALCCORR.NSN#L52-L96 (carga #IPCA-ANO) e #L186-L196 (CALC-INDICE-ACUM)
//   - Mistério: MYS-011 (tabela só carregada para 2010–2012, apesar do
//     comentário "ULTIMA CARGA: 2014"; anos fora da tabela rendem índice 1,0)
//   - Requisito moderno: REQ-PAY-001 (contexto Pagamentos)
// ============================================================================

package br.gov.client.sisdnit.payment.domain;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Fonte única dos índices IPCA mensais (BR-036), substituindo a tabela
 * hardcoded {@code #IPCA-ANO} do CALCCORR. O legado só carrega 2010–2012
 * (MYS-011): competências de outros anos resultam em fator 1,000000 (sem
 * correção). Aqui mantemos a mesma cobertura, mas expomos {@link #isCovered}
 * para que o serviço possa alertar em vez de silenciar — corrigindo MYS-011
 * sem alterar o número calculado.
 */
public final class IpcaIndexTable {

    private static final BigDecimal NEUTRAL_FACTOR = new BigDecimal("1.000000");

    // Índices mensais por ano (CALCCORR.NSN#L52-L96). Posição 0 = janeiro.
    private static final Map<Integer, BigDecimal[]> INDICES = Map.of(
            2010, monthly("0.0075", "0.0078", "0.0052", "0.0057", "0.0043", "0.0000",
                    "0.0001", "0.0004", "0.0045", "0.0075", "0.0083", "0.0063"),
            2011, monthly("0.0083", "0.0080", "0.0079", "0.0077", "0.0047", "0.0015",
                    "0.0016", "0.0037", "0.0053", "0.0043", "0.0052", "0.0050"),
            2012, monthly("0.0056", "0.0045", "0.0021", "0.0064", "0.0036", "0.0008",
                    "0.0043", "0.0041", "0.0054", "0.0059", "0.0060", "0.0079"));

    private IpcaIndexTable() {
    }

    private static BigDecimal[] monthly(String... values) {
        BigDecimal[] table = new BigDecimal[12];
        for (int i = 0; i < 12; i++) {
            table[i] = new BigDecimal(values[i]);
        }
        return table;
    }

    /**
     * Indica se a competência (AAAAMM) está coberta pela tabela IPCA.
     * Anos fora de 2010–2012 não são cobertos (MYS-011).
     */
    public static boolean isCovered(int competence) {
        int year = competence / 100;
        int month = competence - (year * 100);
        return month >= 1 && month <= 12 && INDICES.containsKey(year);
    }

    /**
     * Fator de correção da competência: {@code 1 + IPCA(ano, mês)} do mês da
     * competência (CALC-INDICE-ACUM — CALCCORR.NSN#L186-L196). Competência fora
     * da tabela retorna fator neutro 1,000000 (MYS-011 — sem correção).
     *
     * @param competence competência no formato AAAAMM
     * @return fator multiplicativo (1,0 quando não coberta)
     */
    public static BigDecimal correctionFactor(int competence) {
        int year = competence / 100;
        int month = competence - (year * 100);
        if (month < 1 || month > 12) {
            return NEUTRAL_FACTOR;
        }
        BigDecimal[] table = INDICES.get(year);
        if (table == null) {
            return NEUTRAL_FACTOR;
        }
        return BigDecimal.ONE.add(table[month - 1]);
    }
}
