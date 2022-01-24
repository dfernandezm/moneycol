resource "google_service_account" "data_collector_service_account" {
    account_id   = "gcs-buckets"
    description  = "Only for read/write access to specific gcs buckets"
    disabled     = false
    display_name = "gcs-buckets"
}