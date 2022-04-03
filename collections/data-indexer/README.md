# Data Indexer module

This module implements an indexing pipeline utilised to get data into Elasticsearch so that is searchable.
It uses serverless pattern known as Fan-in / Fan-out to process the raw data (JSON) stored in GCS after the 
crawling process finishes.

## Workflow to index crawled data into ElasticSearch

`data-indexer` is a module that contains 3 Cloud Functions,

- `batcher` (`BatcherFunction` class): it starts as a result of the event `crawlingDone` (crawler publishes event
into `*.crawler.events`) PubSub Topic. It reads the GCS bucket where all raw JSON data is and builds batches with it. 
It registers a `TaskList` comprised of all the batches, for further processing. It publishes each batch as message in PubSub into,
triggering the Fan-Out (topic `*.indexer.batches`).

- `worker` (`WorkerFunction` class): this is a function that gets triggered on each message sent to `*.indexer.batches`. 
Each of the messages contains a single batch of JSON data (files), that gets read and processed into a `sink` topic (`*.indexer.sink`),
ready to be indexed into Elasticsearch. Many of these functions run in parallel and store results in sink topic. This is done
like this and not directly into Elastic to avoid overwhelming it. 
As the tasks run, the `TaskList` status is being tracked for completion. Once all the spawned `tasks` are completed, this is signalled
into the PubSub topic `*.indexer.batching.done`.

- `indexer` (`IndexingFunction` class): as a result of the batching done, the `sink` topic contains all the data ready to be indexed.
This function reads from the `sink` topic in small batches and indexes the data into ElasticSearch. It also tracks time and
restarts itself every 8m30s (timeout as 9m)

### Infrastructure required

- Cloud Scheduler runs every week to invoke the crawling process (see `data-collector/crawler` at root folder)
- Pubsub Topics and Subscriptions
  - 1 topic for notification of crawling complete `*.crawler.events`
  - 1 topic to trigger worker functions `*.indexer.batches`
  - 1 topic to publish intermediate results computed by workers `*.indexer.sink`
  - 1 topic to indicate processing of all intermediate tasks is done `*.indexer.batching.done`
- 1 service account for the Cloud Functions with permission:
  - Cloud Storage read/write
  - PubSub publish/subscribe
- Deploy Cloud Functions
  - batcher
  - worker
  - indexer
- Cloud Functions to GKE connectivity infrastructure
  - To connect to ElasticSearch without Load Balancer
  - VPC serverless access
  - Updates to the GKE cluster (VPC-native, Workload Identity...)


## Automatically add timestamp to Elasticsearch

```bash
curl -XPUT http://elastic-server/_ingest/pipeline/add-current-time -H 'Content-Type: application/json' -d '{                          ~/development/repos/moneycol/infra/gke
  "description" : "automatically add the current time to the documents",
  "processors" : [
    {
      "set" : {
        "field": "@timestamp",
        "value": "_ingest.timestamp"
      }
    }
  ]
}'
```

Note: currently the above does not work