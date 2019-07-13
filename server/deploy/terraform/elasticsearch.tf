
variable "do_token" {}

# Configure the DigitalOcean Provider
provider "digitalocean" {
  token = "${var.do_token}"
}

resource "digitalocean_droplet" "elasticsearch_dev" {
  image  = "ubuntu-18-04-x64"
  name   = "elasticsearch-1"
  region = "lon1"
  size   = "s-2vcpu-2gb"
  ssh_keys = ["3f:ac:7f:d9:b5:81:e8:f2:36:3b:5f:6e:93:f2:9e:95"]
# currently not uploading, maybe specifics to DO?
  provisioner "file" {
    source      = "script.sh"
    destination = "/tmp/script.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "chmod +x /tmp/script.sh",
      "/tmp/script.sh ${digitalocean_droplet.elasticsearch_dev.ipv4_address}"
    ]
  }
}