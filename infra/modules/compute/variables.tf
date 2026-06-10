variable "prefix"              { type = string }
variable "location"            { type = string }
variable "resource_group_name" { type = string }
variable "tags"                { type = map(string) }
variable "app_subnet_id"       { type = string }
variable "acr_login_server"    { type = string }
variable "acr_resource_id"     { type = string; default = "" }
variable "image_name"          { type = string }
variable "app_insights_key"    { type = string }
variable "sku_name"            { type = string; default = "P1v3" }
variable "app_settings"        { type = map(string); default = {} }

output "webapp_principal_id"      { value = azurerm_linux_web_app.this.identity[0].principal_id }
output "webapp_default_hostname"  { value = azurerm_linux_web_app.this.default_hostname }
