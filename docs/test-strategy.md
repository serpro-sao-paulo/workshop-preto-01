# Test Strategy — SIFAP 2.0 (REQ-BEN-* / REQ-PAY-* / REQ-ELI-* / REQ-AUD-*)

**QA responsável:** Par 4 — QA Engineer  
**Data:** 2026-06-10  
**Status:** Aprovado pelo Technical Lead (Par 3)

---

## 1. Escopo

**Em escopo:**
- REQ-BEN-001 a REQ-BEN-007 (contexto `beneficiary`)
- REQ-PRG-001 (contexto `program`)
- REQ-PAY-001 a REQ-PAY-007 (contexto `payment`)
- REQ-ELI-001 a REQ-ELI-005 (elegibilidade)
- REQ-AUD-001 (trilha de auditoria imutável)

**Fora de escopo:**
- Integração SIAFI / remessa CNAB 240 (`REQ-INT-*`) — escopo Par 2
- Relatórios analíticos (`REQ-RPT-*`)
- Frontend Next.js — abordado em estratégia separada de componente

---

## 2. Perfil de risco por módulo

| Módulo | Classificação | Blast radius se quebrar | Cobertura alvo |
|--------|--------------|------------------------|----------------|
| `payment.BenefitCalculator` | **P0 — Financeiro CRÍTICO** | 4,2M famílias recebem valor errado | 90% branches |
| `payment.DiscountCalculator` | **P0 — Legal CRÍTICO** | Teto 30% violado — risco regulatório | 90% branches |
| `payment.EligibilityChecker` | **P0 — Financeiro CRÍTICO** | Pagamento para beneficiário inelegível | 90% branches |
| `beneficiary.BeneficiaryService` | **P0 — Integridade** | Duplicidade de CPF, status inválido | 80% linhas |
| `shared.CpfValidator` | **P0 — Segurança** | CPFs de teste em produção (MYS-007) | 100% |
| `audit.AuditEventListener` | **P1 — Regulatório** | Trilha de auditoria incompleta (IN-TCU 63/2010) | 70% linhas |
| `program.SocialProgram` | **P2 — Dados** | Valor-base errado (seed apenas) | 60% linhas |

---

## 3. Pirâmide de testes

```
       ┌──────────────┐
       │   E2E (5%)   │  Playwright — jornada completa cadastro + cálculo
       ├──────────────┤
       │Integration   │  Testcontainers (PostgreSQL real) — 20%
       │   (20%)      │  MockMvc — controller layer
       ├──────────────┤
       │ Unit (75%)   │  JUnit 5 + Mockito + AssertJ
       └──────────────┘
```

**Justificativa de desvio:** SIFAP é um sistema de cálculo-intensivo. A maioria das regras de negócio (BenefitCalculator, DiscountCalculator, EligibilityChecker, CpfValidator) são funções puras sem dependências externas → pirâmide unitária pesada é apropriada. Integration tests cobrem o contrato Repository ↔ PostgreSQL e a imutabilidade via triggers DB.

---

## 4. Ferramentas por camada

| Camada | Ferramenta | Versão | Uso |
|--------|-----------|--------|-----|
| Unit | JUnit 5 + Mockito + AssertJ | (Spring Boot 3.3 BOM) | Lógica de domínio pura |
| Unit — Parâmetros | `@ParameterizedTest` + `@CsvSource` | JUnit 5 | Tabelas de fatores regionais/etários |
| Integration | Testcontainers (`postgresql:16-alpine`) | 1.19.8 | Repository + triggers DB |
| Integration — API | MockMvc + Spring Security Test | Spring Boot 3.3 | Controllers com auth mock |
| Architecture | ArchUnit | 1.3.x | Bounded context isolation |
| Coverage | JaCoCo | 0.8.12 | Gate ≥70% em `domain` + `application` |
| Mutation | PIT (PITest) | Recomendado Estágio 4 | Detectar testes fantasmas em BenefitCalculator |

---

## 5. Estratégia de dados de teste

- **Dados sintéticos determinísticos:** CPF válido canônico `529.982.247-25` (gerado pelo algoritmo); nunca usar CPF real.
- **Valores de benefício:** calculados manualmente a partir de `CALCBENF.NSN` e fixados nos testes — mudanças na fórmula **quebram** o teste (desejado).
- **Seeds de programas sociais:** `V3__seed_programs.sql` provê 10 programas; testes IT usam `cod_programa = 1` (vlr_base=347.22).
- **Nenhum PII de produção** em qualquer ambiente — mandato LGPD.
- **CPFs de teste:** usar perfixo `529982247` + variações de DV; documentados em `CpfValidatorTest`.

---

## 6. Ambientes e quando rodar

| Ambiente | Quando dispara | Suítes que rodam |
|----------|---------------|-----------------|
| `local` | `./mvnw test` | Unit + ArchUnit |
| `CI / PR` | Push para feature branch | Unit + Integration (Testcontainers via Docker) + ArchUnit |
| `develop` | Merge em develop | Todas as suítes + JaCoCo gate |
| `stage` | Noturno | E2E Playwright |
| `prod-shadow` | Semanal | Performance (k6 — backlog) |

---

## 7. Critérios de saída (Definition of Done — testes)

| Camada | Critério |
|--------|---------|
| Unit | Todos os REQ-IDs P0 têm ≥3 testes: happy path + 1 valor limite + 1 negativo |
| Integration | Os 3 fluxos críticos têm IT: criar beneficiário, calcular pagamento, auditoria imutável |
| ArchUnit | 8 regras verdes (sem cross-context import) |
| JaCoCo | ≥70% linhas em `domain` + `application` (gate no CI) |
| Rastreabilidade | Todo `@Test` tem `// @implements REQ-XXX-NNN` inline |
| Flakiness | ≤1% de taxa flaky (quarentena automática acima disso) |
| CI runtime | Suíte unit+IT < 3 minutos em CI paralelo |

---

## 8. Riscos e mitigações

| Risco | Probabilidade | Impacto | Mitigação |
|-------|--------------|---------|-----------|
| Testcontainers não inicia (sem Docker no CI runner) | Média | Alto | `@DisabledIfSystemProperty` com fallback H2; alertar no CI |
| Testes lentos por Flyway V1-V5 em cada IT | Alta | Médio | `@DirtiesContext(classMode=AFTER_CLASS)` + shared Testcontainer via static field |
| CPF real em snapshot de teste | Baixa | Crítico | Code review gate + `grep -r '[\d]{11}'` no CI |
| Formula change sem atualizar testes | Média | Crítico | PIT mutation testing (backlog Estágio 4) |
| AuditEventListener silencia exceção | Baixa | Alto | Testar que evento AEM persiste no DB em IT |
