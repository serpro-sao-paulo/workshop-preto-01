# AGENTS.md — SIFAP 2.0

> Fonte de verdade de context engineering para GitHub Copilot.
> Mantido pelo **Technical Lead** (Par 3). Atualizar a cada mudança estrutural.
> Última revisão: 2026-06-10

---

## Stack

| Camada | Tecnologia | Versão |
| --- | --- | --- |
| Backend | Java + Spring Boot | 21 / 3.3 |
| ORM | JPA / Hibernate + PostgreSQL | — / 16 |
| Frontend | Next.js + TypeScript | 15 / 5 (strict) |
| Estilos | Tailwind CSS + shadcn/ui | — |
| Containers | Docker + Docker Compose | — |
| IaC | Terraform (Azure provider) | ~> 3.x |
| CI/CD | GitHub Actions | — |
| Testes (BE) | JUnit 5 + Testcontainers | — |
| Testes (FE) | Vitest + Testing Library | — |

---

## Comandos essenciais

```bash
# Backend
./mvnw verify                          # build + testes + ArchUnit
./mvnw test -pl payment                # testes só do módulo payment
./mvnw spring-boot:run                 # subir localmente (sem Docker)

# Full stack local
docker compose up -d                   # sobe app + postgres:16 (Dockerfile multi-stage)
docker compose logs -f backend         # acompanhar logs

# Terraform (validate only — nunca apply em CI)
cd infra && terraform init -backend=false
terraform validate
terraform fmt -check -recursive

# CI/CD — GitHub Secrets obrigatórios:
# AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_SUBSCRIPTION_ID (OIDC federated identity)
# GitHub Variables (vars.*): ACR_REGISTRY, ACR_NAME, AZURE_WEBAPP_NAME_DEV, AZURE_WEBAPP_NAME_STAGE

# Frontend
cd frontend && npm run dev             # dev server :3000
cd frontend && npm run lint            # ESLint
cd frontend && npm test                # Vitest

# Database
./mvnw flyway:migrate                  # rodar migrations manualmente
```

---

## Bounded Contexts (módulos Java)

Definidos em [`02-spec-moderna/bounded-contexts.md`](02-spec-moderna/bounded-contexts.md).

| Contexto | Package | Owner | Tabelas |
| --- | --- | --- | --- |
| `beneficiary` | `br.gov.sifap.beneficiary` | Par 3 · Dev | `ben_*` |
| `program` | `br.gov.sifap.program` | Par 3 · Dev | `prg_*` |
| `payment` | `br.gov.sifap.payment` | Par 3 · Dev | `pay_*` |
| `audit` | `br.gov.sifap.audit` | Par 3 · Dev | `aud_*` |
| `shared` | `br.gov.sifap.shared` | — (cross-cutting) | — |

**Regra de ouro:** nenhum contexto importa classes de `domain/` ou `infrastructure/` de outro. Verificado por ArchUnit no CI.

---

## Convenções de código

### Java (backend)

- **Camadas por contexto:** `domain/` → `application/` → `infrastructure/`. Dependência sempre inward.
- **`@Transactional`** somente em `*Service` (application layer). Nunca em repository ou controller.
- **DTOs como records:** `public record PaymentDto(UUID id, BigDecimal valorLiquido) {}`
- **Validação** na camada controller com `@Valid` + Bean Validation. Nunca no service.
- **CPF em logs**: sempre mascarado — usar `CpfMaskingConverter`. Jamais logar CPF completo.
- **Aritmética financeira**: `BigDecimal` com `RoundingMode.HALF_UP` via `MoneyRounding` (shared). Nunca `double` ou `float`.
- **Nomes**: classes em PascalCase inglês; packages em lowercase; rotas em kebab-case.
- **Injeção**: constructor injection; usar `@RequiredArgsConstructor` (Lombok) ou construtor explícito. Sem `@Autowired` em campo.

### TypeScript (frontend)

- `strict: true` em `tsconfig.json` — sem exceções.
- Somente **named exports** — sem default exports em componentes.
- Server components por padrão; `"use client"` somente quando necessário (event handlers, hooks).
- Server actions para mutations — nunca expor secrets em client components.
- `async/await` — nunca cadeias `.then()`.

### REST API

- Prefixo: `/api/v1/{resource}`
- HTTP verbs corretos: `GET` (leitura), `POST` (criação → 201), `PUT/PATCH` (atualização), `DELETE` (204).
- `409` para conflito (CPF duplicado), `422` para inelegível, `404` para não encontrado.
- Todos os endpoints com annotations OpenAPI/Swagger (`@Operation`, `@ApiResponse`).

---

## Padrões de teste

| Tipo | Ferramenta | Cobertura mínima | Onde vive |
| --- | --- | --- | --- |
| Unitário (BE) | JUnit 5 + Mockito | 70% domain + application | `src/test/java/` |
| Integração (BE) | Testcontainers + MockMvc | happy + auth fail + validation fail | `src/test/java/.../IT*.java` |
| Unitário (FE) | Vitest + Testing Library | 60% componentes | `src/**/*.test.tsx` |

Todo teste referencia o REQ-ID que cobre via comentário: `// @implements REQ-PAY-001`.

---

## Policy de PR / Code Review

| Critério | Regra |
| --- | --- |
| Tamanho máximo | 400 linhas (diff) |
| Latência de review | < 4h em dias de trabalho |
| Aprovações mínimas | 1 (par do mesmo par) |
| ArchUnit | CI deve estar verde antes de merge |
| `main` | Sempre verde. Nenhum merge com CI vermelho. |
| Branches | `feature/<REQ-ID>-<slug>` → `develop` → `main` |
| Commits | Conventional Commits: `feat(payment): add BenefitCalculator` |

**Blocking vs non-blocking:**
- 🔴 **Blocking:** cross-context import, CPF em log, secret hardcoded, ArchUnit violado, cobertura < 70%.
- 🟡 **Non-blocking (suggestion):** estilo de nomenclatura, ordem de métodos, javadoc ausente.

---

## Segurança (OWASP — não negociável)

- Sem secrets no código. Usar Azure Key Vault via Managed Identity.
- SQL somente via JPA/JPQL — sem concatenação de strings.
- CORS explícito — sem wildcard `*` em produção.
- Autenticação OAuth2/JWT (Spring Security).
- CPF e valores de benefício nunca em logs sem máscara.

---

## Agentes disponíveis

| Agent | Arquivo | Quando usar |
| --- | --- | --- |
| `tech-lead` | `.github/agents/tech-lead.agent.md` | Curadoria CODEMAP, audit de contexto, routing |
| `software-architect` | `.github/agents/software-architect.agent.md` | C4, bounded contexts, API validation |
| `builder` | `.github/agents/builder.agent.md` | Implementação de features (Stage 3) |
| `dba` | `.github/agents/dba.agent.md` | Migrations Flyway, query tuning |
| `qa-engineer` | `.github/agents/qa-engineer.agent.md` | Cobertura, testes de equivalência |
| `devops-engineer` | `.github/agents/devops-engineer.agent.md` | CI/CD, IaC, Docker |

---

## Links-chave

| Artefato | Path |
| --- | --- |
| Especificação funcional (EARS) | [`02-spec-moderna/SPECIFICATION.md`](02-spec-moderna/SPECIFICATION.md) |
| Bounded contexts + C4 L2/L3 | [`02-spec-moderna/bounded-contexts.md`](02-spec-moderna/bounded-contexts.md) |
| Plano de implementação | [`03-implementacao/IMPLEMENTATION_PLAN.md`](03-implementacao/IMPLEMENTATION_PLAN.md) |
| Codemap do contexto payment | [`03-implementacao/codemap-payment.md`](03-implementacao/codemap-payment.md) |
| Regras de negócio (36 BRs) | [`01-arqueologia/business-rules-catalog.md`](01-arqueologia/business-rules-catalog.md) |
| Constituição arquitetural | [`CONSTITUTION.md`](CONSTITUTION.md) |
| ADR-001 Modular Monolith | [`08-exemplos/ADR-001-monolito-modular-exemplo.md`](08-exemplos/ADR-001-monolito-modular-exemplo.md) |
| ADR-002 Integrações | [`02-spec-moderna/ADRs/ADR-002-integracao-sistemas-externos.md`](02-spec-moderna/ADRs/ADR-002-integracao-sistemas-externos.md) |
| ADR-003 Strangler Fig | [`02-spec-moderna/ADRs/ADR-003-coexistencia-strangler-fig.md`](02-spec-moderna/ADRs/ADR-003-coexistencia-strangler-fig.md) |
| ADR-004 Estrutura de módulos | [`02-spec-moderna/ADRs/ADR-004-estrutura-modulos-persistencia.md`](02-spec-moderna/ADRs/ADR-004-estrutura-modulos-persistencia.md) |
