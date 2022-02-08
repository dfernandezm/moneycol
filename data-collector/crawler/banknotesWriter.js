 // https://stackoverflow.com/questions/42684177/node-js-es6-classes-with-require
 const fs = require('fs');
 var pathUtil = require('path');
 const { Storage } = require('@google-cloud/storage');
 const dayjs = require('dayjs');

 const PATH = "/tmp";

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

    writeToGcs(banknoteDataset, fileGuid) {
        const jsonData = JSON.stringify(banknoteDataset);
        const filename = `${banknoteDataset.language}-${banknoteDataset.country}-p-${banknoteDataset.pageNumber}-${fileGuid}.json`;

        const dataUri = `colnect/${this.dateOfToday()}`;
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

        console.log(`Written to filePath: ${filePath}`);
    }
    
    dateOfToday() {
        const now = dayjs();
        const dateOfToday = now.format("DD-MM-YYYY");
        console.info(`Date of today is ${dateOfToday}`);
        return dateOfToday;
    }
}

module.exports = BanknotesWriter;