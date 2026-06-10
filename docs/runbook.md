<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Runbook

![DOC Runbook](https://img.shields.io/badge/DOC-Runbook-00A4EF?style=for-the-badge) ![DONO DevOps](https://img.shields.io/badge/DONO-DevOps-1A1A1A?style=for-the-badge) ![USE Quando subir/derrubar](https://img.shields.io/badge/USE-Quando%20subir/derrubar-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Docs](README.md) → **Runbook**

> **Para quem é isto?** Para o time durante o workshop e quem opera o ambiente local + CI/CD.
>
> **O que você terá ao final desta leitura:**
>
> 1. Comandos para subir/derrubar ambiente local
> 2. Como ler status do CI
> 3. O que fazer quando algo falha


> O que fazer quando algo roda (ou não roda). O DevOps Engineer é responsável por este arquivo.

## Local — primeira vez

O backend Java e o banco de dados estão prontos para rodar. O repositório inclui:

- `04-sifap-backend/` — Spring Boot 3.3 + Java 21
- `docker-compose.yml` — PostgreSQL 16 + backend (multi-stage Dockerfile)

```bash
git checkout develop && git pull
cd workshop-preto-01

# Sobe banco + backend (primeira build leva ~5 min — Maven baixa dependências)
docker compose up -d

# Acompanhar logs até aparecer "Started SifapApplication"
docker compose logs -f backend
```

Depois que o backend estiver saudável:

- Backend health: <http://localhost:8080/actuator/health>
- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8080/api-docs>

## Local — diariamente

```bash
git checkout develop && git pull # atualize antes de começar
docker compose up -d # se a aplicação já existir e estiver parada
git status # confira o que vai commitar
```

## CI

Acionado automaticamente em push para `main`, `develop`, `spec/**`, `impl/**`.

| Fluxo de trabalho  | O que faz                                                                  | Quando                            |
| ------------------ | -------------------------------------------------------------------------- | --------------------------------- |
| `ci.yml`           | Backend `mvn verify`, frontend lint+test+typecheck, Terraform fmt+validate | Todo push e PR                    |
| `spec-quality.yml` | markdownlint + rastreabilidade de REQ-ID                                   | Quando `**.md` ou `specs/` mudam |

Verifique execuções com falha na aba Actions. Reproduza localmente rodando os
mesmos comandos do `ci.yml` (por exemplo `mvn verify`, lint do frontend).

## Azure — Terraform (Estágio 4)

A infraestrutura está descrita em `infra/` com módulos para networking, database, compute, registry, keyvault e monitoring.

**Validar localmente (sem aplicar):**

```bash
cd infra
terraform init -backend=false
terraform fmt -check -recursive
terraform validate
```

**Aplicar em dev (requer credenciais Azure configuradas):**

```bash
terraform init
terraform plan -var-file=envs/dev.tfvars -out=plan.tfplan
terraform apply plan.tfplan
```

> O CI nunca executa `terraform apply` automaticamente. O `plan` é gerado como comentário no PR para revisão antes de qualquer mudança na infraestrutura.

## Problemas comuns

| Sintoma | Causa provável | Correção |
|---------|---------------|---------|
| `docker compose up` trava na etapa `deps 6/6` | Maven baixando dependências pela primeira vez (~200 MB) | Aguardar — processo normal. Próximas builds usam cache |
| `docker compose up` falha com porta em uso | Porta 5432 ou 8080 já ocupada | `netstat -ano \| findstr :5432` (Windows) e encerre o processo |
| `mvn verify` falha em `*IT` com "Docker not found" | Docker Desktop não está em execução | Iniciar o Docker Desktop antes de rodar os testes |
| Backend sobe mas retorna 401 em todos os endpoints | OAuth2 ativo sem IdP local | Verificar variável `SPRING_AUTOCONFIGURE_EXCLUDE` no `docker-compose.yml` |
| `Flyway migration checksum mismatch` | Arquivo de migration editado após aplicação | Nunca editar migrations já aplicadas. Criar nova versão `V6__...` |
| `ArchUnit test failed` | Importação cross-context entre bounded contexts | Remover import do pacote `domain/` ou `infrastructure/` de outro contexto |
| `JaCoCo coverage gate failed` | Cobertura < 70% em `domain` ou `application` | Adicionar testes unitários para o código novo |
| `terraform plan` falha | `terraform init` não rodou | Executar `terraform init -backend=false` antes do `validate` |
| GitHub Actions não consegue acessar Azure | OIDC federated identity não configurada | Configurar `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID` como secrets |

## Quando escalar para uma pessoa facilitadora

- Build ainda falhando após 20 minutos de depuração.
- A assinatura Azure parece suspensa.
- Qualquer coisa irreversível (por exemplo, `terraform destroy` executado por engano).

Use o formato de escalonamento de 3 linhas de [00-TEAM-FLOW.md §4](../00-TEAM-FLOW.md).

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="FAQ.md"><strong>FAQ</strong></a><br/>
<sub>Perguntas frequentes.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="troubleshooting.md"><strong>Troubleshooting</strong></a><br/>
<sub>Erros comuns e soluções.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

