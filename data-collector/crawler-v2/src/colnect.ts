import { CheerioCrawler, Dataset, EnqueueLinksOptions, createCheerioRouter } from 'crawlee';
import {CheerioAPI, Cheerio, Element } from 'cheerio';
import { BanknoteParser } from './banknote-parser';
import { Banknote } from './banknote';
import fs from 'fs';

export const router = createCheerioRouter();
const colnectUrl = "https://colnect.com";

router.addDefaultHandler(async ({ enqueueLinks, log }) => {
    log.info(`enqueueing new URLs`);

    const enqueueOpts: EnqueueLinksOptions = {
        urls: ['https://colnect.com/en/banknotes/countries'],
        label: 'allCountries',
    }
    
    await enqueueLinks(enqueueOpts); 
   
});

router.addHandler('allCountries', async ({ request, $, log, enqueueLinks }) => {
    

    log.info("Visiting all countries");
    let countriesLinks = $("div.country a");
    let countryUrls: string[] = [];
    countriesLinks.each(function(_, el) {
        let href = $(el).attr('href');
        let countryUrl = colnectUrl + href;
        countryUrl = countryUrl.replace('/series/','/list/');
        countryUrls.push(countryUrl);
        log.info("Sending for process: " + countryUrl);
    });


    await Dataset.pushData({
        url: request.loadedUrl,
        countryUrls: countryUrls,
    });

    //TODO: uncomment to generate test files
    //fs.writeFileSync('./all-countries.html', $.html());

    log.info(`Countries to visit ${countryUrls.length}`);
    const enqueueOpts: EnqueueLinksOptions = {
        urls: countryUrls,
        label: 'singleCountry',
    }

    await enqueueLinks(enqueueOpts);
});

router.addHandler('singleCountry', async ({ request, $, log }) => {
    log.info(`Visiting country url ${request.loadedUrl}`);

    // banknote detail (list with pages)
    let bankNoteDetails: Cheerio<Element> = $("#plist_items div.pl-it");

      //TODO: uncomment to generate test files
    //fs.writeFileSync('./single-page-country.html', $.html());

    //TODO:
    // let totalPages
    // pageLink += /page/{number}
    // enqueue

    if (bankNoteDetails.length > 0) {
        log.info("Found banknote detail");
        await banknoteDetail($, bankNoteDetails);
    }

    // await Dataset.pushData({
    //     url: request.loadedUrl,
    // });

    //TODO: enqueue all pages

});

/**
 * Country and Series from named filters
 * 
 */
const extractFromFilter = ($: CheerioAPI, filterClass: string): string => {
    let filterNameHtml = $(`div.filter_one.${filterClass}`).text();
    let filterName = filterNameHtml.replace("Series:","");
    filterName = filterName.replace("Country:","");
    let length = filterName.length;

    if (filterNameHtml.indexOf("Series")!= -1) {
        console.log("Series: " + filterName);
    }

    let lastIndex = filterName.lastIndexOf("x");
    if (filterName.lastIndexOf("x") + 1 == length) {
        filterName = filterName.substring(0, lastIndex);
    }
    return filterName.trim();
}

export const banknoteDetail = async ($: CheerioAPI, banknoteDetailsBlock: Cheerio<Element>) => {

    let banknotesList = [];
    let countryName = extractFromFilter($,"_flt-country");
    let banknoteCount = banknoteDetailsBlock.length;
   
    let banknotes: Cheerio<Banknote> = banknoteDetailsBlock.map((_, el) => parseBanknote($, el));
    
    let banknoteElements: Banknote[] = Array(banknotes.length).fill(0).map((_, i) => banknotes[i]);

    await Dataset.pushData({
        country: countryName,
        count: banknoteCount
    });

    return banknoteElements;
}

const parseBanknote = ($: CheerioAPI, el: Element): Banknote => {

    const parser = new BanknoteParser();

    let { year, distribution, themes, faceValue, size, composition, 
        hasVariants, catalogCode, desc, banknoteLink } = parser.readBanknoteInfoFromDdDt($, el, "http");

    let valueName = $("h2.item_header a", $(el)).text();

    // Images
    let imageLinkEl = $("div.item_thumb a img", $(el));
    let imageLinkFront = imageLinkEl.length > 0 ? "https:" + imageLinkEl.eq(0).attr("data-src") : "No-front-img";
    let imageLinkBack = imageLinkEl.length > 1 ? "https:" + imageLinkEl.eq(1).attr("data-src") : "No-back-img";

    // replace thumbnails with big imgs
    imageLinkFront = imageLinkFront.replace(/\/t\//g, '/b/'); 
    imageLinkBack = imageLinkBack.replace(/\/t\//g, '/b/');

 return {year: parseInt(year), distribution, themes, faceValue, size, composition, 
    hasVariants, catalogCode, description: desc, originalLink: banknoteLink, series: "", name: valueName, country: '', score: '', imageLinkBack, imageLinkFront};
}