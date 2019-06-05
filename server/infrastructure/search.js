const elasticsearch = require("elasticsearch");
const useMockedData = true;
const mockedData = require("../test/testData/sample-data/sampleData_noruega.json");
const client = new elasticsearch.Client({
    host: 'localhost:9200',
    log: 'info'
  });

const search = async (language, query) => {
    
    let theIndexName = 'banknotes-catalog';
    if (language !== 'en') {
       theIndexName = "banknotes-catalog-" + language;
    }
    console.log("Searching in index " + theIndexName + ", query is " + query) ;
    // Search in every field but Description and CatalogCode
    // https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html
    const baseQueryString = {
        "from" : 0, "size" : 24,
        query: {
            
             query_string : {   
                 "query" : query,
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
    let results, total, response;

    if (!useMockedData) {

        const resp = await client.search({
            index: theIndexName,
            body:  baseQueryString
        });
    
        results = resp.hits.hits.map(hit => hit._source)
        total = resp.hits.total;
        response = { results, total };
        console.log("Results: " + JSON.stringify(results));
        console.log("Total: " + resp.hits.total);
    } else {
        results = mockedData;
        total = mockedData.length;
        response = {results, total};

        console.log("--Using mocked data--");
        console.log("Results: " + JSON.stringify(results));
        console.log("Total: " + total);
    }
    
    return response;
}

module.exports = { search };