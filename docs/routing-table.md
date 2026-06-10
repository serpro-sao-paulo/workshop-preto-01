# Tabela de Roteamento de Modelos — SIFAP 2.0

> Produzido pelo **Technical Lead** via `/routing-table`.
> Baseado em [`03-implementacao/IMPLEMENTATION_PLAN.md`](../03-implementacao/IMPLEMENTATION_PLAN.md).
> Última revisão: 2026-06-10

---

## Critérios de roteamento

| Categoria | Modelo | Justificativa |
| --- | --- | --- |
| Descoberta / decisão ambígua / ADR | **Opus** | Requer raciocínio longo sobre trade-offs; errar custa muito |
| Implementação / domain services complexos / review | **Sonnet** | Qualidade alta com custo controlado; ~70% dos tokens |
| Transformação mecânica / boilerplate / seed data | **Haiku** | Padrão repetitivo; contexto pequeno; velocidade > profundidade |

---

## Tabela por tarefa

| Task ID | Título resumido | Categoria | Modelo recomendado | Justificativa | Est. Cost Tier |
| --- | --- | --- | --- | --- | --- |
| F0-T01 | Criar projeto Spring Boot 3.3 | Mechanical | Sonnet | Scaffold padrão; bem documentado; nenhuma decisão ambígua | 🟢 Baixo |
| F0-T02 | `docker-compose.yml` | Mechanical | Sonnet | Template conhecido; copiar do protótipo existente | 🟢 Baixo |
| F0-T03 | Flyway `V1__init_schema.sql` | Design | Sonnet | Requer entender o DDM Adabas → SQL; 5 tabelas simples | 🟡 Médio |
| F0-T04 | Regra ArchUnit cross-context | Design | **Opus** | Decisão arquitetural; erro aqui invalida o CI inteiro | 🟡 Médio |
| F0-T05 | `application.yml` profiles | Mechanical | **Haiku** | Config yml repetitiva; sem lógica de negócio | 🟢 Baixo |
| F0-T06 | Estrutura de pacotes vazia | Mechanical | **Haiku** | `mkdir -p` lógico; zero complexidade | 🟢 Baixo |
| F1-T01 | `CpfValidator` | Implementação | Sonnet | Módulo 11 bem conhecido; teste crítico mas algoritmo standard | 🟢 Baixo |
| F1-T02 | `MoneyRounding` (half-up) | Implementação | Sonnet | Uma linha de BigDecimal; wrapper simples | 🟢 Baixo |
| F1-T03 | Records de eventos de domínio | Mechanical | **Haiku** | Records Java 21 boilerplate; padrão repetitivo | 🟢 Baixo |
| F1-T04 | `AuditEvent` (@Entity imutável) | Design | **Opus** | Imutabilidade no JPA é sutil; errar quebra auditoria para sempre | 🟡 Médio |
| F1-T05 | `AuditEventListener` + repository | Implementação | Sonnet | @EventListener padrão Spring; sem novidade | 🟡 Médio |
| F1-T06 | `AuditController` | Implementação | Sonnet | CRUD read-only; seguir padrão do `BeneficiaryController` | 🟢 Baixo |
| F1-T07 | Testes CpfValidator, MoneyRounding | Implementação | Sonnet | Testes unitários simples; golden cases da spec | 🟢 Baixo |
| F1-T08 | `AuditEventListenerIT` Testcontainers | Implementação | Sonnet | Integração padrão; copiar setup de outros ITs | 🟡 Médio |
| F2-T01 | `Beneficiary` @Entity | Implementação | Sonnet | Mapeamento DDM FNR150 → JPA; documentado no modular-monolith.instructions | 🟡 Médio |
| F2-T02 | `BeneficiaryDependent` @Entity | Mechanical | **Haiku** | Segue exato padrão de F2-T01 | 🟢 Baixo |
| F2-T03 | `BeneficiaryDiscount` @Entity | Mechanical | **Haiku** | Segue exato padrão de F2-T01 | 🟢 Baixo |
| F2-T04 | `BeneficiaryStatus` enum | Mechanical | **Haiku** | 5 valores; sem lógica | 🟢 Baixo |
| F2-T05 | `BeneficiaryValidator` interface + adapter | Implementação | Sonnet | Interface + impl simples; padrão Ports & Adapters | 🟢 Baixo |
| F2-T06 | `BeneficiaryService` | Implementação | **Opus** | Máquina de status complexa; regra idade-75 (MYS-001); transações | 🔴 Alto |
| F2-T07 | `BeneficiaryRepository` | Mechanical | **Haiku** | `extends JpaRepository<Beneficiary, UUID>`; zero lógica | 🟢 Baixo |
| F2-T08 | `BeneficiaryController` | Implementação | Sonnet | REST adapter; @Valid; 409/404; OpenAPI | 🟡 Médio |
| F2-T09 | Status AGE_LIMIT → evento | Design | **Opus** | Comportamento legado silencioso (MYS-001) sendo explicitado; errar = bug silencioso | 🔴 Alto |
| F2-T10 | `BeneficiaryServiceTest` | Implementação | Sonnet | Testes unitários de máquina de status; mocks de validator | 🟡 Médio |
| F2-T11 | `BeneficiaryControllerIT` | Implementação | Sonnet | MockMvc + Testcontainers; happy + error + 409 | 🟡 Médio |
| F3-T01 | `SocialProgram` @Entity | Implementação | Sonnet | Substitui constante 0.347215; FNR151 → JPA | 🟡 Médio |
| F3-T02 | `ProgramRepository` + `ProgramService` | Mechanical | **Haiku** | Read-only service; sem lógica complexa | 🟢 Baixo |
| F3-T03 | `ProgramController` GET | Mechanical | **Haiku** | GET simples; sem escrita | 🟢 Baixo |
| F3-T04 | `V3__seed_programs.sql` (45 programas) | Mechanical | **Haiku** | Inserção de dados de referência; repetitivo | 🟢 Baixo |
| F3-T05 | `ProgramServiceTest` | Implementação | Sonnet | Testes unitários simples | 🟢 Baixo |
| F4-T01 | `Payment` + `PaymentDiscount` @Entity | Implementação | Sonnet | FNR152 → JPA; PE group → @OneToMany | 🟡 Médio |
| F4-T02 | `BenefitCalculator` (fórmula principal) | Design | **Opus** | Fórmula com 5 fatores; 29 anos de legado; BR-017 a BR-022; errar = bug financeiro | 🔴 Alto |
| F4-T03 | Lógica de 13º + abono dezembro | Design | **Opus** | BR-023/024; competência especial; edge case dezembro-31 | 🔴 Alto |
| F4-T04 | `DiscountCalculator` (cap 30%, half-up) | Design | **Opus** | BR-026 (cap) + REQ-PAY-006 (half-up vs truncamento legado) — mudança de comportamento | 🔴 Alto |
| F4-T05 | `EligibilityChecker` | Design | **Opus** | 5 REQs de elegibilidade; lógica multi-fator; cruzamento com 2 contextos | 🔴 Alto |
| F4-T06 | Exceção region-99 controlada | Design | **Opus** | Substituição de bypass silencioso (MYS-008); auditoria obrigatória; risco regulatório | 🔴 Alto |
| F4-T07 | `ReceitalFederalAdapter` circuit breaker | Design | **Opus** | Circuit breaker, timeout, fallback — padrão de resiliência; falhar = CPF não validado | 🔴 Alto |
| F4-T08 | `PaymentService` (orquestrador) | Implementação | **Opus** | Orquestra 4 componentes; transação; evento; coordenação crítica | 🔴 Alto |
| F4-T09 | `PaymentController` + `MonthlyBatchScheduler` | Implementação | Sonnet | REST adapter + @Scheduled cron; usar padrões estabelecidos | 🟡 Médio |
| F4-T10 | `BenefitCalculatorTest` | Implementação | Sonnet | Sonnet pode gerar casos de teste a partir da spec; não precisa de raciocínio criativo | 🟡 Médio |
| F4-T11 | `DiscountCalculatorTest` | Implementação | Sonnet | Idem F4-T10 | 🟡 Médio |
| F4-T12 | `EligibilityCheckerTest` | Implementação | Sonnet | Idem F4-T10 | 🟡 Médio |
| F4-T13 | `PaymentServiceIT` (ciclo completo) | Implementação | Sonnet | Testcontainers boilerplate + asserções da spec | 🟡 Médio |
| F5-T01 | Spring Security OAuth2/JWT | Design | **Opus** | Configuração de segurança tem surface de ataque ampla; errar = vulnerabilidade | 🔴 Alto |
| F5-T02 | `CpfMaskingConverter` | Mechanical | Sonnet | Pattern simples; regex de substituição | 🟢 Baixo |
| F5-T03 | CORS explícito | Mechanical | Sonnet | Config de 5 linhas; template conhecido | 🟢 Baixo |
| F5-T04 | GitHub Actions `ci.yml` | Implementação | Sonnet | Workflow padrão; seguir `cicd.instructions.md` | 🟡 Médio |
| F5-T05 | `legacy-traceability` check | Mechanical | **Haiku** | Script grep por `source_legacy:`; mecânico | 🟢 Baixo |
| F5-T06 | Springdoc OpenAPI annotations | Mechanical | **Haiku** | Annotations repetitivas em controllers; Haiku faz isso em lote | 🟢 Baixo |
| F5-T07 | Testes de contrato auth fail | Implementação | Sonnet | Seguir padrão de ITs existentes | 🟡 Médio |

---

## Resumo de distribuição

| Modelo | Qtd tarefas | % | Quando usar |
| --- | --- | --- | --- |
| **Opus** | 14 | 29% | Decisões financeiras, segurança, arquitetura, comportamento legado ambíguo |
| **Sonnet** | 24 | 49% | Maioria da implementação, testes, controllers, adapters |
| **Haiku** | 11 | 22% | Boilerplate repetitivo, config, seed data, scaffolding |

**Candidatos prioritários a downgrade (Opus → Sonnet):**
- F4-T09 (`PaymentController`) — já é Sonnet por ser adapter puro.
- F1-T05 (`AuditEventListener`) — já é Sonnet; Spring pattern bem documentado.

**Candidatos onde Sonnet NÃO substitui Opus:**
- F4-T02/03/04 (cálculos financeiros com regras legadas) — custo de erro financeiro > custo de token.
- F5-T01 (Spring Security) — surface de ataque grande.
- F2-T06/09 (máquina de status + MYS-001) — bug silencioso de 29 anos sendo explicitado.
