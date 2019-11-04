import { SearchService } from './SearchService';
import { Client } from 'elasticsearch';
import { SearchResult } from './SearchResult';
import { BankNote } from '../types/BankNote';

// it should include endpoint:port
const ELASTICSEARCH_ENDPOINT_WITH_PORT = process.env.ELASTICSEARCH_ENDPOINT_WITH_PORT || "elasticsearch";

class ElasticSearchService implements SearchService {

    async search(language: string, searchTerm: string, from: number, size: number): Promise<SearchResult> {
        let fromParam = from ? from : 0;
        let sizeParam = size ? size : 24;
        let theIndexName = "banknotes-catalog-" + language;

        console.log("Searching in index " + theIndexName + ", query is " + searchTerm);
        // Search in every field but Description and CatalogCode
        // https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html
        const baseQueryString = {
            "from": fromParam, "size": sizeParam,
            query: {
                query_string: {
                    "query": searchTerm,
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
            body: baseQueryString
        });

        const results = resp.hits.hits.map(hit => hit._source)
        const total = resp.hits.total;
        const bankNotes: BankNote[] = results.map(result => new BankNote(result));
        
        const searchResult = new SearchResult(bankNotes, total);
        //console.log("Results: " + JSON.stringify(results));
        console.log("Banknotes", searchResult.results);
        console.log("Total: " + searchResult.total);
        console.log("Search Result", searchResult);
        return searchResult;
    }

    private elasticClient() {
        return new Client({
            host: `${ELASTICSEARCH_ENDPOINT_WITH_PORT}`,
            log: 'info'
        });
    }
}

export { ElasticSearchService };