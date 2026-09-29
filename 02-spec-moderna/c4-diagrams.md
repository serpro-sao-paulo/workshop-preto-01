<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Diagramas C4 — sisdnit 2.0

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIME Par 2 · Arquitetura](https://img.shields.io/badge/TIME-Par%202%20·%20Arquitetura-1A1A1A?style=for-the-badge)

> Artefato do **Par 2 · Arquitetura**. C4 L1 (System Context) pelo Enterprise Architect; C4 L2/L3 (Containers/Components) pelo Software Architect.
> Base: `bounded-contexts.md`, `01-arqueologia/discovery-report.md`, `dependency-map.md`.

---

## C4 Nível 1 — Contexto do Sistema

Mostra o sisdnit 2.0 como uma caixa e seus atores/sistemas externos.

```mermaid
flowchart TB
    OPER["👤 Operador SENARC<br/>(gera folha, consulta)"]
    GESTOR["👤 Gestor / Auditor<br/>(relatórios, trilha)"]
    BENEF["👤 Beneficiário<br/>(consulta indireta)"]

    sisdnit["🏛️ sisdnit 2.0<br/>Sistema de administração<br/>de pagamentos sociais"]

    BB["🏦 Banco do Brasil<br/>(arquivo retorno CNAB 240)"]
    SIAFI["🏛️ SIAFI<br/>(integração orçamentária —<br/>a confirmar)"]
    TCU["🏛️ TCU / CGU<br/>(relatórios regulatórios)"]

    OPER -->|"gera/consulta pagamentos"| sisdnit
    GESTOR -->|"relatórios e auditoria"| sisdnit
    BENEF -->|"consulta benefício"| sisdnit
    sisdnit -->|"envia remessa / lê retorno"| BB
    sisdnit -.->|"presta contas orçamentárias<br/>(a confirmar com SENARC)"| SIAFI
    sisdnit -->|"exporta relatórios"| TCU

    classDef sys fill:#00A4EF,stroke:#0a2540,color:#ffffff
    classDef ext fill:#1f2937,stroke:#475569,color:#e2e8f0
    classDef tbd fill:#3f3f46,stroke:#a1a1aa,color:#e4e4e7,stroke-dasharray: 4 3
    classDef person fill:#0f172a,stroke:#334155,color:#e2e8f0
    class sisdnit sys
    class BB,TCU ext
    class SIAFI tbd
    class OPER,GESTOR,BENEF person
```

> **Rastreabilidade dos sistemas externos.** BB/CNAB 240 (BR-029/BR-030, REQ-REC-001..003) e TCU/CGU (MYS-007 — relatórios regulatórios) estão ancorados na arqueologia. O **SIAFI não tem evidência de integração no legado** — aparece apenas como referência cosmética do terminal 3270 (fidelidade visual SIAFI/SIAPE na demo). É uma integração-alvo **presumida, a confirmar com o SENARC** (ver [`adr/ADR-004-integracao-coexistencia.md`](adr/ADR-004-integracao-coexistencia.md)).

---

## C4 Nível 2 — Containers

Decompõe o sisdnit 2.0 em aplicações/serviços executáveis e armazéns de dados. Monólito modular (ver `adr/ADR-001`).

```mermaid
flowchart TB
    subgraph sisdnit["🏛️ sisdnit 2.0 (Monólito Modular)"]
        WEB["🖥️ Web App<br/>Next.js<br/>(UI operadores/gestores)"]
        API["⚙️ Backend API<br/>Java 21 + Spring Boot<br/>(módulos: Beneficiário, Pagamento, Conciliação, Auditoria)"]
        BATCH["⏱️ Batch Runner<br/>Spring Batch<br/>(folha mensal, conciliação)"]
        DB[("🗄️ PostgreSQL<br/>beneficiários, pagamentos,<br/>programas, auditoria")]
    end

    OPER["👤 Operador / Gestor"]
    BB["🏦 Banco do Brasil<br/>(CNAB 240)"]

    OPER -->|"HTTPS"| WEB
    WEB -->|"REST/JSON"| API
    API -->|"JPA"| DB
    BATCH -->|"JPA"| DB
    BATCH -->|"lê retorno"| BB
    API -.->|"agenda/dispara"| BATCH

    classDef cont fill:#0e7490,stroke:#0a2540,color:#ffffff
    classDef data fill:#334155,stroke:#64748b,color:#e2e8f0
    classDef ext fill:#1f2937,stroke:#475569,color:#e2e8f0
    classDef person fill:#0f172a,stroke:#334155,color:#e2e8f0
    class WEB,API,BATCH cont
    class DB data
    class BB ext
    class OPER person
```

---

## C4 Nível 3 — Componentes (Backend API + Batch)

Detalha os componentes internos alinhados aos bounded contexts. Foco do Par 2: módulo de **Pagamento** e **Conciliação** (batches).

```mermaid
flowchart TB
    subgraph API["⚙️ Backend API + Batch Runner"]
        direction TB

        subgraph PAY["📦 Módulo Pagamento"]
            FOLHA["FolhaMensalService<br/>(BR-019, BR-020 idempotência)"]
            CALC["CalculoBeneficioService<br/>(BR-021..BR-024 fatores)"]
            DEC["DecimoTerceiroService<br/>(BR-025 13º + abono)"]
            DESC["DescontoService<br/>(BR-026)"]
        end

        subgraph REC["📦 Módulo Conciliação"]
            CNAB["CnabParser<br/>(CNAB 240, registros tipo 3)"]
            CONC["ConciliacaoService<br/>(BR-029 status, BR-030 divergência)"]
        end

        subgraph BEN["📦 Módulo Beneficiário"]
            BENSVC["BeneficiarioService<br/>(BR-001..BR-012)"]
            CPF["CpfValidator<br/>(BR-003 Módulo 11 · shared kernel)"]
        end

        subgraph AUD["📦 Módulo Auditoria"]
            AUDSVC["AuditoriaService<br/>(trilha BR-030)"]
        end

        REGTAB["TabelaFatorRegional<br/>(config única — resolve MYS-008)"]
    end

    DB[("🗄️ PostgreSQL")]
    BB["🏦 CNAB 240"]

    FOLHA --> CALC
    FOLHA --> DEC
    FOLHA --> DESC
    CALC --> REGTAB
    DEC --> REGTAB
    FOLHA --> BENSVC
    BENSVC --> CPF
    FOLHA -.->|"evento"| AUDSVC
    CNAB --> CONC
    CONC --> CNAB
    CONC -->|"atualiza status"| FOLHA
    CONC -.->|"divergência > R$0,01"| AUDSVC
    CONC --> BB
    FOLHA --> DB
    CONC --> DB
    BENSVC --> DB
    AUDSVC --> DB

    classDef comp fill:#0e7490,stroke:#0a2540,color:#ffffff
    classDef shared fill:#1e293b,stroke:#64748b,color:#e2e8f0,stroke-dasharray: 4 3
    classDef data fill:#334155,stroke:#64748b,color:#e2e8f0
    classDef ext fill:#1f2937,stroke:#475569,color:#e2e8f0
    class FOLHA,CALC,DEC,DESC,CNAB,CONC,BENSVC,AUDSVC comp
    class CPF,REGTAB shared
    class DB data
    class BB ext
```

---

**Definição de Pronto:** L1 (atores + sistemas externos), L2 (containers + tecnologias), L3 (componentes rastreados a BR-XXX), todos os Mermaid renderizam. Decisão de topologia (monólito modular) registrada em `adr/ADR-001-monolito-modular.md`.
