# Protótipo sisdnit 2.0 — Estágio 3 · Implementação

![ESTÁGIO 03 Implementação](https://img.shields.io/badge/ESTÁGIO-03%20Implementação-7FBA00?style=for-the-badge) ![Java 21](https://img.shields.io/badge/Java-21-1A1A1A?style=for-the-badge) ![PLAN ONLY infra](https://img.shields.io/badge/Infra-PLAN%20ONLY-D13438?style=for-the-badge)

> Núcleo de regras de negócio do sisdnit migrado do legado Natural/Adabas para Java 21,
> com testes de paridade contra o mainframe. Cada regra rastreia para um `BR-NNN`
> do [catálogo de regras](../../01-arqueologia/business-rules-catalog.md) e um programa
> `.NSN` em [`01-arqueologia/legado-sisdnit`](../../01-arqueologia/legado-sisdnit/).

## O que este protótipo é (e o que não é)

- **É** uma biblioteca de domínio (núcleo de cálculo e validação) com testes unitários — a
  fatia de maior risco do legado, traduzida com fidelidade e cobertura.
- **Não é** ainda uma aplicação web completa: não há controllers REST, persistência ativa
  nem frontend. As classes usam `@Service` do Spring, mas os métodos de cálculo são puros
  (sem I/O) para máxima testabilidade e paridade com o mainframe.
- A **infraestrutura** ([`infra/`](infra/)) e as **migrações** ([`backend/src/main/resources/db/migration`](backend/src/main/resources/db/migration/))
  descrevem a topologia-alvo e o schema, mas são **plan-only** no workshop (nunca aplicados).

## Estrutura

| Caminho | Conteúdo |
| --- | --- |
| [`backend/`](backend/) | Núcleo Java 21 (Maven). Pacotes por bounded context: `payment`, `validation`, `eligibility`. |
| [`backend/src/main/resources/db/migration/`](backend/src/main/resources/db/migration/) | Migrações Flyway (V2 cálculo, V3 beneficiário, V4 auditoria append-only). |
| [`infra/`](infra/) | Terraform plan-only da topologia-alvo no Azure (App Service + PostgreSQL 16). |

### Módulos do backend

| Pacote | O que faz | Regras (BR) | Programa legado |
| --- | --- | --- | --- |
| `payment` | Cálculo do benefício mensal, 13º/abono, descontos e correção retroativa IPCA. | BR-021..025, BR-031..036 | CALCBENF, CALCDSCT, CALCCORR |
| `validation` | Validação cadastral do beneficiário (CPF, nome, data, UF, RG, status). | BR-037..042 | VALBENEF, VALDOCS |
| `eligibility` | Avaliação de elegibilidade (status, faixa etária, renda, tipo de programa). | BR-043, BR-044 | VALELEG |

## Como rodar

Pré-requisitos: **JDK 21** e **Maven** (ou o wrapper, quando presente).

```bash
cd 03-implementacao/prototipo/backend

# Compilar e rodar os testes unitários
mvn -B test

# Build completo + gate de cobertura JaCoCo (≥ 70% de linha)
mvn -B verify
```

O relatório de cobertura é gerado em `target/site/jacoco/index.html` após `mvn verify`.

## Paridade com o legado (testes)

Vários serviços expõem dois caminhos: o comportamento **corrigido** do sisdnit 2.0 e um
`*LegacyParity` que **reproduz o bug do mainframe** para comparação. Os testes documentam
mistérios reais do legado:

| Mistério | Onde | Comportamento |
| --- | --- | --- |
| MYS-010 | `DescontoService` | Teto de desconto sobrescrevia descontos judiciais conforme a ordem; corrigido em `calculate`. |
| MYS-011 | `CorrecaoRetroativaService` | Tabela IPCA só cobre 2010–2012; o novo código sinaliza (`indexCovered`) em vez de zerar em silêncio. |
| MYS-014 | `BeneficiarioValidacaoService` | Prefixo especial de CPF anulava toda a validação (bypass); o novo código continua validando. |
| MYS-015 | `ElegibilidadeService` | Região 99 concedia elegibilidade automática (backdoor); agora exige revisão manual. |
| MYS-016 | `BeneficiarioValidacaoService` | Legado aceitava 29/02 em qualquer ano; o novo código valida o calendário. |

## CI

O pipeline [`.github/workflows/prototipo-backend.yml`](../../.github/workflows/prototipo-backend.yml)
roda em push/PR: `mvn verify` (build + testes + cobertura) e `terraform fmt/validate`
(plan-only). **Nunca** faz deploy nem `terraform apply`.
