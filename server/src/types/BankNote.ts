export class BankNote {
    readonly country: string;
    readonly banknoteName: string;
    readonly year: string;
    readonly catalogCode: string;
    readonly description: string;
    readonly detailLink: string;
    readonly imageFront: string;
    readonly imageBack: string

    constructor(searchResultJson: any) {
        console.log("SearchResult: ", searchResultJson);
        this.country = searchResultJson.Country;
        this.banknoteName = searchResultJson.BanknoteName;
        this.year = searchResultJson.Year;
        this.catalogCode = searchResultJson.CatalogCode;
        this.description = searchResultJson.Description;
        this.detailLink = searchResultJson.DetailLink;
        this.imageBack = searchResultJson.ImageBack;
        this.imageFront = searchResultJson.ImageFront;
    }

}