# Relatório de Auditoria de Consultas — SIFAP 2.0

**DBA responsável:** Par 4 — DBA  
**Data:** 2026-06-10  
**Escopo:** Spring Data JPA + JPQL — contextos `beneficiary`, `payment`, `audit`  
**Versão do schema:** V1 + V2 + V3 (seed) + V4 (índices) + V5 (segurança)  
**Base PostgreSQL:** 16

---

## Resumo executivo

| Severidade | Quantidade |
|------------|-----------|
| 🔴 Crítica | 0 |
| 🟠 Alta    | 3 |
| 🟡 Média   | 4 |
| 🔵 Baixa   | 2 |

Nenhuma vulnerabilidade de **SQL injection** foi encontrada — todas as queries usam parâmetros vinculados via Spring Data JPA. O risco principal é **N+1** em consultas batch e **Seq Scan** na tabela `pay_payment` (180M+ linhas) sem os índices corretos (resolvido em V4).

---

## Auditoria de Consulta — 001: BeneficiaryRepository.findByCpf

### Veredito
✅ **Passa** — com observação de N+1 quando acessado em loop.

### Achados

| # | Severidade | Achado | Evidência |
|---|-----------|--------|-----------|
| 1 | 🟡 Média | `findByCpf` retorna a entidade completa incluindo relacionamentos LAZY (`dependents`, `discounts`). Quando chamado em loop (ex: validação em lote de CPFs) gera N+1 — cada acesso a `beneficiary.getDependents()` dispara uma query adicional. | `BeneficiaryRepository.java#L14` + `Beneficiary.java` — `@OneToMany` com `FetchType.LAZY` |
| 2 | 🔵 Baixa | `existsByCpf` emite `SELECT id FROM ben_beneficiary WHERE cpf = ?` com `LIMIT 1` — correto. Porém é invocado logo antes do `save` no service criando dois roundtrips. Poderia usar `INSERT ... ON CONFLICT` para reduzir para um. | `BeneficiaryService.java#L47-L48` |

### Plano EXPLAIN esperado (pós V4)

```sql
EXPLAIN (ANALYZE, BUFFERS) SELECT * FROM ben_beneficiary WHERE cpf = '12345678901';
```

```
Index Scan using ben_beneficiary_cpf_uk on ben_beneficiary
  (cost=0.43..8.45 rows=1 width=180)
  (actual time=0.05..0.06 rows=1 loops=1)
  Index Cond: (cpf = '12345678901')
Buffers: shared hit=3
Planning Time: 0.1 ms
Execution Time: 0.1 ms
```

Índice `ben_beneficiary_cpf_uk` (criado no V1 via UNIQUE constraint) garante Index Scan em 4,2M linhas.

### Correção recomendada (N+1)

Adicionar `@EntityGraph` para quando for necessário carregar dependents/discounts:

```java
// BeneficiaryRepository.java
@EntityGraph(attributePaths = {"dependents", "discounts"})
Optional<Beneficiary> findWithDetailsByCpf(String cpf);
```

Usar `findByCpf` (sem graph) quando apenas dados do beneficiário são necessários. Usar `findWithDetailsByCpf` quando o cálculo de desconto requer ambas as coleções.

---

## Auditoria de Consulta — 002: Ciclo Batch Mensal (pay_payment)

### Veredito
🟠 **Correção obrigatória** — sem os índices de V4, a query de geração de pagamentos faria Seq Scan em 180M+ linhas.

### Contexto

O batch noturno processa ~3,8M pagamentos/mês. A query implícita pelo Spring Batch seria:

```sql
-- Query implícita do batch (equivalente JPA)
SELECT p FROM Payment p
WHERE p.competencia = :competencia
  AND p.status = 'PENDING'
```

### Achados

| # | Severidade | Achado | Evidência |
|---|-----------|--------|-----------|
| 1 | 🟠 Alta | Sem `idx_pay_payment_comp_status` (criado em V4), esta query executa **Seq Scan** em tabela com 180M+ linhas. Estimativa: >30 minutos de execução, bloqueando a janela noturna do ciclo. | `PAGAMENTO.ddm#OBS` — "CRESCIMENTO MEDIO: 3.8 MILHOES REGISTROS/MES" |
| 2 | 🟠 Alta | A entidade `pay_payment` não tem `@BatchSize` configurado. Em consultas paginadas do batch que acessam `pay_payment_discount`, o ORM dispara uma query por pagamento para carregar os descontos (N+1 clássico). | `pay_payment` entity — `@OneToMany` LAZY para `discounts` |
| 3 | 🟡 Média | `UNIQUE(beneficiary_id, competencia)` impede reprocessamento por período, mas a constraint não cobre reprocessamentos com `status = 'REPROCESSED'` — o DDM tinha campo `NUM-CICLO` para controle. Reprocessamentos deveriam usar novo ciclo + nova linha. | `V1__init_schema.sql` — constraint `pay_payment_benef_comp_uk` |

### Plano EXPLAIN esperado (pós V4)

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, beneficiary_id, vlr_bruto, vlr_desconto, vlr_liquido
FROM pay_payment
WHERE competencia = '2026-06' AND status = 'PENDING'
LIMIT 1000 OFFSET 0;
```

```
Limit (cost=0.56..512.70 rows=1000)
  -> Index Scan using idx_pay_payment_comp_status on pay_payment
       Index Cond: ((competencia = '2026-06') AND (status = 'PENDING'))
       Buffers: shared hit=145 read=12
Rows Removed by Filter: 0
Planning Time: 0.2 ms
Execution Time: 1.8 ms
```

### Correção obrigatória — N+1 nos descontos

```java
// PaymentRepository.java — adicionar:
@Query("""
    SELECT p FROM Payment p
    LEFT JOIN FETCH p.discounts
    WHERE p.competencia = :competencia
      AND p.status = 'PENDING'
    """)
@QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
List<Payment> findPendingWithDiscounts(@Param("competencia") String competencia);
```

Alternativamente, usar `@BatchSize(size = 500)` na coleção `discounts` para carregar em lotes ao invés de 1 por 1:

```java
@OneToMany(mappedBy = "payment", fetch = FetchType.LAZY)
@BatchSize(size = 500)
private List<PaymentDiscount> discounts = new ArrayList<>();
```

---

## Auditoria de Consulta — 003: AuditEventRepository

### Veredito
✅ **Passa** — repositório somente leitura/inserção, sem risco de injection.

### Achados

| # | Severidade | Achado | Evidência |
|---|-----------|--------|-----------|
| 1 | 🟠 Alta | Consultas de auditoria por período longo (ex: 10 anos de retenção legal) sem paginação podem retornar milhões de linhas para o heap da JVM. O repositório não expõe `Pageable` por padrão. | `AuditEventRepository.java` — métodos `findByAggregateTypeAndAggregateId` sem `Pageable` |
| 2 | 🔵 Baixa | O campo `ip_origem` foi adicionado em V2 como tipo `INET` no PostgreSQL. JPA/Hibernate não mapeiam `INET` nativamente — requer `@Column(columnDefinition = "inet")` + converter customizado. Sem o converter, Hibernate tentará usar `VARCHAR` causando erro de tipo. | `V2__extend_schema_from_ddm.sql#aud_audit_event.ip_origem` |

### Correção — paginação obrigatória

```java
// AuditEventRepository.java
Page<AuditEvent> findByAggregateTypeAndAggregateId(
    String aggregateType, String aggregateId, Pageable pageable
);

// Uso (máximo 100 eventos por página):
PageRequest.of(0, 100, Sort.by("occurredAt").descending())
```

### Correção — mapeamento INET

```java
// InetAddressConverter.java (shared/)
@Converter(autoApply = false)
public class InetAddressConverter implements AttributeConverter<InetAddress, String> {
    @Override public String convertToDatabaseColumn(InetAddress attr) {
        return attr == null ? null : attr.getHostAddress();
    }
    @Override public InetAddress convertToEntityAttribute(String dbData) {
        try { return dbData == null ? null : InetAddress.getByName(dbData); }
        catch (UnknownHostException e) { return null; }
    }
}
```

Se manter `String` no Java, usar `@Column(columnDefinition = "INET")` e aceitar que o CAST seja tratado pelo PostgreSQL driver JDBC.

---

## Auditoria de Consulta — 004: EligibilityChecker (cross-context)

### Veredito
🟠 **Correção obrigatória** — uso cruzado de entidades viola regra de bounded context.

### Achados

| # | Severidade | Achado | Evidência |
|---|-----------|--------|-----------|
| 1 | 🟠 Alta | `EligibilityChecker` recebe `EligibilityParams` com dados do beneficiário e do programa social. Se o chamador buscar esses dados com queries separadas (beneficiário + programa + descontos ativos), são 3 queries por beneficiário em lote = 3× N+1. | `EligibilityChecker.java` + `BeneficiaryService.checkAgeSuspension` — leitura de `discounts` em loop |
| 2 | 🟡 Média | O cálculo de `isActiveOn(LocalDate)` em `BeneficiaryDiscount` é feito em memória — correto. Porém a coleção `discounts` (LAZY) é carregada inteira antes do filtro. Para beneficiários com histórico longo de descontos, considerar query direta com predicado de data. | `BeneficiaryDiscount.java#isActiveOn` — filtro em memória pós-LAZY-load |

### Correção recomendada

```java
// ben_discount — query com predicado de data (evita load da coleção completa)
@Query("""
    SELECT d FROM BeneficiaryDiscount d
    WHERE d.beneficiary.id = :beneficiaryId
      AND d.dtIniDsct <= :referenceDate
      AND (d.dtFimDsct IS NULL OR d.dtFimDsct >= :referenceDate)
    """)
List<BeneficiaryDiscount> findActiveDiscounts(
    @Param("beneficiaryId") UUID beneficiaryId,
    @Param("referenceDate") LocalDate referenceDate
);
```

---

## Auditoria de Consulta — 005: Varredura de SQL Injection

### Veredito
✅ **Passa** — nenhuma concatenação de string com dados do usuário encontrada.

### Evidências de conformidade

| Local | Técnica | Status |
|-------|---------|--------|
| `BeneficiaryRepository.findByCpf` | Spring Data JPA (query derivada) — parâmetro vinculado | ✅ |
| `BeneficiaryRepository.existsByCpf` | Spring Data JPA (query derivada) | ✅ |
| `BeneficiaryController` | `@Valid` + Bean Validation antes de chegar ao repositório | ✅ |
| `GlobalExceptionHandler` | Não expõe SQL errors para o cliente | ✅ |
| Flyway migrations | DDL estático, sem interpolação de runtime | ✅ |

**Nota:** CPF é tratado como `String` com validação de formato (CpfValidator.isValid) antes de qualquer acesso ao banco — elimina risco de injection via CPF malformado.

---

## Checklist de conformidade com padrões SIFAP

| Padrão | Status | Observação |
|--------|--------|-----------|
| Nomes em `snake_case` | ✅ | Todas as tabelas e colunas |
| `TIMESTAMPTZ` para timestamps | ✅ | `created_at`, `updated_at`, `occurred_at` |
| `NUMERIC` para valores monetários | ✅ | Nunca `FLOAT` |
| Colunas PII com `COMMENT` | ✅ | `cpf`, `nome`, `nome_mae` comentados em V1/V2 |
| Parâmetros vinculados (no injection) | ✅ | Spring Data JPA |
| Sem `SELECT *` em paths quentes | ✅ | Spring Data projections / DTOs |
| Paginação em coleções grandes | 🟠 | `AuditEventRepository` — adicionar `Pageable` |
| `@Transactional` somente em Services | ✅ | ADR-004 respeitado |
| N+1 em batch | 🟠 | `pay_payment.discounts` — usar `@BatchSize(500)` |

---

## Itens de ação

| Prioridade | Ação | Arquivo | REQ-ID |
|-----------|------|---------|--------|
| P1 | Adicionar `@BatchSize(size=500)` em `Payment.discounts` | `Payment.java` | REQ-PAY-001 |
| P1 | Adicionar `Pageable` em `AuditEventRepository` | `AuditEventRepository.java` | REQ-AUD-001 |
| P2 | Criar `findWithDetailsByCpf` com `@EntityGraph` | `BeneficiaryRepository.java` | REQ-BEN-001 |
| P2 | Criar `findActiveDiscounts` com predicado de data | `BeneficiaryDiscountRepository.java` (novo) | REQ-PAY-007 |
| P3 | Criar `InetAddressConverter` para campo `ip_origem` | `shared/InetAddressConverter.java` (novo) | REQ-AUD-001 |

---

*Gerado pelo DBA (Par 4) — workflow segue safe-migration.SKILL.md (expand-only, nunca editar migrations já aplicadas) e query-optimization.SKILL.md (medir antes de otimizar, justificar cada índice).*
