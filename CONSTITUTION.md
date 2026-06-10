<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# CONSTITUTION — SIFAP 2.0

> Documento de governança arquitetural mantido pelo **Enterprise Architect** (Par 2 · Arquitetura).
> Define as **restrições inegociáveis** que toda spec, código, dado e deploy do SIFAP 2.0 deve respeitar.
> A constituição **supersede** preferências individuais. Violações seguem o Protocolo de Violação (§7).

**Versão**: 1.0.0 · **Ratificado**: 2026-06-10 · **Última emenda**: 2026-06-10

---

## 1. Princípios Centrais

### I. Rastreabilidade legado → moderno (NÃO-NEGOCIÁVEL)
Todo requisito carrega `source_legacy:` apontando para `01-arqueologia/legado-sifap/*.NSN` / `*.ddm` ou `[GREENFIELD] + justificativa`. Nenhuma regra de negócio de 29 anos é descartada sem decisão explícita e documentada (catálogo BR + discovery-report).

### II. O ciclo mensal de pagamento é sagrado
Nenhuma decisão técnica pode introduzir, no caminho de geração da folha, dependência síncrona de sistema externo (BB, CAIXA, SIAFI). A ordem batch acoplada ao SIAFI (MYS-009) não é reordenada sem migrar a conciliação junto.

### III. Coexistência sobre big bang
A migração segue Strangler Fig (ADR-003): contextos migram um a um, com `payment` por último e validação por shadow run. O legado permanece autoritativo até cada contexto ser conciliado.

### IV. Auditoria imutável e completa
Toda transição de status e todo cálculo financeiro gera registro de auditoria imutável (sem update/delete). É proibido ocultar tipos de evento do trilho de auditoria — corrige explicitamente o MYS-010 (ação `EX` escondida do relatório legado).

### V. Test-first para regra de negócio
Lógica financeira e de elegibilidade tem teste antes ou junto da implementação, rastreando o REQ-ID. Cálculos críticos (BR-017, BR-023, BR-026, BR-030) têm casos de borda cobertos.

---

## 2. Restrições de Segurança (OWASP Top 10 — inegociáveis)

1. **Dados sensíveis nunca em claro em logs.** CPF mascarado no formato `XXX.XXX.NNN-NN`; valores de benefício e dados pessoais (NIS, RG, endereço) não são logados sem mascaramento.
2. **Sem segredos hardcoded.** Credenciais, chaves e strings de conexão apenas via cofre (`azurerm_key_vault_secret` / variáveis de ambiente). Nunca em código, `locals`, commits, logs ou descrições de PR.
3. **SQL apenas via JPA/JPQL parametrizado.** Proibida concatenação de strings em consultas (injeção).
4. **Validação em toda fronteira do sistema.** Entrada validada na borda (controller `@Valid` + Bean Validation); CPF por módulo 11.
5. **Proibido replicar backdoors do legado.** Os CPFs/documentos de teste aceitos sem verificação (MYS-007, MYS-010, EGG-002) **não** são migrados; testes usam ambiente isolado. (REQ-BEN-004 marca o comportamento como `unwanted`.)
6. **Autenticação e autorização explícitas.** OAuth2/JWT (Spring Security); nenhum caminho concede acesso ou elegibilidade total sem trilha (corrige o bypass da região 99 — MYS-008/REQ-ELI-005).
7. **CORS explícito.** Sem wildcard `*` em produção.
8. **Identidade gerenciada (Managed Identity)** para autenticação serviço-a-serviço no Azure; sem credenciais estáticas entre serviços.
9. **Integridade dos dados financeiros.** Arredondamento half-up único em todo o sistema (REQ-PAY-006), eliminando o truncamento divergente (MYS-005/INC-004).

---

## 3. Restrições Arquiteturais

- **Topologia:** monolito modular (ADR-001), 1 deployable, 4+ bounded contexts com fronteiras verificadas em CI (ArchUnit).
- **Integração híbrida por contrato** (ADR-002): síncrono só onde o contrato externo é síncrono (Receita); BB/CAIXA/SIAFI/CadÚnico assíncronos por arquivo, atrás de adapters.
- **Sem microsserviços** sem um ADR que supere o ADR-001 com justificativa de contexto.
- **Sem novas dependências** sem ADR justificando.
- **Stack-alvo fixa:** Java 21 + Spring Boot 3.3 + PostgreSQL 16 (backend); Next.js 15 + TS strict (frontend); Terraform (Azure); GitHub Actions.

---

## 4. Restrições de Dados

- Cada DDM Adabas vira um schema/tabela PostgreSQL; grupos `PE` (dependentes, descontos) viram tabelas filhas (1:N), não colunas repetidas.
- Migração de dados validada por conciliação antes do flip de cada contexto (ADR-003).
- Sem expurgo silencioso de histórico de pagamento/auditoria sem decisão de negócio registrada.

---

## 5. Quality Gates (aplicados no CI)

- `legacy-traceability`: rejeita REQ-ID sem `source_legacy:`.
- Fronteiras de módulo (ArchUnit) verdes.
- Cobertura mínima: backend ≥ 70%, frontend ≥ 60% de linhas.
- `terraform fmt` + `terraform validate` antes do merge.
- Pelo menos uma revisão entre pares antes do merge em `main`.

---

## 6. Workflow e Revisão

- Branch: `spec/<NNN>-<feature>` → `develop` → `main`.
- Todo recurso de infraestrutura tem `tags` com `project`, `environment`, `owner`.
- ADRs registram o "caminho não tomado"; substituem-se, nunca se apagam.

---

## 7. Protocolo de Violação

Quando uma restrição desta constituição for violada por uma proposta de spec, código ou infra:

1. **PARE** — não implemente.
2. **SINALIZE** no formato: `CONSTITUTION VIOLATION: [restrição] — [motivo]`.
3. **ESCALE** para decisão humana (PO + EA).
4. **DOCUMENTE** a exceção (com expiração) se aprovada, ou ajuste a proposta.

> Exemplo: replicar o aceite de CPF `000…` "porque o legado faz" → `CONSTITUTION VIOLATION: §2.5 (backdoor de teste em produção) — comportamento inseguro; usar ambiente de teste isolado`.

---

## Governança

Esta constituição supersede práticas individuais. Emendas exigem documentação, aprovação do Par 2 (EA+SA) com revisão do PO, e registro de versão abaixo. Toda PR é verificada quanto à conformidade; complexidade adicional precisa ser justificada em ADR.

**Referências:** [`.github/copilot-instructions.md`](.github/copilot-instructions.md) · [`02-spec-moderna/SPECIFICATION.md`](02-spec-moderna/SPECIFICATION.md) · [`02-spec-moderna/ADRs/`](02-spec-moderna/ADRs/) · [`01-arqueologia/mysteries-found.md`](01-arqueologia/mysteries-found.md)
