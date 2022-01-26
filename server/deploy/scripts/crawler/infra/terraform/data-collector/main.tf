resource "google_service_account" "data_collector_main_service_account" {
    account_id   = "data-collector"
    description  = "Data Collector (aka Crawler)"
    disabled     = false
    display_name = "data-collector"
}

resource "google_project_iam_custom_role" "data_collector_role" {
  role_id     = "data_collector"
  title       = "Data Collector"
  description = "Data Collector"
  permissions = [
      "pubsub.topics.publish",
      "storage.buckets.create",
      "storage.objects.create",
      "storage.objects.delete",
      "storage.objects.get",
      "storage.objects.list"
    ]
}

resource "google_project_iam_binding" "data_collector_sa_project_iam_binding" {
  project = "moneycol"
  role    = google_project_iam_custom_role.data_collector_role.name
  members = [
    "serviceAccount:${google_service_account.data_collector_main_service_account.email}",
  ]
}

resource "google_service_account_key" "data_collector_sa_key" {
  service_account_id = google_service_account.data_collector_main_service_account.name
}

resource "google_service_account" "gke_resize_main_service_account" {
    account_id   = "gke-resize"
    description  = "GKE resize"
    disabled     = false
    display_name = "gke-resize"
}

resource "google_project_iam_custom_role" "gke_resize_role" {
  role_id     = "gke_resize"
  title       = "GKE Resize"
  description = "GKE Resize"
  permissions = [
    "container.clusters.get",
    "container.clusters.getCredentials",
    "container.clusters.update",
    "container.nodes.list",
    "container.pods.list",
    "container.services.get",
    "cloudfunctions.functions.invoke"
    ]
}

resource "google_project_iam_binding" "gke_resize_sa_project_iam_binding" {
  project = "moneycol"
  role    = google_project_iam_custom_role.gke_resize_role.name
  members = [
    "serviceAccount:${google_service_account.gke_resize_main_service_account.email}",
  ]
}

resource "google_service_account_key" "gke_resize_sa_key" {
  service_account_id = google_service_account.gke_resize_main_service_account.name
}

resource "local_file" "data_collector_key" {
    content     = base64decode(google_service_account_key.data_collector_sa_key.private_key)
    filename = "/tmp/data-collector.json"
}

resource "local_file" "gke_resize_key" {
    content     = base64decode(google_service_account_key.data_collector_sa_key.private_key)
    filename = "/tmp/gke-resize.json"
}

resource "google_cloud_scheduler_job" "start_crawler_job" {
  name             = "start-crawler"
  description      = "Call GKE Resize to get Crawler Cronjob started"
  schedule         = var.crawler_resize_job_schedule
  time_zone        = "Europe/Madrid"
  attempt_deadline = "300s"

  http_target {
    http_method = "POST"
    uri         = var.crawler_resize_uri
    body = base64encode(var.crawler_resize_payload)

    oidc_token {
      service_account_email = google_service_account.gke_resize_main_service_account.email
    }
  }
}

resource "kubernetes_secret" "data_collector_key_secret" {
  metadata {
    name = "data-collector-key"
  }

  data = {
    "data-collector-key.json" = base64decode(google_service_account_key.data_collector_sa_key.private_key)
  }

  type = "generic"
}