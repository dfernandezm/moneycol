const elasticsearch = require("elasticsearch");
const uniqid = require('uniqid');
const csv = require('csvtojson')

const csvFilePath='/Users/david/Desktop/banknotes-with-links-en.csv'
const indexName = 'banknotes-catalog-en';

const client = new elasticsearch.Client({
    host: 'localhost:9200',
    log: 'info'
  });

//TODO: use specific indexer for spanish (ascii) to remove accents
//TODO: remove fantasy banknotes from search as has many countries ??
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

const createIndex = async () => {
   const index = await client.indices.create({
        index: 'banknotes-catalog-es'});
}

readCsvToJson();