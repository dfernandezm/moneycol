 // https://stackoverflow.com/questions/42684177/node-js-es6-classes-with-require
 class BanknoteDataset {

    constructor(country, pageNumber, language, banknotes) {
        this.country = country;
        this.pageNumber = pageNumber;
        this.language = language;
        this.banknotes = banknotes;
    }
}

module.exports = BanknoteDataset;