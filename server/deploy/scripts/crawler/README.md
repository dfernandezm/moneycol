## Crawler

## Build Docker

```
docker build . -t eu.gcr.io/moneycol/data-collector-node:0.0.1
```

```
docker run -v /Users/david/development/repos:/tmp -e GOOGLE_APPLICATION_CREDENTIALS=/tmp/moneycol-gcs.json -it  eu.gcr.io/moneycol/data-collector-node:0.0.1
```

## Deployment

Deploy in GKE using Helm from charts repository `data-collector`.


