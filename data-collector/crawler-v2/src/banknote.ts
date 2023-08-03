export interface Banknote {

    catalogCode: string;
    series: string;
    name: string;
    year: number;
    country: string;
    faceValue: string;
    score: string;
    description: string;
    hasVariants: boolean;
    composition: string;
    size: string;
    distribution: string;
    themes: string;
    originalLink: string;
    imageLinkFront: string;
    imageLinkBack: string;

    // constructor(catalogCode: string, series: string, name: string, year: number, country: string, 
    //             faceValue: number, score: string, description: string, hasVariants: boolean, composition: string, 
    //             size: string, distribution: string, themes: string, originalLink: string, 
    //             imageLinkFront: string, imageLinkBack: string) {
    //     this.catalogCode = catalogCode;
    //     this.series = series;
    //     this.name = name;
    //     this.year = year;
    //     this.country = country;
    //     this.faceValue = faceValue;
    //     this.score = score;
    //     this.description = description;
    //     this.hasVariants = hasVariants;
    //     this.composition = composition;
    //     this.size = size;
    //     this.distribution = distribution;
    //     this.themes = themes;
    //     this.originalLink = originalLink;
    //     this.imageLinkFront = imageLinkFront;
    //     this.imageLinkBack = imageLinkBack;
    // }
}