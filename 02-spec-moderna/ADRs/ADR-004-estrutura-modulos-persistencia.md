<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-004 — Estrutura Interna de Módulos e Persistência por Bounded Context

![Status](https://img.shields.io/badge/STATUS-accepted-green?style=for-the-badge) ![Par 2 · SA](https://img.shields.io/badge/PAR-Par%202%20·%20SA-FFB900?style=for-the-badge)

**Data:** 2026-06-10
**Deciders:** Software Architect (Par 2), Technical Lead (Par 3)
**Consultados:** DBA (Par 4), Enterprise Architect (Par 2)

---

## Contexto

Definidos os 5 bounded contexts (`beneficiary`, `program`, `payment`, `audit`, `shared`), precisamos de uma convenção de estrutura interna **uniforme** — camadas, ownership de dados e comunicação entre contextos — para que o time possa trabalhar em paralelo sem breaking changes implícitos.

O legado usa um único arquivo Adabas por conceito (FNR 150-153); no SIFAP 2.0 cada bounded context é dono exclusivo de seu schema PostgreSQL.

---

## Decisão

### D1: Package-by-feature com 3 camadas por contexto

```
<context>/
  domain/        ← entidades JPA, domain services, interfaces (ports)
  application/   ← services que orquestram (casos de uso)
  infrastructure/ ← controllers REST, JPA repositories, adapters externos
```

`shared/` contém apenas concerns cross-cutting (sem camadas internas).

### D2: Nenhum contexto importa classes de `domain/` ou `infrastructure/` de outro contexto

Comunicação permitida:
1. **Leitura síncrona via interface** — `PaymentService` injeta `BeneficiaryService` via interface declarada em `beneficiary/domain/`; nunca injeta a classe concreta.
2. **Eventos de domínio** — publicados via `ApplicationEventPublisher` do Spring; consumidos por `@EventListener` no `audit/`.
3. **Records imutáveis** em `shared/` usados como DTOs entre contextos.

Regra verificada por **ArchUnit** no CI (F0-T04):

```java
noClasses().that().resideInAPackage("..payment..")
    .should().dependOnClassesThat()
    .resideInAPackage("..beneficiary..infrastructure..")
    .check(importedClasses);
```

### D3: Schema PostgreSQL por bounded context — mesmo banco, prefixo de tabela diferente

| Bounded context | Prefixo de tabela |
| --- | --- |
| `beneficiary` | `ben_` |
| `program` | `prg_` |
| `payment` | `pay_` |
| `audit` | `aud_` |

Permite futura separação em schemas distintos (ou microservices) sem renomear tabelas.

### D4: `audit_event` é imutável — sem `UPDATE`, sem `DELETE`

`AuditEvent` (@Entity) sem setters; Spring `@Immutable`; repository não expõe métodos de escrita além de `save()`. Flyway aplica constraint `REVOKE UPDATE, DELETE ON aud_audit_event FROM app_user`.

### D5: `@Transactional` somente em `*Service` (application layer)

Nunca em `*Repository`, `*Controller` ou domain services. Domain services são POJOs puros sem dependência de framework.

---

## Alternativas consideradas

| Alternativa | Motivo de rejeição |
| --- | --- |
| **Schema PostgreSQL separado por contexto** | Complexidade operacional desnecessária (hackathon de 8h); não resolve o problema de cruzamento de imports que é puramente de código |
| **Uma única camada flat por contexto** | Viola o princípio de inversão de dependência; domain service dependeria de JPA diretamente |
| **Módulos Maven por bounded context** | Overhead de build e configuração para o tempo disponível; ArchUnit dá a mesma garantia com custo zero |
| **Comunicação entre contextos via REST interno** | Latência e complexidade de rede desnecessárias dentro de um único processo |

---

## Consequências

**Positivas:**
- Times paralelos (TL + Dev) podem trabalhar em `beneficiary` e `payment` simultaneamente sem conflito.
- ArchUnit CI elimina introdução acidental de acoplamento cross-context.
- Schema prefixado permite zero-downtime split em microservices no futuro.
- Domain services puros são testáveis sem Spring context.

**Negativas / Trade-offs:**
- Alguma verbosidade de interfaces onde uma chamada direta seria mais simples (mitigado: os contextos são 5, não 50).
- `EligibilityChecker` tem duas dependências cross-context (`BeneficiaryService`, `ProgramService`); isso é inevitável — está documentado em [codemap-payment.md](../../03-implementacao/codemap-payment.md).

---

## Gate de verificação

- [ ] `mvn test` com `ArchUnitTest` verde antes do merge em `develop`
- [ ] Nenhuma tabela sem prefixo de contexto no schema final
- [ ] `AuditEvent` sem setters públicos (verificável por code review)
