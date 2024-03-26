provider "google" {

  credentials = file(var.gcp_credentials_path)
  project     = var.gcp_project_id

}

resource "google_compute_firewall" "node-ports" {
  name    = "node-ports"
  network = "default"

  allow {
    protocol = "udp"
    ports    = ["21391"]  
  }

  allow {
    protocol = "tcp"
    ports    = ["21391"]  
  }

  source_ranges = ["0.0.0.0/0"] 

  target_tags = ["node-ports"]
}

module "node" {
  source               = "./modules/node"


  gcp_default_machine_type = var.gcp_default_machine_type
  gcp_default_machine_image = var.gcp_default_machine_image
}
