<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Descoberta — Estágio 1: Arqueologia Digital

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **discovery-report**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado sisdnit
> 2. Rastreabilidade para `01-arqueologia/legado-sisdnit/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Este documento consolida todas as descobertas do Estágio 1.
> Preencha cada seção com as conclusões do time. **Este é o input principal do Estágio 2** — sem ele, a especificação vira chute.

**Time**: Par 1 · Visão (Product Owner + Requirements Engineer)
**Data**: 19/05/2026
**Edição**: Workshop sisdnit — Estágio 1 (Arqueologia)
**Participantes**: Product Owner, Requirements Engineer — agente `@archaeologist`

---

## 1. Sumário Executivo

> Em 3 a 5 frases, resuma o que o time descobriu sobre o sisdnit legado.
> O que é este sistema? Qual sua criticidade? Qual o estado do código?

O sisdnit (Sistema Fiscal de Administração de Pagamentos) é um sistema Natural/Adabas de 29 anos que cadastra beneficiários, seus dependentes e os programas sociais que pagam benefícios a milhões de pessoas. O Par 1 analisou o núcleo de **cadastros** (`CADBENEF`, `CADDEPEND`, `CADPROG`) e extraiu **18 regras de negócio**, das quais 3 são críticas (validação de CPF por Módulo 11, status automático de idosos e o Fator-K de valor do programa). O código funciona, mas carrega regras tacitamente codificadas e **divergências entre programa e DDM** que ninguém documentou. Foram registrados **5 mistérios de alta confiança**, incluindo uma constante mágica financeira (`0,347215`) e um provável bug histórico (idoso → status SUSPENSO). Sem decisões do PO sobre esses pontos, a especificação do Estágio 2 herdaria ambiguidades de risco financeiro.

---

## 2. Visão Geral do Sistema

### 2.1 Propósito do sisdnit

Gerir o ciclo de pagamentos de programas sociais: cadastrar beneficiários e dependentes, manter o catálogo de programas sociais (com valores e critérios de elegibilidade) e servir de base para os batches de cálculo e pagamento. O Par 1 cobriu a porta de entrada do domínio — as **entidades** que viram subject das EARS no Estágio 2.

### 2.2 Arquitetura Legada

15 programas Natural + 4 DDMs Adabas (`BENEFICIARIO`/ARQ 150, `PAGAMENTO`, `PROGRAMA-SOCIAL`/ARQ 155, `AUDITORIA`). Os 3 cadastros do Par 1 são programas **online** (terminal 3270), autônomos, sem `CALLNAT` entre si. A validação de CPF é uma subrotina interna ao `CADBENEF`. Dependentes são gravados em um **grupo periódico (PE)** dentro do registro do beneficiário.

### 2.3 Usuários e Perfis

Operadores administrativos que incluem/alteram beneficiários (`I`/`A`), incluem dependentes e cadastram/consultam programas (`I`/`C`). Não há operação de exclusão — o ciclo de vida é controlado por status (`A`/`S`/`C`/`D`).

---

## 3. Principais Descobertas

### 3.1 Regras de Negócio Críticas

> Liste as 5 regras de negócio mais importantes encontradas.

1. **BR-003** — Validação de CPF por Módulo 11 (2 dígitos verificadores). CRÍTICA: porta de entrada do beneficiário; candidata a serviço compartilhado.
2. **BR-017** — Fator-K ajusta o valor base do programa: `VLR-BASE × (1,00 + FATOR-REAJUSTE × 0,347215)`. CRÍTICA: define o valor financeiro de cada programa.
3. **BR-010** — Beneficiário > 75 anos recebe status `S` (que no DDM significa SUSPENSO). CRÍTICA: regra escondida, provável bug.
4. **BR-007** — Unicidade do beneficiário por CPF (inclusão exige CPF inexistente; alteração exige CPF existente). ALTA.
5. **BR-013** — Limite de 5 dependentes por beneficiário (DDM permite 10). ALTA: conflito programa × estrutura de dados.

### 3.2 Dependências Complexas

> Quais programas estão mais acoplados? Onde há risco de efeito cascata?

Baixo acoplamento entre os cadastros do Par 1 — são autônomos. O ponto de acoplamento lógico é a chave `COD-PROGRAMA` (beneficiário → programa social) e o `STATUS` do beneficiário, lido por `CADDEPEND` para bloquear inclusão de dependentes. A subrotina `VALIDA-CPF` está embutida em `CADBENEF` (duplicação em potencial com os validadores do Par 4).

### 3.3 Dívida Técnica Identificada

> Que problemas no código legado vão complicar a migração?

- [x] Divergência de códigos de domínio entre programa e DDM (sexo, parentesco) — ver MYS-004.
- [x] Constante financeira mágica sem origem documentada (`0,347215`) — ver MYS-005.
- [x] Reuso semântico ambíguo do status `S` (suspenso vs idoso) — ver MYS-001.
- [x] Cálculo de idade impreciso (só por ano) — ver MYS-002.

### 3.4 Gaps de Documentação

> O que a documentação existente NÃO cobre?

A documentação parcial (1997–2018) não explica a origem do Fator-K, o significado do status `S` para idosos, nem por que os códigos de domínio diferem entre código e DDM. As regras vivem apenas no código Natural.

---

## 4. Mistérios e Riscos

### 4.1 Mistérios Não Resolvidos

> Resuma os mistérios do arquivo `mysteries-found.md` que permanecem sem explicação.

| ID  | Descrição | Risco para Migração |
| --- | --------- | ------------------- |
| MYS-001 | Idoso > 75 anos vira status `S` (SUSPENSO no DDM) | Idosos podem ser excluídos da folha por engano |
| MYS-002 | Idade calculada só por ano (ignora mês/dia) | Limiar de 75 anos disparado até 11 meses antes |
| MYS-003 | Limite de 5 dependentes no código vs 10 no DDM | Registros com 6–10 dependentes podem ficar invisíveis |
| MYS-004 | Códigos de sexo/parentesco divergem programa × DDM | Dados legados podem ser rejeitados ou mal mapeados |
| MYS-005 | Constante mágica `0,347215` no Fator-K | Valor financeiro de todo programa depende de origem desconhecida |

### 4.2 Riscos para o Estágio 2

> O que o time de especificação precisa saber antes de começar?

1. O significado do status `S` para idosos precisa de decisão do PO antes de virar requisito (BR-010 / MYS-001).
2. A constante do Fator-K exige confirmação com o SENARC antes de ser especificada (BR-017 / MYS-005).
3. Os códigos de domínio canônicos (sexo, parentesco) e o limite de dependentes precisam ser reconciliados na spec (MYS-003 / MYS-004).

---

## 5. Recomendações

### 5.1 O que migrar primeiro

> Com base na priorização do Par 1 (Product Owner), quais funcionalidades devem ser migradas primeiro?

| Prioridade | Funcionalidade | Justificativa |
| ---------- | -------------- | ------------- |
| 1          | Cadastro de beneficiário + validação de CPF | Entidade central; sem ela não há pagamento. Afeta o ciclo mensal. |
| 2          | Cadastro de programas sociais (com Fator-K) | Define o valor pago; regra financeira crítica. |
| 3          | Cadastro de dependentes | Depende do beneficiário; impacta valor e elegibilidade. |

### 5.2 O que descartar

> Funcionalidades que provavelmente não precisam ser migradas:

- Telas de terminal 3270 (INPUT/WRITE): substituídas por API/UI moderna — a lógica de negócio permanece, a apresentação não.

### 5.3 O que evoluir

> Funcionalidades que devem ser migradas E melhoradas:

- Cálculo de idade: migrar considerando mês/dia (corrigir MYS-002).
- Status do beneficiário: introduzir um código explícito para "sênior" em vez de reusar `S` (resolver MYS-001).
- Validação de CPF: extrair `VALIDA-CPF` como serviço compartilhado reutilizável.

---

## 6. Métricas do Estágio

| Métrica                       | Valor        |
| ----------------------------- | ------------ |
| Programas analisados          | 3 / 15 (Par 1)  |
| DDMs mapeados                 | 2 / 4 (BENEFICIARIO, PROGRAMA-SOCIAL)   |
| Regras de negócio encontradas | 18       |
| Regras escondidas encontradas | 2 / 10 (BR-010, BR-017)  |
| Easter eggs encontrados       | 0 / 3   |
| Termos no glossário           | 34       |
| Mistérios catalogados         | 5       |
| Tempo total gasto             | ~2 horas |

---

## 7. Notas para o Próximo Estágio

> Deixe aqui mensagens para o time no Estágio 2 (Especificação Moderna):

As entidades `Beneficiario`, `Dependente` e `ProgramaSocial` estão prontas para virar bounded contexts. Antes de escrever EARS, **resolvam 3 decisões de negócio com o PO**: (1) status do idoso (MYS-001), (2) origem/valor do Fator-K com o SENARC (MYS-005), (3) códigos de domínio canônicos e limite de dependentes (MYS-003/004). A validação de CPF (BR-003) deve virar um requisito reutilizável. Cada EARS deve referenciar o `source_legacy:` do catálogo de regras.

---

## Definição de Pronto deste relatório

- [ ] Todas as seções acima preenchidas (sem placeholders).
- [ ] Pelo menos 5 regras críticas listadas em §3.1, cada uma referenciando uma `BR-XXX` do catálogo.
- [ ] Decisões de migrar/descartar/evoluir em §5 cobrem as 8+ funcionalidades principais.
- [ ] Métricas de §6 conferem com os outros artefatos (glossary.md, business-rules-catalog.md, mysteries-found.md).

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-found.md"><strong>mysteries-found.md</strong></a><br/>
<sub>Lista de mistérios.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec</strong></a><br/>
<sub>Próximo estágio: spec moderna.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

