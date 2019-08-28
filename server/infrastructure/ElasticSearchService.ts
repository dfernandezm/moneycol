import { SearchService } from './SearchService';
import { Client } from 'elasticsearch';

class ElasticSearchService implements SearchService {

    async search(language: string, searchTerm: string, from: number, size: number): Promise<SearchResult> {
        let fromParam = from ? from : 0;
        let sizeParam  = size ? size : 24;
        let theIndexName = "banknotes-catalog-" + language;
        
        console.log("Searching in index " + theIndexName + ", query is " + searchTerm) ;
        // Search in every field but Description and CatalogCode
        // https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html
        const baseQueryString = {
            "from" : fromParam, "size" : sizeParam,
            query: {
             query_string : {   
                 "query" : searchTerm,
                 "default_operator": "AND",
                 "fields": [
                    "BanknoteName",
                    "Year",
                    "Country"
                  ]
             }   
            }
        };

        console.log("Querystring: ", baseQueryString);
    
        const resp = await this.elasticClient().search({
            index: theIndexName,
            body:  baseQueryString
        });

        const results = resp.hits.hits.map(hit => hit._source)
        const total = resp.hits.total;
        const response = { results, total }
        console.log("Results: " + JSON.stringify(results));
        console.log("Total: " + resp.hits.total);
        return response;
    }

    elasticClient() {
        return new Client({
            host: 'localhost:9200',
            log: 'info'
        });
    }
}

export { ElasticSearchService };