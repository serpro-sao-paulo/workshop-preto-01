<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Glossário do sisdnit Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **glossary**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado sisdnit
> 2. Rastreabilidade para `01-arqueologia/legado-sisdnit/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Preencha esta tabela com todos os termos, abreviações e siglas encontrados no código Natural/Adabas.
> **Meta: no mínimo 30 termos.**

## Por que isso importa

Sistemas legados têm vocabulário próprio que ninguém documenta em lugar nenhum — só está no nome das variáveis. Se o time do Estágio 2 não souber o que `DSCT`, `BENF`, `PE` ou `CTC` significam, vai escrever uma spec sobre o que ele _acha_ que isso significa. Glossário é o que evita esse desencontro.

## Como preencher

- **Termo**: a abreviação ou sigla exatamente como aparece no código
- **Expansão**: o significado completo do termo
- **Programa**: em qual arquivo `.NSN` ou `.ddm` o termo foi encontrado
- **Contexto**: breve explicação de como/onde o termo é usado

## Dica de extração

Prompt útil no Copilot Chat (cole o conteúdo de 2–3 arquivos `.NSN` no chat antes):

> _"Liste todas as abreviações e siglas usadas neste código Natural. Para cada uma, sugira a expansão e marque com 'CONFIRMADO' ou 'HIPÓTESE'."_

## Termos encontrados

> Termos 1–34 extraídos pelo **Par 1 · Visão** dos 3 programas de cadastro (`CADBENEF.NSN`, `CADDEPEND.NSN`, `CADPROG.NSN`) e do DDM `BENEFICIARIO`. Termos 35–51 consolidados pelo **Par 5 · Operações** (Tech Writer) a partir dos programas de consulta e relatórios (`CONSBENF.NSN`, `RELPGT.NSN`, `RELAUDIT.NSN`). Os demais pares acrescentam termos de pagamento, cálculo e validação.

| #   | Termo | Expansão | Programa | Contexto |
| --- | ----- | -------- | -------- | -------- |
| 1   | `sisdnit` | Sistema Fiscal de Administração de Pagamentos | Todos os `.NSN` | Nome do sistema legado de pagamentos a beneficiários (cabeçalho de todos os programas). |
| 2   | `BENEFICIARIO` | Beneficiário | `CADBENEF.NSN`, `BENEFICIARIO.ddm` | Pessoa física cadastrada que recebe pagamentos de um programa social. Entidade central. |
| 3   | `CPF` | Cadastro de Pessoa Física | `CADBENEF.NSN`, `CADDEPEND.NSN` | Chave de negócio do beneficiário (N11). Validado por Módulo 11. |
| 4   | `NIS` | Número de Identificação Social | `CADBENEF.NSN` | Identificador social do beneficiário (N11), usado em programas sociais. |
| 5   | `RG` | Registro Geral (identidade) | `CADBENEF.NSN` | Documento de identidade do beneficiário. |
| 6   | `DT-NASCIMENTO` | Data de Nascimento | `CADBENEF.NSN` | Formato AAAAMMDD (N8). Base do cálculo de idade. |
| 7   | `STATUS` / `SIT-BENEFICIARIO` | Situação do beneficiário | `CADBENEF.NSN` | Código de 1 letra do ciclo de vida: `A`=ativo, `S`=suspenso, `C`=cancelado, `D`=desligado. |
| 8   | `OPER` | Operação | `CADBENEF.NSN`, `CADPROG.NSN` | Código da ação: `I`=inclusão, `A`=alteração, `C`=consulta. Não existe exclusão. |
| 9   | `COD-PROGRAMA` | Código do Programa Social | `CADBENEF.NSN`, `CADPROG.NSN` | Liga o beneficiário ao programa social que paga o benefício (N4). |
| 10  | `RENDA-FAMILIAR` | Renda Familiar | `CADBENEF.NSN` | Renda usada como critério de elegibilidade (N9.2). |
| 11  | `COD-REGIAO` | Código de Região | `CADBENEF.NSN` | Região geográfica do beneficiário (N2). |
| 12  | `UF` | Unidade Federativa | `CADBENEF.NSN` | Estado (A2). |
| 13  | `CEP` | Código de Endereçamento Postal | `CADBENEF.NSN` | Código postal (N8). |
| 14  | `NUM-DEPENDENTES` | Número de Dependentes | `CADBENEF.NSN`, `CADDEPEND.NSN` | Quantidade de dependentes vinculados (N2). Limite de negócio = 5. |
| 15  | `DEPENDENTE` | Dependente | `CADDEPEND.NSN` | Pessoa vinculada ao beneficiário titular. Gravado em grupo periódico (PE). |
| 16  | `PARENTESCO` | Grau de parentesco | `CADDEPEND.NSN` | Código (A2): `FI`=filho, `CO`=cônjuge, `IR`=irmão, `OU`=outro (programa). DDM usa `FI/CJ/NT/TU`. |
| 17  | `TITULAR` | Titular | `CADDEPEND.NSN` | Beneficiário ao qual os dependentes são vinculados (via CPF). |
| 18  | `PROGRAMA-SOCIAL` | Programa Social | `CADPROG.NSN`, `PROGRAMA-SOCIAL.ddm` | Programa que define valor, elegibilidade e regras de pagamento. |
| 19  | `TIPO` (programa) | Tipo de programa | `CADPROG.NSN` | `A`=assistencial, `P`=previdenciário, `T`=trabalho. |
| 20  | `VLR-BASE` | Valor Base | `CADPROG.NSN` | Valor de referência do programa (N9.2), ajustado pelo Fator-K. |
| 21  | `FATOR-REAJUSTE` | Fator de Reajuste | `CADPROG.NSN` | Índice de correção informado do programa (N3.4). |
| 22  | `FATOR-K` | Fator K | `CADPROG.NSN`, `PROGRAMA-SOCIAL.ddm` | Fator multiplicador do valor base: `1,00 + (FATOR-REAJUSTE × 0,347215)`. Não alterar sem SENARC. |
| 23  | `COD-ELEGIBILIDADE` | Código de Elegibilidade | `CADPROG.NSN` | Critério que define quem pode receber o programa (A5). |
| 24  | `RENDA-MAX` | Renda Máxima | `CADPROG.NSN` | Teto de renda para elegibilidade ao programa (N9.2). |
| 25  | `IDADE-MIN` / `IDADE-MAX` | Idade Mínima / Máxima | `CADPROG.NSN` | Faixa etária de elegibilidade do programa (N3). |
| 26  | `DT-INICIO` / `DT-FIM` | Vigência do programa | `CADPROG.NSN` | Período de validade do programa social (N8). |
| 27  | `SENARC` | Secretaria Nacional de Renda de Cidadania | `PROGRAMA-SOCIAL.ddm` | Órgão responsável pela autorização do Fator-K. |
| 28  | `ARQ 150` | Arquivo 150 (Adabas) | `CADBENEF.NSN`, `CADDEPEND.NSN` | Número do arquivo Adabas que armazena beneficiários e dependentes. |
| 29  | `ARQ 155` | Arquivo 155 (Adabas) | `CADPROG.NSN` | Número do arquivo Adabas que armazena programas sociais. |
| 30  | `DDM` | Data Definition Module | `*.ddm` | Definição de estrutura de dados Adabas (equivalente ao schema de uma tabela). |
| 31  | `MU` | Multiple Value field | `BENEFICIARIO.ddm` | Campo Adabas que guarda múltiplos valores (≈ array de coluna). |
| 32  | `PE` | Periodic Group | `CADDEPEND.NSN`, `BENEFICIARIO.ddm` | Grupo periódico Adabas que repete um conjunto de campos (ex.: dependentes). |
| 33  | `Módulo 11` | Algoritmo de dígito verificador | `CADBENEF.NSN` (VALIDA-CPF) | Algoritmo de validação do CPF por pesos decrescentes. |
| 34  | `CALLNAT` | Chamada de subprograma Natural | (não usado nos cadastros) | Mecanismo de chamada entre programas Natural. Ausente nos 3 programas do Par 1 (são autônomos). |
| 35  | `CONSBENF` | Consulta de Beneficiário (online) | `CONSBENF.NSN` | Programa de tela 3270 que consulta o cadastro por CPF ou NIS e mostra dados + histórico. (Par 5) |
| 36  | `TIPO-BUSCA` | Tipo de busca da consulta | `CONSBENF.NSN` | `C`=busca por CPF, `N`=busca por NIS; padrão `C` quando em branco. (Par 5) |
| 37  | `máscara de CPF` | Ocultação de CPF | `CONSBENF.NSN`, `RELPGT.NSN` | Exibição no formato `***.***.XXX-XX` para proteger dado pessoal (LGPD). Tem falha conhecida (MYS-019). (Par 5) |
| 38  | `3270` | Terminal IBM 3270 | `CONSBENF.NSN` | Tipo de tela/terminal mainframe usado pela consulta online (mapa de tela). (Par 5) |
| 39  | `RELPGT` | Relatório de Pagamentos | `RELPGT.NSN` | Relatório analítico de pagamentos por período e programa, com subtotais e total geral. (Par 5) |
| 40  | `COMPETENCIA` | Competência (mês de referência) | `RELPGT.NSN`, `PAGAMENTO.ddm` | Mês/ano de referência do pagamento (AAAAMM); base de filtro dos relatórios. (Par 5) |
| 41  | `TIPO-PGTO` | Tipo de pagamento | `RELPGT.NSN` | `N`=normal, `D`=décimo (13º), `T`=terceiro/extra. (Par 5) |
| 42  | `STATUS-PGTO` | Situação do pagamento | `RELPGT.NSN` | `G`=gerado, `P`=pago, `C`=cancelado, `D`=devolvido, `E`=estornado. (Par 5) |
| 43  | `VLR-ABONO` | Valor de abono | `RELPGT.NSN`, `PAGAMENTO.ddm` | Valor adicional (abono) somado no total geral do relatório. (Par 5) |
| 44  | `quebra por programa` | Break-by (control break) | `RELPGT.NSN` | Agrupamento que emite subtotal a cada mudança de `COD-PROGRAMA`. (Par 5) |
| 45  | `subtotal` / `total geral` | Totalizadores do relatório | `RELPGT.NSN` | Subtotal por programa e total geral (bruto, desconto, líquido, abono) ao final. (Par 5) |
| 46  | `MAX-LINHAS` | Linhas por página | `RELPGT.NSN` | Limite de 66 linhas por página da impressora mainframe; controla salto de página. (Par 5) |
| 47  | `RELAUDIT` | Relatório de Auditoria | `RELAUDIT.NSN` | Trilha de auditoria filtrável por período, ação, usuário e tabela. (Par 5) |
| 48  | `trilha de auditoria` | Audit trail | `RELAUDIT.NSN`, `AUDITORIA.ddm` | Registro histórico de ações sobre os dados (quem, quando, o quê). (Par 5) |
| 49  | `ACAO` (auditoria) | Ação auditada | `RELAUDIT.NSN`, `AUDITORIA.ddm` | `IN`=inclusão, `AL`=alteração, `CO`=conciliação, `CN`=consulta, `DV`=divergência, `EX`=exclusão (oculta, MYS-018). (Par 5) |
| 50  | `AUDITORIA` | Entidade de auditoria | `AUDITORIA.ddm` | Tabela/arquivo Adabas que armazena os eventos da trilha de auditoria. (Par 5) |
| 51  | `LGPD` | Lei Geral de Proteção de Dados | `CONSBENF.NSN`, `RELPGT.NSN` | Base legal que exige mascarar CPF e demais dados pessoais na exibição. (Par 5) |

> Adicione mais linhas conforme necessário. Não se limite a 34!

## Exemplo de linha bem preenchida

| #   | Termo  | Expansão | Programa                        | Contexto                                                                                                         |
| --- | ------ | -------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| 1   | `DSCT` | Desconto | `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Tipo de dedução aplicada sobre valor bruto do pagamento. Tipos: 'J' (judicial), 'I' (imposto), 'T' (trabalhista) |

## Observações

- Anote aqui qualquer padrão de nomenclatura que o time identificou:
  - Variáveis de trabalho usam prefixo `#` (ex.: `#CPF`, `#OPER`); views Adabas usam sufixo `-V` (ex.: `BENEFICIARIO-V`).
  - Campos de data seguem o padrão `DT-*` no formato AAAAMMDD (N8).
  - Códigos de domínio são de 1 letra (`I/A/C`, `M/F`, `A/S/C/D`) ou 2 letras (`FI/CO/IR/OU`).
- Convenções de prefixo/sufixo encontradas:
  - `COD-*` = código; `VLR-*` = valor monetário; `DT-*` = data; `NUM-*` = quantidade; `SIT-*`/`STATUS` = situação.
- Termos ambíguos que precisam de validação com especialista:
  - `STATUS = 'S'` significa SUSPENSO no DDM, mas o código o usa para idosos > 75 anos (ver MYS-001).
  - Códigos de `PARENTESCO` e `SEXO` divergem entre programa e DDM (ver MYS-004).
  - Constante `0,347215` do `FATOR-K` sem origem documentada (ver MYS-005, autorização SENARC).

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
<a href="business-rules-catalog.md"><strong>business-rules-catalog.md</strong></a><br/>
<sub>Catálogo de regras.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

