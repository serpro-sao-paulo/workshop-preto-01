# Relatório de Lacunas de Cobertura — SIFAP 2.0

**QA responsável:** Par 4 — QA Engineer  
**Data:** 2026-06-10  
**Escopo:** Todos os REQ-IDs de `02-spec-moderna/SPECIFICATION.md`

---

## Resumo

- Requisitos no escopo: **20**
- ✅ OK: **10** — happy path + limite + negativo
- 🟡 WEAK: **6** — apenas happy path ou apenas negativo
- 🔴 MISSING: **4** — nenhum teste encontrado

---

## Lacunas por risco

| REQ-ID | Padrão EARS | Status | Risco (P×I) | Receita de teste |
|--------|------------|--------|-------------|-----------------|
| REQ-BEN-007 | Unwanted | 🔴 MISSING | **9 (3×3)** | Testar: 6º dependente rejeitado; dependente em beneficiário CANCELLED rejeitado; CPF de dependente duplicado rejeitado. |
| REQ-AUD-001 | Event | 🔴 MISSING | **9 (3×3)** | IT Testcontainers: salvar beneficiário → verificar linha em `aud_audit_event`; tentar UPDATE na tabela → esperar exceção do trigger; verificar que DELETE também é bloqueado. |
| REQ-ELI-005 | Event | 🔴 MISSING | **9 (3×3)** | Testar: região 99 gera exceção auditada em vez de pular elegibilidade silenciosamente (MYS-008 fix). |
| REQ-PRG-001 | Event | 🔴 MISSING | **6 (2×3)** | Testar: programa com reajuste 0 mantém vlr_base; fator 0.347215 parametrizável aplicado corretamente. |
| REQ-BEN-005 | Event | 🟡 WEAK | **9 (3×3)** | Existe 1 teste em `BeneficiaryServiceTest`. Faltam: (a) exatamente 75 anos → ainda ACTIVE, (b) 76 anos → SUSPENDED, (c) já SUSPENDED → não regride para ACTIVE, (d) evento de auditoria publicado. |
| REQ-PAY-004 | Event | 🟡 WEAK | **6 (2×3)** | Existe 1 teste de cap. Faltam: (a) desconto judicial acima de 30% **não** é limitado, (b) múltiplos descontos mistos (judicial + contrib) com cap parcial. |
| REQ-BEN-003 | Event | 🟡 WEAK | **4 (2×2)** | Existe validação de CPF. Falta: (a) nome sem sobrenome rejeitado, (b) sexo inválido (`X`) rejeitado via Bean Validation, (c) data de nascimento futura rejeitada. |
| REQ-PAY-006 | State | 🟡 WEAK | **6 (3×2)** | Existe teste HALF_UP vs truncamento em `MoneyRoundingTest`. Falta: teste de integração que verifica que `vlr_liquido` salvo no DB é arredondado corretamente (não truncado pelo ORM). |
| REQ-PAY-002 | Event | 🟡 WEAK | **4 (2×2)** | Fator dezembro existe em `BenefitCalculatorTest`. Falta: (a) mês 11 (novembro) **não** gera abono, (b) programa tipo `B` em dezembro **não** gera 13º. |
| REQ-PAY-005 | Constraint | 🟡 WEAK | **6 (3×2)** | Existe teste cap 30%. Falta: bruto − descontos < 0 → líquido == 0 (not negative). |

---

## REQ-IDs com cobertura OK

| REQ-ID | Testes existentes |
|--------|------------------|
| REQ-BEN-001 | `CpfValidatorTest` — 8 casos incluindo all-same-digits e prefixo de teste |
| REQ-BEN-002 | `BeneficiaryServiceTest.create_duplicateCpf_throws409` |
| REQ-BEN-004 | `CpfValidatorTest.reject_testPrefixes` — backdoor bloqueado |
| REQ-BEN-006 | `BeneficiaryServiceTest.changeStatus_*` — estados válidos testados |
| REQ-PAY-001 | `BenefitCalculatorTest` — 20+ parametrized cases, todos os fatores |
| REQ-PAY-003 | `BenefitCalculatorTest.calculate_withReajuste_*` |
| REQ-PAY-007 | `DiscountCalculatorTest.apply_discountExceedsCap_*` |
| REQ-ELI-001 | `EligibilityCheckerTest` — status ACTIVE/SUSPENDED/CANCELLED |
| REQ-ELI-002 | `EligibilityCheckerTest.check_incomeExceedsThreshold_*` |
| REQ-ELI-004 | `EligibilityCheckerTest.check_programInactive_*` |

---

## Mapeamento REQ-ID → Testes existentes

```
REQ-BEN-001  CpfValidatorTest (8 casos)
REQ-BEN-002  BeneficiaryServiceTest (2 casos)
REQ-BEN-003  BeneficiaryServiceTest (1 caso — WEAK)
REQ-BEN-004  CpfValidatorTest (3 casos)
REQ-BEN-005  BeneficiaryServiceTest (1 caso — WEAK)
REQ-BEN-006  BeneficiaryServiceTest (3 casos)
REQ-BEN-007  ← MISSING
REQ-PRG-001  ← MISSING
REQ-PAY-001  BenefitCalculatorTest (20+ casos)
REQ-PAY-002  BenefitCalculatorTest (1 caso — WEAK)
REQ-PAY-003  BenefitCalculatorTest (2 casos)
REQ-PAY-004  DiscountCalculatorTest (1 caso — WEAK)
REQ-PAY-005  DiscountCalculatorTest (1 caso — WEAK)
REQ-PAY-006  MoneyRoundingTest (2 casos — WEAK, falta IT)
REQ-PAY-007  DiscountCalculatorTest (3 casos)
REQ-ELI-001  EligibilityCheckerTest (3 casos)
REQ-ELI-002  EligibilityCheckerTest (2 casos)
REQ-ELI-003  EligibilityCheckerTest (1 caso — WEAK)
REQ-ELI-004  EligibilityCheckerTest (2 casos)
REQ-ELI-005  ← MISSING
REQ-AUD-001  ← MISSING (nenhum IT de imutabilidade)
```

---

## Plano de ação priorizdo

| Prioridade | Ação | Arquivo | Complexidade |
|-----------|------|---------|-------------|
| P0-1 | IT de imutabilidade do audit | `AuditImmutabilityIT.java` (novo) | Alta |
| P0-2 | Testes limites REQ-BEN-005 (age suspension) | `BeneficiaryServiceTest.java` (expandir) | Baixa |
| P0-3 | Testes REQ-ELI-005 (região 99 auditada) | `EligibilityCheckerTest.java` (expandir) | Baixa |
| P1-1 | Testes REQ-BEN-007 (limite dependentes) | `BeneficiaryServiceTest.java` (expandir) | Média |
| P1-2 | IT criação beneficiário via REST | `BeneficiaryControllerIT.java` (novo) | Alta |
| P1-3 | IT cálculo de pagamento com DB real | `PaymentCalculationIT.java` (novo) | Alta |
| P2-1 | Testes REQ-PAY-005 (líquido não negativo) | `DiscountCalculatorTest.java` (expandir) | Baixa |
| P2-2 | Testes REQ-PAY-002 (dezembro/novembro) | `BenefitCalculatorTest.java` (expandir) | Baixa |
| P2-3 | Testes REQ-PRG-001 | `SocialProgramTest.java` (novo) | Baixa |
