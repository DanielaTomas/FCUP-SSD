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
  default = "1"
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

variable "gce_ssh_user" {
  type = string
}

variable "ssh_pub_key_path" {
  type = string
}

variable "ssh_private_key_path" {
  type = string
}