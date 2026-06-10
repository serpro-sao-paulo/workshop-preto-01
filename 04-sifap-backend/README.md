# SIFAP 2.0 — Backend

Sistema de Fiscalização e Administração de Pagamentos — modernizado de Natural/Adabas (1997) para Java 21 + Spring Boot 3.3.

---

## O que este serviço faz

Calcula e registra pagamentos mensais de benefícios sociais para ~4,2 milhões de beneficiários. Substitui os programas Natural `CALCBENF.NSN`, `CALCDSCT.NSN` e `VALELEG.NSN` do SIFAP legado.

**Bounded contexts implementados:**

| Contexto | Responsabilidade |
|----------|-----------------|
| `beneficiary` | Cadastro, validação de CPF, máquina de status, dependentes |
| `program` | Parametrização de programas sociais e valor-base |
| `payment` | Cálculo do benefício, descontos, elegibilidade |
| `audit` | Trilha imutável de auditoria (mandato IN-TCU 63/2010) |
| `shared` | CPF, arredondamento, eventos de domínio |

---

## Como executar

### Pré-requisitos

- Docker Desktop em execução
- Java 21 (apenas para rodar sem Docker)

### Full stack com Docker Compose

```bash
# Na raiz do repositório
docker compose up -d

# Aguardar o backend subir (~90s na primeira vez)
docker compose logs -f backend
```

O backend está pronto quando os logs mostrarem `Started SifapApplication`.

### Só o backend (com Java local)

```bash
# Sobe só o banco
docker compose up -d postgres

# Roda o backend (Flyway migra o schema automaticamente)
./mvnw spring-boot:run
```

---

## Endpoints

| URL | Método | Descrição |
|-----|--------|-----------|
| `http://localhost:8080/actuator/health` | GET | Health check |
| `http://localhost:8080/swagger-ui/index.html` | GET | Documentação interativa da API |
| `http://localhost:8080/api-docs` | GET | Especificação OpenAPI JSON |
| `http://localhost:8080/api/v1/beneficiaries` | POST | Cadastrar beneficiário |
| `http://localhost:8080/api/v1/beneficiaries/{id}` | GET | Consultar beneficiário por ID |

Documentação completa: [docs/api-endpoints.md](../docs/api-endpoints.md)

---

## Rodar os testes

```bash
# Todos os testes (unit + integração com Testcontainers — requer Docker)
./mvnw verify

# Só testes unitários (sem Docker)
./mvnw test -Dtest="!*IT" -DfailIfNoTests=false

# Relatório de cobertura
open target/site/jacoco/index.html
```

**Gate de cobertura:** ≥ 70% de linhas nos pacotes `domain` e `application` (verificado pelo CI via JaCoCo).

---

## Stack técnica

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3 |
| ORM | JPA / Hibernate |
| Banco | PostgreSQL 16 |
| Migrations | Flyway (V1–V5) |
| Segurança | Spring Security + OAuth2 JWT |
| Documentação | SpringDoc OpenAPI 2.5 |
| Testes | JUnit 5 + Mockito + Testcontainers + ArchUnit |
| Cobertura | JaCoCo ≥ 70% |

---

## Migrations do banco

| Versão | Conteúdo |
|--------|----------|
| `V1__init_schema.sql` | Schema completo: tabelas `ben_*`, `prg_*`, `pay_*`, `aud_*` |
| `V2__extend_schema_from_ddm.sql` | Campos dos DDMs Adabas ausentes no V1 (endereço, contato, biometria, banco) |
| `V3__seed_programs.sql` | 10 programas sociais iniciais |
| `V4__performance_indexes.sql` | Índices para ciclo batch de 3,8M pagamentos/mês |
| `V5__security_constraints.sql` | Roles, REVOKE em `aud_audit_event`, triggers de imutabilidade |

---

## Estrutura de pacotes

```
br.gov.sifap/
├── beneficiary/
│   ├── domain/          # Entidades JPA, enums
│   ├── application/     # BeneficiaryService (@Transactional aqui)
│   └── infrastructure/  # BeneficiaryController, BeneficiaryRepository
├── payment/
│   ├── domain/          # BenefitCalculator, DiscountCalculator, EligibilityChecker
│   └── infrastructure/  # (PaymentController — backlog)
├── program/
│   ├── domain/          # SocialProgram
│   └── infrastructure/  # ProgramRepository
├── audit/
│   ├── domain/          # AuditEvent (@Immutable)
│   └── infrastructure/  # AuditEventRepository, AuditEventListener
└── shared/              # CpfValidator, MoneyRounding, GlobalExceptionHandler
```

**Regra arquitetural:** nenhum contexto importa classes de `domain/` ou `infrastructure/` de outro. Verificado pelo ArchUnit no CI.

---

## Rastreabilidade legado → código

| Programa Natural | Classe Java | REQ-IDs |
|-----------------|-------------|---------|
| `CALCBENF.NSN` | `BenefitCalculator` | REQ-PAY-001 a 006 |
| `CALCDSCT.NSN` | `DiscountCalculator` | REQ-PAY-004, 007 |
| `VALELEG.NSN` | `EligibilityChecker` | REQ-ELI-001 a 005 |
| `CADBENEF.NSN` | `BeneficiaryService` | REQ-BEN-001 a 007 |
| `CADPROG.NSN` | `SocialProgram` | REQ-PRG-001 |
| `AUDITORIA.ddm` | `AuditEvent` | REQ-AUD-001 |

Especificação completa: [02-spec-moderna/SPECIFICATION.md](../02-spec-moderna/SPECIFICATION.md)
