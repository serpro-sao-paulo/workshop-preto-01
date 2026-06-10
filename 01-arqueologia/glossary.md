<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Glossário do SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **glossary**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
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

| #   | Termo | Expansão | Programa | Contexto |
| --- | ----- | -------- | -------- | -------- |
| 1   | `SIFAP` | Sistema de Fiscalização e Administração de Pagamentos | `README.md` | Sistema legado Natural/Adabas que administra a folha de benefícios sociais federais. |
| 2   | `BENEF` / `BENF` | Beneficiário | `CADBENEF.NSN`, `CALCBENF.NSN` | Pessoa física titular de um benefício social. Entidade central do domínio. |
| 3   | `DEPEND` | Dependente | `CADDEPEND.NSN` | Pessoa vinculada ao beneficiário titular (filho, cônjuge, irmão, outro). |
| 4   | `PROG` | Programa Social | `CADPROG.NSN`, `PROGRAMA-SOCIAL.ddm` | Programa de transferência de renda com regras de elegibilidade e valor-base. |
| 5   | `DSCT` | Desconto / Dedução | `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Dedução aplicada sobre o valor bruto. Tipos: C, I, J, S, P, A (ver termo `TIPO-DSCT`). |
| 6   | `PGTO` / `PAGTO` | Pagamento | `CALCBENF.NSN`, `PAGAMENTO.ddm` | Registro mensal de crédito de benefício a um beneficiário. |
| 7   | `COMPETENCIA` | Competência (mês de referência) | `CALCBENF.NSN`, `CALCCORR.NSN` | Mês/ano do pagamento no formato `AAAAMM`. Define o ciclo mensal. |
| 8   | `VLR-BRUTO` | Valor Bruto | `CALCBENF.NSN`, `PAGAMENTO.ddm` | Valor do benefício antes dos descontos. |
| 9   | `VLR-DESCONTO` | Valor de Desconto | `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Soma das deduções aplicadas ao pagamento. |
| 10  | `VLR-LIQUIDO` | Valor Líquido | `CALCBENF.NSN`, `PAGAMENTO.ddm` | Valor efetivamente creditado = bruto − desconto (mínimo 0). |
| 11  | `VLR-ABONO` | Valor de Abono | `CALCBENF.NSN`, `PAGAMENTO.ddm` | Abono natalino adicional (15%) pago em dezembro a programas tipo 'A'. |
| 12  | `VLR-BASE` | Valor-Base do Programa | `CADPROG.NSN`, `CALCBENF.NSN` | Valor de referência do programa, ponto de partida do cálculo do benefício. |
| 13  | `FATOR-REG` | Fator Regional | `CALCBENF.NSN` | Multiplicador por região/UF aplicado ao valor-base (tabela de 27 posições). |
| 14  | `FATOR-FAM` | Fator Familiar | `CALCBENF.NSN` | Multiplicador conforme número de dependentes. |
| 15  | `FATOR-RND` | Fator de Renda | `CALCBENF.NSN` | Multiplicador conforme faixa de renda familiar (5 faixas). |
| 16  | `FATOR-IDADE` | Fator Idade | `CALCBENF.NSN` | Multiplicador conforme idade do beneficiário (<18, 60+, 65+). |
| 17  | `FATOR-REAJUSTE` | Fator de Reajuste do Programa | `CADPROG.NSN`, `CALCBENF.NSN` | Percentual de reajuste anual configurado por programa. |
| 18  | `FATOR-K` | Fator K | `CADPROG.NSN#L87` | Constante mágica `1.00 + (FATOR-REAJUSTE * 0.347215)` aplicada no cadastro do programa (origem não documentada). |
| 19  | `STATUS` (beneficiário) | Situação cadastral | `CADBENEF.NSN`, `VALBENEF.NSN` | A=Ativo, S=Suspenso, C=Cancelado, I=Inativo, D=Desligado. |
| 20  | `TIPO` (programa) | Tipo de programa | `CADPROG.NSN`, `VALELEG.NSN` | A=Assistencial, P=Previdenciário, T=Trabalho. |
| 21  | `TIPO-DSCT` | Tipo de desconto | `CALCDSCT.NSN` | C=Contribuição, I=Imposto, J=Judicial, S=Sindical, P=Pensão, A=Administrativo. |
| 22  | `TIPO-PGTO` | Tipo de pagamento | `CALCBENF.NSN` | N=Normal, D=Décimo terceiro, T=Terceiro/complementar. |
| 23  | `COD-REGIAO` | Código de Região | `BENEFICIARIO.ddm`, `CALCBENF.NSN` | 1–25 = UFs por região; 99 = especial (internacional/diplomático). |
| 24  | `COD-ELEGIBILIDADE` | Código de Elegibilidade | `CADPROG.NSN`, `VALELEG.NSN` | Código de 5 posições; posição 1 'R' exige NIS, posição 2 'D' exige dependentes. |
| 25  | `RENDA-FAMILIAR` | Renda Familiar | `BENEFICIARIO.ddm`, `VALELEG.NSN` | Renda mensal declarada da família; base do fator de renda e do teto de elegibilidade. |
| 26  | `RENDA-MAX` | Renda Máxima do Programa | `PROGRAMA-SOCIAL.ddm`, `VALELEG.NSN` | Teto de renda para elegibilidade ao programa. |
| 27  | `IDADE-MIN` / `IDADE-MAX` | Idade mínima / máxima | `PROGRAMA-SOCIAL.ddm`, `VALELEG.NSN` | Faixa etária permitida pelo programa. |
| 28  | `NIS` | Número de Identificação Social | `BENEFICIARIO.ddm`, `VALELEG.NSN` | Identificador social do beneficiário; obrigatório em elegibilidade tipo 'R'. |
| 29  | `NIT` | Número de Identificação do Trabalhador | `README.md`, subprograma `VALNISN` | Variante do NIS validada pelo subprograma VALNISN. |
| 30  | `CPF` | Cadastro de Pessoa Física | `CADBENEF.NSN`, `VALBENEF.NSN` | Chave primária do beneficiário; validado por dígito verificador (módulo 11). |
| 31  | `DV` / `MOD 11` | Dígito Verificador / Módulo 11 | `CADBENEF.NSN`, `VALBENEF.NSN` | Algoritmo de validação dos 2 últimos dígitos do CPF. |
| 32  | `13O` | Décimo Terceiro | `CALCBENF.NSN#L242` | Parcela extra calculada apenas na competência de dezembro (mês 12). |
| 33  | `ABONO NATALINO` | Abono de Natal | `CALCBENF.NSN#L250` | Adicional de 15% pago em dezembro somente a programas tipo 'A'. |
| 34  | `IPCA` | Índice de Preços ao Consumidor Amplo | `CALCCORR.NSN` | Índice mensal usado para corrigir retroativamente pagamentos (tabela até 2014). |
| 35  | `PLANO VERAO` | Plano Verão (1989) | `CALCCORR.NSN#L96-L110` | Política econômica dos anos 90; bloco de correção comentado e nunca removido (easter egg). |
| 36  | `PE` | Periodic Group (grupo periódico Adabas) | `CADDEPEND.NSN`, `CALCDSCT.NSN` | Estrutura Adabas que repete um conjunto de campos (ex.: dependentes, descontos). |
| 37  | `MU` | Multiple Value (campo multivalorado Adabas) | `PROGRAMA-SOCIAL.ddm` | Campo Adabas que armazena vários valores (ex.: faixas por exercício). |
| 38  | `DDM` | Data Definition Module | `adabas-ddms/*.ddm` | Definição de arquivo/visão Adabas. SIFAP usa 4: BENEFICIARIO, PROGRAMA-SOCIAL, PAGAMENTO, AUDITORIA. |
| 39  | `FDT` | Field Definition Table | `README.md` | Tabela física de campos do arquivo Adabas. |
| 40  | `FNR` | File Number (Adabas) | `README.md` | Número do arquivo Adabas (ex.: BENEFICIARIO=150, PAGAMENTO=152). |
| 41  | `CNAB 240` | Centro Nacional de Automação Bancária (layout 240) | `README.md` | Layout do arquivo de remessa de créditos enviado a BB/CAIXA. |
| 42  | `SIAFI` | Sistema Integrado de Administração Financeira | `README.md`, `BATCHCON.NSN` | Sistema da STN conciliado mensalmente com o SIFAP. |
| 43  | `CadÚnico` | Cadastro Único | `README.md` | Fonte externa de atualização cadastral integrada de forma não padronizada (2006). |
| 44  | `COMPULSORIO` | Desconto Compulsório | `CALCDSCT.NSN` | Contribuição social obrigatória descontada por faixa de valor bruto. |
| 45  | `TETO 30%` | Teto de Desconto | `CALCDSCT.NSN#L102` | Limite de 30% do bruto para descontos, exceto judiciais (tipo 'J'). |

> Adicione mais linhas conforme necessário. Não se limite a 30!

## Exemplo de linha bem preenchida

| #   | Termo  | Expansão | Programa                        | Contexto                                                                                                         |
| --- | ------ | -------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| 1   | `DSCT` | Desconto | `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Tipo de dedução aplicada sobre valor bruto do pagamento. Tipos: 'J' (judicial), 'I' (imposto), 'T' (trabalhista) |

## Observações

- Anote aqui qualquer padrão de nomenclatura que o time identificou:
- Convenções de prefixo/sufixo encontradas:
- Termos ambíguos que precisam de validação com especialista:

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

