<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# IMPLEMENTATION_PLAN.md — SIFAP 2.0

![ESTÁGIO 03 Implementação](https://img.shields.io/badge/ESTÁGIO-03%20Implementação-00A4EF?style=for-the-badge) ![SA Software Architect](https://img.shields.io/badge/SA-Software%20Architect-FFB900?style=for-the-badge)

> Sequenciado a partir de: [`SPECIFICATION.md`](../02-spec-moderna/SPECIFICATION.md) · [`bounded-contexts.md`](../02-spec-moderna/bounded-contexts.md) · ADR-001 (Modular Monolith) · ADR-003 (Strangler Fig)
> Lido pelo Par 3 (TL + Developer) na Passagem #2.

---

## Visão Geral

| Fase | Objetivo | Critério de saída | Pares |
| --- | --- | --- | --- |
| F0 · Fundação | Esqueleto Spring Boot + infra local | App sobe; ArchUnit verde; migrations rodam | Par 3 (TL) |
| F1 · `shared` + `audit` | Utilitários cross-cutting e trilha imutável | CpfValidator, MoneyRounding e AuditEvent testados | Par 3 (Dev) + Par 4 (DBA) |
| F2 · `beneficiary` | Cadastro, status, validações | API de beneficiários testada; REQ-BEN-001–007 ✅ | Par 3 (Dev) |
| F3 · `program` | Parâmetros de programa social | REQ-PRG-001 ✅; integração com F2 testada | Par 3 (Dev) |
| F4 · `payment` | Cálculo, elegibilidade, batch | REQ-PAY-001–007, REQ-ELI-001–005 ✅ | Par 3 (Dev) + Par 5 (DevOps) |
| F5 · Hardening | Segurança, cobertura, CI/CD final | Cobertura ≥ 70%; pipeline verde; OWASP scan | Par 4 (QA) + Par 5 (DevOps) |

---

## F0 · Fundação (pré-requisito de tudo)

**Objetivo:** Repositório, estrutura de pacotes, banco, Docker Compose e ArchUnit prontos.
**Duração estimada:** 1–2h
**Critério de saída:** `./mvnw verify` verde; `docker compose up` levanta app e banco; migração V1 executada.

| Task ID | Título | [P] | Modelo | Esforço | REQ-ID |
| --- | --- | --- | --- | --- | --- |
| F0-T01 | Criar projeto Spring Boot 3.3 / Java 21 com Maven Wrapper | — | Sonnet | S | infra |
| F0-T02 | Configurar `docker-compose.yml` (app + postgres:16 + volumes) | — | Sonnet | S | infra |
| F0-T03 | Escrever migração Flyway `V1__init_schema.sql` (5 tabelas) | — | Sonnet | M | infra |
| F0-T04 | Criar regra ArchUnit `NoCrossContextImportTest` | — | Opus | S | CONSTITUTION |
| F0-T05 | Configurar `application.yml` com profiles (`local`, `prod`) | — | Sonnet | S | infra |
| F0-T06 | Criar estrutura de pacotes vazia para os 5 bounded contexts | — | Sonnet | S | infra |

---

## F1 · `shared` + `audit`

**Objetivo:** Utilitários cross-cutting e trilha de auditoria imutável.
**Duração estimada:** 1h
**Critério de saída:** `CpfValidatorTest`, `MoneyRoundingTest`, `AuditEventListenerTest` passando; nenhum update/delete em `audit_event`.

| Task ID | Título | [P] | Modelo | Esforço | REQ-ID |
| --- | --- | --- | --- | --- | --- |
| F1-T01 | Implementar `CpfValidator` (módulo 11 para dígitos verificadores) | ✅ [P] | Sonnet | S | REQ-BEN-002 |
| F1-T02 | Implementar `MoneyRounding` (half-up, `RoundingMode.HALF_UP` em `BigDecimal`) | ✅ [P] | Sonnet | S | REQ-PAY-006 |
| F1-T03 | Criar records de eventos de domínio (`DomainEvent`, `BeneficiaryStatusChanged`, `PaymentCalculated`, `ProgramUpdated`) | ✅ [P] | Sonnet | S | REQ-AUD-001 |
| F1-T04 | Implementar `AuditEvent` (@Entity imutável — sem setter, sem update) | — | Opus | S | REQ-AUD-001 |
| F1-T05 | Implementar `AuditEventListener` (@EventListener) + `AuditEventRepository` | — | Sonnet | S | REQ-AUD-001 |
| F1-T06 | Implementar `AuditController` `GET /api/v1/audit` com filtros por beneficiário e competência | — | Sonnet | S | REQ-AUD-001 |
| F1-T07 | Testes unitários: `CpfValidatorTest`, `MoneyRoundingTest` | ✅ [P] | Sonnet | S | REQ-BEN-002, REQ-PAY-006 |
| F1-T08 | Teste de integração: `AuditEventListenerIntegrationTest` (Testcontainers) | — | Sonnet | M | REQ-AUD-001 |

---

## F2 · `beneficiary`

**Objetivo:** Cadastro de beneficiário com máquina de status, validações e dependentes.
**Duração estimada:** 1–2h
**Critério de saída:** API `/api/v1/beneficiaries` testada; REQ-BEN-001 a 007 rastreados em testes.

| Task ID | Título | [P] | Modelo | Esforço | REQ-ID |
| --- | --- | --- | --- | --- | --- |
| F2-T01 | Implementar `Beneficiary` (@Entity, campos do DDM BENEFICIARIO FNR150) | — | Sonnet | M | REQ-BEN-001 |
| F2-T02 | Implementar `BeneficiaryDependent` (@Entity — ex-PE group DEPENDENTE) | ✅ [P] | Sonnet | S | REQ-BEN-006 |
| F2-T03 | Implementar `BeneficiaryDiscount` (@Entity — ex-PE group DESCONTO) | ✅ [P] | Sonnet | S | REQ-BEN-007 |
| F2-T04 | Implementar `BeneficiaryStatus` (enum: ACTIVE, SUSPENDED, CANCELLED, INACTIVE, DISCHARGED) | ✅ [P] | Sonnet | S | REQ-BEN-005 |
| F2-T05 | Implementar `BeneficiaryValidator` (interface) + `CpfValidatorAdapter` (impl) | — | Sonnet | S | REQ-BEN-002 |
| F2-T06 | Implementar `BeneficiaryService` (criar, atualizar status, consultar) — `@Transactional` aqui, não no repository | — | Opus | M | REQ-BEN-001–005 |
| F2-T07 | Implementar `BeneficiaryRepository` (JpaRepository) | — | Sonnet | S | REQ-BEN-001 |
| F2-T08 | Implementar `BeneficiaryController` com `@Valid` + Bean Validation; status 409 para CPF duplicado | — | Sonnet | M | REQ-BEN-001–003 |
| F2-T09 | Garantir que status > 75 anos gera `BeneficiaryStatusChanged` com motivo `AGE_LIMIT` (MYS-001 → REQ-BEN-005) | — | Opus | S | REQ-BEN-005 |
| F2-T10 | Testes unitários `BeneficiaryServiceTest` (status machine, CPF inválido, duplicado) | ✅ [P] | Sonnet | M | REQ-BEN-001–005 |
| F2-T11 | Teste de integração `BeneficiaryControllerIT` (Testcontainers, happy + error paths) | — | Sonnet | M | REQ-BEN-001–003 |

---

## F3 · `program`

**Objetivo:** Parâmetros de programas sociais — read-heavy, write raramente.
**Duração estimada:** 45m
**Critério de saída:** REQ-PRG-001 verificado; `SocialProgram` retorna `valorBase` (substitui constante 0.347215).

| Task ID | Título | [P] | Modelo | Esforço | REQ-ID |
| --- | --- | --- | --- | --- | --- |
| F3-T01 | Implementar `SocialProgram` (@Entity, inclui `valorBase` BigDecimal — substitui BR-016) | — | Sonnet | S | REQ-PRG-001 |
| F3-T02 | Implementar `ProgramRepository` + `ProgramService` | — | Sonnet | S | REQ-PRG-001 |
| F3-T03 | Implementar `ProgramController` `GET /api/v1/programs` | ✅ [P] | Sonnet | S | REQ-PRG-001 |
| F3-T04 | Migração Flyway `V3__seed_programs.sql` com os 45 programas do legado | — | Sonnet | S | REQ-PRG-001 |
| F3-T05 | Testes unitários `ProgramServiceTest` | ✅ [P] | Sonnet | S | REQ-PRG-001 |

---

## F4 · `payment`

**Objetivo:** Coração financeiro — cálculo, elegibilidade, descontos, batch mensal.
**Duração estimada:** 2–3h
**Critério de saída:** `BenefitCalculatorTest` e `DiscountCalculatorTest` cobrem todos os cenários de BR-017 a BR-030; batch mensal integrado via `@Scheduled`.

| Task ID | Título | [P] | Modelo | Esforço | REQ-ID |
| --- | --- | --- | --- | --- | --- |
| F4-T01 | Implementar `Payment` + `PaymentDiscount` (@Entity — campos de FNR152) | — | Sonnet | M | REQ-PAY-001 |
| F4-T02 | Implementar `BenefitCalculator` (fórmula `VLR-BASE × FATOR-REG × FATOR-FAM × FATOR-RND × FATOR-IDADE × (1 + FATOR-REAJ)`) | — | Opus | M | REQ-PAY-001, 002, 003 |
| F4-T03 | Implementar lógica de 13º + abono em `BenefitCalculator.applyDecemberBonus()` | — | Opus | S | REQ-PAY-004, 005 |
| F4-T04 | Implementar `DiscountCalculator` com cap 30% e arredondamento half-up | — | Opus | S | REQ-PAY-006, 007 |
| F4-T05 | Implementar `EligibilityChecker` — status, renda, idade, tipo/código | — | Opus | M | REQ-ELI-001–005 |
| F4-T06 | Implementar exceção controlada para região 99 (substitui MYS-008) + auditoria obrigatória | — | Opus | S | REQ-ELI-005 |
| F4-T07 | Implementar `ReceitalFederalPort` + `ReceitalFederalAdapter` (circuit breaker 5s timeout, fallback `PENDING_RF`) | — | Opus | M | REQ-BEN-002 |
| F4-T08 | Implementar `PaymentService` (orquestra ELG → CAL → DIS → persistência → evento) | — | Opus | M | REQ-PAY-001–007 |
| F4-T09 | Implementar `PaymentController` + `MonthlyBatchScheduler` | — | Sonnet | M | REQ-PAY-001 |
| F4-T10 | Testes unitários `BenefitCalculatorTest` (fórmula, 13º, abono, todos fatores) | ✅ [P] | Sonnet | M | REQ-PAY-001–005 |
| F4-T11 | Testes unitários `DiscountCalculatorTest` (cap, half-up, tipos) | ✅ [P] | Sonnet | S | REQ-PAY-006, 007 |
| F4-T12 | Testes unitários `EligibilityCheckerTest` (status inativo, renda, idade, região 99) | ✅ [P] | Sonnet | S | REQ-ELI-001–005 |
| F4-T13 | Teste de integração `PaymentServiceIT` (ciclo completo Testcontainers) | — | Sonnet | L | REQ-PAY-001 |

> **[P]**: F4-T10, F4-T11, F4-T12 podem ser desenvolvidas em paralelo pois tocam domínio services independentes.

---

## F5 · Hardening

**Objetivo:** Segurança OWASP, cobertura ≥ 70%, CI/CD final, documentação.
**Duração estimada:** 1h
**Critério de saída:** Pipeline GitHub Actions verde; Jacoco ≥ 70%; ArchUnit sem violações; sem segredos em código.

| Task ID | Título | [P] | Modelo | Esforço | REQ-ID |
| --- | --- | --- | --- | --- | --- |
| F5-T01 | Configurar Spring Security (OAuth2 / JWT) + roles `OPERATOR`, `AUDITOR`, `ADMIN` | — | Opus | M | CONSTITUTION (security) |
| F5-T02 | Mascaramento de CPF em todos os logs (`CpfMaskingConverter`) | ✅ [P] | Sonnet | S | CONSTITUTION (security) |
| F5-T03 | Configurar CORS explícito para o domínio do frontend (sem wildcard `*`) | ✅ [P] | Sonnet | S | CONSTITUTION (security) |
| F5-T04 | GitHub Actions — workflow `ci.yml` (build + test + ArchUnit + coverage gate) | — | Sonnet | M | CONSTITUTION |
| F5-T05 | GitHub Actions — `legacy-traceability` check (REQ-IDs sem `source_legacy:`) | — | Sonnet | S | CONSTITUTION |
| F5-T06 | Springdoc OpenAPI (`/swagger-ui`) — annotações em todos os controllers | ✅ [P] | Sonnet | S | todos |
| F5-T07 | Testes de contrato: `BeneficiaryControllerIT`, `PaymentControllerIT` (auth fail + validation fail) | — | Sonnet | M | REQ-BEN-001, REQ-PAY-001 |

---

## Riscos globais

| Risco | Probabilidade | Impacto | Mitigação |
| --- | --- | --- | --- |
| Fórmula `CALCBENF` mal interpretada (MYS-005 — truncamento vs. half-up) | Alta | Crítico | `BenefitCalculatorTest` usa valores reais extraídos de `CALCBENF.NSN#L30-L45`; diff aprovado pelo PO antes de F4-T02 |
| Circuit breaker Receita Federal indisponível na demo | Média | Alto | `ReceitalFederalAdapter` tem fallback `PENDING_RF`; ambiente de demo usa stub via profile `demo` |
| Dependência de ordem batch (`BATCHPGT → BATCHCON → BATCHREL`) — MYS-009 | Média | Médio | Não reordenar enquanto SIAFI não for migrado (ADR-003 §4); `MonthlyBatchScheduler` documenta ordem explicitamente |
| ArchUnit quebra time por import cross-context acidental | Baixa | Baixo | Falha no CI desde F0-T04; feedback imediato; não chega ao PR |
| Backdoors de CPF migrando acidentalmente | Baixa | Crítico | REQ-BEN-004 marcado como `unwanted`; teste que verifica ausência do comportamento (`CPF000BackdoorAbsenceTest`) em F5 |
