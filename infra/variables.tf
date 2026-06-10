# =============================================================================
# infra/variables.tf — SIFAP 2.0
# =============================================================================

variable "environment" {
  description = "Deployment environment: dev | stage | prod"
  type        = string
  validation {
    condition     = contains(["dev", "stage", "prod"], var.environment)
    error_message = "environment must be dev, stage, or prod."
  }
}

variable "location" {
  description = "Azure region. Default: Brazil South (closest to MDAS datacenters)."
  type        = string
  default     = "brazilsouth"
}

variable "owner_team" {
  description = "Team that owns this deployment (for cost allocation tags)."
  type        = string
  default     = "Par5-Operacoes"
}

variable "cost_center" {
  description = "Cost center code for billing tags."
  type        = string
  default     = "CGPB-SIFAP"
}

# Networking
variable "vnet_address_space" {
  description = "VNet CIDR block."
  type        = string
  default     = "10.10.0.0/16"
}

variable "app_subnet_cidr" {
  description = "Subnet CIDR for App Service VNet integration."
  type        = string
  default     = "10.10.1.0/24"
}

variable "db_subnet_cidr" {
  description = "Subnet CIDR for PostgreSQL private endpoint."
  type        = string
  default     = "10.10.2.0/24"
}

# Database
variable "postgres_sku" {
  description = "PostgreSQL Flexible Server SKU."
  type        = string
  default     = "GP_Standard_D2s_v3"
}

variable "postgres_storage_mb" {
  description = "PostgreSQL storage in MB."
  type        = number
  default     = 32768
}

variable "postgres_backup_days" {
  description = "PostgreSQL backup retention days (7-35)."
  type        = number
  default     = 14
  validation {
    condition     = var.postgres_backup_days >= 7 && var.postgres_backup_days <= 35
    error_message = "backup_retention_days must be between 7 and 35."
  }
}

# Container Registry
variable "acr_sku" {
  description = "Azure Container Registry SKU: Basic | Standard | Premium."
  type        = string
  default     = "Standard"
  validation {
    condition     = contains(["Basic", "Standard", "Premium"], var.acr_sku)
    error_message = "acr_sku must be Basic, Standard, or Premium."
  }
}

# Compute
variable "app_service_sku" {
  description = "App Service Plan SKU name."
  type        = string
  default     = "P1v3"
}

# Monitoring
variable "log_retention_days" {
  description = "Log Analytics workspace retention (30-730)."
  type        = number
  default     = 90
}

# Auth
variable "jwt_issuer_uri" {
  description = "OAuth2 JWT issuer URI (Azure AD tenant endpoint)."
  type        = string
  # No default — must be set per environment
}
