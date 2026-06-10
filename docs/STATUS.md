<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# 📊 STATUS do Dia — Dashboard de Progresso

![DASHBOARD Status](https://img.shields.io/badge/DASHBOARD-Status%20do%20dia-7FBA00?style=for-the-badge) ![ATUALIZE A cada 30 min](https://img.shields.io/badge/ATUALIZE-A%20cada%2030%20min-1A1A1A?style=for-the-badge) ![DONO Technical Lead](https://img.shields.io/badge/DONO-Technical%20Lead-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Docs](README.md) → **STATUS**

> **Para quem é isto?** Para o líder do time atualizar e o facilitador ler de longe.
>
> **O que você terá ao final desta leitura:** visão de 1 página do que está verde, amarelo e vermelho agora.

---

## 🎯 Status global

| Indicador | Estado | Observação |
|---|---|---|
| Time inteiro presente | ✅ | Workshop em andamento |
| Repositório do time aberto | ✅ | `develop` ativo |
| Branch `develop` protegida | ✅ | CI + ArchUnit como gates |
| CI configurado | ✅ | `.github/workflows/ci.yml` com SHA pinning + OIDC |
| Docker Compose funcional | ✅ | `docker compose up -d` sobe postgres + backend |
| Demo ensaiada | ⚪ | Pendente Estágio 4 |

---

## 🏰 Progresso dos 4 mundos

| Estágio | Status | Owner | Entregáveis | DoD verde? |
|---|---|---|---|---|
| 🟦 **1 — Arqueologia** | ✅ | Par 1 | glossary (37 termos), discovery-report, business-rules-catalog (36 BRs) | ✅ |
| 🟫 **2 — Spec Moderna** | ✅ | Par 2 | SPECIFICATION.md (22 REQ-IDs), bounded-contexts.md, 5 ADRs, IMPLEMENTATION_PLAN.md | ✅ |
| 🟧 **3 — Implementação** | ✅ | Pares 3 + 4 | Backend Spring Boot completo, 5 migrations, 10 testes unitários, 4 ITs, query audit | ✅ |
| 🏰 **4 — Evolução** | 🔄 | Par 5 | CI/CD hardened, Terraform 6 módulos, Dockerfile multi-stage, ADR-005 | 🔄 |

**Legenda:** ⚪ não começou · 🔄 em progresso · ✅ pronto · ⚠️ atrasado · 🔴 bloqueado

---

## 🟢 Passagens (canos verdes entre mundos)

| Passagem | De → Para | Status |
|---|---|---|
| **H1** | Par 1 → Par 2 | ✅ |
| **H2** | Par 2 → Pares 3+4 | ✅ |
| **H3** | Pares 3+4 → Par 5 | ✅ |

---

## 🪙 Métricas do dia (preencher conforme avança)

| Métrica | Meta | Atual |
|---|---|---|
| Termos no glossário | ≥ 30 | — |
| Regras BR-NNN documentadas | ≥ 15 | — |
| REQ-IDs em EARS | ≥ 12 | — |
| ADRs aprovadas | ≥ 3 | — |
| Endpoints REST funcionais | ≥ 3 | — |
| Cobertura backend | ≥ 70% | —% |
| Cobertura frontend | ≥ 60% | —% |
| Issues criadas para Agent | ≥ 1 | — |
| PRs mergeados | — | — |

---

## 🚨 Sinais de alerta

> Preencha quando aparecer. Líder lê em voz alta no próximo stand-up.

```
- [ ] (vazio)
```

---

## 🏆 Achievements desbloqueadas

Marque conforme conquistar:

- [ ] 🍄 **Primeira regra BR-NNN com `Programa Fonte`** — bem-vindo à arqueologia!
- [ ] ⭐ **Primeira EARS escrita** com `source_legacy:`
- [ ] 📜 **3 ADRs aprovadas** pelo time
- [ ] 🚩 **CI verde no primeiro try**
- [ ] 🦖 **Primeiro endpoint REST funcionando** via Swagger
- [ ] 🧪 **Cobertura ≥70% backend**
- [ ] 🎭 **Primeiro PR do Agent revisado e mergeado**
- [ ] 🏰 **Terraform plan sem erro**
- [ ] 👸 **DEMO RODOU** — Princesa salva!

---

## 📝 Stand-ups (registre 1 frase por par a cada transição)

### H1 (fim Estágio 1)

- Par 1 · Visão: _____________________________
- Par 2 · Arquitetura: _____________________________
- Par 3 · Implementação: _____________________________
- Par 4 · Qualidade: _____________________________
- Par 5 · Operações: _____________________________

### H2 (fim Estágio 2)

- Par 1: _____________________________
- Par 2: _____________________________
- Par 3: _____________________________
- Par 4: _____________________________
- Par 5: _____________________________

### H3 (fim Estágio 3)

- Par 1: _____________________________
- Par 2: _____________________________
- Par 3: _____________________________
- Par 4: _____________________________
- Par 5: _____________________________

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="demo-script.md"><strong>Script da Demo</strong></a><br/>
<sub>3 minutos finais.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="CHECKLIST-LIDER.md"><strong>Checklist do Líder</strong></a><br/>
<sub>Hora a hora.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
