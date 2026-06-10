# =============================================================================
# infra/modules/keyvault/main.tf — Azure Key Vault
# =============================================================================
# All application secrets live here. App Service accesses them via
# Managed Identity + Key Vault References (@Microsoft.KeyVault(SecretUri=...)).
# No plaintext secrets in Terraform state or variables — iac-review.SKILL.md.
# =============================================================================

data "azurerm_client_config" "current" {}

resource "azurerm_key_vault" "this" {
  name                       = "${var.prefix}-kv"
  location                   = var.location
  resource_group_name        = var.resource_group_name
  tenant_id                  = data.azurerm_client_config.current.tenant_id
  sku_name                   = "standard"
  soft_delete_retention_days = 90
  purge_protection_enabled   = true   # Cannot be disabled once set — intentional
  enable_rbac_authorization  = true   # RBAC over access policies (modern approach)
  tags                       = var.tags

  network_acls {
    default_action = "Deny"
    bypass         = "AzureServices"
  }

  lifecycle {
    prevent_destroy = true
  }
}

# Grant App Service Managed Identity read access to secrets
resource "azurerm_role_assignment" "webapp_kv_reader" {
  scope                = azurerm_key_vault.this.id
  role_definition_name = "Key Vault Secrets User"
  principal_id         = var.webapp_principal_id
}

# Grant Terraform pipeline (OIDC) officer access to create secrets
resource "azurerm_role_assignment" "terraform_kv_officer" {
  scope                = azurerm_key_vault.this.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = data.azurerm_client_config.current.object_id
}

# Postgres admin password — value set externally via `az keyvault secret set`
# Terraform only creates the placeholder; rotation is handled by Key Vault policies.
# IMPORTANT: never put actual passwords in Terraform code or tfvars.
data "azurerm_key_vault_secret" "postgres_admin_password" {
  name         = "postgres-admin-password"
  key_vault_id = azurerm_key_vault.this.id
  depends_on   = [azurerm_role_assignment.terraform_kv_officer]
}
