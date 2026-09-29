<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# SPECIFICATION — sisdnit 2.0

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIME Par 2 · Arquitetura](https://img.shields.io/badge/TIME-Par%202%20·%20Arquitetura-1A1A1A?style=for-the-badge)

> Spec produzida pelo **Par 2 · Arquitetura** (Requirements Engineer + Software Architect) no Estágio 2.
> Todo requisito rastreia o legado via `source_legacy:` (ou `[GREENFIELD]` justificado).

## Metadados

- **Versão da spec:** 0.1.0 (Estágio 2 — fim)
- **Time:** Par 2 · Arquitetura
- **Aprovado pelo Product Owner:** ☑ Escopo v1 assinado (Par 1 · Visão) — ver §1.1 · pendências de stakeholder sinalizadas
- **Origem dos requisitos:** `01-arqueologia/business-rules-catalog.md` (BR-001..BR-030), `mysteries-found.md` (MYS-001..MYS-009)
- **Bounded contexts:** ver [`bounded-contexts.md`](bounded-contexts.md) · **Arquitetura:** ver [`c4-diagrams.md`](c4-diagrams.md)

---

## 1. Escopo desta spec

Cobre o ciclo mensal de pagamento (foco do Par 2): geração da folha, cálculo do benefício, 13º/abono, descontos, conciliação bancária e auditoria — além dos requisitos de cadastro herdados do Par 1 e dos requisitos greenfield de autenticação. **Fora de escopo deste par:** detalhamento interno de CALCBENF/CALCCORR/CALCDSCT (Par 3), validações estendidas (Par 4) e relatórios remanescentes (Par 5).

## 1.1 Escopo e Não-Escopo — v1 (Product Owner · Par 1)

> Seção de propriedade do **Product Owner**. Define o que entra no sisdnit 2.0 **v1** e o que vai para o **backlog**. Regra de corte aplicada: _"Afeta o ciclo mensal de pagamento dos 2,3 mi de beneficiários? → v1. Não? → backlog."_ Fonte: [`scope-decisions.md`](scope-decisions.md).

### Dentro do escopo (v1)

| Capacidade | Decisão | REQ-IDs | Por que é v1 |
| --- | --- | --- | --- |
| Geração mensal da folha (BATCHPGT) + idempotência por competência | Migrar | REQ-PAY-001, REQ-PAY-002, REQ-PAY-008 | Coração do ciclo mensal; sem isso não há pagamento. |
| Cálculo do benefício (fatores regional/familiar/renda/idade + reajuste, truncamento) | Migrar | REQ-PAY-003, REQ-PAY-004, REQ-PAY-005 | Define o valor pago; paridade financeira obrigatória. |
| 13º e abono de dezembro | Migrar | REQ-PAY-006 | Regra sazonal de alto impacto financeiro. |
| Desconto simplificado (3% acima de R$ 500) | Migrar | REQ-PAY-007 | Compõe o valor líquido da folha. |
| Conciliação bancária CNAB 240 (BB) + transição de status | Migrar | REQ-REC-001, REQ-REC-002, REQ-REC-003 | Fecha o ciclo financeiro pós-banco; obrigatório. |
| Validação de CPF (Módulo 11) | Migrar | REQ-BEN-001 | Integridade do beneficiário; reusada por todos os contextos. |
| Autenticação moderna (OAuth2/OIDC + RBAC) | Evoluir / Greenfield | REQ-ADM-001 | Substitui RACF/3270; gate de segurança para disparar a folha. |
| Política única de arredondamento no relatório | Evoluir | REQ-RPT-002 | Reconciliação TCU; corrige a divergência legada (MYS-007). |

### Não-Escopo da v1 (backlog)

| Item | Decisão | Destino | Justificativa |
| --- | --- | --- | --- |
| Telas verdes 3270 | Descartar | — | Substituídas pela UI web (Next.js); sem uso após migração. |
| Consolidação regional de 5 macrorregiões no relatório | Adiar | Backlog v1.1 | Não bloqueia o pagamento; conflita com 27 fatores (MYS-009) e precisa de decisão de stakeholder. REQ-RPT-001 fica `proposto`, não implementado na v1. |
| Reprocessamento idempotente sob demanda (re-run de competência) | Adiar | Backlog v1.1 (N2) | Operacional; útil mas não bloqueia o 1º ciclo. |
| Painel de divergências de conciliação | Adiar | Backlog v1.1 (N3) | Evolução de UX sobre a auditoria já migrada. |
| Detalhamento de CALCBENF/CALCCORR/CALCDSCT, validações estendidas (Par 4) e relatórios remanescentes (Par 5) | Fora do recorte do Par 2 | Specs dos respectivos pares | Mantém o foco do recorte do ciclo mensal. |

### ⚠️ Itens que exigem decisão de stakeholder antes de virar requisito firme (sinalizados pelo PO)

> Por princípio, o PO **não presume** estas regras de negócio. Cada uma carrega risco financeiro, de segurança ou de compliance e precisa de confirmação do dono de negócio (SENARC/Auditoria) antes do Estágio 3.

| Mistério | Natureza | Posição do PO para a v1 | Decisão pendente |
| --- | --- | --- | --- |
| MYS-001 (idade > 75 → status `S`=SUSPENSO) | Provável bug histórico | Replicar com **alerta de revisão** (REQ-BEN-002), sem excluir o idoso da folha | Confirmar se idoso deve ficar ativo. |
| MYS-005 (Fator-K `0,347215`) | Constante mágica financeira | Preservar bit-a-bit; **não alterar** | Validar origem/autorização SENARC. |
| MYS-007 (relatório arredonda vs cálculo trunca) | Inconsistência regulatória | Política única de truncamento (REQ-RPT-002) | Aprovar a política única (TCU). |
| MYS-014 / MYS-017 / BR-042 (prefixos de CPF que **anulam** validação) | 🔴 **Bypass de segurança** | **Não migrar o backdoor**; validação de CPF sempre aplicada na v1 | Confirmar remoção do bypass de teste. |
| MYS-015 / BR-043 (`COD-REGIAO = 99` ignora elegibilidade) | 🔴 **Backdoor de elegibilidade** | **Não migrar o backdoor**; elegibilidade sempre avaliada | Confirmar remoção. |
| MYS-018 / BR-050 (eventos `EX` somem da auditoria) | 🔴 **Risco de compliance** | v1 **deve registrar e exibir** exclusões na trilha | Confirmar com Auditoria. |
| MYS-019 / BR-046 (máscara de CPF vaza 3 dígitos) | 🔴 **LGPD** | v1 **deve corrigir** a máscara (`***.***.XXX-XX`) | Confirmar com Auditoria a correção. |

> Os quatro itens marcados 🔴 são **backdoors/anomalias que NÃO devem ser portados** para o sisdnit 2.0. A migração fiel ao legado para nesses pontos: a v1 fecha o controle. Esta é uma decisão de segurança/compliance do PO, registrada aqui para rastreabilidade.

## 2. Requisitos (EARS)

### REQ-PAY-001 · Geração mensal apenas de beneficiários ativos

```yaml
REQ-PAY-001:
  pattern: event-driven
  text: "Quando o batch de folha mensal é iniciado no 1º dia útil,
         o sisdnit deve gerar um registro de pagamento somente para
         beneficiários com status 'A' (ativo)."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L195
  business_rule: BR-019
  acceptance:
    - "100 beneficiários ativos + 30 inativos → 100 pagamentos gerados."
    - "Beneficiário com status 'S' (suspenso) não entra na folha."
  priority: P0
  risk: ALTO
```

### REQ-PAY-002 · Idempotência por competência

```yaml
REQ-PAY-002:
  pattern: unwanted
  text: "O sisdnit não deve gerar um segundo pagamento para o mesmo CPF
         dentro da mesma competência (ano-mês de referência)."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L201-L209
  business_rule: BR-020
  acceptance:
    - "Reexecução do batch na mesma competência não cria pagamentos duplicados."
    - "Dado pagamento já existente do CPF em 2026-05, o beneficiário é ignorado."
  priority: P0
  risk: CRÍTICO
```

### REQ-PAY-003 · Cálculo do valor bruto do benefício

```yaml
REQ-PAY-003:
  pattern: ubiquitous
  text: "O sisdnit deve calcular o valor bruto como
         VLR-BASE × Fator-Regional × Fator-Familiar × Fator-Renda × Fator-Idade,
         depois multiplicado por (1 + Fator-Reajuste), truncado em 2 casas decimais."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L280-L287
  business_rule: BR-021
  acceptance:
    - "Dados fatores conhecidos, o resultado é truncado (não arredondado) em 2 decimais."
    - "Ordem das multiplicações preserva o resultado do legado bit a bit."
  priority: P0
  risk: CRÍTICO
  notes: "Trunca, não arredonda — divergência com relatório documentada em MYS-007."
```

### REQ-PAY-004 · Fator familiar por faixa de dependentes

```yaml
REQ-PAY-004:
  pattern: ubiquitous
  text: "O sisdnit deve aplicar o Fator-Familiar por faixa:
         0 dep = 1,00; 1–2 dep = 1,00 + (dep × 0,05);
         3–4 dep = 1,10 + ((dep−2) × 0,03); 5+ dep = 1,16 + ((dep−4) × 0,02)."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L244-L260
  business_rule: BR-022
  acceptance:
    - "2 dependentes → fator 1,10."
    - "5 dependentes → fator 1,18."
  priority: P1
  risk: ALTO
```

### REQ-PAY-005 · Fator idade

```yaml
REQ-PAY-005:
  pattern: ubiquitous
  text: "O sisdnit deve aplicar o Fator-Idade: idade ≥ 65 = 1,15;
         ≥ 60 = 1,10; < 18 = 1,05; demais idades = 1,00."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L265-L278
  business_rule: BR-024
  acceptance:
    - "Beneficiário de 70 anos → fator 1,15."
    - "Beneficiário de 30 anos → fator 1,00."
  priority: P1
  risk: ALTO
```

### REQ-PAY-006 · 13º salário e abono de dezembro

```yaml
REQ-PAY-006:
  pattern: event-driven
  text: "Quando a competência processada é o mês 12 (dezembro), o sisdnit deve
         gerar o 13º (VLR-BASE × Fator-Regional × Fator-Idade) e, para programas
         tipo 'A' (assistencial), adicionar abono de 15% do benefício mensal."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L292-L307
  business_rule: BR-025
  acceptance:
    - "Em dezembro, beneficiário de programa 'A' recebe benefício + 13º + abono 15%."
    - "Em dezembro, programa não-'A' recebe benefício + 13º, sem abono."
    - "Em meses 1–11, nenhum 13º/abono é gerado."
  priority: P0
  risk: CRÍTICO
```

### REQ-PAY-007 · Desconto simplificado acima de R$ 500

```yaml
REQ-PAY-007:
  pattern: event-driven
  text: "Quando o valor bruto do pagamento excede R$ 500,00, o sisdnit deve
         aplicar desconto de 3% sobre o valor bruto."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L308-L312
  business_rule: BR-026
  acceptance:
    - "Bruto R$ 600 → desconto R$ 18,00."
    - "Bruto R$ 500,00 → nenhum desconto (limite exclusivo)."
  priority: P1
  risk: MÉDIO
```

### REQ-PAY-008 · Preservação da ordenação por CPF

```yaml
REQ-PAY-008:
  pattern: ubiquitous
  text: "O sisdnit deve processar e persistir a folha mensal ordenada por CPF,
         preservando a ordem do legado até que os consumidores downstream
         sejam mapeados."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/BATCHPGT.NSN#L177-L179
  business_rule: BR-019
  acceptance:
    - "O arquivo/resultado da folha sai ordenado ascendentemente por CPF."
  priority: P1
  risk: ALTO
  notes: "Acoplamento oculto — ver MYS-006. Não alterar ordenação sem mapear consumidores."
```

### REQ-REC-001 · Processamento de retorno bancário CNAB 240

```yaml
REQ-REC-001:
  pattern: event-driven
  text: "Quando um arquivo de retorno CNAB 240 do Banco do Brasil é recebido,
         o sisdnit deve processar apenas os registros de detalhe (tipo '3') e
         converter o valor de centavos para reais."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/BATCHCON.NSN#L116-L132
  business_rule: BR-029
  acceptance:
    - "Registros tipo '1','5','9' (header/trailer) são ignorados."
    - "Valor 0000150000 em centavos vira R$ 1.500,00."
  priority: P0
  risk: CRÍTICO
```

### REQ-REC-002 · Transição de status por código de retorno

```yaml
REQ-REC-002:
  pattern: event-driven
  text: "Quando um registro de retorno é conciliado, o sisdnit deve atualizar o
         status do pagamento conforme o código de retorno: '00' → pago (P);
         '01' → devolvido (D); '02' → estornado (E)."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/BATCHCON.NSN#L172-L196
  business_rule: BR-029
  acceptance:
    - "Código '00' marca pagamento como P e grava data de pagamento."
    - "Código '01' marca pagamento como D."
    - "Código '02' marca pagamento como E."
  priority: P0
  risk: CRÍTICO
```

### REQ-REC-003 · Divergência de valor gera auditoria

```yaml
REQ-REC-003:
  pattern: unwanted
  text: "O sisdnit não deve atualizar o status de um pagamento cuja diferença
         de valor entre o sisdnit e o retorno bancário seja superior a R$ 0,01;
         em vez disso, deve gerar um registro de auditoria."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/BATCHCON.NSN#L160-L168
  business_rule: BR-030
  acceptance:
    - "Diferença de R$ 0,02 → registro de auditoria criado, status inalterado."
    - "Diferença de R$ 0,01 → conciliação prossegue normalmente."
  priority: P0
  risk: ALTO
```

### REQ-RPT-001 · Consolidação regional do relatório mensal

```yaml
REQ-RPT-001:
  pattern: ubiquitous
  text: "O sisdnit deve consolidar o relatório mensal mapeando COD-REGIAO em 5
         macrorregiões por faixa (1–5 Norte, 6–10 Nordeste, 11–15 Sudeste,
         16–20 Sul, demais Centro-Oeste)."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/BATCHREL.NSN#L117-L133
  business_rule: BR-027
  acceptance:
    - "Beneficiário com COD-REGIAO 7 é consolidado em 'Nordeste'."
    - "Beneficiário com COD-REGIAO 22 é consolidado em 'Centro-Oeste'."
  priority: P2
  risk: MÉDIO
  notes: "Modelo de 5 regiões conflita com 27 fatores do cálculo — ver MYS-009."
```

### REQ-RPT-002 · Política única de arredondamento

```yaml
REQ-RPT-002:
  pattern: complex
  text: "Se o relatório consolidar valores monetários, então o sisdnit deve usar
         a mesma política de arredondamento do cálculo de pagamento (truncamento
         em 2 casas), de modo que os totais do relatório reconciliem com a soma
         dos pagamentos."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/BATCHREL.NSN#L136-L139
  business_rule: BR-028
  acceptance:
    - "Soma dos pagamentos = total exibido no relatório, sem divergência de centavos."
  priority: P1
  risk: ALTO
  notes: "Corrige a inconsistência legada (round vs truncate) descrita em MYS-007."
```

### REQ-BEN-001 · Validação de CPF (Módulo 11)

```yaml
REQ-BEN-001:
  pattern: unwanted
  text: "O sisdnit não deve aceitar o cadastro de um beneficiário cujo CPF
         seja inválido pelo algoritmo Módulo 11."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN (subrotina VALIDA-CPF)
  business_rule: BR-003
  acceptance:
    - "CPF com dígitos verificadores incorretos é rejeitado."
    - "CPF válido pelo Módulo 11 é aceito."
  priority: P0
  risk: CRÍTICO
```

### REQ-BEN-002 · Idoso acima de 75 anos (mistério MYS-001)

```yaml
REQ-BEN-002:
  pattern: state-driven
  text: "Enquanto a regra de negócio MYS-001 não for esclarecida pelo PO,
         o sisdnit deve replicar o comportamento legado (idade > 75 → status 'S')
         emitindo um alerta de revisão, sem excluir o beneficiário da folha."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN#L166-L169
  business_rule: BR-010
  acceptance:
    - "Beneficiário com 76 anos recebe status 'S' e gera alerta de revisão."
  priority: P1
  risk: CRÍTICO
  notes: "Comportamento suspeito (idoso vira SUSPENSO). Aguardando decisão do PO."
```

### REQ-BEN-003 · Operação de cadastro restrita a inclusão ou alteração

```yaml
REQ-BEN-003:
  pattern: unwanted
  text: "O sisdnit não deve aceitar uma operação de cadastro de beneficiário
         cujo código de operação seja diferente de 'I' (inclusão) ou
         'A' (alteração)."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN#L99-L103
  business_rule: BR-001
  acceptance:
    - "Operação 'I' é aceita; operação 'A' é aceita."
    - "Operação 'X' é rejeitada com mensagem de operação inválida."
    - "Não existe operação de exclusão no cadastro."
  priority: P1
  risk: MÉDIO
```

### REQ-BEN-004 · CPF obrigatório no cadastro

```yaml
REQ-BEN-004:
  pattern: unwanted
  text: "O sisdnit não deve aceitar o cadastro de um beneficiário cujo CPF
         seja zero ou ausente."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN#L105-L109
  business_rule: BR-002
  acceptance:
    - "Cadastro com CPF zero é rejeitado."
    - "CPF é a chave de negócio e não pode ser nulo."
  priority: P1
  risk: ALTO
```

### REQ-BEN-005 · Nome obrigatório

```yaml
REQ-BEN-005:
  pattern: unwanted
  text: "O sisdnit não deve aceitar o cadastro de um beneficiário cujo nome
         esteja em branco."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN#L119-L123
  business_rule: BR-004
  acceptance:
    - "Cadastro com nome em branco é rejeitado."
  priority: P2
  risk: MÉDIO
  notes: "Par 4 refina a composição do nome (BR-039: exige nome + sobrenome)."
```

### REQ-BEN-006 · Data de nascimento obrigatória

```yaml
REQ-BEN-006:
  pattern: unwanted
  text: "O sisdnit não deve aceitar o cadastro de um beneficiário cuja data
         de nascimento seja zero ou ausente."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN#L125-L129
  business_rule: BR-005
  acceptance:
    - "Cadastro sem data de nascimento é rejeitado."
    - "A data de nascimento é necessária para o cálculo de idade (REQ-BEN-009)."
  priority: P2
  risk: MÉDIO
```

### REQ-BEN-007 · Sexo restrito a M ou F

```yaml
REQ-BEN-007:
  pattern: unwanted
  text: "O sisdnit não deve aceitar o cadastro de um beneficiário cujo sexo
         seja diferente de 'M' (masculino) ou 'F' (feminino)."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN#L131-L135
  business_rule: BR-006
  acceptance:
    - "Sexo 'M' é aceito; sexo 'F' é aceito."
    - "Sexo 'I' é rejeitado pelo programa."
  priority: P2
  risk: MÉDIO
  notes: "⚠️ AMBIGUIDADE (MYS-004): o DDM BENEFICIARIO.SEXO permite 'M/F/I' (I=indefinido), mas o programa rejeita 'I'. Decisão do PO necessária: migrar a restrição do código (M/F) ou a do DDM (M/F/I)? Pode rejeitar dados gravados por sistemas externos."
```

### REQ-BEN-008 · Unicidade de CPF por operação

```yaml
REQ-BEN-008:
  pattern: complex
  text: "Quando a operação é 'I' (inclusão), o sisdnit não deve aceitar um CPF
         já cadastrado; quando a operação é 'A' (alteração), o sisdnit não deve
         aceitar um CPF inexistente."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN#L139-L151
  business_rule: BR-007
  acceptance:
    - "Inclusão de CPF já existente é rejeitada ('beneficiário já cadastrado')."
    - "Alteração de CPF inexistente é rejeitada ('beneficiário não encontrado')."
  priority: P1
  risk: ALTO
```

### REQ-BEN-009 · Cálculo de idade por diferença de anos

```yaml
REQ-BEN-009:
  pattern: ubiquitous
  text: "O sisdnit deve calcular a idade do beneficiário como
         (ano atual − ano de nascimento), considerando apenas o ano."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN#L155-L159
  business_rule: BR-008
  acceptance:
    - "Beneficiário nascido em 1951, no ano de 2026, tem idade 75 calculada."
  priority: P2
  risk: MÉDIO
  notes: "⚠️ AMBIGUIDADE (MYS-002): o cálculo ignora mês/dia, então o beneficiário 'envelhece' em 1º de janeiro. Combinado com REQ-BEN-002 (idade > 75 → 'S'), pode antecipar a suspensão em até 11 meses. Decisão do PO: preservar o cálculo legado ou usar idade exata por data?"
```

### REQ-BEN-010 · Status inicial ativo na inclusão

```yaml
REQ-BEN-010:
  pattern: event-driven
  text: "Quando um beneficiário é incluído (operação 'I'), o sisdnit deve
         atribuir o status inicial 'A' (ativo)."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN#L161-L164
  business_rule: BR-009
  acceptance:
    - "Beneficiário recém-incluído tem status 'A'."
  priority: P2
  risk: MÉDIO
  notes: "A regra de idade > 75 (REQ-BEN-002) sobrescreve este status — ordem preservada do legado."
```

### REQ-BEN-011 · Imutabilidade de campos identitários na alteração

```yaml
REQ-BEN-011:
  pattern: event-driven
  text: "Quando um beneficiário é alterado (operação 'A'), o sisdnit não deve
         atualizar CPF, data de nascimento, sexo nem a data de cadastro,
         atualizando apenas dados cadastrais (nome, endereço, contato,
         renda e dependentes)."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADBENEF.NSN#L199-L213
  business_rule: BR-011
  acceptance:
    - "Alteração não modifica o CPF nem a data de nascimento gravados."
    - "Alteração atualiza nome, endereço e renda familiar."
  priority: P2
  risk: MÉDIO
```

### REQ-DEP-001 · Bloqueio de dependentes para beneficiário inativo

```yaml
REQ-DEP-001:
  pattern: unwanted
  text: "O sisdnit não deve permitir a inclusão de dependentes em um
         beneficiário com status 'C' (cancelado) ou 'D' (desligado)."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADDEPEND.NSN#L56-L60
  business_rule: BR-012
  acceptance:
    - "Inclusão de dependente em beneficiário 'C' é rejeitada."
    - "Inclusão de dependente em beneficiário 'A' é permitida."
  priority: P1
  risk: ALTO
```

### REQ-DEP-002 · Limite de dependentes por beneficiário

```yaml
REQ-DEP-002:
  pattern: unwanted
  text: "O sisdnit não deve permitir o cadastro de mais de 5 dependentes
         para o mesmo beneficiário."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADDEPEND.NSN#L63-L66
  business_rule: BR-013
  acceptance:
    - "Cadastro do 6º dependente é rejeitado."
  priority: P1
  risk: ALTO
  notes: "⚠️ AMBIGUIDADE (MYS-003): o grupo periódico GRP-DEPENDENTE no DDM aceita até 10 ocorrências, mas o programa limita a 5. Pode haver beneficiários com 6–10 dependentes gravados por outra via. Decisão do PO: migrar limite 5 (código) ou 10 (DDM)?"
```

### REQ-DEP-003 · Parentesco restrito a domínio fechado

```yaml
REQ-DEP-003:
  pattern: unwanted
  text: "O sisdnit não deve aceitar um dependente cujo parentesco seja
         diferente de 'FI' (filho), 'CO' (cônjuge), 'IR' (irmão) ou
         'OU' (outro)."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADDEPEND.NSN#L84-L88
  business_rule: BR-014
  acceptance:
    - "Parentesco 'FI' é aceito."
    - "Parentesco 'XX' é rejeitado."
  priority: P2
  risk: MÉDIO
  notes: "⚠️ AMBIGUIDADE (MYS-004): o DDM define 'FI/CJ/NT/TU', o programa usa 'FI/CO/IR/OU'. Códigos divergentes. Decisão do PO sobre o domínio canônico antes da migração."
```

### REQ-DEP-004 · Unicidade de CPF de dependente por titular

```yaml
REQ-DEP-004:
  pattern: unwanted
  text: "O sisdnit não deve permitir dois dependentes com o mesmo CPF para o
         mesmo titular, exceto quando o CPF do dependente é zero (não informado)."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADDEPEND.NSN#L97-L100
  business_rule: BR-015
  acceptance:
    - "Segundo dependente com CPF já usado pelo titular é rejeitado."
    - "Dois dependentes com CPF zero são permitidos."
  priority: P2
  risk: MÉDIO
```

### REQ-PRG-001 · Operação de cadastro de programa e unicidade do código

```yaml
REQ-PRG-001:
  pattern: complex
  text: "O sisdnit não deve aceitar uma operação de cadastro de programa social
         diferente de 'I' (inclusão) ou 'C' (consulta); quando a operação é 'I',
         não deve aceitar um código de programa já existente."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADPROG.NSN#L51-L82
  business_rule: BR-016
  acceptance:
    - "Operação 'A' (alteração) é rejeitada — não há alteração de programa."
    - "Inclusão de código de programa já existente é rejeitada."
  priority: P2
  risk: MÉDIO
```

### REQ-PRG-002 · Ajuste do valor base pelo Fator-K

```yaml
REQ-PRG-002:
  pattern: event-driven
  text: "Quando um programa social é incluído, o sisdnit deve calcular
         Fator-K = 1,00 + (FATOR-REAJUSTE × 0,347215) e armazenar
         o valor base como VLR-BASE informado × Fator-K."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADPROG.NSN#L87-L88
  business_rule: BR-017
  acceptance:
    - "Com FATOR-REAJUSTE conhecido, o VLR-BASE gravado reproduz o legado bit a bit."
  priority: P1
  risk: CRÍTICO
  notes: "⚠️ AMBIGUIDADE (MYS-005): a constante mágica 0,347215 não tem origem documentada; o DDM avisa 'NÃO ALTERAR SEM AUTORIZAÇÃO (SENARC)'. Migrar bit a bit e escalar a origem ao PO/SENARC antes de qualquer alteração."
```

### REQ-PRG-003 · Status inicial ativo do programa

```yaml
REQ-PRG-003:
  pattern: event-driven
  text: "Quando um programa social é incluído, o sisdnit deve atribuir o
         status inicial 'A' (ativo)."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/CADPROG.NSN#L97
  business_rule: BR-018
  acceptance:
    - "Programa recém-incluído tem status 'A'."
  priority: P2
  risk: BAIXO
```

### REQ-ADM-001 · Autenticação moderna (greenfield)

```yaml
REQ-ADM-001:
  pattern: event-driven
  text: "Quando um usuário tenta acessar o sisdnit 2.0, o sistema deve
         autenticá-lo via provedor de identidade OAuth2/OIDC e autorizar
         operações conforme seu perfil (RBAC)."
  source_legacy: "[GREENFIELD] Legado usa RACF/3270 sem perfis granulares; segurança e auditoria modernas exigem IdP. Ver scope-decisions N1."
  business_rule: N/A
  acceptance:
    - "Usuário sem perfil de operador não consegue disparar a folha mensal."
    - "Token expirado é rejeitado com 401."
  priority: P1
  risk: ALTO
```

---

## 3. Rastreabilidade e cobertura

| Contexto      | REQ-IDs                                   | Cobertura de BR                      |
| ------------- | ----------------------------------------- | ------------------------------------ |
| Pagamentos    | REQ-PAY-001..008                          | BR-019..026                          |
| Conciliação   | REQ-REC-001..003                          | BR-029, BR-030                       |
| Relatórios    | REQ-RPT-001, REQ-RPT-002                  | BR-027, BR-028                       |
| Beneficiários | REQ-BEN-001..011                          | BR-001..011 (cadastro CADBENEF)      |
| Dependentes   | REQ-DEP-001..004                          | BR-012..015 (CADDEPEND)              |
| Programas     | REQ-PRG-001..003                          | BR-016..018 (CADPROG)               |
| Admin         | REQ-ADM-001                               | [GREENFIELD]                         |

- **Total:** 31 REQ-IDs (≥ 12 exigidos).
- **Com `source_legacy` ou `[GREENFIELD]` justificado:** 31/31 (100%).
- **Padrões EARS usados:** ubiquitous, event-driven, state-driven, unwanted, complex (5 dos 6).
- **Domínio de cadastro (BR-001..018)** formalizado pela RE (Par 1 · Visão) a partir dos programas que o par leu no Estágio 1: `CADBENEF.NSN`, `CADDEPEND.NSN`, `CADPROG.NSN`.
- **Ambiguidades sinalizadas ao PO** (notes nos REQ-IDs): REQ-BEN-007 (MYS-004 sexo), REQ-BEN-009 (MYS-002 idade), REQ-DEP-002 (MYS-003 limite), REQ-DEP-003 (MYS-004 parentesco), REQ-PRG-002 (MYS-005 Fator-K).

## 4. Definição de Pronto (@architect)

- [x] ≥ 12 EARS com REQ-IDs (31 entregues)
- [x] 100% com `source_legacy:` ou `[GREENFIELD]` justificado
- [x] C4 L1/L2/L3 ([`c4-diagrams.md`](c4-diagrams.md))
- [x] ADRs (monólito modular + persistência + autenticação + integração/coexistência) em [`adr/`](adr/)
- [x] Escopo assinado pelo PO (Passagem #2) — §1.1 Escopo e Não-Escopo (v1) + pendências de stakeholder sinalizadas
