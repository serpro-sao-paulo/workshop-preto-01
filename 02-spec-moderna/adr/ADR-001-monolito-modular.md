<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-001: Monólito Modular como topologia do sisdnit 2.0

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![AUTOR Par 2 · Enterprise Architect](https://img.shields.io/badge/AUTOR-Par%202%20·%20Enterprise%20Architect-1A1A1A?style=for-the-badge)

## Status

Aceita

## Data

2026-05-19

## Contexto

O sisdnit legado é um conjunto de programas Natural/Adabas fortemente acoplados (ex.: tabela de 27 fatores regionais duplicada entre `BATCHPGT` e `CALCBENF` — MYS-008; ordenação por CPF com consumidores downstream desconhecidos — MYS-006). Identificamos 4 bounded contexts (Beneficiários, Pagamentos, Conciliação, Auditoria) com forte coesão transacional, especialmente entre cálculo e geração de pagamento. O time precisa entregar valor em um workshop de horas e a equipe é pequena. Precisamos decidir a topologia de implantação do sisdnit 2.0.

## Opções Consideradas

### Opção 1: Microserviços (um serviço por bounded context)

- **Prós:** Escala independente; isolamento de falhas; deploys independentes.
- **Contras:** Overhead operacional alto (4+ serviços, rede, observabilidade distribuída); transações distribuídas entre Pagamento↔Beneficiário difíceis; over-engineering para o tamanho do time e a maturidade atual; risco de não fechar no Estágio 3.

### Opção 2: Monólito Modular (módulos com fronteiras explícitas em um único deployable)

- **Prós:** Fronteiras de contexto preservadas em código (pacotes/módulos) sem custo de rede; transações locais simples (idempotência BR-020 fácil); deploy único; refatoração para microserviços possível depois. Alinhado ao C4 L2.
- **Contras:** Escala em bloco; disciplina de fronteiras depende de revisão de código; um build maior.

### Opção 3: Manter batch monolítico legado encapsulado (strangler sem modularização)

- **Prós:** Menor esforço inicial.
- **Contras:** Perpetua o acoplamento oculto (MYS-006/008); não resolve os mistérios; dívida técnica mantida.

## Decisão

Adotamos o **Monólito Modular** (Opção 2): backend Java 21 + Spring Boot com um módulo por bounded context (`beneficiario`, `pagamento`, `conciliacao`, `auditoria`) e um shared kernel para validação de CPF (BR-003). As fronteiras dos módulos espelham `bounded-contexts.md`; comunicação entre módulos via interfaces de aplicação, não acesso direto a tabelas de outro módulo. Mantém caminho de evolução para microserviços se a escala exigir.

## Consequências

### Positivas

- Idempotência e consistência (BR-020, BR-030) ficam em transações locais simples.
- Fronteiras explícitas resolvem a duplicação de regras (MYS-008) com fonte única (`TabelaFatorRegional`).
- Entrega viável no tempo do workshop.

### Negativas

- Escala é do deployable inteiro, não por contexto.
- Exige disciplina de revisão para impedir vazamento entre módulos.

## Requisitos Relacionados

- REQ-PAY-001..008, REQ-REC-001..003, REQ-BEN-001 (todos os contextos do C4 L2/L3)

---

**DoD:** Formato MADR, 3 opções com prós/contras, decisão datada.
