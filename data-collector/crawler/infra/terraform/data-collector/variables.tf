variable gcp_project {
    default = "moneycol"
    description = "GCP project"
}

variable region {
    default = "europe-west1"
    description = "Region of the Moneycol GCP project "
}

variable index_resizer_function_name {
    default = "resize-down-indexer"
    description = "The Cloud Function that resizes the node pool in GKE performing crawling (indexing-pool)"
}

# CI process should place the function in a different location
variable index_resizer_function_code_path {
    default = "/Users/david/development/repos/moneycol/infra/gke-resizer/code"
    description = "Path to the code of the GKE resizer function for the indexing pool"
}

variable crawler_resize_uri {
    default = "https://europe-west1-moneycol.cloudfunctions.net/gke-resize"
    description = "The URI of the resizer function to invoke"
}

variable crawler_resize_payload {
    default = "{\"projectId\":  \"moneycol\",\"zone\": \"europe-west1-b\",\"clusterId\":  \"cluster-dev2\",\"nodePoolId\": \"indexing-pool\",\"nodeCount\":1}"
    description = "The payload to resize"
}

variable crawler_resize_job_schedule {
    default = "0 1 * * 0"
    description = "The cron schedule to resize the indexing node pool to 1"
}

variable crawling_done_pubsub_topic {
    default = "dev.crawler.events"
    description = "The resizer function (back to 0, crawling done) is triggered when message is published to this topic"
}