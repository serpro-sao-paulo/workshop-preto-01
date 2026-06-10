-- ============================================================================
-- V1__init_schema.sql — SIFAP 2.0 — Schema inicial
-- ============================================================================
-- Cobre todos os bounded contexts: beneficiary, program, payment, audit.
-- Prefixos por contexto (ADR-004):
--   ben_ = beneficiary   prg_ = program   pay_ = payment   aud_ = audit
--
-- REQ-IDs cobertos: REQ-BEN-001, REQ-PRG-001, REQ-PAY-001, REQ-AUD-001
-- Legado: BENEFICIARIO.ddm (FNR150), PROGRAMA-SOCIAL.ddm (FNR151),
--         PAGAMENTO.ddm (FNR152), AUDITORIA.ddm (FNR153)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Contexto: beneficiary
-- ----------------------------------------------------------------------------
CREATE TABLE ben_beneficiary (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    cpf             CHAR(11)     NOT NULL,
    nome            VARCHAR(60)  NOT NULL,
    dt_nascimento   DATE         NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    cod_programa    INTEGER      NOT NULL,
    renda_familiar  NUMERIC(9,2) NOT NULL DEFAULT 0,
    num_dependentes SMALLINT     NOT NULL DEFAULT 0,
    cod_regiao      SMALLINT     NOT NULL DEFAULT 15,
    uf              CHAR(2),
    nis             CHAR(11),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ben_beneficiary_cpf_uk   UNIQUE (cpf),
    CONSTRAINT ben_beneficiary_status_ck CHECK (status IN (
        'ACTIVE', 'SUSPENDED', 'CANCELLED', 'INACTIVE', 'DISCHARGED'
    )),
    CONSTRAINT ben_beneficiary_regiao_ck CHECK (cod_regiao BETWEEN 1 AND 99)
);

COMMENT ON TABLE  ben_beneficiary               IS 'Beneficiário do programa social. Legado: BENEFICIARIO.ddm FNR150.';
COMMENT ON COLUMN ben_beneficiary.cpf           IS 'CPF do beneficiário (11 dígitos sem máscara). Nunca logar sem máscara (CONSTITUTION).';
COMMENT ON COLUMN ben_beneficiary.status        IS 'Máquina de status: ACTIVE→SUSPENDED→CANCELLED/INACTIVE/DISCHARGED. MYS-001: suspensão silenciosa >75 anos agora é explícita (REQ-BEN-005).';
COMMENT ON COLUMN ben_beneficiary.cod_regiao    IS 'Código de região 1-25; 99=exceção controlada (REQ-ELI-005). Mapeado para FATOR-REG em CALCBENF.NSN.';

CREATE TABLE ben_dependent (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    beneficiary_id  UUID         NOT NULL REFERENCES ben_beneficiary(id) ON DELETE CASCADE,
    nome            VARCHAR(60)  NOT NULL,
    dt_nascimento   DATE         NOT NULL,
    parentesco      VARCHAR(30),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE ben_dependent IS 'Dependentes do beneficiário. Legado: PE DEPENDENTES no BENEFICIARIO.ddm.';

CREATE TABLE ben_discount (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    beneficiary_id  UUID         NOT NULL REFERENCES ben_beneficiary(id) ON DELETE CASCADE,
    tipo_dsct       CHAR(1)      NOT NULL,
    vlr_dsct        NUMERIC(9,2) DEFAULT NULL,
    pct_dsct        NUMERIC(5,2) DEFAULT NULL,
    dt_inicio       DATE         NOT NULL,
    dt_fim          DATE,
    num_processo    VARCHAR(20),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ben_discount_tipo_ck CHECK (tipo_dsct IN ('C','I','J','S','P','A'))
);

COMMENT ON TABLE  ben_discount           IS 'Descontos do beneficiário. Legado: PE DESCONTOS no BENEFICIARIO.ddm (FNR150).';
COMMENT ON COLUMN ben_discount.tipo_dsct IS 'C=Contrib I=Imposto J=Judicial S=Sindical P=Pensao A=Admin (CALCDSCT.NSN).';

CREATE INDEX idx_ben_beneficiary_cpf    ON ben_beneficiary(cpf);
CREATE INDEX idx_ben_beneficiary_status ON ben_beneficiary(status);
CREATE INDEX idx_ben_dependent_benef    ON ben_dependent(beneficiary_id);
CREATE INDEX idx_ben_discount_benef     ON ben_discount(beneficiary_id);

-- ----------------------------------------------------------------------------
-- Contexto: program
-- ----------------------------------------------------------------------------
CREATE TABLE prg_social_program (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    cod_programa    INTEGER      NOT NULL,
    nome            VARCHAR(100) NOT NULL,
    tipo            CHAR(1)      NOT NULL,
    vlr_base        NUMERIC(9,2) NOT NULL CHECK (vlr_base > 0),
    fator_reajuste  NUMERIC(7,4) NOT NULL DEFAULT 0,
    status_prog     CHAR(1)      NOT NULL DEFAULT 'A',
    dt_vigencia_ini DATE         NOT NULL,
    dt_vigencia_fim DATE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT prg_social_program_cod_uk  UNIQUE (cod_programa),
    CONSTRAINT prg_social_program_tipo_ck CHECK (tipo IN ('A','B','C','D')),
    CONSTRAINT prg_social_program_stat_ck CHECK (status_prog IN ('A','I'))
);

COMMENT ON TABLE  prg_social_program              IS 'Programas sociais parametrizados. Legado: PROGRAMA-SOCIAL.ddm FNR151.';
COMMENT ON COLUMN prg_social_program.vlr_base     IS 'Valor-base do programa. Substitui constante 0.347215 hardcoded em CALCBENF.NSN#L12 (BR-016, REQ-PRG-001).';
COMMENT ON COLUMN prg_social_program.tipo         IS 'A=elegível para abono natalino (CALCBENF.NSN#L249-L257).';
COMMENT ON COLUMN prg_social_program.fator_reajuste IS 'Multiplicador de reajuste. Ex: 0.0250 = 2.5%. Aplicado em CALCBENF.NSN#L230.';

-- ----------------------------------------------------------------------------
-- Contexto: payment
-- ----------------------------------------------------------------------------
CREATE TABLE pay_payment (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    beneficiary_id  UUID          NOT NULL REFERENCES ben_beneficiary(id),
    competencia     CHAR(7)       NOT NULL,         -- formato YYYY-MM
    vlr_bruto       NUMERIC(11,2) NOT NULL CHECK (vlr_bruto >= 0),
    vlr_desconto    NUMERIC(11,2) NOT NULL DEFAULT 0 CHECK (vlr_desconto >= 0),
    vlr_liquido     NUMERIC(11,2) NOT NULL DEFAULT 0,  -- computed by application: vlr_bruto - vlr_desconto
    tipo_pgto       CHAR(1)       NOT NULL DEFAULT 'N',
    vlr_abono       NUMERIC(11,2) NOT NULL DEFAULT 0,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT pay_payment_tipo_ck   CHECK (tipo_pgto IN ('N','D')),
    CONSTRAINT pay_payment_status_ck CHECK (status IN ('PENDING','APPROVED','REJECTED','CANCELLED')),
    CONSTRAINT pay_payment_benef_comp_uk UNIQUE (beneficiary_id, competencia)
);

COMMENT ON TABLE  pay_payment             IS 'Pagamento mensal. Legado: PAGAMENTO.ddm FNR152.';
COMMENT ON COLUMN pay_payment.competencia IS 'Período de competência YYYY-MM (ex: 2026-06).';
COMMENT ON COLUMN pay_payment.tipo_pgto   IS 'N=Normal D=Décimo (CALCBENF.NSN: TIPO-PGTO).';
COMMENT ON COLUMN pay_payment.vlr_abono   IS 'Abono natalino 15% (programas tipo A em dezembro). BR-024, REQ-PAY-005.';

CREATE TABLE pay_payment_discount (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id      UUID          NOT NULL REFERENCES pay_payment(id) ON DELETE CASCADE,
    tipo_dsct       CHAR(1)       NOT NULL,
    vlr_aplicado    NUMERIC(11,2) NOT NULL CHECK (vlr_aplicado >= 0),
    CONSTRAINT pay_pmt_disc_tipo_ck CHECK (tipo_dsct IN ('C','I','J','S','P','A'))
);

COMMENT ON TABLE pay_payment_discount IS 'Descontos aplicados no pagamento. Detalhamento do vlr_desconto. REQ-PAY-007.';

CREATE INDEX idx_pay_payment_benef      ON pay_payment(beneficiary_id);
CREATE INDEX idx_pay_payment_status     ON pay_payment(status);
CREATE INDEX idx_pay_payment_competencia ON pay_payment(competencia);

-- ----------------------------------------------------------------------------
-- Contexto: audit
-- Tabela IMUTÁVEL — sem UPDATE nem DELETE via aplicação (ADR-004, REQ-AUD-001)
-- ----------------------------------------------------------------------------
CREATE TABLE aud_audit_event (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type      VARCHAR(60)  NOT NULL,
    aggregate_type  VARCHAR(60)  NOT NULL,
    aggregate_id    VARCHAR(100) NOT NULL,
    payload         JSONB        NOT NULL,
    occurred_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    actor           VARCHAR(60)
);

COMMENT ON TABLE  aud_audit_event            IS 'Trilha de auditoria imutável. GREENFIELD: corrige gap MYS-010 do legado (BATCHCON ocultava ação EX). REQ-AUD-001.';
COMMENT ON COLUMN aud_audit_event.event_type IS 'Ex: BeneficiaryStatusChanged, PaymentCalculated, ProgramUpdated.';
COMMENT ON COLUMN aud_audit_event.payload    IS 'Snapshot do estado em JSON — antes e depois quando aplicável.';

CREATE INDEX idx_aud_event_aggregate    ON aud_audit_event(aggregate_type, aggregate_id);
CREATE INDEX idx_aud_event_type         ON aud_audit_event(event_type);
CREATE INDEX idx_aud_event_occurred_at  ON aud_audit_event(occurred_at DESC);
