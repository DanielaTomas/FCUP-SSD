variable "gcp_credentials_path" {
  type    = string
  description = "path to file containing json credetials"
}

variable "gcp_project_id" {
  type    = string
  default = "ssd-project-418411"
}

variable "gcp_region" {
  type    = string
  default = "europe-west1-d"
}

variable "gcp_default_machine_type" {
  type    = string
  default = "e2-medium"
}

variable "gcp_default_machine_image"{
  type    = string
  description = "Default OS image for the VMs"
  default = "ubuntu-os-cloud/ubuntu-2004-lts"
}

variable "default_data_disk_type" {
  type    = string
  default = "pd-standard"
}