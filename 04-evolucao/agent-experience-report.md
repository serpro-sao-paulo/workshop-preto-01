<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Experiência com GitHub Copilot Agent

![ESTÁGIO 04 Evolução](https://img.shields.io/badge/ESTÁGIO-04%20Evolução-FFB900?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S4](https://img.shields.io/badge/PREENCHA-Durante%20S4-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 4](README.md) → **Agent Experience Report**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 4 (Evolução).
>
> **O que você terá ao final do estágio:**
>
> 1. Relatório honesto da experiência com Copilot Agent
> 2. Issues bem escritas que viraram PRs aprovados
> 3. Lições aprendidas para o próximo time
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Preencha este relatório ao final do Estágio 4.
> Seja honesto — queremos aprender o que funciona e o que não funciona. Avaliação positiva forçada não ajuda ninguém.

**Time**: Par 5 · Operações (DevOps + Tech Writer)
**Data**: 19/05/2026
**Edição**: Workshop sisdnit — Legado para Azure
**Participantes**: DevOps Engineer, Tech Writer

---

## 1. Issues Criadas

### Issue 1

- **Título**: [REQ-AUD-01] Trilha de auditoria deve incluir eventos de exclusão (corrigir MYS-018)
- **Link**: ver [`issue-par5-trilha-auditoria.md`](issue-par5-trilha-auditoria.md) (Issue real ainda não aberta no GitHub neste ambiente de workshop)
- **Descrição resumida**: implementar serviço de auditoria que exibe TODAS as ações, corrigindo a omissão silenciosa de exclusões (`EX`) herdada do `RELAUDIT.NSN`.
- **Tempo para escrever a Issue**: ~15 minutos

### Issue 2

- **Título**: (não criada) — candidata: [REQ-CPF-01] Máscara única de CPF corrigindo o vazamento de MYS-019
- **Link**: —
- **Descrição resumida**: unificar a mascaração de CPF (`***.***.XXX-XX`) sobre string de 11 dígitos, eliminando o ramo que vaza os 3 primeiros dígitos.
- **Tempo para escrever a Issue**: —

---

## 2. PRs Gerados pelo Agent

### PR 1 (da Issue 1)

- **Link**: — (não houve PR de Agent na nuvem; o workshop usou os **agentes de estágio** no editor: `@archaeologist` no Estágio 1 e `@evolution` no Estágio 4)
- **Tempo que o Agent levou**: —
- **Arquivos modificados**: —
- **Testes criados**: Não (a Issue REQ-AUD-01 ficou pronta para disparo; implementação não executada por Agent de PR)
- **Precisou de ajustes manuais?**: —
- **Foi mergeado?**: Não

### PR 2 (da Issue 2)

- **Link**: [URL do PR]
- **Tempo que o Agent levou**: \_\_\_ minutos
- **Arquivos modificados**: \_\_\_
- **Testes criados**: Sim / Não
- **Precisou de ajustes manuais?**: Sim / Não
- **Foi mergeado?**: Sim / Não

---

## 3. O que funcionou bem

> Liste o que o Agent fez bem. Exemplos: entendeu a arquitetura, criou testes bons, seguiu padrões, etc.

1. Os agentes de estágio respeitaram as convenções do kit (formato de tabela de regras, âncoras `ARQUIVO.NSN#Ln`, resumos estatísticos) sem precisar repetir as instruções.
2. O `@archaeologist` correlacionou bem mistério ↔ regra ↔ código (MYS-018/019 ligados a BR-049/050 e às linhas do `RELAUDIT`/`CONSBENF`).
3. O `@evolution` gerou CI e esqueleto Terraform coerentes com o princípio "plan-only, nunca apply".

---

## 4. O que surpreendeu o time

> O que vocês não esperavam? Positivo ou negativo.

1. A quantidade de "backdoors" e omissões documentadas com comentários do tipo "NAO CORRIGIR SEM APROVACAO DA AUDITORIA" — o legado sabia dos bugs e os congelou.
2. Quanto a rastreabilidade (BR → REQ → linha Natural) facilita escrever uma Issue boa para o Agent depois.
3. O custo baixo de manter o pipeline separado para o protótipo (`prototipo-backend.yml`) em vez de mexer no `ci.yml` genérico.

---

## 5. O que falhou ou decepcionou

> Onde o Agent errou, não entendeu ou produziu código ruim?

1. O backend do protótipo usa Maven puro (sem `mvnw`), então o `ci.yml` original (que chama `./mvnw`) não serviria — foi preciso um workflow dedicado.
2. O kit referencia pastas (`05-terraform-azure/`, `infra/`) que não existem nesta sub-pasta; foi preciso criar o esqueleto `prototipo/infra` do zero.
3. Não houve execução real de Agent de PR na nuvem neste ambiente — a Issue ficou pronta, mas não virou PR mergeado.

### Tipos de falha encontrados

- [ ] Código não compilava
- [ ] Testes falhavam
- [ ] Não seguiu a arquitetura do projeto
- [ ] Imports incorretos ou circulares
- [ ] Lógica de negócio errada
- [ ] Faltou tratamento de erros
- [ ] Credenciais ou dados sensíveis no código
- [x] Outro: referências a pastas/comandos inexistentes no kit (mvnw, infra/) que exigiram adaptação manual

---

## 6. Qualidade dos PRs (nota 1–5)

| Critério                | Nota (1–5) | Comentário |
| ----------------------- | ---------- | ---------- |
| Corretude do código     | 4          | CI e Terraform válidos; backend do Par 3/4 segue verde (25 testes). |
| Aderência à arquitetura | 5          | Respeitou pacotes `br.gov.client.sisdnit.*` e camadas domain/application. |
| Qualidade dos testes    | 4          | Testes existentes preservados; Issue REQ-AUD-01 já define os casos de teste. |
| Documentação gerada     | 5          | Runbook, glossário e âncoras de rastreabilidade consistentes. |
| Clareza do código       | 4          | YAML/HCL legíveis e comentados (plan-only sinalizado). |
| **Média geral**         | **4,4**    | Bom para artefatos de Operação; falta validar um PR de Agent real. |

Escala: 1=Péssimo, 2=Ruim, 3=Aceitável, 4=Bom, 5=Excelente

---

## 7. Você usaria o Agent novamente?

- [ ] Sim, para tudo — economiza muito tempo
- [x] Sim, para tarefas simples e bem definidas
- [ ] Talvez, mas precisa de muita supervisão
- [ ] Não, gasto mais tempo revisando do que implementando
- [ ] Não tenho certeza ainda

**Justificativa**: para artefatos de Operação (CI, Terraform plan, runbook, glossário) e para tarefas com Issue bem escrita e rastreável, o Agent acelera muito. Para regras de negócio com backdoors do legado, ainda exige revisão humana cuidadosa.

---

## 8. Recomendações para outras equipes

> Se outra equipe fosse usar o Agent pela primeira vez, o que vocês diriam?

1. Escreva a Issue com âncoras para o código legado (`ARQUIVO.NSN#Ln`) e para a BR/MYS — o PR sai muito melhor.
2. Mantenha pipelines de protótipo separados do CI genérico para não quebrar quando faltar `mvnw`/`infra`.
3. Deixe explícito "plan-only, nunca apply" em qualquer Issue de infraestrutura.

---

## 9. Comparação: Agent vs. Copilot Chat vs. Implementação Manual

| Aspecto     | Modo Agent | Copilot Chat | Manual |
| ----------- | ---------- | ------------ | ------ |
| Velocidade  | Alta       | Média        | Baixa  |
| Qualidade   | Boa (com Issue clara) | Boa (com contexto) | Alta (mais lento) |
| Controle    | Médio      | Alto         | Total  |
| Aprendizado | Médio      | Alto         | Alto   |
| Quando usar | Tarefa bem definida e rastreável | Explorar/depurar | Decisão crítica de negócio |

---

## 10. Comentários livres

> Espaço para qualquer observação adicional sobre a experiência com IA generativa no desenvolvimento:

O maior ganho do Par 5 foi transformar achados de arqueologia (MYS-018 omissão de exclusões; MYS-019 vazamento de CPF) em uma Issue acionável e em pipeline/infra reproduzíveis. O fluxo "arqueólogo documenta → Issue rastreável → Agent implementa" é promissor, desde que a rastreabilidade BR → REQ → linha Natural seja mantida.


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 4</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="templates/"><strong>Templates</strong></a><br/>
<sub>Template para preencher.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

