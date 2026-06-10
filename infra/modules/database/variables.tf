variable "prefix"                 { type = string }
variable "location"               { type = string }
variable "resource_group_name"    { type = string }
variable "tags"                   { type = map(string) }
variable "db_subnet_id"           { type = string }
variable "private_dns_zone_id"    { type = string }
variable "key_vault_id"           { type = string }
variable "sku_name"               { type = string; default = "GP_Standard_D2s_v3" }
variable "storage_mb"             { type = number; default = 32768 }
variable "backup_retention_days"  { type = number; default = 14 }
variable "geo_redundant_backup"   { type = bool;   default = false }
variable "high_availability_mode" { type = string; default = "Disabled" }

output "fqdn"      { value = azurerm_postgresql_flexible_server.this.fqdn }
output "server_id" { value = azurerm_postgresql_flexible_server.this.id }
