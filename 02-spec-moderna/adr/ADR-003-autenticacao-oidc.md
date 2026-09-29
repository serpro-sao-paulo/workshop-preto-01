<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-003: Autenticação e autorização via OAuth2/OIDC + RBAC

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![AUTOR Par 2 · Enterprise Architect](https://img.shields.io/badge/AUTOR-Par%202%20·%20Enterprise%20Architect-1A1A1A?style=for-the-badge)

## Status

Aceita

## Data

2026-05-19

## Contexto

O acesso ao sisdnit legado é feito por terminais 3270 autenticados via RACF, sem perfis de autorização granulares no nível da aplicação e sem trilha moderna de quem disparou operações sensíveis (ex.: geração da folha — REQ-PAY-001, conciliação — REQ-REC-002). A decisão de escopo marcou a Gestão de Usuários como **Evoluir** e registrou autenticação moderna como requisito greenfield (N1). Precisamos de um mecanismo de autenticação/autorização para a nova UI web (Next.js) e a API (Spring Boot) que suporte auditoria e segregação de funções.

## Opções Consideradas

### Opção 1: OAuth2/OIDC com provedor de identidade externo (IdP) + RBAC na API

- **Prós:** Padrão de mercado; SSO e MFA delegados ao IdP; tokens JWT auditáveis; perfis (operador, gestor, auditor) via claims; Spring Security integra nativamente; segrega funções sensíveis (quem gera folha ≠ quem concilia).
- **Contras:** Dependência de um IdP; complexidade inicial de configuração; necessidade de gestão de papéis/claims.

### Opção 2: Autenticação própria com usuários/senhas no banco

- **Prós:** Sem dependência externa; controle total.
- **Contras:** Reinventa segurança (hashing, rotação, MFA, lockout); maior superfície de risco (OWASP A07); manutenção e auditoria caras; antipadrão para greenfield.

### Opção 3: Manter RACF/3270 via gateway

- **Prós:** Reaproveita identidades existentes.
- **Contras:** Mantém acoplamento ao mainframe; não oferece RBAC granular na aplicação; contraria a decisão de evoluir a gestão de usuários.

## Decisão

Adotamos **OAuth2/OIDC com IdP externo + RBAC na API** (Opção 1). A UI Next.js e a API Spring Boot validam tokens OIDC; autorização por papéis (`operador`, `gestor`, `auditor`, `admin`) controla operações sensíveis. Operações críticas (geração de folha, conciliação, alteração de status) exigem papel específico e geram evento de auditoria (BR-030 / contexto Auditoria).

## Consequências

### Positivas

- Segurança delegada a um IdP maduro (MFA, rotação, lockout) — reduz risco OWASP.
- Segregação de funções e trilha de auditoria para operações financeiras.
- Tokens stateless facilitam escala do monólito modular.

### Negativas

- Introduz dependência operacional do IdP.
- Requer modelagem e governança de papéis/claims desde o início.

## Requisitos Relacionados

- REQ-ADM-001 (autenticação greenfield), REQ-PAY-001 / REQ-REC-002 (operações sensíveis autorizadas)

---

**DoD:** Formato MADR, 3 opções com prós/contras, decisão datada.
