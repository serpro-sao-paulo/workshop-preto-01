---
description: "Use when writing or reviewing requirements, EARS specifications, acceptance criteria, traceability, and docs-backed requirements."
applyTo: "02-spec-moderna/**,specs/**"
---

<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Convenções de Documentação de Requisitos

## Formato
- Notação EARS para requisitos formais
- Given/When/Then para critérios de aceitação
- Numeração sequencial dentro de features
- MUST/SHALL para obrigatório, SHOULD para recomendado
- **Todo requisito carrega uma linha `source_legacy:`** apontando para um arquivo `*.java`, `*.jsp`, `*.xml` ou `*.js` em `01-arqueologia/legado-sisdnit/`, ou `[GREENFIELD] + justification`. Arquivos `.NSM` e `.ddm` são ignorados pelo gate. O CI rejeita requisitos sem essa linha.
