variable "prefix"              { type = string }
variable "location"            { type = string }
variable "resource_group_name" { type = string }
variable "tags"                { type = map(string) }
variable "vnet_address_space"  { type = string }
variable "app_subnet_cidr"     { type = string }
variable "db_subnet_cidr"      { type = string }

output "app_subnet_id"       { value = azurerm_subnet.app.id }
output "db_subnet_id"        { value = azurerm_subnet.db.id }
output "postgres_dns_zone_id" { value = azurerm_private_dns_zone_virtual_network_link.postgres.id }
output "vnet_id"             { value = azurerm_virtual_network.this.id }
