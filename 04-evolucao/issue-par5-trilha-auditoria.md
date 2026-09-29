<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Issue para Copilot Agent — Par 5 · Operações

![ESTÁGIO 04 Evolução](https://img.shields.io/badge/ESTÁGIO-04%20Evolução-FFB900?style=for-the-badge) ![PAR 5 Operações](https://img.shields.io/badge/PAR-5%20Operações-1A1A1A?style=for-the-badge)

> Issue escrita pelo Par 5 no Estágio 4, seguindo o modelo de
> [`../08-exemplos/issue-para-agent-exemplo.md`](../08-exemplos/issue-para-agent-exemplo.md).
> Cole o bloco entre `BEGIN ISSUE` / `END ISSUE` em uma Issue nova do GitHub e dispare o Agent.

## BEGIN ISSUE ────────────────────────────────────────────────────────────────

# [REQ-AUD-01] Trilha de auditoria deve incluir eventos de exclusão (corrigir MYS-018)

## 🎯 Objetivo (uma frase)

Implementar, no protótipo backend, um serviço de trilha de auditoria que **registra e exibe todas as ações** — inclusive exclusões (`EX`) —, eliminando a omissão silenciosa identificada no legado (MYS-018).

## 📚 Contexto

- **Origem da regra:** `RELAUDIT.NSN` ([`../01-arqueologia/legado-sisdnit/programs/RELAUDIT.NSN`](../01-arqueologia/legado-sisdnit/programs/RELAUDIT.NSN#L103-L108)).
- **Mistério associado:** MYS-018 — eventos `ACAO = 'EX'` sofrem `ESCAPE TOP` e nunca aparecem no relatório (incluído na alteração "LIMPEZA RELATORIO" de 2014).
- **Regras de negócio:** BR-049 (classificação de ações) e BR-050 (omissão de `EX` — a ser **corrigida**, não reproduzida).
- **Por que agora:** auditoria sem exclusões viola transparência/compliance (TCU/LGPD). O sisdnit 2.0 não deve herdar o encobrimento.

## 📝 Requisitos funcionais

- [ ] Modelar `AuditAction` com os valores `INCLUSAO`, `ALTERACAO`, `CONCILIACAO`, `CONSULTA`, `DIVERGENCIA`, `EXCLUSAO` (mapeando os códigos legados `IN/AL/CO/CN/DV/EX`).
- [ ] Implementar `AuditTrailService.listar(filtro)` com filtros opcionais por período, ação, usuário e tabela.
- [ ] Período padrão quando não informado: `1997-01-01` até a data atual (igual ao legado, BR-049).
- [ ] **Diferente do legado:** eventos `EXCLUSAO` SEMPRE aparecem no resultado. Nunca filtrados silenciosamente.
- [ ] Expor um indicador de paridade legada (`listarLegacyParity`) que reproduz a omissão de `EX` apenas para fins de comparação/teste, isolado do caminho de produção (mesmo padrão de `validateLegacyParity` do Par 4).

## 🔧 Requisitos técnicos

- [ ] Pacote `br.gov.client.sisdnit.audit`, camadas `domain` / `application` (domínio não importa infraestrutura).
- [ ] Java 21, sem dependências novas além das já presentes no [`pom.xml`](../03-implementacao/prototipo/backend/pom.xml).
- [ ] Cabeçalho de cada classe mapeando BR-049/BR-050 → REQ-AUD-01 → linhas do `RELAUDIT.NSN`.

## ✅ Critérios de aceite

- [ ] `mvn -B verify` passa (testes novos + 25 existentes permanecem verdes).
- [ ] Teste comprova que `EXCLUSAO` aparece em `listar(...)` e **não** aparece em `listarLegacyParity(...)`.
- [ ] Teste cobre o período padrão (1997-01-01 .. hoje) quando o filtro vem vazio.

## 🚧 Fora de escopo

- Persistência real (banco), UI e geração de PDF/impressora 3270.
- Integração com a tabela `AUDITORIA` do Adabas.

## END ISSUE ──────────────────────────────────────────────────────────────────
