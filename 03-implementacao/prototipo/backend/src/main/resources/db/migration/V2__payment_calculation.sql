-- ============================================================================
-- V2__payment_calculation.sql — Par 3 · Implementação (Estágio 3)
-- ============================================================================
-- Suporta o núcleo de cálculo do benefício migrado do CALCBENF/CALCDSCT/CALCCORR.
--
-- Origem / rastreabilidade:
--   - region_factor                → BR-031, resolve MYS-008 (tabela duplicada → fonte única)
--   - payment.thirteenth/bonus     → BR-025/BR-033 (CALCBENF.NSN#L239-L255)
--   - payment.correction_*         → BR-036 (CALCCORR.NSN)
--   - payment_deduction.valid_*    → BR-035 (vigência de descontos, CALCDSCT.NSN#L111-L118)
--
-- Cobertura de REQ-IDs: REQ-PAY-003/004/005/006, REQ-PAY-DSCT-01
--
-- Flyway: nunca edite migrações antigas; esta é V2 (V1 cria o módulo payment).
-- ============================================================================

SET search_path TO payment, public;

-- ----------------------------------------------------------------------------
-- Tabela: region_factor — FONTE ÚNICA do fator regional (BR-031 / MYS-008)
-- Substitui as duas cópias hardcoded do legado (CALCBENF e BATCHPGT).
-- ----------------------------------------------------------------------------
CREATE TABLE region_factor (
    region_code   SMALLINT      PRIMARY KEY CHECK (region_code BETWEEN 1 AND 27),
    uf            CHAR(2),
    factor        NUMERIC(5,4)  NOT NULL CHECK (factor > 0)
);

COMMENT ON TABLE region_factor IS
  'Fonte única do Fator Regional (BR-031). Resolve MYS-008 — antes duplicado '
  'entre CALCBENF.NSN e BATCHPGT.NSN.';

INSERT INTO region_factor (region_code, uf, factor) VALUES
    (1,'AC',1.3500),(2,'AM',1.3200),(3,'AP',1.3000),(4,'PA',1.2800),(5,'RO',1.3100),
    (6,'MA',1.4000),(7,'PI',1.3800),(8,'CE',1.3500),(9,'BA',1.3200),(10,'PE',1.3600),
    (11,'SP',1.1000),(12,'RJ',1.1200),(13,'MG',1.0800),(14,'ES',1.0500),(15,'REF',1.0000),
    (16,'PR',1.0500),(17,'SC',1.0700),(18,'RS',1.0300),(19,'MS',1.1500),(20,'MT',1.2000),
    (21,'GO',1.1800),(22,'TO',1.2500),(23,'DF',1.1000),(24,'RR',1.2200),(25,'SE',1.3300),
    (26,NULL,1.0000),(27,NULL,1.0000);

-- ----------------------------------------------------------------------------
-- Colunas de cálculo no pagamento (13º, abono, tipo, correção retroativa)
-- ----------------------------------------------------------------------------
ALTER TABLE payment
    ADD COLUMN monthly_amount  NUMERIC(11,2) NOT NULL DEFAULT 0 CHECK (monthly_amount >= 0),  -- benefício mensal (BR-021)
    ADD COLUMN thirteenth      NUMERIC(11,2) NOT NULL DEFAULT 0 CHECK (thirteenth >= 0),      -- 13º (BR-033)
    ADD COLUMN bonus           NUMERIC(11,2) NOT NULL DEFAULT 0 CHECK (bonus >= 0),           -- abono natalino (BR-025)
    ADD COLUMN payment_type    CHAR(1)       NOT NULL DEFAULT 'N' CHECK (payment_type IN ('N','D')), -- N=normal D=dezembro
    ADD COLUMN correction_amount NUMERIC(11,2),                                                -- VLR-CORRECAO (BR-036)
    ADD COLUMN corrected_at    DATE,                                                           -- DT-CORRECAO
    ADD COLUMN corrected_flag  BOOLEAN       NOT NULL DEFAULT FALSE;                           -- IND-CORRIGIDO = 'S' (BR-036)

COMMENT ON COLUMN payment.corrected_flag IS
  'Idempotência da correção retroativa (BR-036 / CALCCORR.NSN#L140). '
  'TRUE impede recorreção do mesmo pagamento.';

-- ----------------------------------------------------------------------------
-- Vigência dos descontos (BR-035) — coluna de tipo padroniza códigos do PE
-- ----------------------------------------------------------------------------
ALTER TABLE payment_deduction
    ADD COLUMN valid_from   DATE,                                   -- DT-INICIO-DSCT
    ADD COLUMN valid_to     DATE;                                   -- DT-FIM-DSCT (NULL = sem fim)

CREATE INDEX idx_payment_deduction_validity ON payment_deduction (valid_from, valid_to);
