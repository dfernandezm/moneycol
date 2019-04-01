const elasticsearch = require("elasticsearch");
const uniqid = require('uniqid');
const csv=require('csvtojson')

const csvFilePath='/Users/david/Desktop/banknotes-usa-es.csv'
const indexName = 'banknotes-catalog-es';

const client = new elasticsearch.Client({
    host: 'localhost:9200',
    log: 'info'
  });

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

const search = async (language, query) => {
    
    let theIndexName = 'banknotes-catalog';
    if (language !== 'en') {
       theIndexName = "banknotes-catalog-" + language;
    }

    // Search in every field but Description
    const baseQueryString = {
        query: {
             query_string : {
                 "query" : query,
                 "fields": [
                    "BanknoteName",
                    "Year",
                    "Country",
                    "CatalogCode"
                  ]
             }   
         }
     };
    
    const resp = await client.search({
        index: theIndexName,
        body:  baseQueryString
    });

    const results = resp.hits.hits.map(hit => hit._source)
    const total = resp.hits.total;
    const response = { results, total}
    console.log(results);
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