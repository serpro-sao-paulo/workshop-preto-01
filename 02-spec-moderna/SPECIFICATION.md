<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# SPECIFICATION — SIFAP 2.0

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-00A4EF?style=for-the-badge) ![PAR Par 1 · Visão](https://img.shields.io/badge/PAR-Par%201%20·%20Visão-F25022?style=for-the-badge) ![NOTAÇÃO EARS](https://img.shields.io/badge/NOTAÇÃO-EARS-1A1A1A?style=for-the-badge)

> Especificação funcional do SIFAP 2.0 produzida pelo **Par 1 (Product Owner + Requirements Engineer)**.
> Todo requisito segue notação EARS, tem REQ-ID único e `source_legacy:` rastreável ao legado.

## Metadados

- **Versão da spec:** 0.1.0 (Estágio 2)
- **Time:** Par 1 · Visão
- **Aprovado pelo Product Owner:** ☑ (escopo assinado no H2)
- **Origem dos requisitos:** [`../01-arqueologia/business-rules-catalog.md`](../01-arqueologia/business-rules-catalog.md) (BR-001 a BR-036)
- **Discovery:** [`../01-arqueologia/discovery-report.md`](../01-arqueologia/discovery-report.md)

---

## 1. Escopo e Não-Escopo (Product Owner)

> Regra de corte: **"Afeta o ciclo mensal de pagamento? → v1. Não? → backlog."**

### 1.1 Em escopo (v1)

| Módulo | O que entra | Origem |
| ------ | ----------- | ------ |
| `beneficiary` | Cadastro, validação de CPF/dados, dependentes, máquina de status | BR-001 a BR-015 |
| `program` | Parametrização de programas sociais e valor-base | BR-016 |
| `payment` | Cálculo do benefício mensal, fatores, 13º/abono, descontos, líquido, arredondamento | BR-017 a BR-030 |
| `eligibility` | Validação de elegibilidade por status, idade, renda, tipo e código | BR-031 a BR-035 |
| `audit` | Trilha imutável de transições de status e cálculos | GREENFIELD (cobre gap do legado) |

### 1.2 Fora de escopo (backlog)

- **Correção retroativa por IPCA** (BR-036) — tabela congelada em 2014; reavaliar como serviço parametrizável.
- **Integração SIAFI / remessa CNAB 240 / conciliação batch** — responsabilidade do Par 2; `REQ-INT-*` em outro módulo.
- **Relatórios analíticos e gerenciais** (`REQ-RPT-*`).
- **Bloco "Plano Verão"** (EGG-001) — código morto, descartado.

### 1.3 Decisões que NÃO devem replicar o legado

| Comportamento legado | Decisão do PO | REQ relacionado |
| -------------------- | ------------- | --------------- |
| Backdoor de CPF `000…` e prefixos de teste (MYS-007/010) | **Não migrar**; usar ambiente de teste isolado | REQ-BEN-004 (unwanted) |
| Truncamento de centavos (MYS-005) | **Evoluir** para arredondamento half-up único | REQ-PAY-006 |
| Suspensão silenciosa > 75 anos (MYS-001) | **Manter, mas explícita e auditável** | REQ-BEN-005, REQ-AUD-001 |
| Região 99 pula elegibilidade (MYS-008) | **Substituir** por exceção controlada e auditada | REQ-ELI-005 |

> ⚠️ **Sinalizado para stakeholders:** os itens 1.3 mudam comportamento de 29 anos. Requerem confirmação de negócio antes do Estágio 3.

---

## 2. User Stories (Product Owner + Requirements Engineer)

### US-001: Cadastrar beneficiário com CPF válido

**As a** operador da CGPB
**I want** cadastrar um beneficiário com dados validados
**So that** apenas pessoas com identificação correta entrem na folha de pagamento

**Acceptance criteria**
- Given um CPF com dígitos verificadores corretos, when o operador inclui o beneficiário, then o cadastro é criado com status `ACTIVE`.
- Given um CPF com dígito verificador inválido, when o operador tenta incluir, then o sistema rejeita com erro de validação.
- Given um CPF já cadastrado, when o operador tenta incluir de novo, then o sistema retorna conflito (HTTP 409).

**Traces to**: REQ-BEN-001, REQ-BEN-002, REQ-BEN-003
**Effort**: M
**Dependencies**: —

### US-002: Calcular o benefício mensal de um beneficiário ativo

**As a** sistema de folha de pagamento
**I want** calcular o valor do benefício aplicando todos os fatores
**So that** cada família receba o valor correto no ciclo mensal

**Acceptance criteria**
- Given um beneficiário `ACTIVE` com programa válido, when o ciclo mensal é processado, then o sistema calcula `VLR-BASE × FATOR-REG × FATOR-FAM × FATOR-RND × FATOR-IDADE × (1 + FATOR-REAJ)`.
- Given a competência de dezembro e programa tipo `A`, when o cálculo roda, then o sistema soma 13º e abono natalino de 15%.
- Given um beneficiário não-ativo, when o cálculo é solicitado, then o sistema não gera pagamento.

**Traces to**: REQ-PAY-001, REQ-PAY-002, REQ-PAY-003, REQ-BEN-006
**Effort**: L
**Dependencies**: US-001

### US-003: Aplicar descontos respeitando o teto legal

**As a** sistema de folha de pagamento
**I want** aplicar descontos com teto de 30%, exceto judiciais
**So that** o valor líquido respeite os limites legais

**Acceptance criteria**
- Given descontos não judiciais que somam mais de 30% do bruto, when calculados, then o total é limitado a 30%.
- Given um desconto judicial (tipo `J`), when calculado, then o valor é aplicado integralmente sem teto.
- Given bruto − descontos < 0, when o líquido é calculado, then o líquido é 0.

**Traces to**: REQ-PAY-004, REQ-PAY-005, REQ-PAY-007
**Effort**: M
**Dependencies**: US-002

---

## 3. Requisitos Funcionais (EARS)

### Módulo `beneficiary`

#### REQ-BEN-001 · Validação de CPF por módulo 11

```yaml
REQ-BEN-001:
  pattern: event-driven
  text: "Quando um beneficiário é incluído ou alterado, o SIFAP deve validar o CPF
         pelo algoritmo módulo 11 (dois dígitos verificadores) e rejeitar CPF inválido."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L120-L320
  business_rule: BR-001
  acceptance:
    - "CPF com DV correto é aceito."
    - "CPF com DV incorreto retorna erro de validação."
    - "A mesma validação vale em CADBENEF, VALBENEF e VALDOCS (regra única, não duplicada)."
  priority: P0
  risk: ALTO
```

#### REQ-BEN-002 · Unicidade do CPF

```yaml
REQ-BEN-002:
  pattern: unwanted
  text: "O SIFAP não deve permitir incluir um beneficiário com CPF já cadastrado,
         nem alterar um beneficiário cujo CPF não exista."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L138-L155
  business_rule: BR-004
  acceptance:
    - "Inclusão com CPF existente retorna HTTP 409 Conflict."
    - "Alteração de CPF inexistente retorna HTTP 404 Not Found."
  priority: P0
  risk: ALTO
```

#### REQ-BEN-003 · Campos obrigatórios na inclusão

```yaml
REQ-BEN-003:
  pattern: event-driven
  text: "Quando um beneficiário é incluído, o SIFAP deve exigir CPF, nome,
         data de nascimento e sexo (M ou F), rejeitando a inclusão se algum faltar."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L114-L160
  business_rule: BR-003, BR-009
  acceptance:
    - "Inclusão sem nome é rejeitada."
    - "Sexo diferente de M/F é rejeitado."
    - "Nome sem sobrenome (sem espaço) é rejeitado."
  priority: P1
  risk: MÉDIO
```

#### REQ-BEN-004 · Proibição de CPF de teste em produção

```yaml
REQ-BEN-004:
  pattern: unwanted
  text: "O SIFAP não deve aceitar como válidos CPFs com todos os dígitos iguais
         nem CPFs de prefixos de teste (000, 001, 002, 010, 011, 099, 100, 999)."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L185-L205
  business_rule: BR-011, BR-012
  acceptance:
    - "CPF 00000000000 é rejeitado em produção."
    - "Documento com prefixo de teste é rejeitado em produção."
  priority: P0
  risk: CRÍTICO
  notes: "Decisão do PO (§1.3): backdoor do legado NÃO é migrado; testes usam ambiente isolado."
```

#### REQ-BEN-005 · Suspensão explícita de beneficiários acima de 75 anos

```yaml
REQ-BEN-005:
  pattern: event-driven
  text: "Quando a idade calculada de um beneficiário ultrapassa 75 anos, o SIFAP deve
         alterar o status para SUSPENDED, registrar o motivo e notificar o operador."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L166-L168
  business_rule: BR-006
  acceptance:
    - "Beneficiário que completa 76 anos passa a SUSPENDED."
    - "A transição gera registro de auditoria (REQ-AUD-001)."
    - "A suspensão não é silenciosa: há motivo e notificação."
  priority: P0
  risk: CRÍTICO
  notes: "Evolução do MYS-001: mantém a regra, mas torna explícita e auditável."
```

#### REQ-BEN-006 · Status válidos do beneficiário

```yaml
REQ-BEN-006:
  pattern: ubiquitous
  text: "O SIFAP deve restringir o status do beneficiário ao conjunto
         {ACTIVE, SUSPENDED, CANCELLED, INACTIVE, DISCHARGED}."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L160-L168
  business_rule: BR-005, BR-007
  acceptance:
    - "Beneficiário recém-incluído tem status ACTIVE."
    - "Qualquer status fora do conjunto é rejeitado."
  priority: P1
  risk: MÉDIO
```

#### REQ-BEN-007 · Limite de dependentes

```yaml
REQ-BEN-007:
  pattern: unwanted
  text: "O SIFAP não deve permitir vincular mais de 5 dependentes a um beneficiário,
         nem incluir dependentes em beneficiário CANCELLED ou DISCHARGED."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L40-L65
  business_rule: BR-013, BR-014
  acceptance:
    - "6º dependente é rejeitado."
    - "Inclusão de dependente em beneficiário CANCELLED é rejeitada."
    - "CPF de dependente duplicado no mesmo titular é rejeitado."
  priority: P1
  risk: ALTO
  notes: "MYS-002: limite hardcoded; confirmar com negócio se 5 ainda vale."
```

### Módulo `program`

#### REQ-PRG-001 · Ajuste do valor-base pelo fator-K

```yaml
REQ-PRG-001:
  pattern: event-driven
  text: "Quando um programa social é cadastrado, o SIFAP deve ajustar o valor-base
         multiplicando-o por (1.00 + FATOR-REAJUSTE × 0.347215)."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L86-L88
  business_rule: BR-016
  acceptance:
    - "Programa com reajuste 0 mantém valor-base inalterado."
    - "O fator 0.347215 é parametrizável e documentado (MYS-003)."
  priority: P1
  risk: CRÍTICO
  notes: "MYS-003: origem da constante a confirmar antes do Estágio 3."
```

### Módulo `payment`

#### REQ-PAY-001 · Cálculo do benefício mensal

```yaml
REQ-PAY-001:
  pattern: event-driven
  text: "Quando o benefício mensal é calculado, o SIFAP deve computar
         VLR-BASE × FATOR-REG × FATOR-FAM × FATOR-RND × FATOR-IDADE × (1 + FATOR-REAJ)."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L222-L230
  business_rule: BR-017, BR-018, BR-019, BR-020, BR-021
  acceptance:
    - "Fator regional vem da tabela de 27 UFs (1,00 a 1,40); região fora de 1–25 usa 1,00."
    - "Fator familiar é progressivo por nº de dependentes."
    - "Fator de renda decresce por faixa (≤300→1,00 … acima→0,40)."
    - "Fator idade: ≥65→1,15; ≥60→1,10; <18→1,05; demais→1,00."
  priority: P0
  risk: CRÍTICO
```

#### REQ-PAY-002 · Cálculo só para beneficiário ativo

```yaml
REQ-PAY-002:
  pattern: unwanted
  text: "O SIFAP não deve calcular nem gerar pagamento para beneficiário
         cujo status não seja ACTIVE."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L160-L165
  business_rule: BR-022
  acceptance:
    - "Beneficiário SUSPENDED não gera pagamento."
    - "Beneficiário CANCELLED não gera pagamento."
  priority: P0
  risk: CRÍTICO
```

#### REQ-PAY-003 · Cálculo diferenciado em dezembro

```yaml
REQ-PAY-003:
  pattern: complex
  text: "Enquanto o beneficiário estiver ACTIVE, quando o ciclo de pagamento for gerado
         na competência de dezembro, o SIFAP deve somar o 13º
         (VLR-BASE × FATOR-REG × FATOR-IDADE) e, onde o programa for do tipo ASSISTENCIAL,
         adicionar abono natalino de 15% do benefício."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L242-L258
  business_rule: BR-023, BR-024
  acceptance:
    - "Em dezembro, o bruto inclui benefício + 13º."
    - "Programa tipo A em dezembro recebe +15% de abono; demais tipos não."
    - "Fora de dezembro, não há 13º nem abono."
  priority: P0
  risk: CRÍTICO
  notes: "MYS-004."
```

#### REQ-PAY-004 · Teto de 30% para descontos não judiciais

```yaml
REQ-PAY-004:
  pattern: unwanted
  text: "O SIFAP não deve permitir que o total de descontos NÃO judiciais
         exceda 30% do valor bruto do pagamento."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L100-L168
  business_rule: BR-026
  acceptance:
    - "Bruto R$ 1000 e desconto não judicial R$ 400 → desconto aplicado R$ 300."
    - "Soma de descontos não judiciais é truncada em 30% do bruto."
  priority: P0
  risk: CRÍTICO
```

#### REQ-PAY-005 · Desconto judicial sem teto

```yaml
REQ-PAY-005:
  pattern: event-driven
  text: "Quando um desconto do tipo JUDICIAL é aplicado, o SIFAP deve somar o valor
         integralmente ao total de descontos, sem aplicar o teto de 30%."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L163-L168
  business_rule: BR-026
  acceptance:
    - "Desconto judicial de 80% do bruto é aceito integralmente."
    - "Múltiplos descontos judiciais somam sem limite."
  priority: P0
  risk: CRÍTICO
  notes: "MYS-006."
```

#### REQ-PAY-006 · Arredondamento monetário padronizado

```yaml
REQ-PAY-006:
  pattern: ubiquitous
  text: "O SIFAP deve arredondar todo valor monetário a 2 casas decimais
         usando arredondamento half-up, de forma única em todo o sistema."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L232-L234
  business_rule: BR-030
  acceptance:
    - "R$ 10,125 arredonda para R$ 10,13."
    - "Nenhum módulo usa truncamento (corrige MYS-005 e INC-004)."
  priority: P0
  risk: CRÍTICO
  notes: "Evolução: legado trunca; BATCHREL usava método divergente."
```

#### REQ-PAY-007 · Valor líquido nunca negativo

```yaml
REQ-PAY-007:
  pattern: unwanted
  text: "O SIFAP não deve gerar valor líquido negativo; quando descontos
         excederem o bruto, o líquido deve ser 0."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L262-L268
  business_rule: BR-029
  acceptance:
    - "Bruto R$ 100 e descontos R$ 150 → líquido R$ 0,00."
  priority: P1
  risk: ALTO
```

### Módulo `eligibility`

#### REQ-ELI-001 · Inelegibilidade por status

```yaml
REQ-ELI-001:
  pattern: unwanted
  text: "O SIFAP não deve considerar elegível um beneficiário cujo status
         seja SUSPENDED, CANCELLED, DISCHARGED ou INACTIVE."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L116-L140
  business_rule: BR-031
  acceptance:
    - "Beneficiário SUSPENDED é inelegível com motivo registrado."
    - "Cada motivo de inelegibilidade é retornado ao operador."
  priority: P0
  risk: ALTO
```

#### REQ-ELI-002 · Elegibilidade por tipo de programa

```yaml
REQ-ELI-002:
  pattern: state-driven
  text: "Enquanto o programa for de um tipo definido, o SIFAP deve aplicar:
         ASSISTENCIAL exige documentação OK (renda > 600 só com dependente);
         PREVIDENCIÁRIO exige idade ≥ 60; TRABALHO exige idade entre 16 e 65."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L165-L205
  business_rule: BR-032
  acceptance:
    - "Programa P com idade 59 → inelegível."
    - "Programa T com idade 70 → inelegível."
    - "Programa A com renda 700 e sem dependente → inelegível."
  priority: P0
  risk: ALTO
```

#### REQ-ELI-003 · Faixa etária e renda máxima do programa

```yaml
REQ-ELI-003:
  pattern: state-driven
  text: "Enquanto o programa definir idade mínima/máxima ou renda máxima (> 0),
         o SIFAP deve recusar beneficiários fora da faixa etária ou acima do teto de renda."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L142-L164
  business_rule: BR-033
  acceptance:
    - "Renda familiar acima da renda máxima → inelegível."
    - "Idade fora de [IDADE-MIN, IDADE-MAX] → inelegível."
  priority: P1
  risk: ALTO
```

#### REQ-ELI-004 · Código de elegibilidade específico

```yaml
REQ-ELI-004:
  pattern: event-driven
  text: "Quando o programa tiver código de elegibilidade, o SIFAP deve exigir NIS
         cadastrado se a posição 1 for 'R' e exigir dependentes se a posição 2 for 'D'."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L210-L235
  business_rule: BR-034
  acceptance:
    - "Código 'R____' sem NIS → inelegível."
    - "Código '_D___' sem dependentes → inelegível."
  priority: P2
  risk: MÉDIO
```

#### REQ-ELI-005 · Exceção controlada para casos especiais (ex-região 99)

```yaml
REQ-ELI-005:
  pattern: unwanted
  text: "O SIFAP não deve conceder elegibilidade automática total a nenhum beneficiário;
         casos especiais (ex.: internacional/diplomático) devem passar por um fluxo de
         exceção autorizado e auditado, e não por um bypass."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L105-L110
  business_rule: BR-035
  acceptance:
    - "Caso especial gera registro de auditoria com autorizador."
    - "Não há caminho que pule todas as verificações sem trilha."
  priority: P0
  risk: CRÍTICO
  notes: "Evolução do MYS-008: substitui o bypass da região 99."
```

### Módulo `audit`

#### REQ-AUD-001 · Trilha imutável de transições

```yaml
REQ-AUD-001:
  pattern: event-driven
  text: "Quando o status de um beneficiário ou pagamento é alterado, o SIFAP deve
         gravar um registro de auditoria com estado anterior, estado novo, autor,
         motivo e timestamp UTC, sem permitir edição ou exclusão."
  source_legacy: "[GREENFIELD] cobre gap do legado: módulos de cálculo e cadastro não registram transições; mysteries-found.md MYS-001 e §3.4 do discovery-report."
  business_rule: BR-006, BR-035
  acceptance:
    - "Toda suspensão automática (REQ-BEN-005) gera registro de auditoria."
    - "Registros de auditoria não podem ser alterados nem excluídos."
    - "CPF é mascarado nos logs no formato XXX.XXX.NNN-NN."
  priority: P0
  risk: ALTO
```

---

## 4. Rastreabilidade (Legado → Requisito)

| BR (catálogo) | REQ-ID | Programa-fonte |
| ------------- | ------ | -------------- |
| BR-001 / BR-009 | REQ-BEN-001 / REQ-BEN-003 | CADBENEF.NSN, VALBENEF.NSN |
| BR-004 | REQ-BEN-002 | CADBENEF.NSN |
| BR-011 / BR-012 | REQ-BEN-004 | VALBENEF.NSN, VALDOCS.NSN |
| BR-006 | REQ-BEN-005 / REQ-AUD-001 | CADBENEF.NSN |
| BR-005 / BR-007 | REQ-BEN-006 | VALBENEF.NSN |
| BR-013 / BR-014 / BR-015 | REQ-BEN-007 | CADDEPEND.NSN |
| BR-016 | REQ-PRG-001 | CADPROG.NSN |
| BR-017 a BR-021 | REQ-PAY-001 | CALCBENF.NSN |
| BR-022 | REQ-PAY-002 | CALCBENF.NSN |
| BR-023 / BR-024 | REQ-PAY-003 | CALCBENF.NSN |
| BR-026 | REQ-PAY-004 / REQ-PAY-005 | CALCDSCT.NSN |
| BR-030 | REQ-PAY-006 | CALCBENF.NSN |
| BR-029 | REQ-PAY-007 | CALCBENF.NSN |
| BR-031 a BR-035 | REQ-ELI-001 a REQ-ELI-005 | VALELEG.NSN |
| Gap de auditoria | REQ-AUD-001 | GREENFIELD |

**Total: 22 REQ-IDs** (meta da passagem #2: ≥ 12). Cobertura de BR: 25 das 36 regras viram requisito; as demais (correção IPCA, batch/SIAFI, relatórios) ficam para o Par 2 ou backlog conforme §1.2.

---

## 5. Pendências para sign-off (Product Owner / stakeholders)

1. **Fator-K 0.347215** (REQ-PRG-001 / MYS-003): confirmar origem e se permanece.
2. **Limite de 5 dependentes** (REQ-BEN-007 / MYS-002): confirmar se o teto ainda vale.
3. **Suspensão > 75 anos** (REQ-BEN-005 / MYS-001): confirmar regra e política de notificação.
4. **Arredondamento half-up** (REQ-PAY-006 / MYS-005): confirmar mudança vs. truncamento legado.
5. **Eliminação dos backdoors de teste** (REQ-BEN-004): confirmar estratégia de ambiente isolado.

> Estas pendências estão registradas como premissas até decisão dos stakeholders, conforme o default de emergência do PO ("Pergunta de negócio sem resposta? Documente como premissa e siga").
