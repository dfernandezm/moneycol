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