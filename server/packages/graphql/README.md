#  Server for MoneyCol

##  Run locally

### Directly

Start Elasticsearch:

```
yarn start:elasticsearch
```

For this script to work, edit `package.json` and set your Elasticsearch installation directory. Restore a backup to have data.

Start GraphQL server in dev mode:
```
FIREBASE_API_KEY=XXXX ELASTICSEARCH_ENDPOINT_WITH_PORT=localhost:9200 COLLECTIONS_API_HOST=localhost:8080 yarn start:dev
```

Visit the playground for GraphQL at http://localhost:4000/graphql.

### GQL query for search

```
query searchBanknotes($searchTerm: String!) {
  search(term: $searchTerm) {
    results {
      ...BanknoteSearchResult
    }
    total
  }
}

fragment BanknoteSearchResult on BankNote {
  country
  banknoteName
  year
  description
  catalogCode
}
```

with parameters

```
{
  "searchTerm": "ireland" 
}
```
## Kubernetes proxied services

Through traefik:

```
$ kubectl proxy &

Visit http://localhost:8001/api/v1/namespaces/kube-system/services/traefik:80/proxy/graphql
```

Directly `moneycolserver`:
```
$ kubectl proxy &
http://localhost:8001/api/v1/namespaces/default/services/moneycolserver:80/proxy/graphql
```

## Elasticsearch backup/restore

Download elasticsearch:
```
#
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.5.0.tar.gz
tar xvf elasticsearch-6.5.0.tar.gz

# Upgrade from 5.3.0 to the dockerized one (6.5.0)
cp -R elasticsearch-5.3.0/data elasticsearch-6.5.0
cp -R elasticsearch-5.3.0/config elasticsearch-6.5.0
```


Install the plugin:
```
sudo bin/elasticsearch-plugin install repository-gcs
```

After the installation ensure you **restart Elasticsearch**.

Ensure service account for gcs has storage admin roles for the backup bucket (change the role, it's too much privileges):

```
# Create the service account first
gcloud projects add-iam-policy-binding moneycol \                                                      
  --member serviceAccount:gcs-buckets@moneycol.iam.gserviceaccount.com \
  --role roles/storage.admin
```

Get the service account key for gcs buckets:
```
# This can be done from the command line
Console > Iam & Admin > Service accounts > gcs-buckets > JSON key
Copy json to /tmp
```

Create a keystore for ES (locally and cloud):
```
bin/elasticsearch-keystore create
bin/elasticsearch-keystore add-file gcs.client.moneycol_dev.credentials_file /tmp/gcs-buckets-service-account.json
```

* Instructions 1: https://www.elastic.co/guide/en/elasticsearch/reference/6.5/secure-settings.html
* Instructions 2: https://www.elastic.co/guide/en/elasticsearch/reference/5.6/rolling-upgrades.html
Create/use a bucket for ElasticSearch snapshots repository:

```
curl -i -H "Content-Type: application/json" -XPUT http://localhost:9200/_snapshot/moneycol_elastic_dev -d '{"type":"gcs","settings":{"bucket":"moneycol-dev-elasticsearch-snapshots","region":"europe-west1","client":"moneycol_dev","compress":true}}'
```

Take a snapshot:
```
curl -i -H "Content-Type: application/json" -XPUT http://localhost:9200/_snapshot/moneycol_elastic_dev/snapshot_moneycol_1?wait_for_completion=true -d '{
"indices": "banknotes-catalog-en,banknotes-catalog-es",
  "ignore_unavailable": true,
  "include_global_state": false,
  "metadata": {
    "taken_by": "david",
    "taken_because": "local backup for GCP"
  }}'
```

## GraphQL for Collections

```
mutation AddCollection($collection: NewCollectionInput!) {
  addCollection(collection: $collection) {
    name
    description
  }
}
---
{
  "collection": {
    "name": "My second collection",
    "description": "This is my second collection"
  }
}
```

```
query collectionsForCollector($collectorId: String!) {
  collections(collectorId: $collectorId) {
    name
    collectionId
  }
}
---
{
  "collectorId": "collectorId9"
}
```

```
mutation AddBankNoteToCollection($input: AddBankNoteToCollection) {
  addBankNoteToCollection(data: $input) {
    collectionId
    name
    description
    bankNotes {
      country
      catalogCode
    }
  }
}
---
{
  "input": {
    "collectionId": "2d5e70fb-0645-4a17-9a8b-82537a39ed77",
    "collectorId": "collectorId9",
    "bankNoteCollectionItem": {
      "catalogCode": "Wor.y99.95"
    }
  }
}
```

```
mutation RemoveBankNoteFromCollection($bankNoteId: String!, $collectionId: String!) {
  removeBankNoteFromCollection(banknoteId: $bankNoteId, collectionId: $collectionId) {
    name
    description
    bankNotes {
      catalogCode
    }
  }
}
---
{
  "collectionId": "abc03b16-740e-4e74-878b-e8724f6a1dc1",
  "bankNoteId": "Wor.rr99.95" 
}
```

```
mutation RemoveCollection($collectionId: String!) {
  deleteCollection(collectionId: $collectionId) 
}
---
{
  "collectionId": "0125da25-a1a0-405a-8ff9-00af42341869"
}
```

```
query itemsForCollection($collectionId: String!) {
  itemsForCollection(collectionId: $collectionId) {
    collectionId
    name
    description
    bankNotes {
      country
      year
      banknoteName
    }
  }
}
---
{
  "collectionId": "9119d775-2fbe-4957-acab-0f6a6829cb4c"
}
```

### Smoke test calls to Collections API

```
# Collections for collectorId
curl -i -XGET http://localhost:8001/collections/collector/collectorId9 -H 'Content-Type: application/json'

# Create collection
curl -i -XPOST http://localhost:8080/collections -H 'Content-Type: application/json' -d '{"name": "A collection1", "description": "Desc1", "collectorId": "collectorId9"}'

# add items to collection
curl -i -XPOST http://localhost:8001/collections/9119d775-2fbe-4957-acab-0f6a6829cb4c/items -H 'Content-Type: application/json' -d '{ "items": [{"itemId":"17516"},{"itemId":"55042"}]}'

# Delete item from collection
curl -i -XDELETE http://localhost:8001/collections/9119d775-2fbe-4957-acab-0f6a6829cb4c/items/Wor:P-157a.2 -H 'Content-Type: application/json'

```


## Use service account to access Firestore

In order to authenticate against Firestore when running `collections-api` inside GKE, a service account is required, otherwise there will be a failure like:

```
Caused by: io.grpc.StatusRuntimeException: PERMISSION_DENIED: Request had insufficient authentication scopes.
```

This is due to the default service account of GKE being used. By default, when no credentials are passed, anything running in GCP will default its credentials to the ones of the Service Account of the underlying service.

In order to fine tune scopes and access rights, it's best practice to create a service account for the deployed service and integrate it as part of it.

## Lerna monorepo

Need to run `yarn build` in the `graphql` module to ensure `dist/schema` is created.
It should run with `yarn start` (compiled) and `yarn start:dev` (nodemon).

### Nodemon 

For `nodemon` to reflect latest changes, a hardcoded watch has been added for `users` and `auth` modules:


```
...
"build:dev": "nodemon -w ../auth -w ../users 'src/server.ts' --exec 'ts-node' -r tsconfig-paths/register src/server.ts -e ts,graphql",
...
```

`nodemon` will watch the folders for changes. For this to complete, incremental compilation should be started in 
the modules. This way changes will be picked up as the source files are saved. Move to the folder where the module
is (`auth` or `users` at this point) and run:

```
yarn compile:watch
```

This way, the TS files will be compiled, the folder will pick the changes and `nodemon` will restart the GraphQL server and the changes will be reflected.