# =============================================================================
# infra/modules/compute/main.tf — App Service (container) + Managed Identity
# =============================================================================

resource "azurerm_service_plan" "this" {
  name                = "${var.prefix}-asp"
  location            = var.location
  resource_group_name = var.resource_group_name
  os_type             = "Linux"
  sku_name            = var.sku_name
  tags                = var.tags
}

# System-assigned Managed Identity — used for ACR pull + Key Vault read
resource "azurerm_linux_web_app" "this" {
  name                      = "${var.prefix}-app"
  location                  = var.location
  resource_group_name       = var.resource_group_name
  service_plan_id           = azurerm_service_plan.this.id
  https_only                = true
  virtual_network_subnet_id = var.app_subnet_id
  tags                      = var.tags

  identity {
    type = "SystemAssigned"
  }

  site_config {
    always_on        = true
    http2_enabled    = true
    ftps_state       = "Disabled"   # FTP disabled — OWASP A05
    minimum_tls_version = "1.2"

    application_stack {
      docker_image_name        = "${var.image_name}:latest"
      docker_registry_url      = "https://${var.acr_login_server}"
      # ACR auth via Managed Identity — no username/password
    }

    health_check_path                 = "/actuator/health"
    health_check_eviction_time_in_min = 5
  }

  app_settings = merge(var.app_settings, {
    "APPINSIGHTS_INSTRUMENTATIONKEY"        = var.app_insights_key
    "WEBSITES_PORT"                          = "8080"
    "DOCKER_ENABLE_CI"                       = "false"
  })

  logs {
    http_logs {
      retention_in_days = 30
    }
    application_logs {
      file_system_level = "Warning"
    }
  }

  lifecycle {
    # Prevent accidental destruction in production
    prevent_destroy = false  # Set to true after first successful deploy
  }
}

# Allow App Service Managed Identity to pull images from ACR
resource "azurerm_role_assignment" "acr_pull" {
  scope                = var.acr_resource_id
  role_definition_name = "AcrPull"
  principal_id         = azurerm_linux_web_app.this.identity[0].principal_id
}
