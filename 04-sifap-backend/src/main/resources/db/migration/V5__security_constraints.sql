-- ============================================================================
-- V5__security_constraints.sql — SIFAP 2.0
-- ============================================================================
-- Imutabilidade da tabela de auditoria (mandato legal IN-TCU 63/2010 e
-- Art. 14 Lei 8.159 — retenção mínima 10 anos).
--
-- Também cria os roles de banco necessários para separação de privilégios
-- entre a aplicação e o batch, conforme ADR-004.
--
-- ATENÇÃO: roles criados com IF NOT EXISTS para ser idempotente em rebases.
-- Em produção, os passwords são gerenciados pelo Azure Key Vault via
-- Managed Identity — não estão hardcoded aqui.
--
-- REQ-IDs: REQ-AUD-001 (imutabilidade), REQ-SEC-001 (mínimo privilégio)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Roles de banco (mínimo privilégio — OWASP A01)
-- ----------------------------------------------------------------------------

-- Role da aplicação: operações normais (SELECT/INSERT/UPDATE em tabelas transacionais)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'sifap_app') THEN
        CREATE ROLE sifap_app LOGIN;
    END IF;
END $$;

-- Role do batch noturno: leitura ampla + INSERT em pagamentos
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'sifap_batch') THEN
        CREATE ROLE sifap_batch LOGIN;
    END IF;
END $$;

-- Role somente-leitura (relatórios, auditoria interna, BI)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'sifap_readonly') THEN
        CREATE ROLE sifap_readonly LOGIN;
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- Permissões — sifap_app
-- ----------------------------------------------------------------------------

-- beneficiary context
GRANT SELECT, INSERT, UPDATE ON ben_beneficiary       TO sifap_app;
GRANT SELECT, INSERT, UPDATE ON ben_dependent         TO sifap_app;
GRANT SELECT, INSERT, UPDATE ON ben_discount          TO sifap_app;

-- program context (somente leitura pela app; escrita apenas via migration/admin)
GRANT SELECT ON prg_social_program TO sifap_app;

-- payment context
GRANT SELECT, INSERT, UPDATE ON pay_payment           TO sifap_app;
GRANT SELECT, INSERT, UPDATE ON pay_payment_discount  TO sifap_app;

-- audit: SOMENTE INSERT — sem UPDATE, sem DELETE (imutabilidade)
GRANT SELECT, INSERT ON aud_audit_event TO sifap_app;
REVOKE UPDATE, DELETE ON aud_audit_event FROM sifap_app;

-- ----------------------------------------------------------------------------
-- Permissões — sifap_batch
-- ----------------------------------------------------------------------------

GRANT SELECT ON ben_beneficiary       TO sifap_batch;
GRANT SELECT ON ben_dependent         TO sifap_batch;
GRANT SELECT ON ben_discount          TO sifap_batch;
GRANT SELECT ON prg_social_program    TO sifap_batch;
GRANT SELECT, INSERT, UPDATE ON pay_payment          TO sifap_batch;
GRANT SELECT, INSERT ON pay_payment_discount         TO sifap_batch;
GRANT SELECT, INSERT ON aud_audit_event              TO sifap_batch;
REVOKE UPDATE, DELETE ON aud_audit_event FROM sifap_batch;

-- ----------------------------------------------------------------------------
-- Permissões — sifap_readonly
-- ----------------------------------------------------------------------------

GRANT SELECT ON ben_beneficiary       TO sifap_readonly;
GRANT SELECT ON ben_dependent         TO sifap_readonly;
GRANT SELECT ON ben_discount          TO sifap_readonly;
GRANT SELECT ON prg_social_program    TO sifap_readonly;
GRANT SELECT ON pay_payment           TO sifap_readonly;
GRANT SELECT ON pay_payment_discount  TO sifap_readonly;
GRANT SELECT ON aud_audit_event       TO sifap_readonly;

-- ----------------------------------------------------------------------------
-- Row-Level Security — aud_audit_event (proteção extra contra UPDATE via SQL direto)
-- PostgreSQL não suporta DDL "DENY UPDATE" nativo sem RLS; usamos trigger +
-- constraint para garantir imutabilidade mesmo para o dono da tabela.
-- ----------------------------------------------------------------------------

-- Trigger que impede UPDATE em qualquer linha da tabela de auditoria
CREATE OR REPLACE FUNCTION aud_prevent_update()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    RAISE EXCEPTION
        'aud_audit_event is immutable (IN-TCU 63/2010). UPDATE is forbidden. '
        'Attempted to update id=%.', OLD.id
        USING ERRCODE = '55000';  -- object_not_in_prerequisite_state
END;
$$;

DROP TRIGGER IF EXISTS trg_aud_no_update ON aud_audit_event;
CREATE TRIGGER trg_aud_no_update
    BEFORE UPDATE ON aud_audit_event
    FOR EACH ROW EXECUTE FUNCTION aud_prevent_update();

-- Trigger que impede DELETE em qualquer linha da tabela de auditoria
CREATE OR REPLACE FUNCTION aud_prevent_delete()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    RAISE EXCEPTION
        'aud_audit_event is immutable (IN-TCU 63/2010). DELETE is forbidden. '
        'Attempted to delete id=%.', OLD.id
        USING ERRCODE = '55000';
END;
$$;

DROP TRIGGER IF EXISTS trg_aud_no_delete ON aud_audit_event;
CREATE TRIGGER trg_aud_no_delete
    BEFORE DELETE ON aud_audit_event
    FOR EACH ROW EXECUTE FUNCTION aud_prevent_delete();

COMMENT ON FUNCTION aud_prevent_update() IS
    'Garante imutabilidade da trilha de auditoria. '
    'Mandato legal: IN-TCU 63/2010 + Art.14 Lei 8.159. '
    'Solução para MYS-010 (legado Adabas também não permitia UPDATE/DELETE neste arquivo).';

COMMENT ON FUNCTION aud_prevent_delete() IS
    'Garante imutabilidade da trilha de auditoria. Mesma base legal de aud_prevent_update().';

-- ----------------------------------------------------------------------------
-- Constraint adicional: CPF não pode ser alterado após cadastro
-- Isso é uma invariante de domínio crítica (REQ-BEN-002)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION ben_prevent_cpf_change()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    IF NEW.cpf <> OLD.cpf THEN
        RAISE EXCEPTION
            'CPF cannot be changed after registration (REQ-BEN-002). '
            'Beneficiary id=%.', OLD.id
            USING ERRCODE = '23514';  -- check_violation
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_ben_no_cpf_change ON ben_beneficiary;
CREATE TRIGGER trg_ben_no_cpf_change
    BEFORE UPDATE OF cpf ON ben_beneficiary
    FOR EACH ROW EXECUTE FUNCTION ben_prevent_cpf_change();

COMMENT ON FUNCTION ben_prevent_cpf_change() IS
    'Impede alteração de CPF após cadastro. REQ-BEN-002. '
    'CPF é a chave natural definitiva do beneficiário no SIFAP.';
