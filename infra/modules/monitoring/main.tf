# =============================================================================
# infra/modules/monitoring/main.tf — Application Insights + Log Analytics
# =============================================================================

resource "azurerm_log_analytics_workspace" "this" {
  name                = "${var.prefix}-law"
  location            = var.location
  resource_group_name = var.resource_group_name
  sku                 = "PerGB2018"
  retention_in_days   = var.retention_days
  tags                = var.tags
}

resource "azurerm_application_insights" "this" {
  name                = "${var.prefix}-appi"
  location            = var.location
  resource_group_name = var.resource_group_name
  workspace_id        = azurerm_log_analytics_workspace.this.id
  application_type    = "java"
  tags                = var.tags
}

# Alert: 5xx error rate > 5% over 5 minutes
resource "azurerm_monitor_metric_alert" "http_5xx" {
  name                = "${var.prefix}-alert-5xx"
  resource_group_name = var.resource_group_name
  scopes              = [var.webapp_id]
  description         = "HTTP 5xx error rate exceeded 5% — SIFAP backend"
  severity            = 2
  frequency           = "PT5M"
  window_size         = "PT5M"
  tags                = var.tags

  criteria {
    metric_namespace = "Microsoft.Web/sites"
    metric_name      = "Http5xx"
    aggregation      = "Total"
    operator         = "GreaterThan"
    threshold        = var.http5xx_threshold
  }

  action {
    action_group_id = var.action_group_id
  }
}
