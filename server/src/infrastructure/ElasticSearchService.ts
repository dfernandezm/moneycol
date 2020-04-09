import { SearchService } from './SearchService';
import { Client } from 'elasticsearch';
import { SearchResult } from './SearchResult';
import { BankNote } from '../types/BankNote';

// it should include endpoint:port
const ELASTICSEARCH_ENDPOINT_WITH_PORT = process.env.ELASTICSEARCH_ENDPOINT_WITH_PORT || "elasticsearch";
const DEFAULT_RESULT_SIZE = 100;
const DEFAULT_FROM_OFFSET = 0;

class ElasticSearchService implements SearchService {

    async search(language: string, searchTerm: string, from: number, size: number): Promise<SearchResult> {
        let fromParam = from ? from : DEFAULT_FROM_OFFSET;
        let sizeParam = size ? size : DEFAULT_RESULT_SIZE;
        let theIndexName = "banknotes-catalog-" + language;

        console.log("Searching in index " + theIndexName + ", query is " + searchTerm);

        // Search in every field but Description and CatalogCode
        // https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html
        // https://www.elastic.co/guide/en/elasticsearch/reference/6.2/query-dsl-multi-match-query.html#type-cross-fields
        // Elastic 6.x onwards?
        const baseQueryString = {
            "from": fromParam, "size": sizeParam,
            "query": {
                "multi_match": {
                    "query": searchTerm,
                    "type": "cross_fields",
                    "fields": ["Country", "Year", "BanknoteName"],
                    "operator": "and"
                }
            }
        }

        console.log("Querystring: ", baseQueryString);

        const resp = await this.elasticClient().search({
            index: theIndexName,
            body: baseQueryString
        });

        const results = resp.hits.hits.map(hit => hit._source)
        const total = resp.hits.total;
        const bankNotes: BankNote[] = results.map(result => new BankNote(result));

        const searchResult = new SearchResult(bankNotes, total);
        console.log("Total: " + searchResult.total);
        return searchResult;
    }

    async decorateUsingIds (language: string, docIds: string[]): Promise<BankNote[]> {
        let theIndexName = "banknotes-catalog-" + language;
        const idsQuery = {
            "query": {
                "ids" : {
                    "values" : docIds
                }
            }
        }

        console.log("Decorator Querystring: ", idsQuery);
        
        const result = await this.elasticClient().search({
            index: theIndexName,
            body: idsQuery
        });

        const results = result.hits.hits.map(hit => hit._source)
        const bankNotes: BankNote[] = results.map(result => new BankNote(result));
        return bankNotes;
    }

    private elasticClient() {
        return new Client({
            host: `${ELASTICSEARCH_ENDPOINT_WITH_PORT}`,
            log: 'info'
        });
    }
}

export { ElasticSearchService };