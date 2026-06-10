# Referência de endpoints — SIFAP 2.0 API

**Versão:** v1  
**Base URL:** `http://localhost:8080/api/v1` (local) · `https://sifap-dev.azurewebsites.net/api/v1` (dev)  
**Documentação interativa:** `GET /swagger-ui/index.html`  
**Spec OpenAPI:** `GET /api-docs`  
**Autenticação:** OAuth2 Bearer token (JWT). Em ambiente local o OAuth2 está desabilitado.

---

## Módulo: beneficiaries

### Cadastrar beneficiário

```
POST /api/v1/beneficiaries
```

**Corpo da requisição**

```json
{
  "cpf": "52998224725",
  "nome": "João da Silva",
  "dtNascimento": "1985-03-15",
  "codPrograma": 1,
  "rendaFamiliar": 450.00,
  "numDependentes": 2,
  "codRegiao": 11,
  "uf": "SP"
}
```

| Campo | Tipo | Obrigatório | Regra |
|-------|------|------------|-------|
| `cpf` | string (11 dígitos) | Sim | Módulo 11; não aceita dígitos iguais nem prefixos de teste (REQ-BEN-001, REQ-BEN-004) |
| `nome` | string | Sim | |
| `dtNascimento` | date `YYYY-MM-DD` | Sim | |
| `codPrograma` | integer | Sim | Deve existir em `prg_social_program` |
| `rendaFamiliar` | decimal | Não | Default 0 |
| `numDependentes` | integer | Não | Default 0; máximo 5 (REQ-BEN-007) |
| `codRegiao` | integer | Não | 1–25 ou 99 (especial). Default 15 |
| `uf` | string (2) | Não | Sigla do estado |

**Respostas**

| Status | Situação |
|--------|---------|
| `201 Created` | Beneficiário cadastrado. Corpo: `BeneficiaryResponse` |
| `400 Bad Request` | CPF inválido ou campo obrigatório ausente |
| `409 Conflict` | CPF já cadastrado (REQ-BEN-002) |
| `422 Unprocessable Entity` | Beneficiário inelegível |

**Exemplo de resposta 201**

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "cpf": "529.982.247-25",
  "nome": "João da Silva",
  "dtNascimento": "1985-03-15",
  "status": "ACTIVE",
  "codPrograma": 1
}
```

> **Nota:** beneficiários com mais de 75 anos são criados com `status: "SUSPENDED"` automaticamente (REQ-BEN-005 — fix do MYS-001 do legado).

---

### Consultar beneficiário por ID

```
GET /api/v1/beneficiaries/{id}
```

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | UUID | ID interno gerado no cadastro |

**Respostas**

| Status | Situação |
|--------|---------|
| `200 OK` | Beneficiário encontrado. Corpo: `BeneficiaryResponse` |
| `404 Not Found` | ID não existe |

---

## Módulo: operações (health + monitoramento)

### Health check

```
GET /actuator/health
```

Retorna `{"status":"UP"}` quando o serviço e o banco de dados estão operacionais. Usado pelo Docker Compose e pelo Azure App Service para saber se o container está pronto.

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

### Informações da aplicação

```
GET /actuator/info
```

Retorna versão, nome e metadados do build.

---

## Erros padronizados (RFC 7807)

Todos os erros seguem o formato `ProblemDetail`:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "CPF inválido: ***.***.***-25",
  "instance": "/api/v1/beneficiaries"
}
```

| Status | Código de negócio | Quando ocorre |
|--------|------------------|--------------|
| `400` | `INVALID_CPF` | CPF com dígito verificador errado ou prefixo de teste |
| `400` | `VALIDATION_ERROR` | Campo obrigatório ausente ou formato inválido |
| `404` | `NOT_FOUND` | Recurso não existe |
| `409` | `DUPLICATE_CPF` | CPF já cadastrado (REQ-BEN-002) |
| `422` | `ELIGIBILITY_FAILED` | Beneficiário não atende critérios do programa (REQ-ELI-*) |

> **Segurança:** o CPF nunca aparece completo em mensagens de erro — sempre mascarado como `***.982.***-25`.

---

## Endpoints planejados (backlog)

Os endpoints abaixo estão especificados em `02-spec-moderna/SPECIFICATION.md` mas ainda não implementados:

| Endpoint | Módulo | REQ-IDs |
|----------|--------|---------|
| `POST /api/v1/payments/cycle` | payment | REQ-PAY-001 |
| `GET /api/v1/payments/{beneficiaryId}` | payment | REQ-PAY-001 |
| `POST /api/v1/beneficiaries/{id}/status` | beneficiary | REQ-BEN-005 |
| `GET /api/v1/audit/{aggregateId}` | audit | REQ-AUD-001 |
| `GET /api/v1/programs` | program | REQ-PRG-001 |
