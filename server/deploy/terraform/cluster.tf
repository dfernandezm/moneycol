provider "google-beta" {
  credentials = "${file("/Users/david/account.json")}"
  project     = "moneycol"
  region      = "europe-west1-b"
}

variable "cluster_zone" {
  default = "europe-west1-b"
}

resource "google_container_cluster" "main" {
  name = "moneycol-main"
  project = "moneycol"
  zone = "${var.cluster_zone}"

  remove_default_node_pool = true
  initial_node_count = 1
  logging_service = "none"
  monitoring_service = "none"

  master_auth {
    username = "admin"
    password = "4dmin_gcloud_moneycol_1"

    client_certificate_config {
      issue_client_certificate = false
    }
  }
}

resource "google_container_node_pool" "main_node_pool" {
  provider   = "google-beta"
  name       = "main-pool"
  location   = "europe-west1-b"
  cluster    = "${google_container_cluster.main.name}"
  node_count = 3

  management {
    auto_repair  = true
    auto_upgrade = false
  }

  node_config {
    preemptible  = true
    machine_type = "f1-micro"
    disk_size_gb = 10

    oauth_scopes = [
      "https://www.googleapis.com/auth/compute",
      "https://www.googleapis.com/auth/devstorage.read_only",
      "https://www.googleapis.com/auth/logging.write",
      "https://www.googleapis.com/auth/monitoring",
    ]
  }
}

resource "google_container_node_pool" "es_node_pool" {
  provider   = "google-beta"
  name       = "elasticsearch-pool"
  location   = "europe-west1-b"
  cluster    = "${google_container_cluster.main.name}"
  node_count = 1

  management {
    auto_repair  = true
    auto_upgrade = false
  }

  node_config {
    preemptible  = true
    machine_type = "n1-standard-1"
    disk_size_gb = 11

    oauth_scopes = [
      "https://www.googleapis.com/auth/compute",
      "https://www.googleapis.com/auth/devstorage.read_only",
      "https://www.googleapis.com/auth/logging.write",
      "https://www.googleapis.com/auth/monitoring",
    ]
  }
}

# https://www.edureka.co/blog/kubernetes-dashboard/