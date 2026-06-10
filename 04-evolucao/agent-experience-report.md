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

**Time**: SIFAP 2.0 — Workshop de Modernização
**Data**: 10/06/2026
**Edição**: Workshop Preto — Ciclo 01
**Participantes**: Par 1 (PO + RE), Par 2 (EA + SA), Par 3 (TL + Dev), Par 4 (DBA + QA), Par 5 (DevOps + TW)

---

## 1. Issues Criadas

### Issue 1

- **Título**: feat(payment): implement BenefitCalculator from CALCBENF.NSN
- **Link**: (gerada durante a sessão de implementação)
- **Descrição resumida**: Implementar o cálculo de benefício líquido com os 25 fatores regionais e as regras de arredondamento do legado Natural
- **Tempo para escrever a Issue**: 15 minutos

### Issue 2

- **Título**: feat(beneficiary): add age-based suspension rule (REQ-BEN-005)
- **Link**: (gerada durante a sessão de implementação)
- **Descrição resumida**: Beneficiários com 75 anos ou mais devem ter status SUSPENDED automaticamente no cadastro
- **Tempo para escrever a Issue**: 10 minutos

---

## 2. PRs Gerados pelo Agent

### PR 1 (da Issue 1)

- **Tempo que o Agent levou**: ~12 minutos
- **Arquivos modificados**: 4 (BenefitCalculator, CalculationParams, BenefitCalculatorTest, codemap-payment)
- **Testes criados**: Sim — 20+ casos parametrizados
- **Precisou de ajustes manuais?**: Sim — fórmula de novembro/dezembro precisou de refinamento
- **Foi mergeado?**: Sim

### PR 2 (da Issue 2)

- **Tempo que o Agent levou**: ~8 minutos
- **Arquivos modificados**: 3 (BeneficiaryService, BeneficiaryServiceTest, SPECIFICATION.md)
- **Testes criados**: Sim — incluiu caso de borda "exatamente 75 anos"
- **Precisou de ajustes manuais?**: Não
- **Foi mergeado?**: Sim

---

## 3. O que funcionou bem

1. **Context engineering funcionou**: O Agent respeitou as regras de bounded contexts definidas em `AGENTS.md`. Nenhum import cross-context foi gerado — ArchUnit verde na primeira rodada.
2. **Traceamento legado → código**: Quando a Issue citava o programa Natural (`CALCBENF.NSN`), o Agent leu o arquivo do legado em `01-arqueologia/` antes de gerar o código, mantendo fidelidade às regras de negócio.
3. **TDD natural**: O Agent criou os testes junto com o código, sem precisar de instrução explícita — o padrão estava estabelecido no `AGENTS.md`.
4. **Migrações Flyway corretas**: O DBA Agent gerou SQL com `REVERT`-safe (expand-contract) na primeira tentativa, sem precisar corrigir.
5. **Pipeline hardened de primeira**: O CI com SHA-pinned actions, OIDC e Trivy foi gerado completo sem ajustes de segurança posteriores.
6. **CPF mascarado nos logs**: O Agent aplicou `CpfMaskingConverter` em todas as ocorrências sem lembrete explícito.

---

## 4. O que surpreendeu o time

> O que vocês não esperavam? Positivo ou negativo.

1. **Velocidade de contexto**: O Agent leu os 15 programas `.NSN` e os 4 DDMs em menos de 2 minutos — foi mais rápido interpretar o legado com IA do que manualmente.
2. **O `AGENTS.md` fez diferença visível**: Quando removemos o `AGENTS.md` em um experimento, o Agent gerou código cross-context e sem testes. Com ele presente, o resultado foi consistentemente melhor.
3. **Erros de compilação foram raros, mas existiram**: O Agent gerou um arquivo de teste com assinatura duplicada em `PaymentCalculationIT`. Corrigir levou mais tempo do que esperado porque o erro não foi reportado claramente.

---

## 5. O que falhou ou decepcionou

> Onde o Agent errou, não entendeu ou produziu código ruim?

1. **Classe de teste duplicada em arquivo** (`PaymentCalculationIT`): O Agent gerou o arquivo com o corpo antigo ainda anexado ao novo. O arquivo compilava com dois corpos de classe, mas falhava em runtime. Identificar a causa levou tempo porque o erro não apontava para o arquivo correto.
2. **Tipo de parâmetro errado em repository**: O Agent gerou `findByCodPrograma(String)` quando o campo era `Integer`. Indica que o Agent não verificou o schema SQL antes de gerar o repositório.
3. **`AuditEventRepository` retornando `List` em vez de `Page`**: Potencial OOM para os 10 anos de retenção de auditoria. O Agent não considerou volume de dados sem instrução explícita.

### Tipos de falha encontrados

- [x] Código não compilava (PaymentCalculationIT — classe duplicada)
- [ ] Testes falhavam
- [ ] Não seguiu a arquitetura do projeto
- [ ] Imports incorretos ou circulares
- [x] Lógica de negócio errada (tipo de parâmetro errado em repository)
- [ ] Faltou tratamento de erros
- [ ] Credenciais ou dados sensíveis no código
- [ ] Outro: \_\_\_

---

## 6. Qualidade dos PRs (nota 1–5)

| Critério                | Nota (1–5) | Comentário |
| ----------------------- | ---------- | ---------- |
| Corretude do código     | 4          | 1 bug de compilação em 15+ arquivos gerados |
| Aderência à arquitetura | 5          | Bounded contexts respeitados, ArchUnit verde |
| Qualidade dos testes    | 4          | Casos de borda cobertos; AuditEventRepository precisou de correção |
| Documentação gerada     | 4          | README e API reference precisos; runbook precisou de atualização |
| Clareza do código       | 5          | Records, sealed interfaces, nomes semânticos |
| **Média geral**         | **4.4**    | Produção com supervisão |

Escala: 1=Péssimo, 2=Ruim, 3=Aceitável, 4=Bom, 5=Excelente

---

## 7. Você usaria o Agent novamente?

- [x] Sim, para tudo — economiza muito tempo
- [ ] Sim, para tarefas simples e bem definidas
- [ ] Talvez, mas precisa de muita supervisão
- [ ] Não, gasto mais tempo revisando do que implementando
- [ ] Não tenho certeza ainda

**Justificativa**: [Explique sua escolha]

---

## 8. Recomendações para outras equipes

> Se outra equipe fosse usar o Agent pela primeira vez, o que vocês diriam?

1. **Invista 30 minutos no `AGENTS.md` antes de gerar qualquer código.** O retorno é imediato — o Agent respeita os bounded contexts e as convenções sem precisar repetir na Issue.
2. **Sempre revise os testes gerados junto com o código.** O Agent tende a não considerar volume de dados ou paginação sem instrução explícita. Cheque se `List` deveria ser `Page`.
3. **Compilação != correção.** Teste o código gerado antes de mergear. Um arquivo pode compilar mas ter uma classe duplicada ou tipo errado no método de busca.
4. **Issues curtas e contextualizadas funcionam melhor.** Uma Issue com título no padrão Conventional Commits + contexto do legado (`source_legacy: CALCBENF.NSN`) produziu resultados mais fiéis do que Issues longas sem estrutura.

---

## 9. Comparação: Agent vs. Copilot Chat vs. Implementação Manual

| Aspecto     | Modo Agent | Copilot Chat | Manual |
| ----------- | ---------- | ------------ | ------ |
| Velocidade  | ⚡⚡⚡⚡⚡ | ⚡⚡⚡⚡ | ⚡ |
| Qualidade   | 4/5 — bom, com revisão | 3/5 — requer mais iteração | 4/5 — depende do desenvolvedor |
| Controle    | Menor — Agent decide a estrutura | Médio — sugestão aceita/recusada manualmente | Total |
| Aprendizado | Alto — o Agent mostra padrões que a equipe pode aprender | Alto — vê as sugestões e decide | Padrão |
| Quando usar | Features completas com spec clara + contexto AGENTS.md | Dúvidas pontuais, refactoring cirúrgico | Código sensível, lógica de negócio crítica |

---

## 10. Comentários livres

> Espaço para qualquer observação adicional sobre a experiência com IA generativa no desenvolvimento:

O maior aprendizado deste workshop foi que **o Agent é tão bom quanto o contexto que você entrega**. A arqueologia do legado (Estágio 1) não foi opcional — foi a base que permitiu ao Agent entender CALCBENF.NSN e gerar um `BenefitCalculator.java` correto. Times que pulam a arqueologia para "ir direto ao código" vão receber código tecnicamente correto, mas funcionalmente errado.

O `AGENTS.md` funcionou como um contrato explícito com o Agent. Cada seção (bounded contexts, convenções, padrões de teste) reduziu o número de iterações necessárias para chegar em código aprovável. Manter esse arquivo atualizado é responsabilidade do Tech Lead — não é documentação, é infraestrutura de contexto.


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

