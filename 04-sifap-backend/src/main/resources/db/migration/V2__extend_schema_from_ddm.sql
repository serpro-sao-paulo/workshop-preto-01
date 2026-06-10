-- ============================================================================
-- V2__extend_schema_from_ddm.sql — SIFAP 2.0
-- ============================================================================
-- Adiciona campos dos DDMs Adabas que faltaram no V1 inicial.
-- Expand-only: todas as colunas são nullable ou têm DEFAULT seguro.
-- Nunca editar V1 — ADR-004 / safe-migration.SKILL.md.
--
-- DDM de origem analisados nesta migration:
--   BENEFICIARIO.ddm (FNR 150) — grupos BA (endereço), EA (contato), FA (biometria)
--   PAGAMENTO.ddm   (FNR 152) — grupo EA (banco), campos DA-DG (status/datas)
--
-- REQ-IDs cobertos: REQ-BEN-001 (dados completos), REQ-PAY-001 (status CNAB)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- ben_beneficiary — campos faltantes do DDM FNR 150
-- Todos nullable (DDM original não tinha NOT NULL explícito; adicionamos
-- onde a lógica de negócio exige via application layer)
-- ----------------------------------------------------------------------------

-- Grupo BA: Endereço (campos BB-BJ do DDM)
ALTER TABLE ben_beneficiary
    ADD COLUMN IF NOT EXISTS logradouro    VARCHAR(60),
    ADD COLUMN IF NOT EXISTS num_end       VARCHAR(10),
    ADD COLUMN IF NOT EXISTS complemento   VARCHAR(30),
    ADD COLUMN IF NOT EXISTS bairro        VARCHAR(40),
    ADD COLUMN IF NOT EXISTS municipio     VARCHAR(40),
    ADD COLUMN IF NOT EXISTS cep           CHAR(8),
    ADD COLUMN IF NOT EXISTS cod_ibge      CHAR(7);

-- Identificação adicional (campos AA, AD-AL)
ALTER TABLE ben_beneficiary
    ADD COLUMN IF NOT EXISTS nome_mae      VARCHAR(60),
    ADD COLUMN IF NOT EXISTS nome_pai      VARCHAR(60),
    ADD COLUMN IF NOT EXISTS sexo          CHAR(1),
    ADD COLUMN IF NOT EXISTS est_civil     CHAR(1),
    ADD COLUMN IF NOT EXISTS rg_numero     VARCHAR(15),
    ADD COLUMN IF NOT EXISTS rg_orgao      VARCHAR(10),
    ADD COLUMN IF NOT EXISTS rg_uf         CHAR(2),
    ADD COLUMN IF NOT EXISTS rg_dt_expedicao DATE;

-- Datas de benefício (campos CC-CD) e motivo de situação (CF)
ALTER TABLE ben_beneficiary
    ADD COLUMN IF NOT EXISTS dt_inicio_benef  DATE,
    ADD COLUMN IF NOT EXISTS dt_fim_benef     DATE,
    ADD COLUMN IF NOT EXISTS mot_situacao     CHAR(3),
    ADD COLUMN IF NOT EXISTS dt_ult_situacao  DATE;

-- Renda per capita calculada (campo CJ)
ALTER TABLE ben_beneficiary
    ADD COLUMN IF NOT EXISTS renda_percap  NUMERIC(7,2);

-- Grupo EA: Contato (campos EA-EC — adicionado em 2015)
ALTER TABLE ben_beneficiary
    ADD COLUMN IF NOT EXISTS tel_fixo      VARCHAR(14),
    ADD COLUMN IF NOT EXISTS tel_celular   VARCHAR(15),
    ADD COLUMN IF NOT EXISTS email         VARCHAR(80);

-- Grupo FA: Biometria (campos FA-FD — adicionado em 2005)
ALTER TABLE ben_beneficiary
    ADD COLUMN IF NOT EXISTS ind_biometria  CHAR(1) DEFAULT 'N',
    ADD COLUMN IF NOT EXISTS dt_coleta_bio  DATE,
    ADD COLUMN IF NOT EXISTS cod_posto_bio  VARCHAR(6);
-- HASH-DIGITAL (FD) omitido: DDM anota "(NAO IMPL)" — campo nunca foi populado

COMMENT ON COLUMN ben_beneficiary.nome_mae         IS 'Nome da mãe (obrigatório na CAIXA). DDM FNR150 campo AD.';
COMMENT ON COLUMN ben_beneficiary.cep              IS 'CEP sem hífen (8 dígitos). DDM FNR150 campo BH.';
COMMENT ON COLUMN ben_beneficiary.mot_situacao     IS 'Código motivo de situação (tabela interna CGPB). DDM FNR150 campo CF.';
COMMENT ON COLUMN ben_beneficiary.renda_percap     IS 'Renda per capita calculada pelo sistema. DDM FNR150 campo CJ.';
COMMENT ON COLUMN ben_beneficiary.ind_biometria    IS 'S=Sim N=Não P=Pendente. DDM FNR150 campo FA.';

-- Constraints de valor para novos campos
ALTER TABLE ben_beneficiary
    ADD CONSTRAINT ben_beneficiary_sexo_ck      CHECK (sexo IN ('M','F','I') OR sexo IS NULL),
    ADD CONSTRAINT ben_beneficiary_est_civil_ck CHECK (est_civil IN ('S','C','D','V','U') OR est_civil IS NULL),
    ADD CONSTRAINT ben_beneficiary_bio_ck       CHECK (ind_biometria IN ('S','N','P') OR ind_biometria IS NULL);

-- ----------------------------------------------------------------------------
-- pay_payment — campos faltantes do DDM FNR 152
-- ----------------------------------------------------------------------------

-- Grupo EA: Dados bancários (campos EA-EE)
ALTER TABLE pay_payment
    ADD COLUMN IF NOT EXISTS cod_banco     CHAR(3),
    ADD COLUMN IF NOT EXISTS num_agencia   VARCHAR(10),
    ADD COLUMN IF NOT EXISTS num_conta     VARCHAR(20),
    ADD COLUMN IF NOT EXISTS tipo_conta    CHAR(1),
    ADD COLUMN IF NOT EXISTS nome_titular  VARCHAR(60);

-- Campos de datas de processamento (campos DB-DG) — para rastreio do ciclo CNAB
ALTER TABLE pay_payment
    ADD COLUMN IF NOT EXISTS dt_emissao        DATE,
    ADD COLUMN IF NOT EXISTS dt_confirmacao    DATE,
    ADD COLUMN IF NOT EXISTS dt_cancelamento   DATE,
    ADD COLUMN IF NOT EXISTS mot_cancelamento  CHAR(3);

-- Campos de integração (origem SIAFI / hash arquivo)
ALTER TABLE pay_payment
    ADD COLUMN IF NOT EXISTS num_ciclo         INTEGER,
    ADD COLUMN IF NOT EXISTS hash_arquivo      CHAR(64);

COMMENT ON COLUMN pay_payment.cod_banco        IS 'Código FEBRABAN do banco. DDM FNR152 campo EA.';
COMMENT ON COLUMN pay_payment.dt_emissao       IS 'Data envio ao banco (CNAB 240). DDM FNR152 campo DD.';
COMMENT ON COLUMN pay_payment.dt_confirmacao   IS 'Data retorno confirmação banco. DDM FNR152 campo DE.';
COMMENT ON COLUMN pay_payment.mot_cancelamento IS 'Código motivo cancelamento (tabela interna). DDM FNR152 campo DG.';
COMMENT ON COLUMN pay_payment.num_ciclo        IS 'Número sequencial do ciclo de processamento. DDM FNR152 campo AF.';
COMMENT ON COLUMN pay_payment.hash_arquivo     IS 'Hash SHA-256 do arquivo CNAB gerado. DDM FNR152 campo (2015).';

-- Status CNAB adicional: D=Devolvido R=Reprocessado (DDM FNR152 campo DA)
ALTER TABLE pay_payment
    DROP CONSTRAINT IF EXISTS pay_payment_status_ck;
ALTER TABLE pay_payment
    ADD CONSTRAINT pay_payment_status_ck CHECK (status IN (
        'PENDING','APPROVED','REJECTED','CANCELLED','RETURNED','REPROCESSED'
    ));

-- ----------------------------------------------------------------------------
-- aud_audit_event — completar mapeamento do DDM FNR 153
-- ----------------------------------------------------------------------------

-- Campos de identificação de origem (campos BB-BF)
ALTER TABLE aud_audit_event
    ADD COLUMN IF NOT EXISTS cod_modulo    VARCHAR(8),
    ADD COLUMN IF NOT EXISTS id_usuario    VARCHAR(20),
    ADD COLUMN IF NOT EXISTS ip_origem     INET,
    ADD COLUMN IF NOT EXISTS id_sessao     VARCHAR(40);

-- Campo legado: ação no formato de 2 chars do Adabas (BB)
-- Mapeia IN/AL/EX/CO/LG/LO/BT/ER/AU/RE → nosso event_type
ALTER TABLE aud_audit_event
    ADD COLUMN IF NOT EXISTS cod_acao_legado CHAR(2);

COMMENT ON COLUMN aud_audit_event.cod_modulo      IS 'Nome do programa/módulo que gerou o evento. DDM FNR153 campo BB.';
COMMENT ON COLUMN aud_audit_event.id_usuario      IS 'Login do usuário. DDM FNR153 campo BC.';
COMMENT ON COLUMN aud_audit_event.ip_origem       IS 'IP de origem (adicionado 2012). DDM FNR153 campo (IP).';
COMMENT ON COLUMN aud_audit_event.cod_acao_legado IS 'Código ação legado: IN/AL/EX/CO/LG/LO/BT/ER/AU/RE. DDM FNR153 campo BA.';
