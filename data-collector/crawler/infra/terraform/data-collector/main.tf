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

resource "google_cloud_scheduler_job" "start_crawler_job" {
  name             = "start-crawler"
  description      = "Call GKE Resize to get Crawler Cronjob started"
  schedule         = var.crawler_resize_job_schedule
  time_zone        = "Europe/Madrid"
  attempt_deadline = "300s"

  http_target {
    http_method = "POST"
    uri         = var.crawler_resize_uri
    body        = base64encode(var.crawler_resize_payload)
    headers = {
      "Content-Type" = "application/json"
      "User-Agent"   = "Google-Cloud-Scheduler"
    }

    oidc_token {
      service_account_email = google_service_account.gke_resize_main_service_account.email
    }
  }

  retry_config {
    max_backoff_duration = "3600s"
    max_doublings        = 5
    max_retry_duration   = "0s"
    min_backoff_duration = "5s"
    retry_count          = 0
  }
}

# Add GKE shrink / expand here


# for this to work it's needed to auth against GKE
# gcloud container clusters get-credentials cluster-dev2 --zone europe-west1-b --project moneycol
resource "kubernetes_secret" "data_collector_key_secret" {
  metadata {
    name = "data-collector-key"
  }

  data = {
    "data-collector-key.json" = base64decode(google_service_account_key.data_collector_sa_key.private_key)
  }

  type = "generic"
}


# Deploy the resizer function with PubSub Trigger on dev.crawler.events topic
# This way when crawler is done (and publishes to that topic), 
# the function resizes the GKE indexer node pool back to 0

locals {
  index_resizer_function_name      = var.index_resizer_function_name
  index_resizer_function_code_path = var.index_resizer_function_code_path
  function_trigger_topic_name      = var.crawling_done_pubsub_topic
}

data "google_pubsub_topic" "function_topic" {
  name = local.function_trigger_topic_name
}

resource "google_storage_bucket" "functions_bucket" {
  name     = "moneycol-functions"
  location = "EU"
}

data "archive_file" "function_archive" {
  type        = "zip"
  source_dir  = local.index_resizer_function_code_path
  output_path = "${local.index_resizer_function_name}.zip"
}

resource "google_storage_bucket_object" "archive" {
  name   = "${local.index_resizer_function_name}.zip#${data.archive_file.function_archive.output_md5}"
  bucket = google_storage_bucket.functions_bucket.name
  source = data.archive_file.function_archive.output_path
}

resource "google_cloudfunctions_function" "resize_function" {
  name                  = local.index_resizer_function_name
  description           = "Resize indexer node pool to 0"
  runtime               = "nodejs14"
  available_memory_mb   = 128
  source_archive_bucket = google_storage_bucket.functions_bucket.name
  source_archive_object = google_storage_bucket_object.archive.name
  timeout               = 60
  entry_point           = "resizeCluster"

  event_trigger {
    event_type = "providers/cloud.pubsub/eventTypes/topic.publish"
    resource   = data.google_pubsub_topic.function_topic.name
  }

  lifecycle {
    create_before_destroy = true
  }
}
