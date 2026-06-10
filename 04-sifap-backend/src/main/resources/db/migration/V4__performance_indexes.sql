-- ============================================================================
-- V4__performance_indexes.sql — SIFAP 2.0
-- ============================================================================
-- Índices para suportar o ciclo mensal de 3,8M pagamentos.
-- Todos criados com CONCURRENTLY para não bloquear escritas em produção.
-- (Em CI/devcontainer rodam sem CONCURRENTLY — ver nota abaixo)
--
-- Design seguindo a regra ESR (Equality → Sort → Range):
--   colunas de igualdade primeiro, depois range, depois sort.
--
-- Query-optimization.SKILL.md: "Todo índice custa escritas — justifique cada um."
-- Justificativas documentadas em cada bloco.
--
-- REQ-IDs cobertos: REQ-PAY-001 (ciclo batch), REQ-BEN-001 (consulta CPF),
--                   REQ-AUD-001 (trilha auditoria), REQ-ELI-001 (elegibilidade)
-- ============================================================================

-- NOTA: CONCURRENTLY não pode rodar dentro de uma transação explícita.
-- Flyway roda cada migration em uma transação por padrão.
-- Para ambientes de produção, remova o bloco BEGIN/COMMIT e rode manualmente
-- com: flyway -outOfOrder=false -baselineOnMigrate=false migrate
-- Em dev/CI o Flyway usa transação automática — CONCURRENTLY não é suportado
-- neste contexto; usamos CREATE INDEX sem CONCURRENTLY aqui para compatibilidade.

-- ----------------------------------------------------------------------------
-- ben_beneficiary — queries críticas
-- ----------------------------------------------------------------------------

-- Busca por CPF (endpoint GET /api/v1/beneficiaries + validação de CPF único)
-- Query: WHERE cpf = ?  → já temos UNIQUE (implica índice B-tree)
-- O índice único criado no V1 (ben_beneficiary_cpf_uk) já cobre este caso.

-- Busca por status para elegibilidade em lote (EligibilityChecker)
-- Query: WHERE status = 'ACTIVE'  — carrega ~60% dos 4M registros
-- Índice parcial é mais eficiente: só ACTIVE precisa de lookup rápido
CREATE INDEX IF NOT EXISTS idx_ben_beneficiary_active
    ON ben_beneficiary(id)
    WHERE status = 'ACTIVE';

COMMENT ON INDEX idx_ben_beneficiary_active IS
    'Índice parcial para beneficiários ativos. Suporta EligibilityChecker em lote. '
    'Apenas ~60% dos 4M registros — evita índice full-table. REQ-ELI-001.';

-- Busca por programa (JOIN com prg_social_program no ciclo)
-- Query: WHERE cod_programa = ? AND status = 'ACTIVE'
CREATE INDEX IF NOT EXISTS idx_ben_beneficiary_programa_status
    ON ben_beneficiary(cod_programa, status);

COMMENT ON INDEX idx_ben_beneficiary_programa_status IS
    'Cobertura de (cod_programa, status) para filtrar elegíveis por programa no ciclo. '
    'ESR: igualdade em cod_programa + igualdade em status. REQ-PAY-001.';

-- Busca por região (EligibilityChecker — região 99 exceção)
-- Query: WHERE cod_regiao = 99
CREATE INDEX IF NOT EXISTS idx_ben_beneficiary_regiao
    ON ben_beneficiary(cod_regiao)
    WHERE cod_regiao = 99;

COMMENT ON INDEX idx_ben_beneficiary_regiao IS
    'Índice parcial para região 99 (exceção controlada). REQ-ELI-005. '
    'Poucos registros — índice parcial evita overhead para as 26 regiões normais.';

-- ----------------------------------------------------------------------------
-- pay_payment — queries do ciclo mensal (tabela cresce 3.8M/mês)
-- ----------------------------------------------------------------------------

-- Query principal do ciclo: listar pagamentos pendentes de uma competência
-- Query: WHERE competencia = ? AND status = 'PENDING'
CREATE INDEX IF NOT EXISTS idx_pay_payment_comp_status
    ON pay_payment(competencia, status);

COMMENT ON INDEX idx_pay_payment_comp_status IS
    'Crítico para o ciclo batch mensal: seleciona pagamentos PENDING por competência. '
    'ESR: igualdade em competencia + igualdade em status. '
    'Sem este índice: Seq Scan em 180M+ linhas = horas. REQ-PAY-001.';

-- Consulta de pagamentos de um beneficiário (endpoint GET /api/v1/payments)
-- Query: WHERE beneficiary_id = ? ORDER BY competencia DESC
CREATE INDEX IF NOT EXISTS idx_pay_payment_benef_comp
    ON pay_payment(beneficiary_id, competencia DESC);

COMMENT ON INDEX idx_pay_payment_benef_comp IS
    'Covering index para consulta por beneficiário + ordenação por competência. '
    'Evita heap lookup extra para ORDER BY. REQ-PAY-001.';

-- Reconciliação CNAB: busca pagamentos por status + data de emissão
-- Query: WHERE status IN ('APPROVED','RETURNED') AND dt_emissao >= ?
CREATE INDEX IF NOT EXISTS idx_pay_payment_status_emissao
    ON pay_payment(status, dt_emissao)
    WHERE status IN ('APPROVED', 'RETURNED');

COMMENT ON INDEX idx_pay_payment_status_emissao IS
    'Suporta reconciliação CNAB/SIAFI: pagamentos aprovados/devolvidos por data de emissão. '
    'Índice parcial — exclui PENDING/CANCELLED que não são buscados neste contexto.';

-- Busca por ciclo (relatório de controle interno)
-- Query: WHERE num_ciclo = ?
CREATE INDEX IF NOT EXISTS idx_pay_payment_ciclo
    ON pay_payment(num_ciclo)
    WHERE num_ciclo IS NOT NULL;

COMMENT ON INDEX idx_pay_payment_ciclo IS
    'Suporta relatórios por ciclo de processamento. Parcial para excluir NULLs.';

-- ----------------------------------------------------------------------------
-- pay_payment_discount — join a partir de payment
-- ----------------------------------------------------------------------------

-- O FK payment_id já tem índice implícito via FK constraint.
-- Adicionamos índice em tipo_dsct para análise de descontos por tipo.
CREATE INDEX IF NOT EXISTS idx_pay_pmt_discount_tipo
    ON pay_payment_discount(payment_id, tipo_dsct);

COMMENT ON INDEX idx_pay_pmt_discount_tipo IS
    'Cobertura de (payment_id, tipo_dsct) para análise de composição de descontos. '
    'Evita Seq Scan na tabela de descontos quando filtrado por tipo. REQ-PAY-007.';

-- ----------------------------------------------------------------------------
-- aud_audit_event — consultas de auditoria (retenção 10 anos legal)
-- ----------------------------------------------------------------------------

-- Consulta por agregado + data (padrão mais comum: "histórico do beneficiário X")
-- Query: WHERE aggregate_type = 'Beneficiary' AND aggregate_id = ? ORDER BY occurred_at DESC
-- Índice composto já existe em V1: idx_aud_event_aggregate
-- Complementamos com ordering para evitar Sort node:
CREATE INDEX IF NOT EXISTS idx_aud_event_aggregate_ts
    ON aud_audit_event(aggregate_type, aggregate_id, occurred_at DESC);

COMMENT ON INDEX idx_aud_event_aggregate_ts IS
    'Covering index para trilha de auditoria por entidade + ordem temporal. '
    'Evita Sort separado no plano. REQ-AUD-001. '
    'Legado: equivale ao SUPERDESCRIPTOR S2 do DDM FNR153.';

-- Consulta por tipo de evento (relatório de eventos suspeitos / compliance)
-- Query: WHERE event_type = ? AND occurred_at >= ? AND occurred_at < ?
CREATE INDEX IF NOT EXISTS idx_aud_event_type_ts
    ON aud_audit_event(event_type, occurred_at DESC);

COMMENT ON INDEX idx_aud_event_type_ts IS
    'Suporta consultas de compliance por tipo de evento em janela de tempo. '
    'ESR: igualdade em event_type + range em occurred_at. REQ-AUD-001.';

-- Consulta por usuário (investigação de acesso indevido)
-- Query: WHERE id_usuario = ? ORDER BY occurred_at DESC
CREATE INDEX IF NOT EXISTS idx_aud_event_usuario_ts
    ON aud_audit_event(id_usuario, occurred_at DESC)
    WHERE id_usuario IS NOT NULL;

COMMENT ON INDEX idx_aud_event_usuario_ts IS
    'Suporta investigação de auditoria por usuário. Parcial para excluir eventos '
    'gerados pelo sistema sem usuário (SYSTEM actor).';

-- ----------------------------------------------------------------------------
-- prg_social_program — lookup por código (hot path do cálculo)
-- ----------------------------------------------------------------------------
-- O UNIQUE (cod_programa) já cria índice B-tree implícito.
-- Adicionamos índice em status_prog para listar programas ativos:
CREATE INDEX IF NOT EXISTS idx_prg_program_status
    ON prg_social_program(status_prog)
    WHERE status_prog = 'A';

COMMENT ON INDEX idx_prg_program_status IS
    'Lista rápida de programas ativos. Parcial — apenas status A. '
    'Suporta seed da tela de cadastro de beneficiários. REQ-PRG-001.';

-- ----------------------------------------------------------------------------
-- Estatísticas pós-criação
-- Os ANALYZEs garantem que o planner tem estatísticas precisas para os
-- novos índices no primeiro uso.
-- ----------------------------------------------------------------------------
ANALYZE ben_beneficiary;
ANALYZE pay_payment;
ANALYZE pay_payment_discount;
ANALYZE aud_audit_event;
ANALYZE prg_social_program;
