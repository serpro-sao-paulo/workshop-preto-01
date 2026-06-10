variable "prefix"              { type = string }
variable "location"            { type = string }
variable "resource_group_name" { type = string }
variable "tags"                { type = map(string) }
variable "webapp_principal_id" { type = string }

output "vault_uri"                         { value = azurerm_key_vault.this.vault_uri }
output "key_vault_id"                      { value = azurerm_key_vault.this.id }
output "postgres_admin_password_secret_id" { value = data.azurerm_key_vault_secret.postgres_admin_password.id }
output "db_password_secret_uri"            { value = data.azurerm_key_vault_secret.postgres_admin_password.versionless_id }
