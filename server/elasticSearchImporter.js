const elasticsearch = require("elasticsearch");
const uniqid = require('uniqid');
const csvFilePath='/Users/david/Desktop/banknotes-usa-es.csv'
const csv=require('csvtojson')


const client = new elasticsearch.Client({
    host: 'localhost:9200',
    log: 'trace'
  });

const indexBanknote = async (banknote) => {
    let result = await client.index({  
        index: 'banknotes-catalog',
        id: uniqid(),
        type: 'banknotes',
        body: banknote
      });
      
    console.log("Indexed: " + result);
}

 async function indexBulk (allBanknotes)  {
    const result = await client.bulk({
            index: 'banknotes-catalog',
            type: 'banknotes',
            body : allBanknotes
    });

    console.log(result);
}

async function readCsvToJson()  {
    //TODO: index in multiple languages
   const index = await client.indices.create({
        index: 'banknotes-catalog-es'});

    const jsonObj = await csv().fromFile(csvFilePath);

    let indexLines = [];
    let i = 1;
    jsonObj.forEach(note => {
        indexLines.push({ index:  { _index: 'banknotes-catalog', _type: 'banknote', _id: i} });
        indexLines.push(note);
        i++;
    });      
    
    await indexBulk(indexLines);
}

const search = async (query) => {
    const resp = await client.search({
        index: 'banknotes-catalog',
        body: {
           query: {
                query_string : {
                    "query" : query
                }
            }
        }
      })
    
      console.log(resp.hits.hits.map(hit => hit._source))
}

//readCsvToJson();
search('United States 10 Dollars');