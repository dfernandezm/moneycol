## Crawler

## Build Docker

```
docker build . -t eu.gcr.io/moneycol/data-collector-node:0.0.1
```

```
docker run -v /Users/david/development/repos:/tmp -e GOOGLE_APPLICATION_CREDENTIALS=/tmp/moneycol-gcs.json -it  eu.gcr.io/moneycol/data-collector-node:0.0.1
```

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
- Run `helm install data-collector data-collector` in the `charts/data-collector`
- This should install a Cron job in K8s that runs crawling at `1:15 am` every Sunday


### Parameters and environment variables

- `RESIZER_FUNCTION_URL`: URL of the GKE resizer function
- `PROJECT_ID`: the GCP project where the crawler is hosted
- `ZONE`
- `CLUSTER_ID` 
- `NODE_POOL_ID` 
- `GCS_BUCKET`
- `WEBSITE_NAME`

## Crawling specifics

If you want to crawl just one country or series, this can be configured:

- For a country

```
let afgUrl = "https://colnect.com/en/banknotes/series/country/1-Afghanistan";
mainCrawler.queue(afgUrl);
```

- For a Series
```
let seriesUrl = "https://colnect.com/en/banknotes/list/country/104-Ireland/series/319062-Promissory_National_Bonds";
mainCrawler.queue(seriesUrl);
```