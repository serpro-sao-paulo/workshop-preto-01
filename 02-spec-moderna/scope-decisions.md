<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Decisões de Escopo — sisdnit 2.0

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S2](https://img.shields.io/badge/PREENCHA-Durante%20S2-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 2](README.md) → **Scope Decisions**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 2 (Spec Moderna).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento preenchido para sua feature
> 2. Rastreabilidade `source_legacy:` para cada REQ-ID
> 3. Sign-off do Product Owner antes da passagem H2
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Para cada funcionalidade encontrada no Estágio 1, decida: **Migrar**, **Descartar** ou **Evoluir**.
>
> - **Migrar**: trazer para o sisdnit 2.0 como está (mesma lógica, nova tecnologia)
> - **Descartar**: não trazer — funcionalidade obsoleta ou desnecessária
> - **Evoluir**: trazer E melhorar (nova UX, novo fluxo, nova capacidade)

**Time**: Par 2 · Arquitetura (Enterprise Architect + Software Architect)
**Data**: 19/05/2026
**Edição**: sisdnit 2.0 — Workshop Pretə
**Par 1 (Product Owner) responsável**: Escopo v1 assinado — ver §Aprovação e [`SPECIFICATION.md` §1.1](SPECIFICATION.md)

## Por que isso importa

O escopo é o que protege o time de chegar às 17h00 com 12 features pela metade. Se o Par 1 não cortar, o Estágio 3 não fecha. **Decisão difícil é tomada aqui, não no Estágio 3.**

## Como decidir

Pergunte de cada funcionalidade:

1. **Afeta o ciclo mensal de pagamento?** Sim → Migrar. Não → considere descartar.
2. **Tem uso documentado nos últimos 12 meses?** Não → descartar.
3. **Faz parte de um relatório regulatório obrigatório (TCU, CGU, BB)?** Sim → Migrar como está.
4. **Tem uma versão moderna mais barata de implementar?** Sim → Evoluir.

---

## Decisões por Funcionalidade

| #   | Funcionalidade            | Decisão                      | Justificativa | Regra de Negócio (BR-XXX) | Prioridade           |
| --- | ------------------------- | ---------------------------- | ------------- | ------------------------- | -------------------- |
| 1   | Cadastro de Beneficiários | Migrar | Núcleo do sistema; alimenta o ciclo mensal de pagamento. Migrar lógica como está, nova tecnologia. | BR-001..BR-012 | Alta |
| 2   | Consulta de Beneficiários | Evoluir | Migrar a consulta e melhorar UX (busca por CPF/NIS, paginação web em vez de tela 3270). | BR-001, BR-003 | Média |
| 3   | Registro de Pagamentos    | Migrar | Tabela `PAGAMENTO` (~180M registros) é fonte de verdade financeira; migrar modelo e histórico. | BR-019..BR-021 | Alta |
| 4   | Processamento Batch (BATCHPGT) | Migrar | Geração mensal da folha no 1º dia útil; coração financeiro. Manter idempotência e ordenação. | BR-019, BR-020, BR-021, BR-025 | Alta |
| 5   | Cálculo de Benefícios     | Migrar | Fatores regional/familiar/renda/idade + reajuste. Migrar como está; consolidar tabela regional (MYS-008). | BR-021, BR-022, BR-023, BR-024 | Alta |
| 6   | Validação de CPF          | Migrar | Módulo 11; regra de integridade obrigatória. Reaproveitada por todos os contextos. | BR-003 | Alta |
| 7   | Relatórios (BATCHREL)     | Evoluir | Relatórios regulatórios (TCU/CGU) obrigatórios → migrar; evoluir de impressão 66 linhas/página para exportação web/PDF. Corrigir arredondamento (MYS-007). | BR-027, BR-028 | Média |
| 8   | Conciliação Bancária (BATCHCON) | Migrar | Retorno CNAB 240 do BB define status financeiro; obrigatório. Migrar como está. | BR-029, BR-030 | Alta |
| 9   | Auditoria                 | Migrar | Trilha de auditoria (divergências, alterações) exigida por compliance. Migrar e centralizar. | BR-030 | Alta |
| 10  | Gestão de Usuários        | Evoluir | Autenticação 3270/RACF legada → evoluir para IdP moderno (OAuth2/OIDC) com RBAC. | [GREENFIELD] | Média |
| 11  | 13º e Abono de Dezembro   | Migrar | Regra sazonal de alto impacto (BR-025); parte do batch de dezembro. | BR-025 | Alta |
| 12  | Telas verdes 3270         | Descartar | Interface de terminal obsoleta; substituída por UI web (Next.js). Sem uso após migração. | — | Baixa |

> Adicione linhas para cada funcionalidade identificada no `discovery-report.md` do Estágio 1.

---

## Funcionalidades Novas (não existem no legado)

> Liste funcionalidades que o sisdnit 2.0 deveria ter e que não existem no sistema legado. Cada uma vira REQ-ID com `source_legacy: [GREENFIELD] <justificativa>`.

| #   | Funcionalidade Nova | Justificativa | Prioridade | Complexidade |
| --- | ------------------- | ------------- | ---------- | ------------ |
| N1  | Autenticação moderna (OAuth2/OIDC + RBAC) | Legado usa RACF/3270 sem perfis granulares; segurança e auditoria modernas exigem IdP. | Alta | Média |
| N2  | Reprocessamento idempotente sob demanda (re-run de competência) | Hoje o batch só roda no 1º dia útil; operadores precisam re-rodar com segurança após correção. | Média | Média |
| N3  | Painel de divergências de conciliação | Tornar visíveis as divergências (BR-030) que hoje só existem em arquivo de auditoria batch. | Média | Baixa |

---

## Resumo de Escopo

| Decisão   | Quantidade | Percentual |
| --------- | ---------- | ---------- |
| Migrar    | 8          | 67%        |
| Descartar | 1          | 8%         |
| Evoluir   | 3          | 25%        |
| **Total** | 12         | 100%       |

## Riscos de Escopo

> Liste os riscos das decisões tomadas:

| Risco | Probabilidade        | Impacto              | Mitigação |
| ----- | -------------------- | -------------------- | --------- |
| Ordenação por CPF tem dependentes downstream desconhecidos (MYS-006) | Média | Alto | Preservar ordenação por CPF no batch migrado até mapear consumidores. |
| Tabela de 27 fatores regionais duplicada (MYS-008) gera cálculo divergente | Alta | Alto | Consolidar em fonte única (tabela de configuração) no sisdnit 2.0. |
| Arredondamento relatório vs cálculo (MYS-007) quebra reconciliação TCU | Média | Alto | Decidir política única de arredondamento e documentar em ADR/relatório. |
| Migração de ~180M registros de PAGAMENTO | Média | Alto | Estratégia de carga incremental + validação de hash (campo existente no DDM). |

## Aprovação

- [x] Par 1 (Product Owner) aprovou as decisões de escopo — escopo v1 e não-escopo registrados em [`SPECIFICATION.md` §1.1](SPECIFICATION.md); 4 backdoors (MYS-014/015/017/018/019) marcados como **não migrar**; pendências de stakeholder (SENARC/Auditoria/TCU) sinalizadas.
- [x] Par 2 (Enterprise Architect) validou a viabilidade técnica
- [ ] Par 3 (Technical Lead) confirmou que cabe nas 3 horas do Estágio 3
- [ ] Time concordou com as prioridades

> **Aprovação obrigatória na Passagem #2** (~16:00). Sem ela, o Estágio 3 não começa.

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 2</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="ADR-TEMPLATE.md"><strong>ADR-TEMPLATE</strong></a><br/>
<sub>Template de ADR.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

