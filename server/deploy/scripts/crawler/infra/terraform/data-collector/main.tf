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


# Deploy the resizer function with PubSub Trigger on dev.crawler.events topic
# This way when crawler is done, GKE resizes indexer node pool back to 0

locals {
    index_resizer_function_name = "resize-down-indexer"
    index_resizer_function_code_path = "/Users/david/development/repos/moneycol/infra/gke-resizer/code"
    function_trigger_topic_name = "dev.crawling.done"
}

resource "google_pubsub_topic" "function_topic" {
  name = local.function_trigger_topic_name
}

resource "google_storage_bucket" "functions_bucket" {
  name     = "moneycol-functions"
  location = "EU"
}

data "archive_file" "function_archive" {
  type         = "zip"
  source_dir   = local.index_resizer_function_code_path
  output_path  = "${local.index_resizer_function_name}.zip"
}

resource "google_storage_bucket_object" "archive" {
  name   = "${local.index_resizer_function_name}.zip#${data.archive_file.function_archive.output_md5}"
  bucket = google_storage_bucket.functions_bucket.name
  source = data.archive_file.function_archive.output_path
}

resource "google_cloudfunctions_function" "resize_function" {
  name        = "${local.index_resizer_function_name}"
  #name        = format("%s#%s", google_storage_bucket_object.archive.name, data.archive_file.function_archive.output_md5)

  description = "Resize indexer node pool to 0"
  runtime     = "nodejs14"
  available_memory_mb   = 128
  source_archive_bucket = google_storage_bucket.functions_bucket.name
  source_archive_object = google_storage_bucket_object.archive.name
  timeout               = 60
  entry_point = "resizeCluster"
  environment_variables = {
    PAYLOAD = "my-env-var-value"
  }

  event_trigger  {
    event_type = "providers/cloud.pubsub/eventTypes/topic.publish"
    resource   = "${google_pubsub_topic.function_topic.name}"
  }

  lifecycle {
      create_before_destroy = true
  }
}

# # IAM entry for a single user to invoke the function
# resource "google_cloudfunctions_function_iam_member" "invoker" {
#   project        = google_cloudfunctions_function.function.project
#   region         = google_cloudfunctions_function.function.region
#   cloud_function = google_cloudfunctions_function.function.name

#   role   = "roles/cloudfunctions.invoker"
#   member = "user:myFunctionInvoker@example.com"
# }