resource "google_pubsub_topic" "indexer_sink_pubsub_topic" {
  name = var.indexer_sink_pubsub_topic

  labels = {
    service = "data-collector"
  }

  message_retention_duration = "86600s"
}

resource "google_pubsub_subscription" "indexer_sink_pubsub_subscription" {
  name                 = var.indexer_sink_pubsub_topic
  topic                = google_pubsub_topic.indexer_sink_pubsub_topic.name
  ack_deadline_seconds = 600

  labels = {
    service = "data-collector"
  }

  message_retention_duration = "86600s"
}

resource "google_pubsub_topic" "crawling_done_pubsub_topic" {
  name = var.crawling_done_pubsub_topic

  labels = {
    service = "data-collector"
  }

  message_retention_duration = "86600s"
}

resource "google_pubsub_subscription" "crawling_done_pubsub_subscription" {
  name                 = var.crawling_done_pubsub_topic
  topic                = google_pubsub_topic.crawling_done_pubsub_topic.name
  ack_deadline_seconds = 600

  labels = {
    service = "data-collector"
  }

  message_retention_duration = "86600s"
}

resource "google_pubsub_topic" "indexer_batches_pubsub_topic" {
  name = var.indexer_batches_pubsub_topic

  labels = {
    service = "data-collector"
  }

  message_retention_duration = "86600s"
}

resource "google_pubsub_subscription" "indexer_batches_pubsub_subscription" {
  name                 = var.indexer_batches_pubsub_topic
  topic                = google_pubsub_topic.indexer_batches_pubsub_topic.name
  ack_deadline_seconds = 600

  labels = {
    service = "data-collector"
  }

  message_retention_duration = "86600s"
}

resource "google_pubsub_topic" "indexer_batch_done_pubsub_topic" {
  name = var.indexer_batch_done_pubsub_topic

  labels = {
    service = "data-collector"
  }

  message_retention_duration = "86600s"
}

resource "google_pubsub_subscription" "indexer_batch_done_pubsub_subscription" {
  name                 = var.indexer_batch_done_pubsub_topic
  topic                = google_pubsub_topic.indexer_batch_done_pubsub_topic.name
  ack_deadline_seconds = 600

  labels = {
    service = "data-collector"
  }

  message_retention_duration = "86600s"
}