<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-002: PostgreSQL como banco relacional do sisdnit 2.0

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![AUTOR Par 2 · Software Architect](https://img.shields.io/badge/AUTOR-Par%202%20·%20Software%20Architect-1A1A1A?style=for-the-badge)

## Status

Aceita

## Data

2026-05-19

## Contexto

O legado usa Adabas com DDMs (`BENEFICIARIO`, `PAGAMENTO` com ~180M registros, `PROGRAMA-SOCIAL`, `AUDITORIA`), incluindo grupos periódicos (MU/PE) e campos descritores. Precisamos de uma persistência moderna que: (1) suporte volume e integridade financeira (BR-021, BR-029, BR-030); (2) preserve a rastreabilidade e a trilha de auditoria; (3) seja viável para o time e operável em nuvem. O modelo de dados é fortemente relacional (chaves CPF/NIS, competência, relacionamentos beneficiário↔pagamento).

## Opções Consideradas

### Opção 1: PostgreSQL (relacional)

- **Prós:** Forte consistência ACID para finanças; maduro e gratuito; suporta `NUMERIC` exato (crítico para truncamento BR-021/BR-028); particionamento por competência para a tabela de 180M; ecossistema Spring Data/JPA; suporte a JSONB para grupos periódicos quando necessário.
- **Contras:** Escala horizontal de escrita exige particionamento/sharding manual; migração do modelo MU/PE do Adabas requer remodelagem.

### Opção 2: Banco NoSQL de documentos (ex.: MongoDB)

- **Prós:** Modela grupos periódicos (dependentes, descontos) naturalmente como documentos aninhados.
- **Contras:** Transações multi-documento e integridade referencial mais fracas — risco para conciliação financeira (BR-029/BR-030); agregações financeiras menos diretas; precisão decimal exige cuidado extra.

### Opção 3: Permanecer em Adabas / banco mainframe

- **Prós:** Sem migração de dados; compatível com o legado.
- **Contras:** Não atende à modernização; licenciamento e escassez de competências; mantém acoplamento ao mainframe.

## Decisão

Adotamos **PostgreSQL** (Opção 1) como banco relacional único do monólito modular. Usaremos tipo `NUMERIC(p,s)` para valores monetários (garantindo truncamento determinístico de BR-021 e a política única de arredondamento de REQ-RPT-002), particionamento da tabela `pagamento` por competência (`ano_mes_ref`), e validação de carga via hash (campo já existente no DDM `PAGAMENTO`). Grupos periódicos viram tabelas filhas (dependentes, descontos) ou JSONB quando a leitura for sempre conjunta.

## Consequências

### Positivas

- Integridade financeira e auditoria garantidas por ACID.
- Precisão decimal exata evita novas divergências de arredondamento (MYS-007).
- Particionamento por competência torna a folha mensal e consultas históricas eficientes.

### Negativas

- Migração dos ~180M registros exige estratégia incremental e janela de validação.
- Remodelagem dos grupos MU/PE do Adabas para o relacional adiciona esforço.

## Requisitos Relacionados

- REQ-PAY-002 (idempotência por competência), REQ-PAY-003 (precisão de cálculo), REQ-REC-003 (auditoria de divergência), REQ-RPT-002 (arredondamento)

---

**DoD:** Formato MADR, 3 opções com prós/contras, decisão datada.
