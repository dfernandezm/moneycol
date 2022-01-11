 // https://stackoverflow.com/questions/42684177/node-js-es6-classes-with-require
 const fs = require('fs');
 var pathUtil = require('path');

 const PATH = "/Users/david/Desktop/banknotes/colnect/11-01-2022";

 class BanknotesWriter {

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

    
}


module.exports = BanknotesWriter;