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
  default = "5"
}

variable "node_pop_region" {
  type = string
  default = "europe-west1"
}

variable "node_pop_zone" {
  type = string
  default = "-d"
}

variable "kademlia_jar_path" {
  type    = string
  description = "path to file containing kademlia executable"
}

variable "bootstrap_ips" {
  type = list(string)
  description = "A list containing the IPs of the original bootstrap nodes of the network"
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