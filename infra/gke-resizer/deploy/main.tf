# Bucket needs to exist previously

terraform {
  backend "gcs" {
    bucket  = "tf-state-moneycol"
    prefix  = "terraform/state"
  }
}

resource "google_cloud_scheduler_job" "gke_cluster_expand" {
  name             = "gke-cluster-expand"
  description      = "Expand the GKE cluster"
  schedule         = "*/8 * * * *"
  time_zone        = "Europe/Madrid"
  attempt_deadline = "320s"

  retry_config {
    retry_count = 5
  }

  http_target {
    http_method = "POST"
    uri         = "https://example.com/ping"
    body        = base64encode("{\"foo\":\"bar\"}")

    oidc_token {
      service_account_email = data.google_compute_default_service_account.default.email
    }

  }
}