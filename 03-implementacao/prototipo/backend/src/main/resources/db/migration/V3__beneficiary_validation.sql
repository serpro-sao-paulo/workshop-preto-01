-- ============================================================================
-- V3__beneficiary_validation.sql — Par 4 · Qualidade (DBA + QA) · Estágio 3
-- ============================================================================
-- Schema do beneficiário com as constraints derivadas das validações do legado
-- (VALBENEF / VALDOCS / VALELEG). Também consolida o mapeamento DDM→PostgreSQL
-- do DDM BENEFICIARIO (ARQ 150), incluindo a conversão do grupo periódico
-- GRP-DEPENDENTE (PE, máx. 10) em tabela filha relacional.
--
-- Origem / rastreabilidade (regra → coluna/constraint → fonte legada):
--   BR-037 → CHECK cpf 11 dígitos          → VALBENEF.NSN#L188-L201
--   BR-038 → birth_date DATE (calendário)  → VALBENEF.NSN#L242-L260 (corrige MYS-016)
--   BR-040 → CHECK uf / status             → VALBENEF.NSN#L143-L166
--   BR-041 → CHECK rg ≥ 5                   → VALDOCS.NSN#L146-L160
--   BR-043 → region_code (99=especial)     → VALELEG.NSN#L105-L111 (MYS-015)
--   BR-044 → eligibility_code              → VALELEG.NSN#L116-L234
--   DDM    → BENEFICIARIO (ARQ 150), GRP-DEPENDENTE (PE)
--
-- Cobertura de REQ-IDs: REQ-VAL-CAD-01, REQ-VAL-DATE-01, REQ-VAL-ELEG-01
--
-- Flyway: nunca edite migrações antigas; esta é V3 (V1 cria payment, V2 cálculo).
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS beneficiary;
SET search_path TO beneficiary, public;

-- ----------------------------------------------------------------------------
-- Tabela: beneficiary — núcleo do DDM BENEFICIARIO (ARQ 150)
-- Cada CHECK reflete uma regra de validação do legado, agora no banco.
-- ----------------------------------------------------------------------------
CREATE TABLE beneficiary (
    id              BIGINT GENERATED ALWAYS AS IDENTITY,       -- ISN/matrícula (AA NUM-INSCRICAO)
    cpf             CHAR(11)     NOT NULL,                     -- AB NUM-CPF (N11) — BR-037
    nome_completo   VARCHAR(60)  NOT NULL,                     -- AC NOME-COMPLETO — BR-039
    birth_date      DATE         NOT NULL,                     -- AF DT-NASCIMENTO — BR-038 (corrige MYS-016)
    sexo            CHAR(1),                                   -- AG SEXO (M/F/I)
    rg_numero       VARCHAR(15),                               -- AI RG-NUMERO — BR-041
    uf              CHAR(2),                                   -- BG UF — BR-040
    region_code     SMALLINT     NOT NULL DEFAULT 1,           -- BJ COD-REGIAO (1-27 ou 99) — BR-043
    status          CHAR(1)      NOT NULL DEFAULT 'A',         -- CE SIT-BENEFICIARIO — BR-040
    renda_familiar  NUMERIC(11,2) NOT NULL DEFAULT 0,          -- CH VLR-RENDA-FAMILIAR — BR-044
    num_dependentes SMALLINT     NOT NULL DEFAULT 0,           -- derivado do PE GRP-DEPENDENTE
    nis             CHAR(11),                                  -- NIS — BR-044 (COD-ELEG 'R')
    documentos_ok   CHAR(1)      NOT NULL DEFAULT 'N',         -- DOCUMENTOS-OK — BR-044
    eligibility_code VARCHAR(5),                               -- COD-ELEGIBILIDADE — BR-044
    is_test_cpf     BOOLEAN      NOT NULL DEFAULT FALSE,        -- MYS-017: CPF de teste isolado (nunca produção)
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT pk_beneficiary PRIMARY KEY (id),
    CONSTRAINT uq_beneficiary_cpf UNIQUE (cpf),
    -- BR-037: CPF deve ter 11 dígitos numéricos
    CONSTRAINT ck_beneficiary_cpf_digits CHECK (cpf ~ '^[0-9]{11}$'),
    -- BR-040: domínio fechado de UF (27) e situação (A/S/C/I/D)
    CONSTRAINT ck_beneficiary_uf CHECK (uf IS NULL OR uf IN (
        'AC','AL','AM','AP','BA','CE','DF','ES','GO','MA','MG','MS','MT',
        'PA','PB','PE','PI','PR','RJ','RN','RO','RR','RS','SC','SE','SP','TO')),
    CONSTRAINT ck_beneficiary_status CHECK (status IN ('A','S','C','I','D')),
    -- BR-041: RG, quando preenchido, com pelo menos 5 caracteres
    CONSTRAINT ck_beneficiary_rg CHECK (rg_numero IS NULL OR length(trim(rg_numero)) >= 5),
    -- BR-043: região 1-27 (normal) ou 99 (especial/diplomático)
    CONSTRAINT ck_beneficiary_region CHECK (region_code BETWEEN 1 AND 27 OR region_code = 99),
    CONSTRAINT ck_beneficiary_documentos_ok CHECK (documentos_ok IN ('S','N'))
);

COMMENT ON TABLE beneficiary IS
  'DDM BENEFICIARIO (ARQ 150) modernizado. CHECKs derivam de VALBENEF/VALDOCS/VALELEG.';
COMMENT ON COLUMN beneficiary.region_code IS
  'COD-REGIAO (BJ). 99 = especial/diplomático: NÃO concede elegibilidade automática '
  'no sisdnit 2.0 (BR-043 / MYS-015) — exige revisão manual.';
COMMENT ON COLUMN beneficiary.is_test_cpf IS
  'CPF de teste (prefixo 000) isolado por flag (MYS-017). Nunca habilitar em produção.';

-- ----------------------------------------------------------------------------
-- Tabela: beneficiary_dependent — grupo periódico GRP-DEPENDENTE (PE, máx. 10)
-- PE do Adabas não existe em SQL relacional → vira tabela filha (MYS-003).
-- ----------------------------------------------------------------------------
CREATE TABLE beneficiary_dependent (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    beneficiary_id   BIGINT       NOT NULL,
    cpf_dependente   CHAR(11),                                  -- DB CPF-DEPENDENTE
    nome_dependente  VARCHAR(60)  NOT NULL,                     -- DC NOME-DEPENDENTE
    dt_nasc_depend   DATE,                                      -- DD DT-NASC-DEPEND
    parentesco       CHAR(2),                                   -- DE PARENTESCO (FI/CJ/NT/TU)
    sit_dependente   CHAR(1)      NOT NULL DEFAULT 'A',          -- DF SIT-DEPENDENTE
    ind_deficiencia  CHAR(1)      NOT NULL DEFAULT 'N',          -- DG IND-DEFICIENCIA
    seq_ocorrencia   SMALLINT     NOT NULL,                      -- ordem original no PE (1-10)
    CONSTRAINT fk_dependent_beneficiary
        FOREIGN KEY (beneficiary_id) REFERENCES beneficiary (id) ON DELETE CASCADE,
    CONSTRAINT ck_dependent_seq CHECK (seq_ocorrencia BETWEEN 1 AND 10),
    CONSTRAINT ck_dependent_sit CHECK (sit_dependente IN ('A','I','D')),
    CONSTRAINT uq_dependent_seq UNIQUE (beneficiary_id, seq_ocorrencia)
);

COMMENT ON TABLE beneficiary_dependent IS
  'Grupo periódico GRP-DEPENDENTE (PE, máx. 10) do DDM BENEFICIARIO convertido '
  'em tabela filha relacional. Limite de 10 preservado (ver MYS-003).';

CREATE INDEX ix_dependent_beneficiary ON beneficiary_dependent (beneficiary_id);
