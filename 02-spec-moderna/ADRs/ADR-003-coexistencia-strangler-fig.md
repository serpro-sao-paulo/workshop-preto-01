<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-003 — Coexistência com o Legado via Strangler Fig (topologia de migração)

**Status**: accepted
**Date**: 2026-06-10
**Deciders**: Par 2 · Arquitetura (Enterprise Architect + Software Architect), revisão do PO
**Context tags**: topology, coexistence, risk, data-migration

## Context

O SIFAP legado é missão crítica (nível 1, SLA 99,5%) e processa ~180 milhões de registros históricos de pagamento e ~4,2 milhões de beneficiários no Adabas. **Não existe janela para um "big bang"**: desligar o legado e ligar o novo num único corte colocaria o pagamento de 4 milhões de famílias em risco. Além disso, o conhecimento das regras está só no código (equipe original aposentada), então a migração precisa ser **verificável regra a regra** contra o legado em produção.

A cadeia batch tem ordem obrigatória (`BATCHPGT` → `BATCHCON` → `BATCHREL`, MYS-009) acoplada ao SIAFI, e há divergências internas de comportamento (truncamento × arredondamento, INC-004) que precisam ser conciliadas durante a transição, não depois.

Forcing function: modernizar sem interromper o ciclo mensal e sem perder rastreabilidade legado→moderno.

## Decision

Adotaremos o padrão **Strangler Fig**: o SIFAP 2.0 cresce ao redor do legado, assumindo **bounded contexts um a um**, com o legado permanecendo como fonte da verdade até cada contexto ser validado.

1. **Ordem de carving (do menor acoplamento para o maior):**
   `audit` → `beneficiary`/`program` (cadastro) → `eligibility` → `payment` (cálculo/folha).
   `payment` é o último porque concentra o risco financeiro e as integrações externas.
2. **Fachada/roteador na borda:** uma camada de roteamento direciona cada capacidade para o legado ou para o SIFAP 2.0, permitindo migrar por contexto sem o usuário perceber.
3. **Shadow run (execução em sombra) para `payment`:** durante a transição, o cálculo moderno roda **em paralelo** ao `BATCHPGT` legado sobre os mesmos dados, e os resultados são comparados (reconciliação). Só se promove o moderno quando a divergência for explicada (ex.: half-up vs. truncamento — REQ-PAY-006).
4. **Dados:** o legado Adabas permanece autoritativo durante a coexistência; o PostgreSQL do novo sistema é populado por sincronização e validado por conciliação antes do flip de cada contexto.
5. **Sem reordenar a cadeia batch** enquanto o SIAFI depender dela (MYS-009): a nova folha respeita a sequência atual até a conciliação ser migrada junto.

## Alternatives considered

- **Opção A · Big bang (corte único).** Pros: sem manter dois sistemas. Cons: risco inaceitável para pagamento de 4M famílias; impossível validar 36 regras de uma vez; rollback catastrófico. **Rejeitado por risco operacional.**
- **Opção B · Reescrever e rodar em paralelo permanente (duplicação indefinida).** Pros: segurança máxima. Cons: custo de manter dois sistemas para sempre; o objetivo é aposentar o legado, não eternizá-lo. **Rejeitado por custo e por não cumprir a meta de descomissionamento.**
- **Opção C (escolhida) · Strangler Fig com shadow run no payment.** Pros: migração incremental, reversível por contexto, com validação contínua contra o legado. Cons: exige fachada/roteador e disciplina de reconciliação; coexistência temporária de dois sistemas. **Aceito.**

## Consequences

### Positive
- Cada bounded context é migrado e validado isoladamente; rollback é por contexto, não global.
- O shadow run dá evidência objetiva de que o cálculo moderno reproduz (ou corrige conscientemente) o legado antes de assumir o pagamento real.
- Rastreabilidade legado→moderno mantida (alinha com a regra `source_legacy:` da spec).

### Negative
- Custo de operar legado + moderno + camada de roteamento durante a transição.
- Reconciliação do shadow run exige instrumentação e tempo de análise (centavos importam — MYS-005/INC-004).
- A fachada de roteamento é um componente novo que precisa de alta disponibilidade.

### Neutral
- A ordem de carving pode ser ajustada se o PO repriorizar, desde que `payment` continue por último.

## Follow-ups
- [ ] Definir com o Software Architect os bounded contexts finais (entrada para C4 L2/L3).
- [ ] Especificar a reconciliação do shadow run de `payment` como requisito verificável.
- [ ] Alinhar com Par 5 (DevOps) a topologia Azure da fachada/roteador e a sincronização Adabas→PostgreSQL no Terraform.
- [ ] Confirmar com Par 4 (DBA) a estratégia de carga e validação de dados por contexto.
- [ ] Revisitar a ordem de carving após a migração de `audit`.

## References
- Mapa de dependências / cadeia batch: [`../../01-arqueologia/dependency-map.md`](../../01-arqueologia/dependency-map.md)
- Mistérios MYS-005, MYS-009, INC-004: [`../../01-arqueologia/mysteries-found.md`](../../01-arqueologia/mysteries-found.md)
- Spec e bounded contexts candidatos: [`../SPECIFICATION.md`](../SPECIFICATION.md)
- ADR-001 (Monolito Modular) e ADR-002 (Integração): mesmo diretório / exemplos
