//https://www.npmjs.com/package/csv-writer
const createCsvWriter = require('csv-writer').createObjectCsvWriter;
const csvPath = '/Users/david/Desktop/banknotes-usa-link-es.csv';
const csvWriter = createCsvWriter({
    path: csvPath,
    header: [
        {id: 'country', title: 'Country'},
        {id: 'banknoteName', title: 'BanknoteName'},
        {id: 'year', title: 'Year'},
        {id: 'catalogCode', title: 'CatalogCode'},
        {id: 'desc', title: 'Description'},
        {id: 'link', title: 'DetailLink'},
        {id: 'imageLinkFront', title: 'ImageFront'},
        {id: 'imageLinkBack', title: 'ImageBack'}
    ]
});

const writeCsvRecords = (records) => {
    return csvWriter.writeRecords(records)
    .then(() => {
        console.log('Batch of records written');
    });
}

module.exports = { writeCsvRecords };


