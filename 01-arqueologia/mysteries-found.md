<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Mistérios Encontrados — sisdnit Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **mysteries-found**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado sisdnit
> 2. Rastreabilidade para `01-arqueologia/legado-sisdnit/` (programas `.NSN` e DDMs)
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

> Mistérios MYS-001 a MYS-005 extraídos pelo **Par 1 · Visão** (cadastros: `CADBENEF.NSN`, `CADDEPEND.NSN`, `CADPROG.NSN`). MYS-006 a MYS-009 extraídos pelo **Par 2 · Arquitetura** (batches: `BATCHPGT.NSN`, `BATCHREL.NSN`, `BATCHCON.NSN`). MYS-010 a MYS-013 extraídos pelo **Par 3 · Implementação** (cálculos: `CALCBENF.NSN`, `CALCDSCT.NSN`, `CALCCORR.NSN`). MYS-014 a MYS-017 extraídos pelo **Par 4 · Qualidade** (validações: `VALBENEF.NSN`, `VALDOCS.NSN`, `VALELEG.NSN`). MYS-018 a MYS-019 extraídos pelo **Par 5 · Operações** (consulta/relatórios: `CONSBENF.NSN`, `RELPGT.NSN`, `RELAUDIT.NSN`). MYS-020+ ficam para os demais pares.

| ID      | Descrição | Onde Encontrado | Impacto Potencial | Confiança |
| ------- | --------- | --------------- | ----------------- | --------- |
| MYS-001 | Beneficiário com idade > 75 anos é forçado ao status `S`, mas no DDM `S` significa SUSPENSO. Idoso entra suspenso em vez de ativo. | `CADBENEF.NSN#L166-L169` | CRÍTICO — idoso pode ser excluído da folha de pagamento por engano | ALTA |
| MYS-002 | Idade calculada só pela diferença de anos (`ano atual − ano nascimento`), ignorando mês e dia. Quem nasceu em dezembro "envelhece" em 1º de janeiro. | `CADBENEF.NSN#L155-L159` | MÉDIO — beneficiário pode cruzar o limiar de 75 anos até 11 meses antes do aniversário real | ALTA |
| MYS-003 | Programa limita a 5 dependentes, mas o grupo periódico `GRP-DEPENDENTE` no DDM aceita até 10 ocorrências. | `CADDEPEND.NSN#L63` | MÉDIO — pode haver beneficiários com 6–10 dependentes gravados por outra via, invisíveis a este cadastro | ALTA |
| MYS-004 | Códigos divergentes entre programa e DDM: sexo aceita `M/F` no código mas `M/F/I` no DDM; parentesco usa `FI/CO/IR/OU` no código mas `FI/CJ/NT/TU` no DDM. | `CADBENEF.NSN#L137` · `CADDEPEND.NSN` (PARENTESCO) | ALTO — dados gravados por sistemas externos podem ser rejeitados ou mal interpretados na migração | ALTA |
| MYS-005 | Constante mágica `0.347215` no cálculo do Fator-K do valor base do programa, sem documentação. DDM avisa "NÃO ALTERAR SEM AUTORIZAÇÃO (SENARC)". | `CADPROG.NSN#L87` | CRÍTICO — afeta o valor financeiro de todo programa social; origem da constante desconhecida | ALTA |
| MYS-006 | Comentário no batch declara que "SISTEMAS DOWNSTREAM DEPENDEM DESTA ORDENAÇÃO" (leitura por CPF), mas nenhum desses sistemas está documentado. | `BATCHPGT.NSN#L177-L179` | ALTO — mudar a ordem de processamento na migração pode quebrar integrações desconhecidas | ALTA |
| MYS-007 | O relatório (BATCHREL) arredonda (`+0,005`) enquanto o batch de pagamento (BATCHPGT) trunca. Os totais do relatório não batem exatamente com a soma dos pagamentos. | `BATCHREL.NSN#L136-L139` vs `BATCHPGT.NSN#L283-L285` | ALTO — divergência de centavos em relatórios regulatórios (TCU) | ALTA |
| MYS-008 | A tabela de 27 fatores regionais é hardcoded e duplicada entre `BATCHPGT` e `CALCBENF` (comentário "MESMA DO CALCBENF"). Sem fonte única. | `BATCHPGT.NSN#L120-L150` | ALTO — alteração de fator em um programa e não no outro gera cálculos divergentes | ALTA |
| MYS-009 | `BATCHPGT` usa 27 fatores regionais (`COD-REGIAO` 1–27), mas `BATCHREL` agrupa em apenas 5 macrorregiões por faixas (1–5, 6–10…). Os dois modelos de "região" coexistem. | `BATCHPGT.NSN#L233-L237` vs `BATCHREL.NSN#L117-L133` | MÉDIO — ambiguidade no conceito de região; risco de mapeamento errado na migração | MÉDIA |
| MYS-010 | Em `CALCDSCT`, o teto de 30% é reavaliado **dentro do loop** após cada desconto: um desconto judicial (sem teto) já somado pode ser sobrescrito (clobbered) quando um desconto não-judicial seguinte dispara `total = teto`. O resultado depende da ordem dos descontos no grupo PE. | `CALCDSCT.NSN#L164-L168` | ALTO — valor de desconto judicial pode ser indevidamente reduzido; prejuízo financeiro e risco legal | ALTA |
| MYS-011 | Em `CALCCORR`, a tabela IPCA só é carregada para 2010–2012, apesar do comentário "ULTIMA CARGA: 2014". Competências de outros anos resultam em índice 1,000000 (sem correção) **silenciosamente**, sem erro nem aviso. | `CALCCORR.NSN#L44` · `#L83-L96` · `#L186-L196` | ALTO — correções retroativas de períodos não cobertos retornam zero sem alertar o operador | ALTA |
| MYS-012 | Bloco "PLANO VERAO" comentado em `CALCCORR` com fatores mágicos `2.7500` e `1.4289` (transição Cruzado→Cruzeiro, 1989–1991), marcado "NAO REMOVER (HISTORICO)". Código morto sem origem documentada. | `CALCCORR.NSN#L98-L110` | MÉDIO — código morto pode ser reativado por engano; constantes sem procedência (possível easter egg) | MÉDIA |
| MYS-013 | Em `CALCBENF`, o 13º usa apenas `Fator-Regional × Fator-Idade` (sem fator familiar nem de renda), enquanto o benefício mensal usa os cinco fatores. A razão da fórmula diferente não está documentada. | `CALCBENF.NSN#L239-L248` | MÉDIO — valor do 13º diverge proporcionalmente do mensal; pode ser intenção ou bug histórico | MÉDIA |
| MYS-014 | Em `VALDOCS`, a subrotina `CHECK-DOC-ESPECIAL` zera `#QTD-ERROS`, força `#RESULTADO = 'V'` e `#CPF-OK = TRUE` sempre que o CPF começa com um dos 8 prefixos especiais (`000`,`001`,`002`,`010`,`011`,`099`,`100`,`999`), anulando as validações de CPF e RG já feitas. | `VALDOCS.NSN#L168-L182` | CRÍTICO — qualquer documento com prefixo especial passa sem validação; vetor de fraude/bypass de controle | ALTA |
| MYS-015 | Em `VALELEG`, `COD-REGIAO = 99` (internacional/diplomático) faz `ESCAPE ROUTINE` declarando o beneficiário elegível **antes** de qualquer checagem de status, idade, renda, tipo de programa ou documentação. Incluído em 05/04/2013. | `VALELEG.NSN#L105-L111` | CRÍTICO — backdoor de elegibilidade; beneficiários marcados região 99 escapam de todos os controles | ALTA |
| MYS-016 | Em `VALBENEF`, a tabela `#DIAS-MES(2)` recebe `29` fixo ("CONSIDERA BISSEXTO"), logo a validação de data aceita `29/02` em **qualquer** ano, mesmo não bissexto. | `VALBENEF.NSN#L96` · `#L256-L258` | MÉDIO — datas de nascimento inválidas (29/02 em ano comum) entram no cadastro | ALTA |
| MYS-017 | A exceção de CPF de teste ("CPFs iniciados com 000 são válidos") aparece em `VALBENEF` e também como prefixo especial em `VALDOCS`/`VALELEG`. `00000000000` é aceito como CPF válido. Backdoor de teste do governo nunca removido. | `VALBENEF.NSN#L196-L200` · `VALDOCS.NSN#L49-L56` | ALTO — CPF de teste em produção pode gerar beneficiários/pagamentos fantasma | ALTA |
| MYS-018 | Em `RELAUDIT`, todo evento com `ACAO = 'EX'` (exclusão) sofre `ESCAPE TOP` e é contado como "filtrado", nunca aparecendo na trilha de auditoria. O comportamento entrou na alteração "LIMPEZA RELATORIO" de 15/09/2014. | `RELAUDIT.NSN#L103-L108` | CRÍTICO — exclusões somem da auditoria; viola transparência/compliance e pode encobrir remoções indevidas | ALTA |
| MYS-019 | Em `CONSBENF`, a subrotina `MASCARA-CPF` revela os **3 primeiros** dígitos do CPF (em vez de mascará-los) quando o valor armazenado tem menos de 11 dígitos (`< 10000000000`). Comentário admite a falha: "NAO CORRIGIR SEM APROVACAO DA AUDITORIA". | `CONSBENF.NSN#L176-L191` | ALTO — vazamento de dado pessoal (LGPD) para CPFs com zeros à esquerda; correção bloqueada por nota interna | ALTA |

### MYS-001: Idoso vira status SUSPENSO em vez de ativo

- **Arquivo**: `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN#L166-L169`
- **Trecho de código**:

```natural
* AJUSTE P/ BENEFICIARIOS ACIMA DE 75 ANOS
IF #IDADE > 75
  MOVE 'S' TO #STATUS
END-IF
```

- **O que esperávamos**: que o status de um idoso recém-cadastrado permanecesse `A` (ativo), ou virasse um código específico de "sênior".
- **O que o código faz**: sobrescreve o status com `S`. No DDM `BENEFICIARIO.SIT-BENEFICIARIO`, `S` = SUSPENSO.
- **Hipótese do time**: o autor pretendia marcar "Sênior" mas reusou a letra `S` já ocupada por SUSPENSO. Alteração de 10/01/2011 ("AJUSTE STATUS IDOSO", Jose Ferreira). Provável bug histórico nunca percebido.
- **Risco se ignorarmos**: na migração, idosos podem ser tratados como suspensos e excluídos da folha. Precisa de decisão do PO antes de virar requisito (ver BR-010).

---

### MYS-002: Idade calculada só pelo ano

- **Arquivo**: `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN#L155-L159`
- **Trecho de código**:

```natural
MOVE #DT-NASC TO #ANO-NASC
DIVIDE 10000 INTO #ANO-NASC REMAINDER #ANO-NASC
  GIVING #ANO-NASC
COMPUTE #IDADE = #ANO-ATUAL - #ANO-NASC
```

- **O que esperávamos**: idade exata considerando se o aniversário já ocorreu no ano corrente.
- **O que o código faz**: subtrai apenas os anos, ignorando mês/dia.
- **Hipótese do time**: simplificação de 1997 para evitar aritmética de datas em Natural.
- **Risco se ignorarmos**: combinado com MYS-001, beneficiários podem ser marcados como idosos (e suspensos) até 11 meses antes de completar 75 anos de fato.

---

### MYS-003: Limite de dependentes diverge do DDM

- **Arquivo**: `01-arqueologia/legado-sisdnit/programs/CADDEPEND.NSN#L63`
- **Trecho de código**:

```natural
IF #NUM-DEP > 5
  WRITE 'LIMITE DE DEPENDENTES ATINGIDO'
  ESCAPE BOTTOM
END-IF
```

- **O que esperávamos**: limite do programa igual à capacidade do grupo periódico no DDM.
- **O que o código faz**: corta em 5, mas o grupo `GRP-DEPENDENTE` (PE) do DDM permite até 10 ocorrências.
- **Hipótese do time**: regra de negócio (máx. 5 dependentes para benefício) imposta no código, enquanto a estrutura de dados foi dimensionada com folga.
- **Risco se ignorarmos**: registros com 6–10 dependentes gravados por outra via ficariam invisíveis a este cadastro; a migração precisa decidir o limite canônico.

---

### MYS-004: Códigos de domínio divergentes legado ↔ DDM

- **Arquivo**: `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN#L137` e `CADDEPEND.NSN` (campo PARENTESCO)
- **Trecho de código**:

```natural
IF #SEXO NE 'M' AND #SEXO NE 'F'
  MOVE 'SEXO INVALIDO' TO #MSG
```

- **O que esperávamos**: o conjunto de valores aceitos pelo programa igual ao declarado no DDM.
- **O que o código faz**: SEXO aceita só `M/F` (DDM permite `M/F/I`); PARENTESCO usa `FI/CO/IR/OU` (DDM define `FI/CJ/NT/TU`).
- **Hipótese do time**: o DDM e o programa evoluíram em momentos diferentes e nunca foram reconciliados.
- **Risco se ignorarmos**: dados legados gravados com códigos do DDM podem ser rejeitados ou mal mapeados na migração (ver BR-006, BR-014).

---

### MYS-005: Constante mágica 0.347215 no Fator-K

- **Arquivo**: `01-arqueologia/legado-sisdnit/programs/CADPROG.NSN#L87`
- **Trecho de código**:

```natural
* CALC VLR BASE AJUSTADO C/ FATOR K
COMPUTE #FATOR-K = 1.00 + (#FATOR-REAJ * 0.347215)
COMPUTE #VLR-CALC = #VLR-BASE * #FATOR-K
```

- **O que esperávamos**: um fator de reajuste documentado, ligado a um índice oficial (IPCA, INPC, etc.).
- **O que o código faz**: multiplica o fator de reajuste informado por uma constante fixa `0.347215` sem qualquer comentário. Incluída em 05/07/2003 ("INC FATOR CORRECAO", Marcos Ribeiro).
- **Hipótese do time**: pode ser um coeficiente de conversão de índice econômico da época, embutido no código.
- **Risco se ignorarmos**: o valor base de todo programa social depende dessa constante. Migrar sem entender a origem pode alterar valores de pagamento. Requer confirmação com o SENARC antes de virar requisito (ver BR-017).

---

> Copie o bloco acima para cada mistério adicional encontrado pelos demais pares.

---

### MYS-014: Prefixo especial de CPF anula toda a validação de documentos

- **Arquivo**: `01-arqueologia/legado-sisdnit/programs/VALDOCS.NSN#L168-L182`
- **Trecho de código**:

```natural
DEFINE SUBROUTINE CHECK-DOC-ESPECIAL
  MOVE FALSE TO #DOC-ESP-OK
  MOVE #CPF TO #CPF-STR
  MOVE SUBSTR(#CPF-STR,1,3) TO #PREF-CPF
  FOR #I = 1 TO 8
    IF #PREF-CPF = #PREF-ESP(#I)
      MOVE TRUE TO #DOC-ESP-OK
      MOVE TRUE TO #CPF-OK
      MOVE 'V' TO #RESULTADO
      MOVE 0 TO #QTD-ERROS      /* <-- zera erros já detectados */
      ESCAPE BOTTOM
    END-IF
  END-FOR
END-SUBROUTINE
```

- **O que esperávamos**: que documentos "especiais" recebessem uma marcação informativa sem afetar o resultado das validações de CPF e RG.
- **O que o código faz**: roda **depois** de `VALIDA-CPF-DOC` e `VALIDA-RG` e, se o CPF começa com um prefixo especial, descarta todos os erros e força o resultado para válido.
- **Hipótese do time**: criado para liberar CPFs de teste/governo, mas funciona como bypass universal de validação em produção (alterado 07/06/2011, Roberto Mendes — "AJUSTE CHECK ESPEC").
- **Risco se ignorarmos**: na migração, replicar este comportamento mantém um vetor de fraude. Deve virar requisito **explícito** com gate de segurança (ver BR-042). O QA precisa de um teste que prove que prefixos especiais **não** burlam a validação no sistema novo.

---

### MYS-015: Região 99 — backdoor de elegibilidade

- **Arquivo**: `01-arqueologia/legado-sisdnit/programs/VALELEG.NSN#L105-L111`
- **Trecho de código**:

```natural
* REGIAO 99 - INTERNACIONAL/DIPLOMATICO
IF #COD-REG = 99
  MOVE TRUE TO #ELEGIVEL
  WRITE 'BENEFICIARIO ELEGIVEL - REGIAO ESPECIAL'
  ESCAPE ROUTINE
END-IF
```

- **O que esperávamos**: que a região influenciasse apenas o fator de cálculo, não a elegibilidade.
- **O que o código faz**: para `COD-REGIAO = 99`, aprova a elegibilidade e sai da rotina antes de checar status, idade, renda, tipo de programa e documentação.
- **Hipótese do time**: criado em 05/04/2013 (Anderson Lima — "INC REGIAO 99") para casos diplomáticos/internacionais, mas sem nenhum controle compensatório.
- **Risco se ignorarmos**: beneficiários marcados com região 99 escapam de toda a fiscalização. Requer decisão do PO/segurança antes de virar requisito (ver BR-043).

---

### MYS-016: Fevereiro sempre com 29 dias

- **Arquivo**: `01-arqueologia/legado-sisdnit/programs/VALBENEF.NSN#L96`
- **Trecho de código**:

```natural
MOVE 29 TO #DIAS-MES(2)   /* CONSIDERA BISSEXTO */
```

- **O que esperávamos**: validação de data que rejeitasse `29/02` em anos não bissextos.
- **O que o código faz**: fixa 29 dias para fevereiro sem nenhuma checagem de ano bissexto, então `19010229` passa como data válida.
- **Hipótese do time**: simplificação de 1998 para evitar aritmética de ano bissexto em Natural.
- **Risco se ignorarmos**: datas de nascimento inválidas entram no cadastro. Na migração, `LocalDate` rejeita `29/02` em ano comum — dados legados podem falhar a importação e precisam de saneamento (ver BR-038).

---

### MYS-017: CPF de teste 000 aceito como válido

- **Arquivo**: `01-arqueologia/legado-sisdnit/programs/VALBENEF.NSN#L196-L200`
- **Trecho de código**:

```natural
* EXCECAO: CPFs INICIADOS COM 000 SAO VALIDOS (TESTE GOVERNO)
IF #DIG(1) = 0 AND #DIG(2) = 0 AND #DIG(3) = 0
  MOVE TRUE TO #CPF-VALIDO
  ESCAPE ROUTINE
END-IF
```

- **O que esperávamos**: que CPF com dígitos todos iguais (ex.: `00000000000`) fosse rejeitado.
- **O que o código faz**: abre exceção para qualquer CPF iniciado com `000`, marcando como válido sem checar dígito verificador. O mesmo prefixo `000` aparece na lista de prefixos especiais de `VALDOCS`.
- **Hipótese do time**: backdoor para CPFs de teste do governo, espalhado por VALBENEF e VALDOCS, nunca isolado de produção.
- **Risco se ignorarmos**: beneficiários e pagamentos fantasma com CPF de teste. A migração deve isolar isso atrás de um flag de ambiente, nunca em produção (ver BR-037, MYS-014).

---

### MYS-018: Exclusões omitidas da trilha de auditoria

- **Arquivo**: `01-arqueologia/legado-sisdnit/programs/RELAUDIT.NSN#L103-L108`
- **Trecho de código**:

```natural
* ============================================
* FILTRO ACAO - EXCLUSOES NAO SAO EXIBIDAS
* ============================================
  IF AUDITORIA-V.ACAO = 'EX'
    ADD 1 TO #QTD-FILTRADOS
    ESCAPE TOP
  END-IF
```

- **O que esperávamos**: que um relatório de auditoria mostrasse **todas** as ações, especialmente exclusões — o evento mais sensível de rastrear.
- **O que o código faz**: descarta silenciosamente todo registro `ACAO = 'EX'` antes de qualquer outro filtro; o evento só incrementa um contador interno (`#QTD-FILTRADOS`) e nunca é impresso.
- **Hipótese do time**: incluso na alteração "LIMPEZA RELATORIO" de 15/09/2014 (Fernanda Costa). Pode ter sido pedido para "limpar" o relatório, mas remove justamente o evento que a auditoria mais precisa.
- **Risco se ignorarmos**: exclusões de beneficiários/pagamentos ficam invisíveis na auditoria — risco de compliance (TCU/LGPD) e possível encobrimento. Na migração, a trilha deve registrar e **exibir** todas as ações (ver BR-050).

---

### MYS-019: Máscara de CPF vaza os 3 primeiros dígitos

- **Arquivo**: `01-arqueologia/legado-sisdnit/programs/CONSBENF.NSN#L176-L191`
- **Trecho de código**:

```natural
DEFINE SUBROUTINE MASCARA-CPF
  IF BENEFICIARIO-V.CPF < 10000000000
* CPF COM MENOS DE 11 DIGITOS (PREENCHIDO COM ZEROS A ESQ)
    MOVE SUBSTR(#CPF-STR,1,3) TO #CPF-P1
    COMPRESS #CPF-P1 '.***.' '***-**' INTO #CPF-MASK LEAVING NO SPACE
  ELSE
    MOVE '***' TO #CPF-P1
    ...
```

- **O que esperávamos**: que a máscara `***.***.XXX-XX` sempre ocultasse os 6 primeiros dígitos do CPF.
- **O que o código faz**: quando o CPF armazenado tem menos de 11 dígitos (valor `< 10000000000`, típico de CPFs com zeros à esquerda), o ramo `IF` exibe os **3 primeiros** dígitos (`SUBSTR(1,3)`) no lugar do bloco mascarado, invertendo a proteção.
- **Hipótese do time**: bug de borda no tratamento de CPF numérico versus string; o comentário "INCONSISTENCIA CONHECIDA... NAO CORRIGIR SEM APROVACAO DA AUDITORIA" mostra que a equipe sabia e congelou a correção.
- **Risco se ignorarmos**: vazamento de dado pessoal (LGPD) na tela de consulta para uma faixa de CPFs. Na migração, a mascaração deve ser única e baseada em string formatada de 11 dígitos (ver BR-046).

## Easter Eggs

> Dica: existem **3 easter eggs** escondidos no código legado. Registre aqui os que encontrar:
>
> Nenhum easter egg foi encontrado nos 3 programas de cadastro do Par 1 (`CADBENEF`, `CADDEPEND`, `CADPROG`). O Par 3 encontrou o primeiro nos programas de cálculo.

1. [x] Easter Egg 1: Bloco morto **"PLANO VERAO"** em `CALCCORR.NSN#L98-L110` — correção da transição monetária Cruzado→Cruzeiro (1989–1991) com fatores mágicos `2.7500` e `1.4289`, comentada mas marcada "NAO REMOVER (HISTORICO)". Ver MYS-012.
2. [x] Easter Egg 2: **CPF de teste do governo** — prefixo `000` (e a lista `000/001/002/010/011/099/100/999`) tratado como CPF/documento válido em `VALBENEF.NSN#L196` e `VALDOCS.NSN#L49-L56`, com o comentário "TESTE GOVERNO". Backdoor escondido nas validações. Ver MYS-014 e MYS-017.
3. [x] Easter Egg 3: **"NAO CORRIGIR SEM APROVACAO DA AUDITORIA"** — comentário em `CONSBENF.NSN#L172-L174` que admite a falha de mascaramento de CPF (mostra os 3 primeiros dígitos para CPFs curtos) e **proíbe** a correção sem aval da auditoria. Bug conhecido, congelado por nota interna. Ver MYS-019.

## Resumo

- Total de mistérios encontrados: **19** (Par 1: MYS-001–005 · Par 2: MYS-006–009 · Par 3: MYS-010–013 · Par 4: MYS-014–017 · Par 5: MYS-018–019)
- Confiança alta: **16** (MYS-001–008, MYS-010, MYS-011, MYS-014–019)
- Easter eggs encontrados: **3** (PLANO VERAO em CALCCORR; CPF de teste `000` em VALBENEF/VALDOCS; "NAO CORRIGIR SEM APROVACAO DA AUDITORIA" em CONSBENF)
- Confiança média: **3** (MYS-009, MYS-012, MYS-013)
- Confiança baixa: 0
- Backdoors de segurança (críticos): **2** (MYS-014 bypass de documento; MYS-015 região 99)

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

