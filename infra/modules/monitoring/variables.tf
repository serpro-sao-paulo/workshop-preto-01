variable "prefix"              { type = string }
variable "location"            { type = string }
variable "resource_group_name" { type = string }
variable "tags"                { type = map(string) }
variable "retention_days"      { type = number; default = 90 }
variable "webapp_id"           { type = string; default = "" }
variable "action_group_id"     { type = string; default = "" }
variable "http5xx_threshold"   { type = number; default = 10 }

output "instrumentation_key" { value = azurerm_application_insights.this.instrumentation_key; sensitive = true }
output "connection_string"   { value = azurerm_application_insights.this.connection_string;   sensitive = true }
output "workspace_id"        { value = azurerm_log_analytics_workspace.this.id }
