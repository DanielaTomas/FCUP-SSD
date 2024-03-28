# variables.tf - bootstrap node

variable "gcp_default_machine_type" {
  type    = string
}

variable "gcp_default_machine_image"{
  type    = string
  description = "Default OS image for the VMs"
}

variable "bootstrap_node_instance_count" {
  type    = string
  default = "3"
}

variable "bootstrap_node_pop_region" {
  type = string
  default = "europe-west1"
}

variable "bootstrap_node_pop_zone" {
  type = string
  default = "-c"
}

variable "kademlia_jar_path" {
    type    = string
  description = "path to file containing kademlia executable"
}

