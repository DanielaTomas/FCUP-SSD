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
  default = "ubuntu-os-cloud/ubuntu-2204-lts"
}

variable "default_data_disk_type" {
  type    = string
  default = "pd-standard"
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