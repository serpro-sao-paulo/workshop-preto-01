# Context Audit — SIFAP 2.0

> Produzido pelo **Technical Lead** via `/audit-context`.
> Escopo: `.github/instructions/`, `.github/prompts/`, `.github/agents/`, `AGENTS.md`, `CODEMAP.md`.
> Data: 2026-06-10

---

## Inventário

| Diretório | Arquivos | Observação |
| --- | --- | --- |
| `.github/instructions/` | 11 arquivos | Inclui `README.md` |
| `.github/prompts/` | ~60 arquivos | Persona prompts + speckit + stage prompts |
| `.github/agents/` | 27 arquivos | 14 personas + 13 speckit |
| `AGENTS.md` | 1 (criado 2026-06-10) | ✅ Novo — em dia |
| `CODEMAP.md` | 1 (criado 2026-06-10) | ✅ Novo — em dia |

---

## Achados — Tabela de severidade

| Arquivo | Problema | Severidade | Correção |
| --- | --- | --- | --- |
| `.github/instructions/frontend-spec.instructions.md` | `applyTo: '**/app/**,**/components/**,**/*.tsx,**/*.ts'` — o glob `**/*.ts` captura TODO arquivo TypeScript incluindo testes e configs, carregando instrução de frontend em contextos não relacionados | 🔴 Alta | Restringir para `'**/app/**,**/components/**,**/app/**/*.tsx,**/components/**/*.tsx'` |
| `.github/instructions/cicd.instructions.md` | `applyTo: ".github/workflows/**,**/*.yml,**/*.yaml"` — captura Docker Compose, Terraform vars e qualquer YAML do repositório | 🔴 Alta | Restringir para `".github/workflows/**,**/*.yml"` e remover `**/*.yaml` ou especificar `.github/workflows/**/*.yml` |
| `.github/agents/speckit.*.agent.md` (13 arquivos) | Nenhum dos 13 agentes speckit tem campo `model:` no frontmatter | 🟡 Média | Adicionar `model: claude-sonnet-4-6` como default a todos; ou `claude-opus-4-6` para `speckit.specify` e `speckit.plan` (decisão arquitetural) |
| `.github/agents/speckit.agent-context.update.agent.md` | Frontmatter tem apenas 2 campos (`description`); sem `model:`, sem `tools:` | 🟡 Média | Adicionar `model:` e `tools:` como nos demais agentes persona |
| `.github/prompts/persona-*.prompt.md` (30 arquivos) | Nenhum dos prompts de persona tem campo `mode:` — usam `agent: agent` mas sem `mode:` definido | 🟡 Média | Adicionar `mode: agent` explícito ao frontmatter; evita comportamento padrão ambíguo em novas versões do Copilot |
| `CODEMAP.md` | Recém-criado (2026-06-10); ainda não reflete código real (Stage 3 não iniciado) | 🟡 Média | Atualizar ao final do Stage 3 com paths reais gerados; agendar revisão pós-implementação |
| `.github/instructions/backend.instructions.md` | Conteúdo muito curto (3 linhas após frontmatter): "Naming: controllers em PascalCase, rotas em kebab-case / Errors: RFC 7807 / Testing: 85% cobertura" — sem exemplos concretos | 🟡 Média | Expandir com pelo menos: exemplo de RFC 7807 response, `@ControllerAdvice` padrão, convenção de `@Valid` |
| `.github/instructions/tests.instructions.md` | Arquivo não verificado (não carregado nesta sessão) — sinalizar para revisão | 🟢 Baixa | Verificar se tem exemplos de `@implements REQ-NNN` comentário inline |
| `AGENTS.md` | Seção "Comandos essenciais" usa paths `cd frontend` mas `frontend/` ainda não existe (Stage 3 não iniciado) | 🟢 Baixa | Marcar com `# (Stage 3+)` as linhas de frontend |
| `.github/copilot-instructions.md` | Seção "Ferramentas Aprovadas" menciona "GitHub Spec-Kit (`Specify CLI`)" — CLI não está disponível no repositório; pode confundir novos membros | 🟢 Baixa | Adicionar nota: "Spec-Kit neste repositório é usado via agents — sem instalação de CLI" |

---

## Top 3 correções imediatas

### 1. 🔴 Restringir `applyTo` do `frontend-spec.instructions.md`

**Problema:** O glob `**/*.ts` faz com que a instrução de frontend Next.js seja carregada ao editar qualquer arquivo TypeScript — incluindo testes de backend se/quando houver TypeScript no projeto. Isso polui o contexto do Copilot com convenções de frontend ao trabalhar no backend.

**Correção:**
```yaml
# Antes
applyTo: '**/app/**,**/components/**,**/*.tsx,**/*.ts'

# Depois
applyTo: '**/app/**,**/components/**,**/*.tsx,**/frontend/**/*.ts'
```

### 2. 🔴 Restringir `applyTo` do `cicd.instructions.md`

**Problema:** `**/*.yaml` captura qualquer arquivo YAML no repositório — Docker Compose, arquivos de config do Terraform, etc. — injetando convenções de GitHub Actions onde não se aplicam.

**Correção:**
```yaml
# Antes
applyTo: ".github/workflows/**,**/*.yml,**/*.yaml"

# Depois
applyTo: ".github/workflows/**"
```

### 3. 🟡 Adicionar `model:` aos 13 agentes speckit

**Problema:** Sem `model:` definido, o Copilot usa o default da sessão — que pode variar. Para agentes de descoberta como `speckit.specify` e `speckit.plan`, o default pode ser um modelo mais barato do que o ideal.

**Correção:** Adicionar ao frontmatter de cada agente:
- `speckit.specify`, `speckit.plan`, `speckit.clarify`: `model: claude-opus-4-6`
- Demais speckit: `model: claude-sonnet-4-6`

---

## Frescor do CODEMAP.md

- **Status:** ✅ Criado hoje (2026-06-10)
- **Próxima revisão obrigatória:** após Stage 3 (quando código real existir)
- **Trigger de atualização:** qualquer PR que adicione, mova ou remova um bounded context ou adapter
