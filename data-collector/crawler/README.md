## Data Collector 

This is the name of the NodeJS version of the crawling process created to parse data from websites into raw json and store in Cloud Storage, ready to be indexed by the module `collections/data-indexer`.


The process is:

1. There's an initial Kubernetes Cronjob (`data-collector-colnect`/`data-collector-colnect-cron`) deployed in GKE Node Pool `indexing-pool`, with size 0 (see `charts/data-collector`). This has been deployed manually at the time of writing using a `helm install` command from `charts/data-collector`

2. The cronjob requires a Service Account with GCS and Cloud Function Invoker role (`data-collector@moneycol.iam.gserviceaccount.com`)

3. Once a week, a Cloud Scheduler job (`start-crawler`) launches `gke-resizer` HTTP function which resizes the `indexing-pool` to 1 (at `01:00 am`, `0 1 * * 0`). This happens 15 minutes before the cronjob hosted in this node pool is scheduled (at `15 1 * * 0`, `01:15am`)

4. The payload of the Cloud Scheduler job `start-crawler` looks like:

```
{
 "projectId":  "moneycol",
  "zone": "europe-west1-b",
  "clusterId":  "cluster-dev2",
  "nodePoolId": "indexing-pool",
  "nodeCount":  1
}
```

5. The cronjob `data-collector-colnect-cron` runs for around 4-6h. Once the crawling process is completed, it publishes a message to the PubSub Topic `dev.crawler.events` containing the destination `bucketName` and the date of today and website as `dataUri`:

```
{
 "bucketName": "moneycol-import",
 "dataUri": "colnect/19-01-2022"
}
```


All the above is automated via Terraform, in the folder `infra/terraform/data-collector`:

```
# log into moneycol project
gcloud auth login
terraform plan
terraform apply
```

Note: no automation has been done so far for the fan-in/fan-out process triggered by publishing to the PubSub topic (`batcher`-`workers`-`indexer`)

## Build and deploy

Build the Docker image locally:

- Get commit hash `git rev-parse HEAD | cut -c1-7`
- Build Docker
```
docker build . -t eu.gcr.io/moneycol/data-collector-node:0.0.4-commitHash
docker push eu.gcr.io/moneycol/data-collector-node:0.0.4-commitHash
```

Deploy in GKE using Helm from charts repository `data-collector`:

- The `indexing-pool` should be 1 before deploying
- Ensure the secret `data-collector-key` is present in GKE (see `infra/terraform`)
- Edit `values.yaml` and put the new image tag, ensure `deployCron: true` too
- Run `helm install data-collector data-collector` from the folder `charts/data-collector`
- This should install a Kubernetes Cron job  that runs crawling at `1:15 am` every Sunday

### Parameters and environment variables

- `RESIZER_FUNCTION_URL`: URL of the GKE resizer function
- `PROJECT_ID`: the GCP project where the crawler is hosted
- `ZONE`: the GCP zone (`europe-west1`...)
- `CLUSTER_ID`: name of the GKE cluster 
- `NODE_POOL_ID`: node pool hosting the crawler
- `GCS_BUCKET`: the name of the Cloud Storage bucket where the JSON data is stored from crawling (`moneycol-import`)
- `WEBSITE_NAME`: currenly only `colnect`

## Crawling specifics

If you want to crawl just one country or series, the following can be added to the code:

- For a country

```
// comment out the block `countriesCrawler`
let afgUrl = "https://colnect.com/en/banknotes/series/country/1-Afghanistan";
mainCrawler.queue(afgUrl);
```

- For a Series
```
let seriesUrl = "https://colnect.com/en/banknotes/list/country/104-Ireland/series/319062-Promissory_National_Bonds";
mainCrawler.queue(seriesUrl);
```

## Notes on GKE

The GKE cluster currently used is deployed via Pulumi, see `infra/gke` folder. This cluster is automatically resized via 2 Cloud Scheduler jobs (manually created at the time of writing) that run the Cloud Function under `infra/gke-resizer`
