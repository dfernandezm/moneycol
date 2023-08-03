import { CheerioAPI, Cheerio, Element } from 'cheerio';

export class BanknoteParser {


    readBanknoteInfoFromDdDt($: CheerioAPI, elem: Element, colnectUrl: string) {
        const map = new Map();
        let group;

        $('div.i_d dl', $(elem)).children().each((_, dlElem) => {
            switch (dlElem.name.toLowerCase()) {
                case "dt":
                    map.set($(dlElem).text(), group = []);
                    break;

                case "dd":
                    // add <dd> to the list for the current <dt>; if there is one.
                    group?.push($(dlElem).text());
                    group = [];
                    break;

                default:
                    group = null;
            }
        });

        let hasVariants = false;
        if (map.get("Variants:")) {
            hasVariants = true;
        }

        let faceValue = "";
        let banknoteLink = colnectUrl + $("h2.item_header a", $(elem)).attr('href');
        let year = "";
        let composition = "";
        let size = "";
        let distribution = "";
        let themes = "";
        let catalogCode = "";
        let desc = "";
        let score = "";
        let description = "";

        map.forEach((value, key) => {

            if (key === 'Catalog codes:') {
                catalogCode = value[0];
            }

            if (key === 'Issued on:') {
                year = value[0];
            }

            if (key === 'Composition:') {
                composition = value[0];
            }

            if (key === 'Face value:') {
                faceValue = value[0];
            }

            if (key === 'Score:') {
                score = value[0];
            }

            if (key === 'Description:') {
                description = value[0];
            }

            if (key === 'Size:') {
                size = value[0];
            }

            if (key === 'Distribution:') {
                distribution = value[0];
            }

            if (key === 'Themes:') {
                themes = value[0];
            }
        });

        return { year, distribution, themes, faceValue, size, composition, hasVariants, catalogCode, desc, banknoteLink };
    }
}