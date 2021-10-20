# Automatically add timestamp to Elasticsearch

```bash
curl -XPUT http://35.190.210.3/_ingest/pipeline/add-current-time -H 'Content-Type: application/json' -d '{                          ~/development/repos/moneycol/infra/gke
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