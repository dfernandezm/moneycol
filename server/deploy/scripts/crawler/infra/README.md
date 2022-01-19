## Crawler 

This is the crawler to parse data from websites into raw json stored in GCS ready to be indexed.


The process is:

- There's a Kubernetes Job `data-collector-colnect`/`data-collector-colnect-cron` deployed in a GKE Node Pool `indexing-pool`, with size 0 (see `charts/data-collector`)
- The cronjob requires a Service Account with GCS and Cloud Function Invoker role (`moneycol-gcs.json` key for `gcs-buckets@moneycol.iam.gserviceaccount.com`)
- Once a week, a Cloud Scheduler `start-crawler` launches `gke-resizer` HTTP function which resizes the node pool to 1 (at `01:00 am`, `0 1 * * 0`). This happens 15 minutes before the cronjob scheduled time (at `15 1 * * 0`, `01:15am`)
- The payload of the Cloud Scheduler is:
```
{
 "projectId":  "moneycol",
  "zone": "europe-west1-b",
  "clusterId":  "cluster-dev2",
  "nodePoolId": "indexing-pool",
  "nodeCount":  1
}
```
- The cronjob `data-collector-colnect-cron` runs for around 4-6h. PubSub topic `dev.crawler.events` is notified with the date of today as `dataUri`:
```
{
 "bucketName": "moneycol-import",
 "dataUri": "colnect/19-01-2022"
}
```

- After this notification is sent, `data-collector-colnect-cron` calls the GKE resizer function for the `indexing-pool` to be size 0 again