# stage.tfvars — SIFAP 2.0 stage environment
environment         = "stage"
location            = "brazilsouth"
owner_team          = "Par5-Operacoes"
cost_center         = "CGPB-SIFAP"

postgres_sku         = "GP_Standard_D2s_v3"
postgres_storage_mb  = 65536
postgres_backup_days = 14
geo_redundant_backup = false
acr_sku              = "Standard"
app_service_sku      = "P1v3"
log_retention_days   = 90

jwt_issuer_uri = "https://login.microsoftonline.com/<TENANT_ID>/v2.0"
