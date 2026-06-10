variable "prefix"              { type = string }
variable "location"            { type = string }
variable "resource_group_name" { type = string }
variable "tags"                { type = map(string) }
variable "webapp_principal_id" { type = string }
variable "sku"                 { type = string; default = "Standard" }
variable "georeplications"     { type = list(string); default = [] }

output "login_server" { value = azurerm_container_registry.this.login_server }
output "acr_id"       { value = azurerm_container_registry.this.id }
