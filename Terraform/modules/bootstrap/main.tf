# main.tf - bootstrap node

resource "google_compute_address" "bootstrap_node_reserved_external_ip" {
  count  = var.bootstrap_node_instance_count
  name   = "bootstrap-node-reserved-external-ip-${count.index}"
  region = var.bootstrap_node_pop_region
}

resource "google_compute_instance" "bootstrap_node_instance" {
  count = var.bootstrap_node_instance_count
  name         = "bootstrap-node-${count.index}"
  machine_type = var.gcp_default_machine_type
  zone  = "${var.bootstrap_node_pop_region}${var.bootstrap_node_pop_zone}"

  metadata = {
    ssh-keys = "${var.gce_ssh_user}:${file(var.ssh_pub_key_path)}"
  }

  metadata_startup_script = file("${path.module}/cloud-init.sh")

  tags = ["node-ports"]

  provisioner "file" {
    source      = var.kademlia_jar_path
    destination = "~/kademlia.jar"

    connection {
      type        = "ssh"
      user        = var.gce_ssh_user
      private_key = file(var.ssh_pub_key_path)
      agent = "false"
      host = google_compute_address.bootstrap_node_reserved_external_ip[count.index].address
    }
  }



boot_disk {
        initialize_params {
                image = var.gcp_default_machine_image
        }
}
        network_interface {
                network = "default"
                access_config {
                        nat_ip = google_compute_address.bootstrap_node_reserved_external_ip[count.index].address
                }

                
        }
}

output "bootstrap_node_ips" {
  value = google_compute_instance.bootstrap_node_instance[*].network_interface.0.access_config.0.nat_ip
}