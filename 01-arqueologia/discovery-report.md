<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Descoberta — Estágio 1: Arqueologia Digital

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **discovery-report**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Este documento consolida todas as descobertas do Estágio 1.
> Preencha cada seção com as conclusões do time. **Este é o input principal do Estágio 2** — sem ele, a especificação vira chute.

**Time**: Par 1 · Visão (Product Owner + Requirements Engineer)
**Data**: 10/06/2026
**Edição**: Workshop de Modernização de Legado — SIFAP 2.0
**Participantes**: Product Owner (escopo/prioridade) + Requirements Engineer (catálogo de regras / EARS)

---

## 1. Sumário Executivo

> Em 3 a 5 frases, resuma o que o time descobriu sobre o SIFAP legado.
> O que é este sistema? Qual sua criticidade? Qual o estado do código?

O SIFAP é um sistema Natural/Adabas de 29 anos (missão crítica, nível 1) que administra a folha mensal de benefícios sociais de ~3,8 milhões de famílias. O conhecimento das regras de cálculo reside **exclusivamente no código-fonte** — a equipe original se aposentou e a documentação parou em 2012. A arqueologia revelou **36 regras de negócio**, das quais **10 estavam escondidas** (sem comentário, constantes mágicas, casos especiais) e **14 são críticas** por terem impacto financeiro direto. O núcleo de valor está em três eixos: cálculo do benefício (CALCBENF), descontos com teto de 30% (CALCDSCT) e elegibilidade (VALELEG). Existem riscos graves de migração: truncamento de centavos sistemático, suspensão automática silenciosa de idosos e dois backdoors de validação ainda ativos em produção.

---

## 2. Visão Geral do Sistema

### 2.1 Propósito do SIFAP

Gestão, cálculo, pagamento, fiscalização e auditoria de benefícios sociais federais: cadastro de beneficiários e dependentes, parametrização de programas sociais, cálculo mensal da folha, aplicação de descontos legais, conciliação com o SIAFI e remessa CNAB 240 para BB/CAIXA.

### 2.2 Arquitetura Legada

Mainframe Natural 6.3 / Adabas 7.4 com **15 programas** (8 online + 7 batch) e **4 DDMs** (BENEFICIARIO, PROGRAMA-SOCIAL, PAGAMENTO, AUDITORIA). Não há integridade referencial no Adabas — toda validação está nos programas Natural. O fluxo central mensal é BATCHPGT → BATCHCON → BATCHREL. O Par 1 leu em profundidade os cadastros (CADBENEF, CADDEPEND, CADPROG) e correlacionou com cálculo (CALCBENF, CALCCORR, CALCDSCT) e validação (VALBENEF, VALDOCS, VALELEG).

### 2.3 Usuários e Perfis

Operadores da CGPB (cadastro e cálculo via telas 3270), DEFIS (fiscalização e auditoria) e jobs batch agendados (folha mensal). Não há um modelo de perfis formal no código de cadastro/cálculo lido pelo Par 1; a autorização operacional vive nos batches (a confirmar com o Par 2).

---

## 3. Principais Descobertas

### 3.1 Regras de Negócio Críticas

> Liste as 5 regras de negócio mais importantes encontradas.

1. **BR-017** — Fórmula do benefício mensal: `VLR-BASE × FATOR-REG × FATOR-FAM × FATOR-RND × FATOR-IDADE × (1 + FATOR-REAJ)` (CALCBENF). Núcleo do sistema.
2. **BR-026** — Teto de desconto de 30% do bruto, exceto judicial (CALCDSCT). Regra financeira/legal.
3. **BR-023/BR-024** — Cálculo diferenciado em dezembro: 13º + abono natalino de 15% (só tipo A) (CALCBENF).
4. **BR-006** — Suspensão automática silenciosa de beneficiários > 75 anos (CADBENEF). Impacto direto no idoso.
5. **BR-035** — Região 99 pula toda a elegibilidade (VALELEG). Furo de controle.

### 3.2 Dependências Complexas

> Quais programas estão mais acoplados? Onde há risco de efeito cascata?

O `CALCBENF` é o hub financeiro: depende de BENEFICIARIO, PROGRAMA-SOCIAL e PAGAMENTO e embute uma versão simplificada do desconto (3%) que conflita com o `CALCDSCT` completo (faixas 3/5/7/9%). O `CADPROG` injeta o fator-K (constante 0.347215) no valor-base, propagando para todo cálculo posterior. A cadeia batch BATCHPGT → BATCHCON → BATCHREL tem ordem que virou dependência do SIAFI. Efeito cascata maior: qualquer erro no valor-base ou nos fatores reaparece em 3,8M pagamentos/mês.

### 3.3 Dívida Técnica Identificada

> Que problemas no código legado vão complicar a migração?

- [x] Validação de CPF (módulo 11) **duplicada** em CADBENEF, VALBENEF e VALDOCS — risco de divergência.
- [x] **Truncamento de centavos** (BR-030) inconsistente entre programas (CALCBENF trunca, BATCHREL usa outro método — INC-004).
- [x] **Backdoors de validação** ativos (CPF `000…` e prefixos especiais) — risco de segurança/fraude.
- [x] Tabela de IPCA da correção retroativa **congelada em 2014** (BR-036) — subcorreção a partir de 2015.
- [x] Lógica de negócio crítica **sem documentação** (constantes mágicas, casos especiais não comentados).

### 3.4 Gaps de Documentação

> O que a documentação existente NÃO cobre?

A documentação parcial (1997–2012) cobre só os módulos de cadastro. **Não há documentação funcional** dos módulos de cálculo (CALCBENF/CALCCORR/CALCDSCT) nem dos batches. As 10 regras ocultas e os 3 easter eggs não aparecem em nenhum documento — só no código (ver `mysteries-found.md`).

---

## 4. Mistérios e Riscos

### 4.1 Mistérios Não Resolvidos

> Resuma os mistérios do arquivo `mysteries-found.md` que permanecem sem explicação.

| ID  | Descrição | Risco para Migração |
| --- | --------- | ------------------- |
| MYS-003 | Constante mágica `0.347215` (fator-K) sem origem | Reproduzir errado distorce o valor-base de todos os programas |
| MYS-005 | Truncamento de centavos vs. arredondamento | Viés financeiro recorrente; quebra conciliação SIAFI |
| MYS-001 | Suspensão automática silenciosa > 75 anos | Decidir se migra como regra explícita e auditável |
| MYS-008 | Região 99 pula elegibilidade | Decidir se vira exceção controlada ou se elimina o bypass |
| MYS-007/MYS-010 | Backdoors de CPF/documento de teste | Não migrar como está — precisa decisão do PO + segurança |
| MYS-009 | Ordem batch acoplada ao SIAFI | Confirmar com Par 2 antes de redesenhar o fluxo |

### 4.2 Riscos para o Estágio 2

> O que o time de especificação precisa saber antes de começar?

1. **Cálculo é o coração e está sem documentação** — toda EARS de cálculo precisa rastrear linha-a-linha ao `.NSN`, sem inferência.
2. **Há regras que NÃO devem ser migradas como estão** (backdoors MYS-007/010): exigem decisão explícita do PO e podem virar requisitos `Unwanted`.
3. **Decisões pendentes de negócio**: arredondamento (truncar vs. half-up), suspensão automática de idosos e tratamento da região 99 precisam de sign-off antes de o Dev codar.

---

## 5. Recomendações

### 5.1 O que migrar primeiro

> Com base na priorização do Par 1 (Product Owner), quais funcionalidades devem ser migradas primeiro?

| Prioridade | Funcionalidade | Justificativa |
| ---------- | -------------- | ------------- |
| 1 | Cálculo do benefício mensal (CALCBENF — BR-017 a BR-024) | É o que credita dinheiro na conta das famílias; erro aqui é o pior cenário. |
| 2 | Descontos e teto de 30% (CALCDSCT — BR-025 a BR-030) | Define o valor líquido; teto judicial e contribuição são obrigações legais. |
| 3 | Cadastro + validação + elegibilidade do beneficiário (CADBENEF/VALBENEF/VALELEG — BR-001 a BR-011, BR-031 a BR-035) | Sem cadastro válido e elegível não há pagamento correto. |

**Regra de corte aplicada:** "Afeta o ciclo mensal de pagamento? → v1. Não? → backlog."

### 5.2 O que descartar

> Funcionalidades que provavelmente não precisam ser migradas:

- **Bloco "Plano Verão" comentado** (CALCCORR, EGG-001): código morto de 1989–1991, descartar.
- **Backdoors de CPF/documento de teste** (MYS-007/MYS-010): NÃO migrar o comportamento; substituir por ambiente de teste isolado.
- **Correção retroativa IPCA com tabela até 2014** (CALCCORR): fora do v1; reavaliar como serviço parametrizável no backlog.

### 5.3 O que evoluir

> Funcionalidades que devem ser migradas E melhoradas:

- **Arredondamento monetário** (BR-030): trocar truncamento por arredondamento half-up padronizado e único em todo o sistema.
- **Suspensão automática > 75 anos** (BR-006): manter a regra, mas torná-la explícita, auditável e com notificação.
- **Região 99** (BR-035): substituir o bypass total por um fluxo de exceção controlado e auditado.
- **Auditoria**: registrar toda transição de status e todo cálculo (imutável), cobrindo os gaps atuais.

---

## 6. Métricas do Estágio

| Métrica                       | Valor        |
| ----------------------------- | ------------ |
| Programas analisados          | 10 / 15 (3 cadastros do Par 1 + 6 cálculo/validação correlatos; batches com Par 2) |
| DDMs mapeados                 | 4 / 4 (referenciados via campos nas regras; detalhamento de schema com Par 4) |
| Regras de negócio encontradas | 36           |
| Regras escondidas encontradas | 10 / 10      |
| Easter eggs encontrados       | 3 / 3        |
| Termos no glossário           | 45           |
| Mistérios catalogados         | 10           |
| Tempo total gasto             | Estágio 1 (bloco do Par 1) |

---

## 7. Notas para o Próximo Estágio

> Deixe aqui mensagens para o time no Estágio 2 (Especificação Moderna):

O Par 1 já adiantou a base de EARS em [`../02-spec-moderna/SPECIFICATION.md`](../02-spec-moderna/SPECIFICATION.md) (módulos beneficiary, payment e eligibility), com `source_legacy:` em todo REQ-ID rastreando ao catálogo BR. Para o Par 2 (Arquitetura): os bounded contexts naturais são **Beneficiary** (cadastro/validação/dependentes), **Program** (parametrização), **Payment** (cálculo/descontos/correção) e **Audit**. Três decisões de negócio aguardam sign-off do PO antes do código: (1) arredondamento half-up, (2) suspensão de idosos como regra explícita, (3) eliminação dos backdoors de teste. Não reordenar a cadeia batch sem validar a conciliação SIAFI (MYS-009).

---

## Definição de Pronto deste relatório

- [x] Todas as seções acima preenchidas (sem placeholders).
- [x] Pelo menos 5 regras críticas listadas em §3.1, cada uma referenciando uma `BR-XXX` do catálogo.
- [x] Decisões de migrar/descartar/evoluir em §5 cobrem as 8+ funcionalidades principais.
- [x] Métricas de §6 conferem com os outros artefatos (glossary.md, business-rules-catalog.md, mysteries-found.md).

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

