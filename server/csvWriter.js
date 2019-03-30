//https://www.npmjs.com/package/csv-writer
const createCsvWriter = require('csv-writer').createObjectCsvWriter;

const csvWriter = createCsvWriter({
    path: '/Users/david/Desktop/banknotes-usa-es.csv',
    header: [
        {id: 'country', title: 'Country'},
        {id: 'banknoteName', title: 'BanknoteName'},
        {id: 'year', title: 'Year'},
        {id: 'catalogCode', title: 'CatalogCode'},
        {id: 'desc', title: 'Description'}
    ]
});
 
const sampleRecords = [
    {name: 'Bob',  lang: 'French, English'},
    {name: 'Mary', lang: 'English'}
];


const writeCsvRecords = (records) => {
    return csvWriter.writeRecords(records)
    .then(() => {
        console.log('Batch of records written');
    });
}

module.exports = { writeCsvRecords };


