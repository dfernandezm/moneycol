const elasticsearch = require("elasticsearch");
const uniqid = require('uniqid');
const csv=require('csvtojson')

const csvFilePath='/Users/david/Desktop/banknotes-usa-es.csv'
const indexName = 'banknotes-catalog-es';

const client = new elasticsearch.Client({
    host: 'localhost:9200',
    log: 'info'
  });

//TODO: use specific indexer for spanish (ascii) to remove accents
const indexBanknote = async (banknote) => {
    let result = await client.index({  
        index: indexName,
        id: uniqid(),
        type: 'banknotes',
        body: banknote
      });
      
    console.log("Indexed: " + result);
}

 async function indexBulk (allBanknotes)  {
    const result = await client.bulk({
            index: indexName,
            type: 'banknotes',
            body : allBanknotes
    });

    console.log(result);
}

async function readCsvToJson()  {
    const jsonObj = await csv().fromFile(csvFilePath);

    let indexLines = [];
    let i = 1;
    jsonObj.forEach(note => {
        indexLines.push({ index:  { _index: indexName, _type: 'banknote', _id: i} });
        indexLines.push(note);
        i++;
    });      
    
    await indexBulk(indexLines);
}

//TODO: Extract from here to its own module
const search = async (language, query) => {
    
    let theIndexName = 'banknotes-catalog';
    if (language !== 'en') {
       theIndexName = "banknotes-catalog-" + language;
    }
    console.log("Searching in index " + theIndexName + ", query is " + query) ;
    // Search in every field but Description and CatalogCode
    // https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html
    const baseQueryString = {
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
    
    const resp = await client.search({
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

const createIndex = async () => {
   const index = await client.indices.create({
        index: 'banknotes-catalog-es'});
}

//readCsvToJson();
//search("en",'United');

module.exports = { search };