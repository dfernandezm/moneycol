# Server for MoneyCol

## Run locally

### Directly

Start Elasticsearch:

```
npm run start:elasticsearch
```

Start Node server in dev mode:
```
npm run build:dev
```

### Using Docker for Mac

Make sure `elasticsearch.yaml` config has `network.host=0.0.0.0`

```
docker build . -t moneycol-server:v0.1.1-alpha
npm run start:elasticsearch
export LOCAL_IP=192.168.1.80
docker run --name server-1 -e ELASTICSEARCH_ENDPOINT_WITH_PORT=192.168.1.80:9200 -p 4000:4000 -it moneycol-server:0.1.0-alpha3
```

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
## Kubernetes proxied services

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

To put back LoadBalancer from GCP:

- Change traefik `values.yaml` value `serviceType: LoadBalancer` to `serviceType: NodePort`
- Re-run upgrade on the helm release
```
helm upgrade [traefik-release] deploy/traefik/chart
```

## Exposing NodePort without LB

Useful for dev or local env:

https://stackoverflow.com/questions/42040238/how-to-expose-nodeport-to-internet-on-gce

* Need to open a firewall rule, allowing TCP traffic on port range tcp:30000-32767. See
 example: https://console.cloud.google.com/networking/firewalls/details/nodeport-access?project=moneycol
 ```
 # Example
 gcloud compute firewall-rules create nodeport-access --allow tcp:30080,tcp:30443
 gcloud compute instances list
 ```

## Get IP and port

* IP
```
kubectl get node gke-moneycol-main-main-pool-ac0c4442-57ff -o json | jq '.status.addresses[1].address'
```

* Port
```
# first port in the output
kubectl get svc traefik -n kube-system -o json | jq '.spec.ports[0].nodePort'
```

- Option 1: use the port and the IP of any node (given the firewall rule) [working]
- Option 2: add a f1-micro instance (3.88$/month), deploy HAProxy/Traefik/Nginx to point to the nodes