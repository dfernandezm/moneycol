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

## Invoke process manually via Pubsub

While in trial phase, the GKE cluster will be shutdown to save cost. This means that the final step of the indexing
process is never completed. The process can be triggered on demand via PubSub.

Publish a message like the following in the `dev.moneycol.indexer.batching.done`:
```
{
  "taskListId": "28031c86-5d8c-40ef-9e79-2bfacef59bfd", 
  "status": "PROCESSING_COMPLETED"
}
```

Ensure the `taskListId` is the correct one by checking logs from `Worker` and `Batcher` functions recently ran. Once 
the message is published, the `Indexer` function will be triggered and will start indexing the contents of the `sink`
topic.

In other cases, only the crawling is done and no actual batching/indexing is performed at all (only data collection
in GCS). From there the fan-in/fan-out can be triggered via Pubsub.

In the PubSub console, publish a message like the following in the `dev.crawler.events` topic:
```
{
"bucketName": "moneycol-import",
"dataUri": "colnect/20-03-2022"
}
```

Change the date in `dataUri` accordingly. The indicated location must contain crawled JSON files which will
be batched, processed and indexed.



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