# =============================================================================
# infra/outputs.tf — SIFAP 2.0
# =============================================================================

output "webapp_url" {
  description = "URL of the deployed App Service."
  value       = "https://${module.compute.webapp_default_hostname}"
}

output "acr_login_server" {
  description = "ACR login server hostname — used by CI to push images."
  value       = module.registry.login_server
}

output "postgres_fqdn" {
  description = "PostgreSQL Flexible Server FQDN — for connection string assembly."
  value       = module.database.fqdn
  sensitive   = false  # FQDN is not a secret; password is in Key Vault
}

output "key_vault_uri" {
  description = "Key Vault URI — for manual secret retrieval if needed."
  value       = module.keyvault.vault_uri
}

output "app_insights_connection_string" {
  description = "Application Insights connection string — paste in monitoring dashboards."
  value       = module.monitoring.connection_string
  sensitive   = true
}
