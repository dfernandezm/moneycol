class Banknote {

    constructor(catalogCode, series, name, year, country, faceValue, score, 
                description, hasVariants, composition, size, distribution, themes, originalLink, 
                imageLinkFront, imageLinkBack) {
        this.catalogCode = catalogCode;
        this.series = series;
        this.name = name;
        this.year = year;
        this.country = country;
        this.faceValue = faceValue;
        this.score = score;
        this.description = description;
        this.hasVariants = hasVariants;
        this.composition = composition;
        this.size = size;
        this.distribution = distribution;
        this.themes = themes;
        this.originalLink = originalLink;
        this.imageLinkFront = imageLinkFront;
        this.imageLinkBack = imageLinkBack;
    }
}

module.exports = Banknote;