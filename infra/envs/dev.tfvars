# dev.tfvars — SIFAP 2.0 dev environment
# All secrets are in Azure Key Vault — not here.
environment         = "dev"
location            = "brazilsouth"
owner_team          = "Par5-Operacoes"
cost_center         = "CGPB-SIFAP"

# Smaller SKUs for dev (cost control)
postgres_sku        = "B_Standard_B2s"
postgres_storage_mb = 32768
postgres_backup_days = 7
acr_sku             = "Basic"
app_service_sku     = "B2"
log_retention_days  = 30

jwt_issuer_uri = "https://login.microsoftonline.com/<TENANT_ID>/v2.0"
