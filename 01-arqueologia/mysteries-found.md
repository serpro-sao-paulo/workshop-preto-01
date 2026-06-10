<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Mistérios Encontrados — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **mysteries-found**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui toda lógica, comportamento ou código que o time não conseguiu explicar.
> "Mistérios" são trechos de código sem documentação, com lógica não-óbvia ou que parecem workarounds.
>
> **Cota mínima para passar pelo portão do Estágio 2:** 5 mistérios documentados.

## O que conta como "mistério"?

- Código que faz algo inesperado sem comentário explicando por quê
- Valores hardcoded sem explicação (números mágicos)
- Lógica condicional que parece um workaround ou gambiarra
- Campos no DDM que não são usados por nenhum programa
- Programas que existem mas não são chamados por ninguém
- Comportamento diferente entre o que a documentação diz e o que o código faz
- Easter eggs deixados pelos desenvolvedores originais

## Níveis de Confiança

| Nível     | Significado                                         |
| --------- | --------------------------------------------------- |
| **ALTA**  | Temos certeza de que há algo estranho aqui          |
| **MÉDIA** | Parece suspeito, mas pode ter explicação            |
| **BAIXA** | Pode ser intencional, mas não conseguimos confirmar |

## Mistérios Catalogados

| ID      | Descrição | Onde Encontrado | Impacto Potencial | Confiança |
| ------- | --------- | --------------- | ----------------- | --------- |
| MYS-001 | Beneficiário com idade > 75 anos é suspenso silenciosamente (status → `S`) no cadastro. | `CADBENEF.NSN#L166-L168` | Corta pagamento de idosos sem aviso nem trilha de auditoria. | ALTA |
| MYS-002 | Limite de 5 dependentes hardcoded no programa, mas o grupo PE do DDM não impõe esse teto. | `CADDEPEND.NSN#L62-L65` | Divergência código × DDM; famílias grandes ficam sem registrar dependentes. | ALTA |
| MYS-003 | Constante mágica `0.347215` no fator-K de ajuste do valor-base do programa, sem origem documentada. | `CADPROG.NSN#L86-L88` | Reproduzir errado distorce o valor-base de todos os programas. | ALTA |
| MYS-004 | Na competência de dezembro (mês 12) o cálculo muda totalmente: soma 13º e abono natalino de 15% (só tipo `A`). | `CALCBENF.NSN#L242-L258` | Perder a regra subpaga milhões em dezembro. | ALTA |
| MYS-005 | Truncamento `valor×100`(inteiro)`÷100` em vez de arredondamento → perda sistemática de centavos. | `CALCBENF.NSN#L232-L234`, `CALCDSCT.NSN#L174-L176` | Erro de centavos × 3,8M pagamentos/mês; divergência na conciliação SIAFI. | ALTA |
| MYS-006 | Desconto judicial (tipo `J`) ignora o teto de 30% que se aplica a todos os outros descontos. | `CALCDSCT.NSN#L163-L168` | Líquido pode zerar; correto por lei, mas precisa ser explícito na migração. | ALTA |
| MYS-007 | CPFs com todos os dígitos iguais iniciados por `000` são aceitos como válidos (teste do governo). | `VALBENEF.NSN#L185-L205` | CPFs de teste entram em produção e podem receber pagamento. | ALTA |
| MYS-008 | Beneficiário com região 99 é declarado elegível e pula TODAS as verificações de elegibilidade. | `VALELEG.NSN#L105-L110` | Bypass total de controles; risco de pagamento indevido/fraude. | ALTA |
| MYS-009 | Ordem batch BATCHPGT → BATCHCON → BATCHREL virou dependência de sistemas externos (SIAFI), não é a mais lógica. | `README.md §4.4`, `BATCHPGT.NSN`, `BATCHCON.NSN`, `BATCHREL.NSN` | Reordenar quebra a conciliação SIAFI. Confirmar com Par 2. | MÉDIA |
| MYS-010 | Documentos com prefixo de CPF especial (`000,001,002,010,011,099,100,999`) são validados sem verificação real e zeram erros. | `VALDOCS.NSN#L48-L56`, `#L166-L184` | Backdoor de teste em produção aceita documentos forjados. | ALTA |

## Detalhamento dos Mistérios

### MYS-001: Suspensão automática de beneficiários acima de 75 anos

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L161-L168`
- **Trecho de código**:

```natural
* DEF STATUS INICIAL
IF #OPER = 'I'
  MOVE 'A' TO #STATUS
END-IF
*
* AJUSTE P/ BENEFICIARIOS ACIMA DE 75 ANOS
IF #IDADE > 75
  MOVE 'S' TO #STATUS
END-IF
```

- **O que esperávamos**: beneficiário incluído permanece Ativo (`A`).
- **O que o código faz**: se a idade calculada passa de 75, grava status `S` (Suspenso) sem mensagem nem registro de auditoria.
- **Hipótese do time**: regra de revisão cadastral de idosos implementada como atalho em 2011 ("AJUSTE STATUS IDOSO" no cabeçalho).
- **Risco se ignorarmos**: na migração, idosos seriam pagos quando o legado os suspende (ou vice-versa). Precisa virar regra explícita e auditável.

---

### MYS-003: Constante mágica 0.347215 no fator-K

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L86-L88`
- **Trecho de código**:

```natural
* CALC VLR BASE AJUSTADO C/ FATOR K
COMPUTE #FATOR-K = 1.00 + (#FATOR-REAJ * 0.347215)
COMPUTE #VLR-CALC = #VLR-BASE * #FATOR-K
```

- **O que esperávamos**: valor-base armazenado como informado pelo operador.
- **O que o código faz**: ajusta o valor-base por um fator derivado de uma constante `0.347215` sem explicação.
- **Hipótese do time**: índice de conversão econômico dos anos 90/2003 (alteração "INC FATOR CORRECAO" de 2003).
- **Risco se ignorarmos**: todos os valores-base ficam errados na migração; impacto em cascata em todo o cálculo de benefício.

---

### MYS-004: Cálculo diferenciado em dezembro (13º + abono natalino)

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L242-L258`
- **Trecho de código**:

```natural
IF #MES = 12
  MOVE 'D' TO #TIPO-PGTO
  COMPUTE #VLR-13 = #VLR-BASE * #FATOR-REG * #FATOR-IDADE
  ...
* ABONO NATALINO - 15% ADICIONAL PARA PROGRAMAS TIPO 'A'
  IF #TIPO-PROG = 'A'
    COMPUTE #VLR-ABONO = #VLR-BENF * 0.15
```

- **O que esperávamos**: cálculo mensal idêntico o ano todo.
- **O que o código faz**: em dezembro soma 13º (fórmula própria) e, só para programas Assistenciais, 15% de abono.
- **Hipótese do time**: política de 13º benefício + abono natalino consolidada em 2009.
- **Risco se ignorarmos**: subpagamento massivo em dezembro — exatamente o pico de maior visibilidade institucional.

---

### MYS-005: Perda sistemática de centavos por truncamento

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L232-L234`
- **Trecho de código**:

```natural
* TRUNCAR P/ 2 CASAS DECIMAIS - PADRAO MAINFRAME
COMPUTE #VLR-TEMP = #VLR-BENF * 100
COMPUTE #VLR-BENF = #VLR-TEMP / 100
```

- **O que esperávamos**: arredondamento padrão (half-up) para 2 casas.
- **O que o código faz**: multiplica por 100 num inteiro (`#VLR-TEMP N11`) e divide — trunca a fração, sempre para baixo.
- **Hipótese do time**: "padrão mainframe" assumido sem revisão; `BATCHREL.NSN#L139` usa outra técnica para o mesmo tipo de valor (INC-004).
- **Risco se ignorarmos**: viés de arredondamento × 3,8M pagamentos gera divergência recorrente na conciliação com o SIAFI.

---

### MYS-008: Região 99 pula todas as verificações de elegibilidade

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L105-L110`
- **Trecho de código**:

```natural
* REGIAO 99 - INTERNACIONAL/DIPLOMATICO
IF #COD-REG = 99
  MOVE TRUE TO #ELEGIVEL
  WRITE 'BENEFICIARIO ELEGIVEL - REGIAO ESPECIAL'
  ESCAPE ROUTINE
END-IF
```

- **O que esperávamos**: toda elegibilidade verificada por status, idade, renda e documentação.
- **O que o código faz**: se a região é 99, retorna "elegível" imediatamente, sem checar nada.
- **Hipótese do time**: tratamento de beneficiários no exterior/diplomáticos adicionado em 2013, virou bypass amplo.
- **Risco se ignorarmos**: na migração isso seria um furo de controle; deve virar um fluxo de exceção explícito e auditado, não um `return true`.

---

> Copie o bloco acima para cada mistério encontrado.

## Easter Eggs

> Dica: existem **3 easter eggs** escondidos no código legado. Registre aqui os que encontrar:

1. [x] Easter Egg 1: **Bloco "PLANO VERÃO"** comentado em `CALCCORR.NSN#L96-L110` — correção monetária do período 1989–1991 (transição Cruzado→Cruzeiro), nunca removido (EGG-001).
2. [x] Easter Egg 2: **Backdoor de validação de documentos** em `VALDOCS.NSN#L166-L184` — prefixos de CPF especiais aceitos sem verificação, zerando erros (EGG-002).
3. [x] Easter Egg 3: **Exceção de CPF de teste** em `VALBENEF.NSN#L196` — CPFs `000…` com dígitos iguais aceitos como válidos (EGG-002 correlato / teste do governo).

## Resumo

- Total de mistérios encontrados: **10** (+ 3 easter eggs)
- Confiança alta: **9**
- Confiança média: **1** (MYS-009 — ordem batch, a confirmar com Par 2)
- Confiança baixa: \_\_\_
- Easter eggs encontrados: \_\_\_ / 3

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-checklist.md"><strong>mysteries-checklist.md</strong></a><br/>
<sub>Lista do que procurar.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="discovery-report.md"><strong>discovery-report.md</strong></a><br/>
<sub>Síntese final.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

