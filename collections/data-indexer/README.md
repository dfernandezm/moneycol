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


## Infrastructure required

- Cloud Scheduler to invoke the crawling process (may need PubSub)
  - HTTP target can be used to resize GKE for example: 
    - `https://cloud.google.com/kubernetes-engine/docs/reference/rest/v1beta1/projects.zones.clusters.nodePools/setSize`
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


##