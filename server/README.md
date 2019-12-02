#  Server for MoneyCol

##  Run locally

### Directly

Start Elasticsearch:

```
npm run start:elasticsearch
```

For this script to work, edit `package.json` and set your Elasticsearch installation directory. Restore a backup to have data. [pending: docker setup with a backup]

Start GraphQL server in dev mode:
```
ELASTICSEARCH_ENDPOINT_WITH_PORT=localhost:9200 npm run start:dev
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