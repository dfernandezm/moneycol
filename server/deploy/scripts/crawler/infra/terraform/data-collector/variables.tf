variable crawler_resize_job_schedule {
    default = "0 1 * * 0"
    description = "The cron schedule to resize the node pool"
}

variable crawler_resize_uri {
    default = "https://europe-west1-moneycol.cloudfunctions.net/gke-resize"
    description = "The URI of the resizer function to invoke"
}

variable crawler_resize_payload {
    default = "{\"projectId\":  \"moneycol\",\"zone\": \"europe-west1-b\",\"clusterId\":  \"cluster-dev2\",\"nodePoolId\": \"indexing-pool\",\"nodeCount\":1}"
    description = "The payload to resize"
}

variable gcp_project {
    default = "moneycol"
    description = "GCP project"
}

variable region {
    default = "europe-west1"
    description = ""
}

variable data_collector_image {
    default = ""
    description = ""
}