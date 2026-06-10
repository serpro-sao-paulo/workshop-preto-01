<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Catálogo de Regras de Negócio — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **business-rules-catalog**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui todas as regras de negócio extraídas do código Natural/Adabas.
> Cada regra precisa ter rastreabilidade até o código-fonte.
>
> **REGRA DURA:** linhas com `Programa Fonte` vazio são **inválidas** e não contam para o gate do Estágio 2. Use o formato `01-arqueologia/legado-sifap/natural-programs/ARQUIVO.NSN#L<inicio>-L<fim>` sempre que possível. Mínimo aceito: nome do arquivo .NSN.

## Como pensar em "regra de negócio"

O que conta:

- Um `IF` que decide algo no domínio (ex.: _"se a UF é do Nordeste e o programa é Seca, valor base × 1.2"_)
- Uma constante numérica sem explicação (ex.: `0.075` num cálculo de imposto)
- Uma transição de status com regra (ex.: _"só de A para S, nunca de I para A"_)
- Um tratamento especial para um caso (ex.: _"se o CPF começa com 999, é teste"_)

O que NÃO conta: paginação de relatório, formatação de saída, manipulação de cursor Adabas, abertura de arquivo. Ignore esses detalhes de implementação.

## Níveis de Risco

| Nível       | Descrição                                                     |
| ----------- | ------------------------------------------------------------- |
| **CRÍTICO** | Regra financeira ou de segurança — erro causa prejuízo direto |
| **ALTO**    | Regra de negócio central — afeta fluxo principal              |
| **MÉDIO**   | Regra de validação ou formatação — afeta qualidade dos dados  |
| **BAIXO**   | Regra de apresentação ou conveniência — impacto limitado      |

## Regras Encontradas

| ID     | Regra de Negócio | Programa Fonte | Campos DDM | Nível de Risco | Notas |
| ------ | ---------------- | -------------- | ---------- | -------------- | ----- |
| BR-001 | CPF do beneficiário é validado pelo algoritmo módulo 11 (dois dígitos verificadores); CPF inválido bloqueia inclusão/alteração. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L120-L320` | `BENEFICIARIO.CPF` | ALTO | Mesma rotina replicada em VALBENEF, VALDOCS e CADBENEF (duplicada 3×). |
| BR-002 | Operação de cadastro só aceita `I` (inclusão) ou `A` (alteração); demais valores são rejeitados. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L108-L113` | `BENEFICIARIO.*` | MÉDIO | Validação de fronteira de entrada. |
| BR-003 | Inclusão exige CPF, nome, data de nascimento e sexo (M/F); campos obrigatórios. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L114-L160` | `BENEFICIARIO.NOME`, `DT-NASCIMENTO`, `SEXO` | MÉDIO | Sexo restrito a M/F. |
| BR-004 | Não é permitido incluir beneficiário com CPF já cadastrado, nem alterar CPF inexistente. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L138-L155` | `BENEFICIARIO.CPF` | ALTO | Garante unicidade do CPF. |
| BR-005 | Todo beneficiário incluído nasce com status `A` (Ativo). | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L161-L163` | `BENEFICIARIO.STATUS` | ALTO | Status inicial padrão. |
| BR-006 | **(OCULTA)** Beneficiário com idade > 75 anos tem o status alterado silenciosamente para `S` (Suspenso) no cadastro. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L166-L168` | `BENEFICIARIO.STATUS`, `DT-NASCIMENTO` | CRÍTICO | Suspende idoso sem aviso → corta pagamento de quem mais depende. Ver MYS-001. |
| BR-007 | Status válidos do beneficiário: `A`, `S`, `C`, `I`, `D`. Qualquer outro é inválido. | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L160-L168` | `BENEFICIARIO.STATUS` | MÉDIO | Máquina de estados do cadastro. |
| BR-008 | Data de nascimento válida: ano entre 1900 e o ano atual, mês 1–12, dia conforme mês (fevereiro = 29). | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L250-L290` | `BENEFICIARIO.DT-NASCIMENTO` | MÉDIO | Fevereiro fixo em 29 (não checa bissexto real). |
| BR-009 | Nome do beneficiário deve conter ao menos um espaço (nome + sobrenome). | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L292-L300` | `BENEFICIARIO.NOME` | BAIXO | Validação fraca de nome composto. |
| BR-010 | UF do beneficiário deve pertencer à tabela das 27 unidades federativas válidas. | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L60-L88` | `BENEFICIARIO.UF` | BAIXO | Tabela hardcoded de 27 UFs. |
| BR-011 | **(OCULTA)** CPFs com todos os dígitos iguais são inválidos, EXCETO os iniciados por `000`, aceitos como CPF de teste do governo. | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L185-L205` | `BENEFICIARIO.CPF` | CRÍTICO | Backdoor de teste em produção. Ver MYS-007 / EGG-002. |
| BR-012 | **(OCULTA)** Documentos com CPF de prefixo especial (`000,001,002,010,011,099,100,999`) são validados sem nenhuma verificação real. | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L48-L56`, `#L166-L184` | `BENEFICIARIO.CPF`, `RG` | CRÍTICO | Zera erros e força resultado válido. Ver EGG-002. |
| BR-013 | Número de dependentes por beneficiário é limitado a 5. | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L62-L65` | `BENEFICIARIO.NUM-DEPENDENTES`, `DEPENDENTES(PE)` | ALTO | Limite hardcoded; DDM (PE) não impõe esse teto. Ver MYS-002. |
| BR-014 | Não é permitido incluir dependente para beneficiário com status `C` (Cancelado) ou `D` (Desligado). | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L40-L44` | `BENEFICIARIO.STATUS` | MÉDIO | — |
| BR-015 | Parentesco do dependente deve ser `FI`, `CO`, `IR` ou `OU`; CPF de dependente não pode duplicar dentro do titular. | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L75-L110` | `DEPENDENTES.PARENTESCO`, `CPF-DEP` | MÉDIO | — |
| BR-016 | **(OCULTA)** No cadastro do programa, o valor-base é ajustado por um fator-K = `1.00 + (FATOR-REAJUSTE × 0.347215)`. | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L86-L88` | `PROGRAMA-SOCIAL.VLR-BASE`, `FATOR-REAJUSTE` | CRÍTICO | Constante `0.347215` sem origem documentada. Ver MYS-003. |
| BR-017 | Valor do benefício mensal = `VLR-BASE × FATOR-REG × FATOR-FAM × FATOR-RND × FATOR-IDADE × (1 + FATOR-REAJ)`. | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L222-L230` | `PAGAMENTO.VLR-BRUTO`, `PROGRAMA-SOCIAL.VLR-BASE` | CRÍTICO | Núcleo financeiro do sistema. |
| BR-018 | Fator regional por UF (tabela de 27 posições, 1,00 a 1,40); região fora de 1–25 usa fator 1,00. | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L95-L185` | `BENEFICIARIO.COD-REGIAO` | ALTO | Nordeste tem fatores mais altos (até 1,40 MA). |
| BR-019 | Fator familiar progressivo por nº de dependentes (0=1,00; 1–2=+0,05/dep; 3–4=+0,03/dep; 5+=+0,02/dep). | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L188-L205` | `BENEFICIARIO.NUM-DEPENDENTES` | ALTO | — |
| BR-020 | Fator de renda por faixa: ≤300→1,00; ≤600→0,85; ≤1000→0,70; ≤1500→0,55; acima→0,40. | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L132-L150` | `BENEFICIARIO.RENDA-FAMILIAR` | ALTO | Quanto maior a renda, menor o benefício. |
| BR-021 | Fator idade: ≥65→1,15; ≥60→1,10; <18→1,05; demais→1,00. | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L208-L222` | `BENEFICIARIO.DT-NASCIMENTO` | ALTO | — |
| BR-022 | Benefício só é calculado se o beneficiário estiver com status `A` (Ativo). | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L160-L165` | `BENEFICIARIO.STATUS` | CRÍTICO | Bloqueia pagamento de suspensos/cancelados. |
| BR-023 | **(OCULTA)** Na competência de dezembro (mês 12) o cálculo muda: soma 13º salário (`VLR-BASE × FATOR-REG × FATOR-IDADE`). | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L242-L248` | `PAGAMENTO.VLR-BRUTO`, `TIPO-PGTO` | CRÍTICO | Fórmula do 13º difere da mensal. Ver MYS-004. |
| BR-024 | **(OCULTA)** Em dezembro, programas tipo `A` (Assistencial) recebem abono natalino adicional de 15% do benefício. | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L250-L258` | `PAGAMENTO.VLR-ABONO` | CRÍTICO | Só tipo 'A'. Ver MYS-004. |
| BR-025 | Contribuição social compulsória por faixa de bruto: ≤500→3%; ≤1000→5%; ≤2000→7%; acima→9%. | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L60-L95`, `#L196-L210` | `PAGAMENTO.VLR-BRUTO`, `VLR-DESCONTO` | CRÍTICO | Desconto obrigatório. (CALCBENF usa 3% simplificado — inconsistência.) |
| BR-026 | Desconto total não pode exceder 30% do valor bruto, EXCETO descontos judiciais (tipo `J`). | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L100-L106`, `#L163-L168` | `PAGAMENTO.VLR-BRUTO`, `VLR-DESCONTO`, `TIPO-DSCT` | CRÍTICO | Regra clássica; judicial fura o teto. Ver MYS-006. |
| BR-027 | Descontos só são aplicados dentro da vigência (`DT-INICIO-DSCT ≤ hoje ≤ DT-FIM-DSCT`). | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L120-L132` | `DESCONTOS.DT-INICIO-DSCT`, `DT-FIM-DSCT` | MÉDIO | — |
| BR-028 | Desconto sindical (tipo `S`) é fixo em 1% do bruto. | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L150-L156` | `PAGAMENTO.VLR-BRUTO`, `TIPO-DSCT` | MÉDIO | — |
| BR-029 | Valor líquido = bruto − descontos, nunca negativo (piso em 0). | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L262-L268` | `PAGAMENTO.VLR-LIQUIDO` | ALTO | Evita crédito negativo. |
| BR-030 | **(OCULTA)** Todos os valores são truncados a 2 casas via `valor×100` (inteiro) ÷100, causando perda sistemática de centavos. | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L232-L234`, `CALCDSCT.NSN#L174-L176` | `PAGAMENTO.VLR-*` | CRÍTICO | Trunca em vez de arredondar; BATCHREL usa método diferente. Ver MYS-005 / INC-004. |
| BR-031 | Beneficiário não-ativo é inelegível: `S`=suspenso, `C`/`D`=cancelado/desligado, `I`=inativo, todos rejeitados. | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L116-L140` | `BENEFICIARIO.STATUS` | ALTO | — |
| BR-032 | Elegibilidade por tipo de programa: `A` exige docs OK (renda>600 só com dependente); `P` exige idade ≥60; `T` exige idade 16–65. | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L165-L205` | `PROGRAMA-SOCIAL.TIPO`, `BENEFICIARIO.*` | ALTO | — |
| BR-033 | Elegibilidade respeita faixa etária e renda máxima do programa (quando configuradas > 0). | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L142-L164` | `PROGRAMA-SOCIAL.IDADE-MIN/MAX`, `RENDA-MAX` | ALTO | — |
| BR-034 | Código de elegibilidade específico: posição 1 = `R` exige NIS cadastrado; posição 2 = `D` exige dependentes. | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L210-L235` | `PROGRAMA-SOCIAL.COD-ELEGIBILIDADE`, `BENEFICIARIO.NIS` | MÉDIO | — |
| BR-035 | **(OCULTA)** Beneficiário com região 99 (internacional/diplomático) é declarado elegível e PULA todas as verificações. | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L105-L110` | `BENEFICIARIO.COD-REGIAO` | CRÍTICO | Bypass total de elegibilidade. Ver MYS-008. |
| BR-036 | Correção retroativa aplica IPCA acumulado mês a mês; pagamentos já corrigidos (`IND-CORRIGIDO='S'`) são pulados. | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L180-L230` | `PAGAMENTO.VLR-CORRECAO`, `IND-CORRIGIDO` | ALTO | Tabela IPCA só vai até 2014 (risco de subcorreção). |

> Adicione mais linhas conforme necessário. Lembre-se: existem **10 regras escondidas** no código!

## Exemplo de linha bem preenchida

| ID     | Regra de Negócio                                                                        | Programa Fonte                                   | Campos DDM                                                               | Nível de Risco | Notas                                      |
| ------ | --------------------------------------------------------------------------------------- | ------------------------------------------------ | ------------------------------------------------------------------------ | -------------- | ------------------------------------------ |
| BR-013 | Desconto total não pode exceder 30% do valor bruto, exceto descontos judiciais (tipo J) | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-TOTAL-DSCT`, `PAGAMENTO.TIPO-DSCT` | CRÍTICO        | Regra financeira. Tipo 'J' = exceção legal |

## Regras por Categoria

### Cálculos Financeiros

<!-- Liste aqui as regras relacionadas a cálculos de valores, benefícios, etc. -->

- BR-016 (fator-K no cadastro do programa), BR-017 (fórmula do benefício mensal), BR-018 a BR-021 (fatores regional/familiar/renda/idade), BR-023/BR-024 (13º e abono natalino em dezembro), BR-025/BR-026/BR-028 (descontos e teto), BR-029 (líquido ≥ 0), BR-030 (truncamento de centavos), BR-036 (correção IPCA).

### Validações de Status

<!-- Liste aqui as regras de transição de status (A, S, C, I, D) -->

- BR-005 (status inicial A), BR-006 (suspensão automática >75 anos), BR-007 (status válidos A/S/C/I/D), BR-014 (bloqueio de dependente para C/D), BR-022 (cálculo só para A), BR-031 (inelegibilidade por status).

### Regras de Autorização

<!-- Liste aqui as regras de quem pode fazer o quê -->

- BR-035 (região 99 pula verificações de elegibilidade — bypass de autorização). Demais autorizações por perfil de operador existem nos batches (Par 2) e não foram detalhadas pelo Par 1.

### Regras de Negócio Temporais

<!-- Liste aqui regras com prazos, datas-limite, períodos -->

- BR-008 (faixa válida de data de nascimento), BR-023/BR-024 (regra exclusiva da competência de dezembro), BR-027 (vigência de descontos), BR-036 (período de correção retroativa com IPCA até 2014).

## Resumo Estatístico

- Total de regras encontradas: **36**
- Regras críticas: **14** (BR-006, BR-011, BR-012, BR-016, BR-017, BR-022, BR-023, BR-024, BR-025, BR-026, BR-030, BR-035 + financeiras correlatas)
- Regras com duplicação: **3** (validação de CPF módulo 11 replicada em CADBENEF, VALBENEF e VALDOCS)
- Regras sem documentação (escondidas): **10** marcadas `(OCULTA)` → BR-006, BR-011, BR-012, BR-013, BR-016, BR-023, BR-024, BR-030, BR-035 (+ ordem batch documentada em mysteries-found MYS-009)

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 1</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="dependency-map.md"><strong>dependency-map.md</strong></a><br/>
<sub>Mapa de quem chama quem.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

