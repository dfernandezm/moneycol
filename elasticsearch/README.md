## Setup locally with Docker

You'll need `docker-compose` which comes bundled with Docker for Mac.

First, some folders need to be created in your local machine to store ES data across container runs. By default Docker for Mac shares the `/Users` folder, so any subfolder would work for this purpose.

```
$ export ES_DATA_PATH=/Users/path/to/data
$ mkdir -p $ES_DATA_PATH
$ export ES_BACKUPS_PATH=/Users/path/to/backups
$ mkdir -p $ES_BACKUPS_PATH
$ export ES_PLUGINS_PATH=/Users/path/to/plugins
$ mkdir -p $ES_PLUGINS_PATH
```

To get the data in, unzip `data.zip` and make sure its contents are into `$ES_DATA_PATH`.

Start up Elasticsearch:
```
$ docker-compose up
```

Check the data is loaded by running in the browser http://localhost:9200/_cat/indices

## Setup of Backup/Restore using snapshot API

The Dockerfile already sets up a bucket in GCP for backups and the corresponding keystore to access it.
The service account keys are in the bucket `gs://moneycol-dev`.

To register the repository:
```
kubectl exec -ti elasticsearch-0 -- curl -i -H "Content-Type: application/json" -XPUT http://localhost:9200/_snapshot/moneycol_elastic_dev -d '{"type":"gcs","settings":{"bucket":"moneycol-dev-elasticsearch-snapshots","region":"europe-west1","client":"moneycol_dev","compress":true}}'
```

To take an snapshot:
```
kubectl exec -ti elasticsearch-0 -- curl -i -H "Content-Type: application/json" -XPUT http://localhost:9200/_snapshot/moneycol_elastic_dev/snapshot_moneycol_1?wait_for_completion=true -d '{
"indices": "banknotes-catalog-en,banknotes-catalog-es",
  "ignore_unavailable": true,
  "include_global_state": false,
  "metadata": {
    "taken_by": "david",
    "taken_because": "local backup for GCP"
  }}'
```

Check the status of the snapshot after creation:
```
kubectl exec -ti elasticsearch-0 -- curl -i -XGET localhost:9200/_snapshot/moneycol_elastic_dev/snapshot_moneycol_1
```

To delete:
```
kubectl exec -ti elasticsearch-0 -- curl -i -XDELETE localhost:9200/_snapshot/moneycol_elastic_dev/snapshot_moneycol_1
```

To restore an snapshot:
```
# cannot restore directly if the indexes are open (_open)
kubectl exec -ti elasticsearch-0 -- curl -XPOST http://localhost:9200/banknotes-catalog-es/_close?pretty
kubectl exec -ti elasticsearch-0 -- curl -XPOST http://localhost:9200/banknotes-catalog-en/_close?pretty

# Issue a restore
kubectl exec -ti elasticsearch-0 -- curl -i -H "Content-Type: application/json" -XPOST http://localhost:9200/_snapshot/moneycol_elastic_dev/snapshot_moneycol_1/_restore?wait_for_completion=true
```
