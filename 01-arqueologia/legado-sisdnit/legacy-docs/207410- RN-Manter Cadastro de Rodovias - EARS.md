<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
title: "EARS - RN-Manter Cadastro de Rodovias"
description: "Conversao das regras do documento legado para requisitos EARS rastreaveis"
source_document: "01-arqueologia/legado-sisdnit/legacy-docs/207410- RN-Manter Cadastro de Rodovias.md"
created: "2026-09-29"
status: "draft"
---

# EARS - Manter Cadastro de Rodovias

Conversao das regras do documento legado para a notacao EARS. As fontes de codigo abaixo pertencem ao legado Java/JSP/XML e foram aceitas pelo gate `legacy-traceability`.

## Requisitos

```yaml
REQ-ROD-001:
  pattern: ubiquitous
  text: "O sistema devera permitir cancelar a operacao em curso e retornar ao fluxo anterior."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/jsp/f_consulta_rodovia1.jsp#L94-L96
  original: "O usuario pode cancelar a operacao."
  notes: "A interface tambem apresenta cancelar nas telas de resultado."

REQ-ROD-002:
  pattern: ubiquitous
  text: "O sistema devera permitir limpar os campos da operacao e retornar ao estado inicial."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/jsp/f_consulta_rodovia1.jsp#L12-L27
  original: "O sistema deve oferecer limpeza das entradas."

REQ-ROD-003:
  pattern: optional
  text: "ONDE a exportacao da consulta estiver disponivel o sistema devera permitir imprimir ou exportar os resultados."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/jsp/f_consulta_rodovia2.jsp#L26-L27
  original: "O usuario pode imprimir a interface."

REQ-ROD-004:
  pattern: event-driven
  text: "QUANDO o operador incluir uma rodovia, o sistema devera exigir sigla, descricao e pelo menos uma UF."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/xml/ValidaRodovia.xml#L11-L28
  original: "Os campos Sigla, Descricao e UF sao obrigatorios na inclusao."

REQ-ROD-005:
  pattern: event-driven
  text: "QUANDO o operador informar a sigla durante a inclusao, o sistema devera converte-la para maiusculas e rejeitar a operacao se a sigla ja existir."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/jsp/f_inclusao_rodovia1.jsp#L41-L53
  original: "A sigla deve ser convertida para maiuscula e nao pode ser duplicada."

REQ-ROD-006:
  pattern: unwanted
  text: "SE o operador incluir uma rodovia sem informar sigla, descricao ou UF, ENTAO o sistema devera rejeitar a operacao e apresentar a validacao do campo ausente."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/xml/ValidaRodovia.xml#L11-L28
  original: "Campos obrigatorios nao informados devem gerar MS2."

REQ-ROD-007:
  pattern: event-driven
  text: "QUANDO o operador incluir ou alterar uma rodovia, o sistema devera permitir selecionar multiplas UFs associadas."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/java/RegraManterCadastroRodovias.java#L39-L51
  original: "Mais de uma UF pode ser selecionada."

REQ-ROD-008:
  pattern: event-driven
  text: "QUANDO o operador incluir uma rodovia sem informar uma situacao, o sistema devera usar Ativa como valor padrao."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/jsp/f_consulta_rodovia1.jsp#L81-L87
  original: "O valor padrao da situacao e Ativa."

REQ-ROD-009:
  pattern: event-driven
  text: "QUANDO o operador alterar uma rodovia, o sistema devera preservar a identificacao da rodovia selecionada e processar a descricao, as UFs e a situacao informadas."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/java/AcaoRodovia.java#L85-L102
  original: "Na alteracao, a sigla nao e editavel e os demais dados podem ser alterados."

REQ-ROD-010:
  pattern: event-driven
  text: "QUANDO o operador consultar rodovias, o sistema devera aceitar sigla, descricao, UF e situacao como filtros."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/java/AcaoRodovia.java#L50-L67
  original: "A consulta deve aceitar os filtros definidos."

REQ-ROD-011:
  pattern: unwanted
  text: "SE a descricao informada para consulta tiver menos de tres caracteres, ENTAO o sistema devera rejeitar a consulta."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/xml/ValidaRodovia.xml#L16-L20
  original: "Descricao informada na consulta deve ter pelo menos 3 caracteres."

REQ-ROD-012:
  pattern: unwanted
  text: "SE a consulta nao retornar rodovias, ENTAO o sistema devera apresentar o fluxo de rodovia nao encontrada."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/java/AcaoRodovia.java#L60-L62
  original: "Consulta sem resultados deve apresentar mensagem de nao encontrado."

REQ-ROD-013:
  pattern: ubiquitous
  text: "O sistema devera apresentar sigla, descricao, UF e situacao no resultado e no detalhamento da consulta."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/jsp/f_consulta_rodovia2.jsp#L26-L48
  original: "O resultado e o detalhamento exibem Sigla, Descricao, UF e Situacao."

REQ-ROD-014:
  pattern: ubiquitous
  text: "O sistema devera ordenar as UFs associadas a cada rodovia antes de apresenta-las."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/java/RepositorioRodovia.java#L98-L124
  original: "As UFs devem ser apresentadas em ordem alfabetica."

REQ-ROD-015:
  pattern: event-driven
  text: "QUANDO o operador selecionar uma rodovia no resultado da consulta, o sistema devera carregar o detalhamento correspondente."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/java/AcaoRodovia.java#L71-L82
  original: "O operador pode acessar o detalhamento do resultado."
```

## Pendencias de Clarificacao

- `MS2`, `MS5`, `MS12` e `MS20` aparecem no documento original, mas nao foram localizadas nas fontes de codigo consultadas. E necessario mapear essas mensagens antes de definir o contrato de erro.
- A regra de que pelo menos um filtro deve ser preenchido na consulta esta documentada no legado, mas a validacao correspondente aparece comentada em `ValidaRodovia.xml`. Confirmar se a regra permanece vigente.

## Fontes Consultadas

- [Documento legado](207410-%20RN-Manter%20Cadastro%20de%20Rodovias.md)
- `programs/java/AcaoRodovia.java`
- `programs/java/RegraManterCadastroRodovias.java`
- `programs/java/RepositorioRodovia.java`
- `programs/jsp/f_consulta_rodovia1.jsp`
- `programs/jsp/f_consulta_rodovia2.jsp`
- `programs/jsp/f_inclusao_rodovia1.jsp`
- `programs/xml/ValidaRodovia.xml`
