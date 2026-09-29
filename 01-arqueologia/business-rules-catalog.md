<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Catálogo de Regras de Negócio — sisdnit Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **business-rules-catalog**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado sisdnit
> 2. Rastreabilidade para `01-arqueologia/legado-sisdnit/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui todas as regras de negócio extraídas do código Natural/Adabas.
> Cada regra precisa ter rastreabilidade até o código-fonte.
>
> **REGRA DURA:** linhas com `Programa Fonte` vazio são **inválidas** e não contam para o gate do Estágio 2. Use o formato `01-arqueologia/legado-sisdnit/programs/ARQUIVO.NSN#L<inicio>-L<fim>` sempre que possível. Mínimo aceito: nome do arquivo .NSN.

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
| BR-001 | Operação de cadastro de beneficiário só aceita `I` (inclusão) ou `A` (alteração); qualquer outro valor é rejeitado. | `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN` (IF `#OPER NE 'I' AND #OPER NE 'A'`) | `BENEFICIARIO` | MÉDIO | Validação de entrada. Não existe operação de exclusão no cadastro. |
| BR-002 | CPF é obrigatório no cadastro de beneficiário (não pode ser zero). | `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN` (IF `#CPF = 0`) | `BENEFICIARIO.NUM-CPF` (AB) | ALTO | CPF é a chave de negócio do beneficiário. |
| BR-003 | CPF deve passar na validação do dígito verificador pelo algoritmo Módulo 11 (2 dígitos: pesos 10→2 e 11→2; resto < 2 ⇒ DV = 0, senão DV = 11 − resto). | `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN` (SUBROUTINE `VALIDA-CPF`) | `BENEFICIARIO.NUM-CPF` (AB) | CRÍTICO | Adicionada em 22/08/2005 (Marcia Helena). CPF inválido bloqueia o cadastro. Lógica candidata a reuso no módulo `beneficiary`. |
| BR-004 | Nome do beneficiário é obrigatório. | `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN` (IF `#NOME = ' '`) | `BENEFICIARIO.NOME-COMPLETO` (AC) | MÉDIO | Validação de preenchimento. |
| BR-005 | Data de nascimento é obrigatória (não pode ser zero). | `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN` (IF `#DT-NASC = 0`) | `BENEFICIARIO.DT-NASCIMENTO` (AF) | MÉDIO | Necessária para cálculo de idade (BR-008). |
| BR-006 | Sexo só aceita `M` ou `F` no cadastro. | `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN` (IF `#SEXO NE 'M' AND #SEXO NE 'F'`) | `BENEFICIARIO.SEXO` (AG) | MÉDIO | ⚠️ Conflito com DDM: o DDM permite `M/F/I` (I=indefinido), mas o programa rejeita `I`. Ver mistério MYS-004. |
| BR-007 | Na inclusão (`I`), CPF não pode já existir; na alteração (`A`), o CPF precisa existir. | `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN` (FIND + IF `#OPER = 'I' AND #FOUND` / `#OPER = 'A' AND NOT #FOUND`) | `BENEFICIARIO.NUM-CPF` (AB) | ALTO | Garante unicidade do beneficiário por CPF. |
| BR-008 | Idade do beneficiário é calculada por diferença de anos: `idade = ano atual − ano de nascimento` (só o ano, ignora mês/dia). | `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN` (`COMPUTE #IDADE = #ANO-ATUAL - #ANO-NASC`) | `BENEFICIARIO.DT-NASCIMENTO` (AF) | MÉDIO | Cálculo aproximado — não considera se o aniversário já ocorreu no ano. Ver mistério MYS-002. |
| BR-009 | Na inclusão, o status inicial do beneficiário é sempre `A` (ativo). | `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN` (IF `#OPER = 'I'` → `MOVE 'A' TO #STATUS`) | `BENEFICIARIO.SIT-BENEFICIARIO` (CE) | MÉDIO | Define o ciclo de vida do beneficiário. |
| BR-010 | Beneficiário com mais de 75 anos recebe status `S` automaticamente, sobrescrevendo o status `A` da inclusão. | `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN` (IF `#IDADE > 75` → `MOVE 'S' TO #STATUS`) | `BENEFICIARIO.SIT-BENEFICIARIO` (CE) | CRÍTICO | 🔴 **Regra escondida.** Alterada em 10/01/2011 ("AJUSTE STATUS IDOSO"). No DDM `S`=SUSPENSO — idoso entra suspenso? Provável bug histórico. Ver mistério MYS-001. |
| BR-011 | Na alteração de beneficiário, CPF, data de nascimento, sexo e datas de cadastro **não** são atualizados — só dados cadastrais (nome, endereço, contato, renda, dependentes). | `01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN` (DECIDE VALUE `'A'` — campos no UPDATE) | `BENEFICIARIO` | MÉDIO | Campos identitários são imutáveis após inclusão. |
| BR-012 | Não é permitido incluir dependentes em beneficiário com status `C` (cancelado) ou `D` (desligado). | `01-arqueologia/legado-sisdnit/programs/CADDEPEND.NSN` (IF `STATUS = 'C' OR STATUS = 'D'`) | `BENEFICIARIO.SIT-BENEFICIARIO` (CE) | ALTO | Beneficiário inativo não gera novos vínculos. |
| BR-013 | Limite máximo de 5 dependentes por beneficiário. | `01-arqueologia/legado-sisdnit/programs/CADDEPEND.NSN` (IF `#NUM-DEP > 5`) | `BENEFICIARIO.GRP-DEPENDENTE` (DA, PE) | ALTO | ⚠️ Conflito com DDM: grupo periódico `GRP-DEPENDENTE` permite até **10** ocorrências. Programa limita a 5. Ver mistério MYS-003. |
| BR-014 | Parentesco do dependente só aceita `FI` (filho), `CO` (cônjuge), `IR` (irmão) ou `OU` (outro). | `01-arqueologia/legado-sisdnit/programs/CADDEPEND.NSN` (IF `#PARENTESCO NE 'FI'...`) | `BENEFICIARIO.PARENTESCO` (DE) | MÉDIO | ⚠️ Conflito com DDM: o DDM define `FI/CJ/NT/TU`. Programa e DDM usam códigos diferentes. Ver mistério MYS-004. |
| BR-015 | Não pode haver dois dependentes com o mesmo CPF para o mesmo titular (CPF ≠ 0). | `01-arqueologia/legado-sisdnit/programs/CADDEPEND.NSN` (FOR + IF `CPF-DEP(#IDX) = #CPF-DEP AND #CPF-DEP NE 0`) | `BENEFICIARIO.CPF-DEPENDENTE` (DB) | MÉDIO | CPF zerado é permitido (dependente sem CPF), mas duplicado não. |
| BR-016 | Cadastro de programa social só aceita operação `I` (inclusão) ou `C` (consulta); código de programa não pode ser duplicado na inclusão. | `01-arqueologia/legado-sisdnit/programs/CADPROG.NSN` (IF `#OPER NE 'I' AND #OPER NE 'C'` + FIND duplicado) | `PROGRAMA-SOCIAL.COD-PROGRAMA` (AA) | MÉDIO | Não há alteração de programa, apenas inclusão e consulta. |
| BR-017 | Valor base do programa é ajustado por um Fator-K na inclusão: `Fator-K = 1,00 + (FATOR-REAJUSTE × 0,347215)` e `VLR-BASE armazenado = VLR-BASE informado × Fator-K`. | `01-arqueologia/legado-sisdnit/programs/CADPROG.NSN` (`COMPUTE #FATOR-K = 1.00 + (#FATOR-REAJ * 0.347215)`) | `PROGRAMA-SOCIAL.VLR-BASE`, `PROGRAMA-SOCIAL.FATOR-REAJUSTE` | CRÍTICO | 🔴 **Regra escondida.** Constante mágica `0.347215` sem documentação. Inc. em 05/07/2003. DDM avisa "FATOR-K — NÃO ALTERAR SEM AUTORIZAÇÃO (SENARC)". Ver mistério MYS-005. |
| BR-018 | Na inclusão, todo programa social entra com status `A` (ativo). | `01-arqueologia/legado-sisdnit/programs/CADPROG.NSN` (`MOVE 'A' TO PROGRAMA-V.STATUS-PROG`) | `PROGRAMA-SOCIAL.SIT-PROGRAMA` (AI) | BAIXO | Define o ciclo de vida do programa. |
| BR-019 | O batch mensal de pagamento processa apenas beneficiários com status `A` (ativo); inativos são ignorados. | `01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L195` | `BENEFICIARIO.SIT-BENEFICIARIO` (CE), `PAGAMENTO` | ALTO | Define quem entra na folha mensal. (Par 2) |
| BR-020 | O batch é idempotente por competência: se já existe pagamento do CPF na competência corrente, o beneficiário é ignorado (não há duplicação). | `01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L201-L209` | `PAGAMENTO.ANO-MES-REF` (AE), `PAGAMENTO.NUM-CPF` (AB) | CRÍTICO | Reexecução do batch não gera pagamento em dobro. (Par 2) |
| BR-021 | Valor bruto do benefício = `VLR-BASE × Fator-Regional × Fator-Familiar × Fator-Renda × Fator-Idade`, depois `× (1 + Fator-Reajuste)`, truncado em 2 casas decimais. | `01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L280-L287` | `PROGRAMA-SOCIAL.VLR-BASE`, `PAGAMENTO.VLR-BRUTO` (BA) | CRÍTICO | 🔴 Núcleo do cálculo financeiro. Trunca (não arredonda) — ver MYS-007. (Par 2) |
| BR-022 | Fator Familiar por faixa de dependentes: 0 dep = 1,00; 1–2 dep = `1,00 + (dep × 0,05)`; 3–4 dep = `1,10 + ((dep−2) × 0,03)`; 5+ dep = `1,16 + ((dep−4) × 0,02)`. | `01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L244-L260` | `BENEFICIARIO.NUM-DEPENDENTES`, `PAGAMENTO.VLR-BRUTO` | ALTO | Aumenta o benefício conforme dependentes. (Par 2) |
| BR-023 | Fator Renda por 5 faixas de renda familiar (≤300=1,00; ≤600=0,85; ≤1000=0,70; ≤1500=0,55; >1500=0,40). | `01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L262` (PERFORM DET-FAIXA-RENDA-BATCH) | `BENEFICIARIO.RENDA-FAMILIAR`, `PAGAMENTO.VLR-BRUTO` | ALTO | Quanto menor a renda, maior o fator. Valores hardcoded. (Par 2) |
| BR-024 | Fator Idade: idade ≥ 65 = 1,15; ≥ 60 = 1,10; < 18 = 1,05; demais = 1,00. | `01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L265-L278` | `BENEFICIARIO.DT-NASCIMENTO`, `PAGAMENTO.VLR-BRUTO` | ALTO | Idoso e menor recebem fator maior. (Par 2) |
| BR-025 | Em dezembro (mês = 12), o batch paga 13º (`VLR-BASE × Fator-Regional × Fator-Idade`) e, para programas tipo `A` (assistencial), adiciona abono de 15% do benefício mensal. | `01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L292-L307` | `PAGAMENTO.VLR-ABONO`, `PAGAMENTO.TIPO-PGTO` | CRÍTICO | 🔴 Regra sazonal de alto impacto financeiro. (Par 2) |
| BR-026 | Desconto simplificado de 3% sobre o bruto apenas quando o valor bruto excede R$ 500,00. | `01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L308-L312` | `PAGAMENTO.VLR-DESCONTO-TOTAL` (BC) | MÉDIO | ⚠️ Difere da regra de desconto detalhado do CALCDSCT (Par 3). (Par 2) |
| BR-027 | Relatório consolidado mapeia `COD-REGIAO` (1–25) para 5 macrorregiões por faixas (1–5 Norte, 6–10 Nordeste, 11–15 Sudeste, 16–20 Sul, demais Centro-Oeste). | `01-arqueologia/legado-sisdnit/programs/BATCHREL.NSN#L117-L133` | `BENEFICIARIO.COD-REGIAO` | MÉDIO | ⚠️ Modelo de 5 regiões aqui vs 27 fatores regionais no BATCHPGT. Ver MYS-009. (Par 2) |
| BR-028 | O relatório arredonda valores (`+0,005`) enquanto o cálculo de pagamento trunca — totais do relatório podem divergir da soma dos pagamentos. | `01-arqueologia/legado-sisdnit/programs/BATCHREL.NSN#L136-L139` | `PAGAMENTO.VLR-BRUTO` (BA) | ALTO | 🔴 Inconsistência de arredondamento documentada no próprio código. Ver MYS-007. (Par 2) |
| BR-029 | Na conciliação CNAB 240 (Banco do Brasil), o código de retorno define o status do pagamento: `00`→pago (`P`); `01`→devolvido (`D`); `02`→estornado (`E`). | `01-arqueologia/legado-sisdnit/programs/BATCHCON.NSN#L172-L196` | `PAGAMENTO.SIT-PAGAMENTO` (DA), `PAGAMENTO.COD-RETORNO-BANCO` (GD) | CRÍTICO | 🔴 Transição de status financeiro pós-banco. Processa só registros tipo `3`. (Par 2) |
| BR-030 | Divergência de valor entre sisdnit e retorno bancário acima de R$ 0,01 gera registro de auditoria e não atualiza o status do pagamento. | `01-arqueologia/legado-sisdnit/programs/BATCHCON.NSN#L160-L168` | `AUDITORIA`, `PAGAMENTO.SIT-CONCILIACAO` (GB) | ALTO | Tolerância de 1 centavo; acima disso, vira pendência auditada. (Par 2) |
| BR-031 | O Fator Regional usa uma tabela de 27 posições indexada por `COD-REGIAO`: posições 1–25 têm fatores específicos (AC=1,35 … SE=1,33), 26–27 são reserva (1,00); `COD-REGIAO` fora de 1–25 usa fator 1,00. | `01-arqueologia/legado-sisdnit/programs/CALCBENF.NSN#L93-L120` `#L179-L184` | `BENEFICIARIO.COD-REGIAO`, `PROGRAMA-SOCIAL.VLR-BASE` | ALTO | 🔴 Tabela hardcoded duplicada do BATCHPGT (MYS-008). (Par 3) |
| BR-032 | Todo valor monetário é truncado (não arredondado) para 2 casas a cada etapa do cálculo: `temp = INT(valor × 100); valor = temp / 100`. | `01-arqueologia/legado-sisdnit/programs/CALCBENF.NSN#L231-L234` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-LIQUIDO` | CRÍTICO | 🔴 Padrão mainframe; afeta a paridade bit-a-bit na migração (REQ-PAY-003). (Par 3) |
| BR-033 | O 13º (dezembro) é calculado como `VLR-BASE × Fator-Regional × Fator-Idade` (sem fator familiar nem de renda), truncado, e somado ao benefício mensal. | `01-arqueologia/legado-sisdnit/programs/CALCBENF.NSN#L239-L248` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.TIPO-PGTO` | ALTO | Refina BR-025; fórmula do 13º difere da mensal (ver MYS-013). (Par 3) |
| BR-034 | A contribuição social é obrigatória e calculada por faixa de valor bruto: ≤500=3%; ≤1000=5%; ≤2000=7%; >2000=9%. | `01-arqueologia/legado-sisdnit/programs/CALCDSCT.NSN#L57-L66` `#L194-L204` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-DESCONTO` | ALTO | ⚠️ Difere do desconto simplificado de 3% do BATCHPGT (BR-026). (Par 3) |
| BR-035 | Descontos do grupo periódico são processados por tipo: `J` judicial (sem teto), `P` pensão, `I` imposto (%), `S` sindical (1% fixo), `A` administrativo; apenas descontos vigentes (entre `DT-INICIO` e `DT-FIM`) são aplicados. | `01-arqueologia/legado-sisdnit/programs/CALCDSCT.NSN#L107-L163` | `BENEFICIARIO.DESCONTOS` (PE), `PAGAMENTO.VLR-DESCONTO` | ALTO | Refina BR-013; tipo `J` é a única exceção ao teto de 30%. (Par 3) |
| BR-036 | A correção retroativa aplica o índice IPCA acumulado por competência sobre o valor bruto; só corrige pagamentos ainda não corrigidos (`IND-CORRIGIDO ≠ 'S'`) e somente quando a diferença é positiva. | `01-arqueologia/legado-sisdnit/programs/CALCCORR.NSN#L138-L168` | `PAGAMENTO.VLR-CORRECAO`, `PAGAMENTO.IND-CORRIGIDO` | MÉDIO | Nunca reduz valores; idempotente por flag de correção. Ver MYS-011. (Par 3) |
| BR-037 | CPF é validado por Módulo 11 (mesmo algoritmo do BR-003); CPF com todos os dígitos iguais é **inválido**, exceto quando começa com `000` (CPF de teste do governo), que é aceito mesmo sendo `00000000000`. | `01-arqueologia/legado-sisdnit/programs/VALBENEF.NSN#L188-L201` | `BENEFICIARIO.NUM-CPF` (AB) | CRÍTICO | 🔴 Exceção `000` é backdoor de teste não documentado. Ver MYS-017. (Par 4) |
| BR-038 | Data de nascimento é válida quando: ano entre 1900 e o ano atual; mês entre 1 e 12; dia entre 1 e o limite do mês pela tabela `#DIAS-MES`. Fevereiro tem **sempre 29 dias** na tabela (não verifica ano bissexto). | `01-arqueologia/legado-sisdnit/programs/VALBENEF.NSN#L96` `#L242-L260` | `BENEFICIARIO.DT-NASCIMENTO` (AF) | MÉDIO | 🔴 Aceita 29/02 em qualquer ano. Ver MYS-016. (Par 4) |
| BR-039 | Nome é válido apenas se contiver pelo menos um espaço em posição > 1 (exige nome + sobrenome); nome em branco é rejeitado. | `01-arqueologia/legado-sisdnit/programs/VALBENEF.NSN#L262-L273` | `BENEFICIARIO.NOME-COMPLETO` (AC) | MÉDIO | Refina BR-004 com regra de composição do nome. (Par 4) |
| BR-040 | UF, quando preenchida, deve pertencer à tabela das 27 unidades federativas; STATUS do beneficiário só aceita `A`, `S`, `C`, `I` ou `D`. | `01-arqueologia/legado-sisdnit/programs/VALBENEF.NSN#L143-L166` | `BENEFICIARIO.UF` (BG), `BENEFICIARIO.SIT-BENEFICIARIO` (CE) | MÉDIO | Domínio fechado de UF e situação; alinhado ao DDM. (Par 4) |
| BR-041 | RG é válido quando preenchido e com pelo menos 5 caracteres (comprimento medido até o primeiro espaço). | `01-arqueologia/legado-sisdnit/programs/VALDOCS.NSN#L146-L160` | `BENEFICIARIO.RG-NUMERO` (AI) | MÉDIO | Validação mínima de documento. (Par 4) |
| BR-042 | Se o CPF começa com um dos 8 prefixos especiais (`000`,`001`,`002`,`010`,`011`,`099`,`100`,`999`), o documento é marcado como válido e **todos os erros de validação são zerados** (CPF e RG deixam de ser checados). | `01-arqueologia/legado-sisdnit/programs/VALDOCS.NSN#L168-L182` | `BENEFICIARIO.NUM-CPF` (AB), `BENEFICIARIO.DOCUMENTOS-OK` | CRÍTICO | 🔴 **Bypass de segurança.** Prefixos de teste/governo anulam toda a validação. Ver MYS-014. (Par 4) |
| BR-043 | Beneficiário com `COD-REGIAO = 99` (internacional/diplomático) é considerado elegível imediatamente, **ignorando** status, faixa etária, renda, tipo de programa e documentação. | `01-arqueologia/legado-sisdnit/programs/VALELEG.NSN#L105-L111` | `BENEFICIARIO.COD-REGIAO` (BJ) | CRÍTICO | 🔴 **Backdoor de elegibilidade** incluído em 05/04/2013. Ver MYS-015. (Par 4) |
| BR-044 | Elegibilidade combina: status do beneficiário (`A`; `S`/`C`/`D`/`I` reprovam), faixa etária e renda máxima do programa, e regras por tipo — `A` assistencial (renda > 600 só com dependentes; exige `DOCUMENTOS-OK = 'S'`), `P` previdenciário (idade ≥ 60), `T` trabalho (idade 16–65); `COD-ELEG` posição 1 = `R` exige NIS, posição 2 = `D` exige dependentes. | `01-arqueologia/legado-sisdnit/programs/VALELEG.NSN#L116-L234` | `BENEFICIARIO.SIT-BENEFICIARIO` (CE), `BENEFICIARIO.VLR-RENDA-FAMILIAR` (CH), `PROGRAMA-SOCIAL.TIPO`, `PROGRAMA-SOCIAL.IDADE-MIN/MAX`, `PROGRAMA-SOCIAL.RENDA-MAX` | ALTO | Núcleo de elegibilidade; idade por diferença de anos (mesmo padrão de MYS-002). (Par 4) |
| BR-045 | A consulta de beneficiário aceita busca por CPF (`C`) ou NIS (`N`); o padrão é CPF quando o tipo vem em branco. | `01-arqueologia/legado-sisdnit/programs/CONSBENF.NSN#L80-L99` | `BENEFICIARIO.NUM-CPF` (AB), `BENEFICIARIO.NIS` | MÉDIO | Tela online 3270; dois índices de busca para o mesmo cadastro. (Par 5) |
| BR-046 | O CPF é mascarado para exibição no formato `***.***.XXX-XX` (oculta os 6 primeiros dígitos), tanto na consulta quanto no relatório de pagamentos. | `01-arqueologia/legado-sisdnit/programs/CONSBENF.NSN#L176-L191` `RELPGT.NSN#L109-L113` | `BENEFICIARIO.NUM-CPF` (AB) | ALTO | 🔴 Proteção de dado sensível (LGPD); implementação tem falha conhecida (MYS-019). (Par 5) |
| BR-047 | A consulta exibe apenas os **últimos 12** pagamentos do beneficiário no histórico. | `01-arqueologia/legado-sisdnit/programs/CONSBENF.NSN#L150-L160` | `PAGAMENTO.CPF-BENEF`, `PAGAMENTO.COMPETENCIA` | BAIXO | Limite de apresentação da tela; não é regra financeira. (Par 5) |
| BR-048 | O relatório analítico de pagamentos agrupa por programa com **subtotais** (bruto, líquido, quantidade) a cada quebra de `COD-PROGRAMA` e fecha com um **total geral** (bruto, desconto, líquido, abono). | `01-arqueologia/legado-sisdnit/programs/RELPGT.NSN#L92-L99` `#L177-L213` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-LIQUIDO`, `PAGAMENTO.VLR-ABONO`, `PAGAMENTO.COD-PROGRAMA` | MÉDIO | Filtro por período (competência) e por programa (0=todos). (Par 5) |
| BR-049 | A trilha de auditoria classifica eventos por ação: `IN` inclusão, `AL` alteração, `CO` conciliação, `CN` consulta, `DV` divergência, `EX` exclusão; o relatório oferece filtros por período, ação, usuário e tabela. | `01-arqueologia/legado-sisdnit/programs/RELAUDIT.NSN#L137-L161` | `AUDITORIA.ACAO`, `AUDITORIA.USUARIO`, `AUDITORIA.TABELA-REF`, `AUDITORIA.DT-EVENTO` | MÉDIO | Período padrão: 1997-01-01 até hoje quando não informado. (Par 5) |
| BR-050 | Eventos de ação `EX` (exclusão) **nunca** são exibidos na trilha de auditoria — são contados como filtrados e omitidos da listagem. | `01-arqueologia/legado-sisdnit/programs/RELAUDIT.NSN#L103-L108` | `AUDITORIA.ACAO` | CRÍTICO | 🔴 Exclusões somem do relatório de auditoria (incluído em "LIMPEZA RELATORIO" 2014). Ver MYS-018. (Par 5) |

> Adicione mais linhas conforme necessário. Lembre-se: existem **10 regras escondidas** no código!

## Exemplo de linha bem preenchida

| ID     | Regra de Negócio                                                                        | Programa Fonte                                   | Campos DDM                                                               | Nível de Risco | Notas                                      |
| ------ | --------------------------------------------------------------------------------------- | ------------------------------------------------ | ------------------------------------------------------------------------ | -------------- | ------------------------------------------ |
| BR-013 | Desconto total não pode exceder 30% do valor bruto, exceto descontos judiciais (tipo J) | `01-arqueologia/legado-sisdnit/programs/CALCDSCT.NSN#L142-L148` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-TOTAL-DSCT`, `PAGAMENTO.TIPO-DSCT` | CRÍTICO        | Regra financeira. Tipo 'J' = exceção legal |

## Regras por Categoria

### Cálculos Financeiros

- **BR-017** — Ajuste do valor base do programa pelo Fator-K (`1,00 + FATOR-REAJUSTE × 0,347215`). Constante mágica não documentada.
- **BR-008** — Cálculo de idade por diferença de anos (usado para decidir BR-010).
- **BR-021** — (Par 2) Cálculo do benefício mensal: base × 5 fatores, truncado.
- **BR-022 / BR-023 / BR-024** — (Par 2) Fatores familiar, renda e idade.
- **BR-025** — (Par 2) 13º + abono de 15% em dezembro.
- **BR-026 / BR-028** — (Par 2) Desconto de 3% e arredondamento de relatório.

### Validações de Status

- **BR-009** — Status inicial `A` na inclusão do beneficiário.
- **BR-010** — Beneficiário > 75 anos vira status `S` (escondida; conflito semântico com DDM).
- **BR-012** — Beneficiário `C`/`D` não aceita novos dependentes.
- **BR-018** — Programa social entra com status `A`.
- **BR-040** — (Par 4) Domínio fechado de UF (27) e STATUS (`A/S/C/I/D`).
- **BR-044** — (Par 4) Status do beneficiário e tipo de programa decidem elegibilidade.

### Validações de Dados (Par 4)

- **BR-037** — CPF Módulo 11 com exceção de teste para prefixo `000` (backdoor, MYS-017).
- **BR-038** — Data de nascimento; fevereiro sempre com 29 dias (MYS-016).
- **BR-039** — Nome exige nome + sobrenome (espaço em posição > 1).
- **BR-041** — RG com no mínimo 5 caracteres.
- **BR-042** — Prefixos especiais de CPF anulam toda a validação de documentos (MYS-014).
- **BR-043** — Região 99 torna o beneficiário elegível ignorando todas as regras (MYS-015).

### Consulta, Relatórios e Auditoria (Par 5)

- **BR-045** — Consulta de beneficiário por CPF (`C`) ou NIS (`N`); padrão CPF.
- **BR-046** — Máscara de CPF `***.***.XXX-XX` para exibição (LGPD); falha conhecida em MYS-019.
- **BR-047** — Histórico de consulta limitado aos últimos 12 pagamentos.
- **BR-048** — Relatório de pagamentos com subtotais por programa e total geral.
- **BR-049** — Classificação de ações da trilha de auditoria (`IN/AL/CO/CN/DV/EX`).
- **BR-050** — Exclusões (`EX`) nunca aparecem na trilha de auditoria (crítico, MYS-018).

### Regras de Autorização

- **BR-001 / BR-016** — Operações permitidas (`I`/`A` para beneficiário; `I`/`C` para programa). Não existe exclusão.
- **BR-007 / BR-016** — Unicidade por chave (CPF do beneficiário; código do programa).

### Regras de Negócio Temporais

- **BR-008 / BR-010** — Idade (derivada da data de nascimento) determina transição de status.
- _Nenhuma regra de prazo/data-limite foi encontrada nos 3 programas de cadastro do Par 1 (essas tendem a aparecer nos batches do Par 2)._

## Resumo Estatístico

- Total de regras encontradas: **50** (Par 1 · cadastros BR-001–018; Par 2 · batches BR-019–030; Par 3 · cálculos BR-031–036; Par 4 · validações BR-037–044; Par 5 · consulta/relatórios/auditoria BR-045–050)
- Regras críticas: **14** (BR-003, BR-010, BR-017, BR-020, BR-021, BR-025, BR-029, BR-032, BR-037, BR-042, BR-043, BR-050, + financeiras de batch/cálculo)
- Regras com duplicação/conflito legado↔DDM: **3** (BR-006, BR-013, BR-014)
- Regras sem documentação (escondidas/backdoors): **6** (BR-010, BR-017, BR-042, BR-043, BR-037-exceção-000, BR-050)
- Inconsistências de cálculo/arredondamento entre programas: **3** (BR-028 vs BR-021; BR-027 modelo de regiões; BR-034 vs BR-026 descontos)

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

