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

  metadata = {
    ssh-keys = "${var.gce_ssh_user}:${file(var.ssh_pub_key_path)}"
  }

  metadata_startup_script = templatefile("${path.module}/cloud-init.sh", {
        bootstrap_ip = var.bootstrap_ips[0]
  })
  

  tags = ["node-ports","default-allow-ssh"]

  provisioner "file" {
    source      = var.kademlia_jar_path
    destination = "/home/${var.gce_ssh_user}/kademlia.jar"

    connection {
      type        = "ssh"
      user        = var.gce_ssh_user
      private_key = file(var.ssh_private_key_path)
      agent = "false"
      host = google_compute_address.node_reserved_external_ip[count.index].address
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
                        nat_ip = google_compute_address.node_reserved_external_ip[count.index].address
                }

                
        }
}