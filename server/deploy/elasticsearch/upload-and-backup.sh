#!/bin/bash

# path.repo in elasticsearch.yml 

# Create the snapshot repository
# curl -i -H "Content-Type: application/json" -XPUT http://localhost:9200/_snapshot/moneycol-banknotes-backup -d '{ "type": "fs", "settings": {"location": "/tmp/backups"}}'

# take snapshot
curl -i -XPUT "http://localhost:9200/_snapshot/moneycol-banknotes-backup/snapshot_moneycol_3?wait_for_completion=true" -d '{
"indices": "banknotes-catalog-en,banknotes-catalog-es",
  "ignore_unavailable": true,
  "include_global_state": false,
  "metadata": {
    "taken_by": "david",
    "taken_because": "local backup for GCP"
  }}'

cd ~/Desktop
tar -cvzf /tmp/moneycol-data.tar.gz backups
kubectl cp /tmp/moneycol-data.tar.gz default/elasticsearch-0:/tmp

# Inside elasticsearch-0
kubectl exec -ti elasticsearch-0 -- bash
mkdir -p /tmp/backups
tar -xvf /tmp/moneycol-data.tar.gz 
curl -i -H "Content-Type: application/json" -XPUT http://localhost:9200/_snapshot/moneycol-banknotes-backup -d '{ "type": "fs", "settings": {"location": "/tmp/backups"}}'
curl -i -XGET http://localhost:9200/_snapshot/moneycol-banknotes-backup
curl -i -XPOST "http://localhost:9200/_snapshot/moneycol-banknotes-backup/snapshot_moneycol_3/_restore?pretty"
