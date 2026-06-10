# =============================================================================
# infra/modules/registry/main.tf — Azure Container Registry
# =============================================================================

resource "azurerm_container_registry" "this" {
  name                = replace("${var.prefix}acr", "-", "")  # ACR names: alphanumeric only
  location            = var.location
  resource_group_name = var.resource_group_name
  sku                 = var.sku
  admin_enabled       = false  # Use Managed Identity, never admin credentials
  tags                = var.tags

  # Geo-replication for Standard+ SKUs in prod
  dynamic "georeplications" {
    for_each = var.georeplications
    content {
      location                = georeplications.value
      zone_redundancy_enabled = true
      tags                    = var.tags
    }
  }
}

# Grant App Service Managed Identity AcrPull role
resource "azurerm_role_assignment" "webapp_acr_pull" {
  scope                = azurerm_container_registry.this.id
  role_definition_name = "AcrPull"
  principal_id         = var.webapp_principal_id
}
