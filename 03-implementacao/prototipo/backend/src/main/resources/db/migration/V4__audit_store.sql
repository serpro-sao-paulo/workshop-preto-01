-- ============================================================================
-- V4__audit_store.sql — Par 4 · Qualidade (DBA) · Estágio 3
-- ============================================================================
-- Trilha de auditoria do sisdnit 2.0, modernizando o DDM AUDITORIA (ARQ 153).
-- Append-only de verdade: nenhum UPDATE/DELETE é permitido no schema (gatilho).
-- Obrigatoriedade legal preservada: IN-TCU 63/2010 e retenção mínima de 10 anos
-- (Art. 14, Lei 8.159) — citadas no próprio DDM.
--
-- Origem / rastreabilidade (regra → coluna/constraint → fonte legada):
--   BR-049 → action / domínio de ações     → RELAUDIT.NSN#L137-L161
--   DDM    → AUDITORIA (ARQ 153)            → adabas-ddms/AUDITORIA.ddm
--   MU     → GRP-ANTES/GRP-DEPOIS (MU 20)   → tabela filha audit_field_change
--
-- Lacunas do legado corrigidas (decisão DBA, sisdnit 2.0):
--   * Consultas 'CO' deixaram de ser gravadas em 2010 (Port. CGTI 213/2010).
--     No sisdnit 2.0 toda ação é gravável — sem exceção silenciosa de volume.
--   * RELAUDIT.NSN filtra ações 'EX' na exibição (exclusões ficavam ocultas).
--     Aqui não há filtro no armazenamento: exclusões SEMPRE constam na trilha.
--
-- Cobertura de REQ-IDs: REQ-AUD-01 (trilha imutável de auditoria)
--
-- Flyway: nunca edite migrações antigas; esta é V4 (V2 cálculo, V3 beneficiário).
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS audit;
SET search_path TO audit, public;

-- ----------------------------------------------------------------------------
-- Tabela: audit_event — núcleo do DDM AUDITORIA (ARQ 153)
-- Registro imutável: o gatilho abaixo bloqueia UPDATE e DELETE (append-only).
-- ----------------------------------------------------------------------------
CREATE TABLE audit_event (
    id              BIGINT GENERATED ALWAYS AS IDENTITY,        -- AA NUM-AUDITORIA (sequencial único)
    event_ts        TIMESTAMP    NOT NULL DEFAULT now(),         -- AD TS-EVENTO (AAAAMMDDHHMMSS)
    action          CHAR(2)      NOT NULL,                       -- BA COD-ACAO — BR-049
    module          VARCHAR(8),                                 -- BB COD-MODULO (programa/serviço)
    action_desc     VARCHAR(80),                                -- BC DES-ACAO
    entity_type     CHAR(4)      NOT NULL,                       -- CA TIPO-ENTIDADE (BENF/PGTO/PROG/ADMN/SIST)
    entity_id       VARCHAR(15),                                -- CB ID-ENTIDADE
    cpf_afetado     CHAR(11),                                   -- CC NUM-CPF-AFETADO (mascarado na exibição — BR-046)
    usr_event       VARCHAR(8),                                 -- EA USR-EVENTO (login)
    usr_name        VARCHAR(40),                                -- EB NOME-USUARIO
    usr_profile     CHAR(3),                                    -- EC COD-PERFIL (ADM/OPR/CON/AUD/SUP)
    ip_origem       VARCHAR(45),                                -- EE IP-ORIGEM (45 = comporta IPv6)
    session_id      VARCHAR(36),                                -- EF ID-SESSAO
    batch_cycle     INTEGER,                                    -- FA NUM-CICLO-BATCH (quando action='BT')
    correlation_id  UUID,                                       -- GA ID-CORRELACAO (operação composta)
    correlation_seq SMALLINT,                                   -- GB NUM-SEQ-CORRELACAO
    CONSTRAINT pk_audit_event PRIMARY KEY (id),
    -- BR-049: domínio fechado de ações da trilha (inclui 'CO' e 'EX', sempre gravadas)
    CONSTRAINT ck_audit_action CHECK (action IN
        ('IN','AL','EX','CO','LG','LO','BT','ER','AU','RE','CN','DV')),
    CONSTRAINT ck_audit_entity CHECK (entity_type IN ('BENF','PGTO','PROG','ADMN','SIST')),
    CONSTRAINT ck_audit_cpf CHECK (cpf_afetado IS NULL OR cpf_afetado ~ '^[0-9]{11}$')
);

COMMENT ON TABLE audit_event IS
  'DDM AUDITORIA (ARQ 153) modernizado. Append-only: UPDATE/DELETE bloqueados '
  'por gatilho. Retenção mínima 10 anos (IN-TCU 63/2010, Lei 8.159 art. 14).';
COMMENT ON COLUMN audit_event.action IS
  'BR-049. Todas as ações são gravadas — inclusive CO (consulta) e EX (exclusão), '
  'que o legado omitia (Port. CGTI 213/2010 e filtro do RELAUDIT.NSN).';

-- ----------------------------------------------------------------------------
-- Tabela: audit_field_change — grupos MU GRP-ANTES/GRP-DEPOIS (MU, máx. 20)
-- MU do Adabas (campo antes/depois) → tabela filha relacional, sem JSONB.
-- ----------------------------------------------------------------------------
CREATE TABLE audit_field_change (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    audit_event_id  BIGINT       NOT NULL,
    field_name      VARCHAR(30)  NOT NULL,                       -- DB/DE CAMPO-ALTERADO
    old_value       VARCHAR(80),                                 -- DC VALOR-ANTERIOR
    new_value       VARCHAR(80),                                 -- DF VALOR-POSTERIOR
    seq_ocorrencia  SMALLINT     NOT NULL,                       -- ordem original no MU (1-20)
    CONSTRAINT fk_field_change_event
        FOREIGN KEY (audit_event_id) REFERENCES audit_event (id),
    CONSTRAINT ck_field_change_seq CHECK (seq_ocorrencia BETWEEN 1 AND 20),
    CONSTRAINT uq_field_change_seq UNIQUE (audit_event_id, seq_ocorrencia)
);

COMMENT ON TABLE audit_field_change IS
  'Grupos MU GRP-ANTES/GRP-DEPOIS (máx. 20 ocorrências) do DDM AUDITORIA '
  'convertidos em tabela filha relacional (estado antes/depois de cada campo).';

-- ----------------------------------------------------------------------------
-- Append-only: bloqueia UPDATE e DELETE em todo o schema de auditoria.
-- Reproduz a garantia do DDM ("REGISTRO IMUTAVEL - NAO PERMITE UPDATE/DELETE")
-- no nível do banco — não confia na disciplina da aplicação.
-- ----------------------------------------------------------------------------
CREATE FUNCTION audit_forbid_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Trilha de auditoria é append-only: % não permitido em %',
        TG_OP, TG_TABLE_NAME
        USING ERRCODE = 'integrity_constraint_violation';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_event_immutable
    BEFORE UPDATE OR DELETE ON audit_event
    FOR EACH ROW EXECUTE FUNCTION audit_forbid_mutation();

CREATE TRIGGER trg_audit_field_change_immutable
    BEFORE UPDATE OR DELETE ON audit_field_change
    FOR EACH ROW EXECUTE FUNCTION audit_forbid_mutation();

-- ----------------------------------------------------------------------------
-- Índices para as consultas do RELAUDIT (BR-049): por período, ação, usuário e
-- entidade — equivalentes aos superdescriptors S1/S2/S3 do DDM.
-- ----------------------------------------------------------------------------
CREATE INDEX ix_audit_event_ts_action ON audit_event (event_ts, action);            -- S1: data + ação
CREATE INDEX ix_audit_event_entity    ON audit_event (entity_type, entity_id, event_ts); -- S2: entidade + data
CREATE INDEX ix_audit_event_user      ON audit_event (usr_event, event_ts);          -- S3: usuário + data
CREATE INDEX ix_audit_event_cpf       ON audit_event (cpf_afetado);                  -- rastreio por CPF afetado
CREATE INDEX ix_field_change_event    ON audit_field_change (audit_event_id);
