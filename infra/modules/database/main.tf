# =============================================================================
# infra/modules/database/main.tf — PostgreSQL Flexible Server
# =============================================================================
# - Private endpoint only (no public access)
# - Admin password stored in Key Vault (never in tfvars)
# - prevent_destroy = true — database must not be accidentally deleted
# - Geo-redundant backup for prod
# =============================================================================

data "azurerm_key_vault_secret" "admin_password" {
  name         = "postgres-admin-password"
  key_vault_id = var.key_vault_id
}

resource "azurerm_postgresql_flexible_server" "this" {
  name                   = "${var.prefix}-pgsql"
  location               = var.location
  resource_group_name    = var.resource_group_name
  administrator_login    = "sifap_admin"
  administrator_password = data.azurerm_key_vault_secret.admin_password.value
  sku_name               = var.sku_name
  version                = "16"
  storage_mb             = var.storage_mb
  backup_retention_days  = var.backup_retention_days
  geo_redundant_backup_enabled = var.geo_redundant_backup
  zone                   = "1"

  # Private endpoint — no public access (OWASP A05)
  delegated_subnet_id    = var.db_subnet_id
  private_dns_zone_id    = var.private_dns_zone_id

  high_availability {
    mode = var.high_availability_mode  # "ZoneRedundant" for prod
  }

  maintenance_window {
    day_of_week  = 0  # Sunday
    start_hour   = 2
    start_minute = 0
  }

  tags = var.tags

  lifecycle {
    prevent_destroy = true  # Protect production data — iac-review.SKILL.md
    ignore_changes  = [
      # Ignore password changes after initial creation; rotated via Key Vault
      administrator_password
    ]
  }
}

# Create the application database
resource "azurerm_postgresql_flexible_server_database" "sifap" {
  name      = "sifap"
  server_id = azurerm_postgresql_flexible_server.this.id
  collation = "pt_BR.utf8"
  charset   = "utf8"

  lifecycle {
    prevent_destroy = true
  }
}

# Firewall rule: block all public access (private endpoint handles connectivity)
resource "azurerm_postgresql_flexible_server_firewall_rule" "deny_public" {
  name             = "deny-all-public"
  server_id        = azurerm_postgresql_flexible_server.this.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}
