<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-002 — Estratégia de Integração com Sistemas Externos (SIAFI, BB/CAIXA, Receita, CadÚnico)

**Status**: accepted
**Date**: 2026-06-10
**Deciders**: Par 2 · Arquitetura (Enterprise Architect + Software Architect), revisão do PO
**Context tags**: integration, topology, resilience, security

## Context

O SIFAP 2.0 não vive isolado: o C4 L1 ([`../../01-arqueologia/dependency-map.md`](../../01-arqueologia/dependency-map.md)) mostra **5 contratos externos** que existem há até 27 anos e que não podem quebrar na modernização:

- **Receita Federal (CPF)** — consulta **síncrona** na inclusão/alteração cadastral, timeout de 30s (legado: `CADBENEF.NSN`).
- **Banco do Brasil / CAIXA** — remessa e retorno **assíncronos** via arquivo **CNAB 240**, ciclo mensal (legado: `BATCHPGT.NSN` → `BATCHCON.NSN`).
- **SIAFI (STN)** — conciliação **assíncrona** mensal por arquivo TXT, com hash totalizador (legado: `BATCHCON.NSN`).
- **CadÚnico** — carga periódica posicional, integração **não padronizada** desde 2006.

Forcing function: a folha mensal paga ~3,8 milhões de famílias numa janela batch fixa. Um acoplamento síncrono indevido com BB/SIAFI durante o ciclo transformaria qualquer lentidão externa em atraso de pagamento — exatamente o incidente de Mar/2016 (timeout do `BATCHPGT`). Ao mesmo tempo, a validação de CPF é naturalmente síncrona (não dá para cadastrar sem confirmar o CPF).

Restrições: time de 5 pessoas, 1 dia de implementação, sem skill operacional para mensageria distribuída; os contratos CNAB 240 e o layout SIAFI são **fixos por terceiros** (não negociáveis).

## Decision

Adotaremos um **modelo de integração híbrido, classificado por contrato**, isolado atrás de **portas/adapters** (hexagonal) no módulo correspondente:

1. **Receita Federal: síncrono com timeout curto + circuit breaker.** Chamada REST com timeout de 5s (não 30s) e fallback "validação local módulo 11 + marcação `PENDING_RF`" quando a Receita estiver indisponível, evitando bloquear o cadastro. A confirmação externa roda de forma assíncrona depois.
2. **BB/CAIXA e SIAFI: assíncrono por arquivo, fora do caminho de request.** Geração/ingestão de **CNAB 240** e TXT SIAFI como jobs agendados, **preservando os layouts** atuais. Nenhuma chamada externa síncrona dentro do ciclo de geração da folha.
3. **CadÚnico: ingestão assíncrona idempotente** com validação de esquema na borda; correções de qualidade não derrubam a carga.
4. **Todo I/O externo atravessa um adapter** (`infrastructure/integration/...`); o domínio não conhece protocolo. Isso permite trocar arquivo→API no futuro sem tocar a regra de negócio.

## Alternatives considered

- **Opção A · Tudo síncrono (REST) com os bancos e o SIAFI.** Pros: simplicidade conceitual, sem arquivos. Cons: BB/CAIXA/SIAFI **não oferecem** APIs síncronas de pagamento em massa — o contrato real é CNAB/arquivo; acoplar a folha a chamadas externas reintroduz o risco de timeout de Mar/2016. **Rejeitado por incompatibilidade com o contrato externo e por risco operacional.**
- **Opção B · Tudo assíncrono via mensageria (event bus) inclusive validação de CPF.** Pros: desacoplamento máximo. Cons: cadastro precisa de resposta imediata do CPF (UX 3270 e regra de negócio); event bus para 5 pessoas em 1 dia é overkill operacional (mesmo argumento do ADR-001). **Rejeitado por excesso de complexidade e por inadequação ao caso síncrono.**
- **Opção C (escolhida) · Híbrido por contrato + adapters.** Pros: respeita a natureza real de cada integração, isola o domínio, remove o ponto único de falha do ciclo mensal. Cons: dois estilos de integração para manter; exige disciplina nos adapters. **Aceito.**

## Consequences

### Positive
- O ciclo mensal de pagamento deixa de ter dependência síncrona de BB/SIAFI — elimina a classe de incidente de Mar/2016.
- Layouts CNAB 240 e SIAFI preservados → sem renegociar contrato com terceiros.
- Cadastro continua usável mesmo com a Receita fora do ar (fallback `PENDING_RF`).

### Negative
- Estado `PENDING_RF` adiciona uma máquina de estados extra no cadastro e exige reconciliação posterior (novo requisito).
- Manter dois estilos de integração (síncrono/assíncrono) aumenta a superfície de teste.

### Neutral
- A troca futura de arquivo→API só afeta os adapters, não o domínio.

## Follow-ups
- [ ] Criar `REQ-INT-*` para o estado `PENDING_RF` e a reconciliação assíncrona da Receita.
- [ ] Confirmar com o PO se o fallback de CPF (cadastro com validação local) é aceitável para o negócio.
- [ ] Validar com o Par 2/Par 5 a janela e o agendamento dos jobs CNAB/SIAFI no Terraform.
- [ ] Revisitar quando BB/SIAFI publicarem APIs síncronas oficiais.

## References
- C4 L1 e inventário de integrações: [`../../01-arqueologia/dependency-map.md`](../../01-arqueologia/dependency-map.md)
- Legado: `BATCHPGT.NSN`, `BATCHCON.NSN`, `CADBENEF.NSN`
- Incidente Mar/2016 (timeout BATCHPGT): [`../../01-arqueologia/legado-sifap/README.md`](../../01-arqueologia/legado-sifap/README.md) §7.3
- ADR-001 (Monolito Modular): [`../../08-exemplos/ADR-001-monolito-modular-exemplo.md`](../../08-exemplos/ADR-001-monolito-modular-exemplo.md)
