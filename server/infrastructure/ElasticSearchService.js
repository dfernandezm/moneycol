"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
const elasticsearch_1 = require("elasticsearch");
class ElasticSearchService {
    search(language, searchTerm, from, size) {
        return __awaiter(this, void 0, void 0, function* () {
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
            const resp = yield this.elasticClient().search({
                index: theIndexName,
                body: baseQueryString
            });
            const results = resp.hits.hits.map(hit => hit._source);
            const total = resp.hits.total;
            const response = { results, total };
            console.log("Results: " + JSON.stringify(results));
            console.log("Total: " + resp.hits.total);
            return response;
        });
    }
    elasticClient() {
        return new elasticsearch_1.Client({
            host: 'localhost:9200',
            log: 'info'
        });
    }
}
exports.ElasticSearchService = ElasticSearchService;
