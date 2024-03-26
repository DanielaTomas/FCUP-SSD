# main.tf - node

resource "google_compute_address" "node_reserved_external_ip" {
  count  = var.node_instance_count
  name   = "node-reserved-external-ip-${count.index}"
  region = var.node_pop_region
}

resource "google_compute_instance" "node_instance" {
  count = var.node_instance_count
  name         = "node-${count.index}"
  machine_type = var.gcp_default_machine_type
  zone  = "${var.node_pop_region}${var.node_pop_zone}"

  metadata_startup_script = file("${path.module}/cloud-init.sh")

  tags = ["node-ports"]


boot_disk {
        initialize_params {
                image = var.gcp_default_machine_image
        }
}
        network_interface {
                network = "default"
                access_config {
                        nat_ip = google_compute_address.node_reserved_external_ip[count.index].address
                }

                
        }
}