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
