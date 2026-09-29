/*
 * sisdnit 2.0 — Par 4 · Qualidade (DBA + QA) · Estágio 3
 * ----------------------------------------------------------------------------
 * Validação de data no formato legado N8 (AAAAMMDD).
 *
 * Rastreabilidade:
 *   BR-038  → VALBENEF.NSN#L242-L260 (VALIDA-DATA) e #L96 (#DIAS-MES(2)=29)
 *   MYS-016 → fevereiro sempre com 29 dias (sem checagem de bissexto)
 *   REQ-VAL-DATE-01
 *
 * Oferece duas leituras:
 *   - isValidLegacy : reproduz fielmente o legado (29/02 sempre aceito)
 *   - isValidStrict : comportamento correto (rejeita 29/02 em ano não bissexto)
 */
package br.gov.client.sisdnit.validation.domain;

import java.time.LocalDate;

public final class LegacyDate {

    /** Tabela #DIAS-MES do legado: fevereiro fixo em 29 (MYS-016). */
    private static final int[] DAYS_IN_MONTH_LEGACY =
        {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private LegacyDate() {
    }

    public static int year(int yyyymmdd) {
        return yyyymmdd / 10000;
    }

    public static int month(int yyyymmdd) {
        return (yyyymmdd / 100) % 100;
    }

    public static int day(int yyyymmdd) {
        return yyyymmdd % 100;
    }

    /** Reproduz VALIDA-DATA: ano 1900..atual, mês 1..12, dia 1..#DIAS-MES (fev=29). */
    public static boolean isValidLegacy(int yyyymmdd, int currentYear) {
        int year = year(yyyymmdd);
        int month = month(yyyymmdd);
        int day = day(yyyymmdd);
        if (year < 1900 || year > currentYear) {
            return false;
        }
        if (month < 1 || month > 12) {
            return false;
        }
        return day >= 1 && day <= DAYS_IN_MONTH_LEGACY[month - 1];
    }

    /** Validação correta usando o calendário real (rejeita 29/02 em ano comum). */
    public static boolean isValidStrict(int yyyymmdd, int currentYear) {
        int year = year(yyyymmdd);
        int month = month(yyyymmdd);
        int day = day(yyyymmdd);
        if (year < 1900 || year > currentYear) {
            return false;
        }
        if (month < 1 || month > 12) {
            return false;
        }
        try {
            LocalDate.of(year, month, day);
            return true;
        } catch (java.time.DateTimeException e) {
            return false;
        }
    }
}
