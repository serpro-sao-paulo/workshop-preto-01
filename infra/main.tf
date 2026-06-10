# =============================================================================
# infra/main.tf — SIFAP 2.0 — Azure Infrastructure (Terraform)
# =============================================================================
# Module layout (one module per Azure service area — IaC convention):
#   networking  → VNet, subnets, NSGs
#   database    → Azure Database for PostgreSQL Flexible Server
#   compute     → Azure App Service Plan + Web App (container)
#   registry    → Azure Container Registry
#   monitoring  → Application Insights + Log Analytics Workspace
#   keyvault    → Azure Key Vault (secrets via Managed Identity — no plaintext)
#
# IaC conventions (infrastructure.instructions.md):
#   Naming: {project}-{env}-{resource}-{region}
#   Tags:   environment, project, owner, cost-center
#   Secrets: ONLY via Azure Key Vault — never in locals/variables/tfvars
# =============================================================================

terraform {
  required_version = ">= 1.9"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.116"  # pinned to minor — iac-review.SKILL.md
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "~> 2.53"
    }
  }

  # Remote state with blob lease locking — never committed to git
  backend "azurerm" {
    resource_group_name  = "sifap-tfstate-rg"
    storage_account_name = "sifaptfstate"
    container_name       = "tfstate"
    key                  = "sifap.tfstate"
    use_oidc             = true  # OIDC — no service principal secret
  }
}

provider "azurerm" {
  features {
    key_vault {
      purge_soft_delete_on_destroy    = false
      recover_soft_deleted_key_vaults = true
    }
    resource_group {
      prevent_deletion_if_contains_resources = true
    }
  }
  use_oidc = true
}

# =============================================================================
# Locals — naming convention enforcement
# =============================================================================
locals {
  project  = "sifap"
  env      = var.environment
  region   = var.location
  # Short region codes for naming
  region_short = {
    "brazilsouth"  = "brs"
    "eastus2"      = "eus2"
    "westeurope"   = "weu"
  }
  loc_code = lookup(local.region_short, var.location, "unk")

  # Name prefix: sifap-<env>-<loc>
  prefix = "${local.project}-${local.env}-${local.loc_code}"

  common_tags = {
    environment  = var.environment
    project      = "SIFAP 2.0"
    owner        = var.owner_team
    cost-center  = var.cost_center
    managed-by   = "terraform"
    repository   = "github.com/org/workshop-preto-01"
  }
}

# =============================================================================
# Resource Group
# =============================================================================
resource "azurerm_resource_group" "main" {
  name     = "${local.prefix}-rg"
  location = var.location
  tags     = local.common_tags
}

# =============================================================================
# Modules
# =============================================================================
module "networking" {
  source              = "./modules/networking"
  prefix              = local.prefix
  location            = var.location
  resource_group_name = azurerm_resource_group.main.name
  tags                = local.common_tags
  vnet_address_space  = var.vnet_address_space
  app_subnet_cidr     = var.app_subnet_cidr
  db_subnet_cidr      = var.db_subnet_cidr
}

module "keyvault" {
  source              = "./modules/keyvault"
  prefix              = local.prefix
  location            = var.location
  resource_group_name = azurerm_resource_group.main.name
  tags                = local.common_tags
  # App Service Managed Identity will be granted GET/LIST on secrets
  webapp_principal_id = module.compute.webapp_principal_id
}

module "database" {
  source              = "./modules/database"
  prefix              = local.prefix
  location            = var.location
  resource_group_name = azurerm_resource_group.main.name
  tags                = local.common_tags
  db_subnet_id        = module.networking.db_subnet_id
  private_dns_zone_id = module.networking.postgres_dns_zone_id
  # Admin password fetched from Key Vault — NOT from variable
  admin_password_kv_id = module.keyvault.postgres_admin_password_secret_id
  sku_name             = var.postgres_sku
  storage_mb           = var.postgres_storage_mb
  backup_retention_days = var.postgres_backup_days
}

module "registry" {
  source              = "./modules/registry"
  prefix              = local.prefix
  location            = var.location
  resource_group_name = azurerm_resource_group.main.name
  tags                = local.common_tags
  sku                 = var.acr_sku
  # App Service Managed Identity gets AcrPull role
  webapp_principal_id = module.compute.webapp_principal_id
}

module "monitoring" {
  source              = "./modules/monitoring"
  prefix              = local.prefix
  location            = var.location
  resource_group_name = azurerm_resource_group.main.name
  tags                = local.common_tags
  retention_days      = var.log_retention_days
}

module "compute" {
  source              = "./modules/compute"
  prefix              = local.prefix
  location            = var.location
  resource_group_name = azurerm_resource_group.main.name
  tags                = local.common_tags
  app_subnet_id       = module.networking.app_subnet_id
  acr_login_server    = module.registry.login_server
  image_name          = "sifap-backend"
  app_insights_key    = module.monitoring.instrumentation_key
  sku_name            = var.app_service_sku

  # Environment variables injected via App Settings — secrets from Key Vault refs
  app_settings = {
    "SPRING_PROFILES_ACTIVE"                                 = var.environment
    "SPRING_DATASOURCE_URL"                                  = "jdbc:postgresql://${module.database.fqdn}:5432/sifap?sslmode=require"
    "SPRING_DATASOURCE_USERNAME"                             = "sifap_app"
    # Secrets via Key Vault reference — never plaintext
    "SPRING_DATASOURCE_PASSWORD"                             = "@Microsoft.KeyVault(SecretUri=${module.keyvault.db_password_secret_uri})"
    "SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI"  = var.jwt_issuer_uri
    "APPLICATIONINSIGHTS_CONNECTION_STRING"                  = module.monitoring.connection_string
    "JAVA_OPTS"                                              = "-XX:+UseG1GC -XX:MaxRAMPercentage=75"
  }
}
