 // https://stackoverflow.com/questions/42684177/node-js-es6-classes-with-require
 const fs = require('fs');
 var pathUtil = require('path');
 const { Storage } = require('@google-cloud/storage');
 const PATH = "/Users/david/Desktop/banknotes/colnect/13-01-2022";

 class BanknotesWriter {

    constructor() {
        this.storage = new Storage();
    }

    ensureDirectoryExistence(filePath) {
        var dirname = pathUtil.dirname(filePath);
        if (fs.existsSync(dirname)) {
          return true;
        }
        this.ensureDirectoryExistence(dirname);
        fs.mkdirSync(dirname);
    }

    writeJson(banknoteDataset) {
        const jsonData = JSON.stringify(banknoteDataset);
        const filename = `${banknoteDataset.language}-${banknoteDataset.country}-p-${banknoteDataset.pageNumber}.json`;
        const filePath = `${PATH}/${filename}`;

        this.ensureDirectoryExistence(filePath);
        fs.writeFileSync(filePath, jsonData);
    }

    writeToGcs(banknoteDataset) {
        const jsonData = JSON.stringify(banknoteDataset);
        const filename = `${banknoteDataset.language}-${banknoteDataset.country}-p-${banknoteDataset.pageNumber}.json`;

        const dataUri = process.env.DATA_URI || `colnect/13-01-2022`;
        const filePath = `${dataUri}/${filename}`;

        const bucket = this.storage.bucket("moneycol-import");
        const file = bucket.file(filePath);

        file.save(jsonData, {
            metadata: { contentType: "application/json" },
            validation: 'md5'
        }, (error) => {
        
            if (error) {
                console.log(`Error uploading ${filePath}`, error)
            }
        
            return true;
        });
    }

    
}


module.exports = BanknotesWriter;