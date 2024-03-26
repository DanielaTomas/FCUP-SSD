# variables.tf - node

variable "gcp_default_machine_type" {
  type    = string
}

variable "gcp_default_machine_image"{
  type    = string
  description = "Default OS image for the VMs"
}

variable "node_instance_count" {
  type    = string
  default = "10"
}

variable "node_pop_region" {
  type = string
  default = "europe-west1"
}

variable "node_pop_zone" {
  type = string
  default = "-c"
}