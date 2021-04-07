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

## Exposing NodePort without LB

To save cost of LoadBalancer in GCP.

* Need to open a firewall rule, allowing TCP traffic on port range tcp:30000-32767. This the the port range that Kubernetes uses to expose NodePort services. See
 example: https://console.cloud.google.com/networking/firewalls/details/nodeport-access?project=moneycol
 ```
 # Example
 gcloud compute firewall-rules create nodeport-access --allow tcp:30080,tcp:30443
 gcloud compute instances list
 ```

### Get IP and port

* IP
```
# the node name can be any of them, can be checked with kubectl get nodes
kubectl get node gke-moneycol-main-main-pool-ac0c4442-57ff -o json | jq '.status.addresses[1].address'
```

* Port
```
# first port in the output
kubectl get svc traefik -o json | jq '.spec.ports[0].nodePort'
```

- Option 1: use the port and the IP of any node (given the firewall rule) [working]
- Option 2: add a f1-micro instance (3.88$/month), deploy HAProxy/Traefik/Nginx to point to the nodes


### Put back LoadBalancer for production-like environment

The service is exposed in GKE as NodePort to save cost of LoadBalancer. To put back LoadBalancer from GCP:

- Change `deploy/traefik/chart/values.yaml` value `serviceType: LoadBalancer` to `serviceType: NodePort`
- Re-run upgrade on the traefik helm release (this does not affect the underlying services, only the Ingress)
```
helm upgrade [traefik-release] deploy/traefik/chart
```
- The IP served is stable and can be linked to a CloudDNS domain name
```
kubectl -n kube-system get svc traefik
```

## Terraform

Create a storage bucket for terraform state with versioning:

```
$ gsutil mb gs://moneycol-tf-state-dev
$ gsutil versioning set on gs://moneycol-tf-state-dev
```

## Basic DNS setup

Manually:

- CloudDNS create zone, set name and DNS suffix
- Copy NS from the recordset NS
- Using google account, go to freenom: https://my.freenom.com/clientarea.php?action=domaindetails
- Add the NS from GCP (one off, or until the NS changes)
- Create record set (prefix.dns.suffix) with A for IP of LB or similar
- Poll: dig +short NS `dns.domain`

Terraform:

With terraform the NS are fixed to a name in moneycol project already. This way we avoid going to Freenom/Godaddy and update them every time they're created.

- Edit `dns.tf` and change `dev_ip` variable to the new value
- Run
```
$ terraform plan 
$ terraform apply
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
## Alternative to NodePort to get services on port 80/443

Only Kubernetes Services of type LoadBalancer support this. A workable workaround has been found [here](https://serverfault.com/questions/801189/expose-port-80-and-443-on-google-container-engine-without-load-balancer)

Steps:

- Create a variant of `traefik` service that lists the internal IPs of every node in the cluster (see: `deploy/traefik/service-externalIps.yaml)
- This service requires all **internal IPs** of the cluster nodes. These can be obtained like this:
```
# Internal IP
kubectl get nodes -o json | jq '.items[i].status.addresses[0].address'
# External IP
kubectl get nodes -o json | jq '.items[i].status.addresses[1].address'
```
- Once the internal Ips are populated the service can be then deployed alongside `traefik` chart.
- In the GKE dashboard, observe a new `traefik-dev` service
- The DNS can now be updated to the IP of **any** node in the cluster
- The service should be available in port 80/443 now through its DNS after the update


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