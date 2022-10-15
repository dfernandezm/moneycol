terraform {
  backend "gcs" {
    bucket = "moneycol-terraform"
    prefix = "dev/data-collector"
  }
}