provider "google" {
  project = "moneycol"
  region  = "europe-west1"
}

provider "google-beta" {
  project = "moneycol"
  region  = "europe-west1"
}

provider "kubernetes" {
  config_path = "~/.kube/config"
  #config_context = "default"
}