const Crawler = require("crawler");
const { v4: uuidv4 } = require('uuid');
const Mutex = require('async-mutex').Mutex;
const mutex = new Mutex();

const Banknote = require("./banknote")
const BanknoteDataset = require("./banknoteDataset")
const BanknotesWriter = require("./banknotesWriter");
const CrawlerNotifier = require("./crawlerNotifier");
const crawlerNotifier =  new CrawlerNotifier();
const banknotesWriter = new BanknotesWriter();
const googleUserAgent = "APIs-Google (+https://developers.google.com/webmasters/APIs-Google.html)"
const colnectUrl = "https://colnect.com";

const banknotesMap = new Map();
let totalBanknotes = 0;
let visitedUrls = []

/**
 * Reads the banknote data stored in the `<dt>` and `<dd>` details
 * 
 * @param {*} $ 
 * @param {*} elem 
 * @returns 
 */
const readBanknoteInfoFromDdDt = ($, elem) => {
    const map = new Map();
    let group;

    $('div.i_d dl', $(elem)).children().each((i, dlElem) => {
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

/**
 * Parses a single banknote from a listing page
 * 
 * @param {*} elem 
 * @param {*} $ 
 * @param {*} countryName 
 * @param {*} seriesName 
 * @returns 
 */
const parseBanknoteInfo = (elem, $, countryName, seriesName) => {

    let { year, distribution, themes, faceValue, size, composition, 
        hasVariants, catalogCode, desc, banknoteLink } = readBanknoteInfoFromDdDt($, elem);

    let valueName = $("h2.item_header a", $(elem)).text();

    // Images
    let imageLinkEl = $("div.item_thumb a img", $(elem));
    let imageLinkFront = imageLinkEl.length > 0 ? "https:" + imageLinkEl.eq(0).attr("data-src") : "No-front-img";
    let imageLinkBack = imageLinkEl.length > 1 ? "https:" + imageLinkEl.eq(1).attr("data-src") : "No-back-img";

    // replace thumbnails with big imgs
    imageLinkFront = imageLinkFront.replace(/\/t\//g, '/b/'); 
    imageLinkBack = imageLinkBack.replace(/\/t\//g, '/b/');

    const banknote = new Banknote();
    banknote.country = countryName;
    banknote.series = seriesName;
    banknote.name = valueName;
    banknote.year = year;
    banknote.distribution = distribution;
    banknote.themes = themes;

    banknote.faceValue = faceValue;
    banknote.score = score;
    banknote.size = size;
    banknote.composition = composition;
    banknote.hasVariants = hasVariants;

    //TODO: catalogCode is wrong for link
    // https://colnect.com/en/banknotes/banknote/79878-1_Pound-Specialized_Issues-Antigua_and_Barbuda
    banknote.catalogCode = catalogCode;
    banknote.description = desc;
    banknote.originalLink = banknoteLink;
    banknote.imageLinkFront = imageLinkFront;
    banknote.imageLinkBack = imageLinkBack;
    return banknote;
}

let mainCrawler = new Crawler({
    maxConnections : 10,
    rateLimit: 2000,
    userAgent: googleUserAgent,

    // This will be called for each country link
    callback : async function (error, res, done) {
        if(error){
            console.log("Error occurred");
            console.log(error);
        } else {
            const $ = res.$;
            console.log(`Visiting: ${res.options.uri}`);

            // series links
            let links = $("div.pl_list a");

            // banknote detail (list with pages)
            let bankNoteDetails = $("#plist_items div.pl-it");

            if (links.length == 0 && bankNoteDetails.length == 0) {
                console.log(`[WARNING] Cannot find data in url ${res.options.uri}`);
            }

            // banknote found
            if (bankNoteDetails.length > 0) {

                let banknotesList = [];
                let countryName = extractFromFilter($,"_flt-country");
                let seriesName = extractFromFilter($,"_flt-series");
    
                bankNoteDetails.each(async (i, elem) => {

                    const currentUri = res.options.uri;
                    console.log(`About to parse banknote from ${currentUri}`);
                    const banknote = parseBanknoteInfo(elem, $, countryName, seriesName);
                    banknotesList.push(banknote);

                    //TODO: these numbers don't current match with real website
                    await mutex.runExclusive(() => {
                        console.log("Updating banknote counts in mutex section");
                        totalBanknotes++;
                        let banknotesPerCountryCount = banknotesMap.get(banknote.country);
                        if (banknotesPerCountryCount !== null && banknotesPerCountryCount !== undefined) {
                            banknotesMap.set(banknote.country, ++banknotesPerCountryCount)
                        } else {
                            banknotesMap.set(banknote.country, 1);
                        }
                    });
                    
                    console.log(`Series: ${banknote.series}, ${res.options.uri} -> ${banknote.name}`);
                    console.log(`Banknotes count for country ${banknote.country}: ${banknotesMap.get(banknote.country)}`)
                    console.log(`Total banknotes is ${totalBanknotes}`);
                    console.log(`Country ${banknote.country}: ${JSON.stringify(banknote)}`)
                });

                const banknoteDataset = new BanknoteDataset(countryName, banknotesMap.get(countryName), "en", banknotesList);
                const fileGuid = uuidv4();

                banknotesWriter.writeToGcs(banknoteDataset, fileGuid);
                console.log(">>>Banknotes batch written<<<<");

                // navigate page if required
                if (moreThanOnePage($)) {
                    $("div.navigation_box div a.pager_page").each(function(i, el) {
                        let href = $(el).attr('href');
                        
                        // control pagelink are the > and >> to go one page more or to the end
                        let notControl = $('.pager_control',$(el)).length == 0
                        let url = colnectUrl + href;
                        let notFirstPage = !url.endsWith('/page/1');
                        console.log("Checking url " + url);
                        if (!visitedUrls.includes(url)) {
                            if (notControl && notFirstPage) {
                                console.log("Added to url list: " + url);
                                visitedUrls.push(url);
                                mainCrawler.queue(url);
                            }   
                        }
                    });
                }
            }

            links.each(function(i, elem) {
                let linkUrl = $(elem).attr('href');
                mainCrawler.queue(colnectUrl + linkUrl);
            });
        }

        done();
    }
});

/**
 * Crawler for all banknotes from all countries
 */
const countriesCrawler = new Crawler({
     maxConnections : 20,
     rateLimit: 500,
     userAgent: googleUserAgent,
     // This will be called for each crawled page
     callback : function (error, res, done) {
        if(error){
            console.log(error);
        } else {
            let $ = res.$;
            console.log("Crawling main list");
            let countriesLinks = $("div.country a")
            
            countriesLinks.each(function(i, el) {
                let href = $(el).attr('href');
                let countryUrl = colnectUrl + href;
                console.log("Sending for process: " + countryUrl);
                mainCrawler.queue(countryUrl);
            });
        }
     }
});

/**
 * Country and Series from named filters
 * 
 */
const extractFromFilter = ($, filterClass) => {
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

const moreThanOnePage = ($) => {
    return $("div.navigation_box div a.pager_page").length > 0
}

// let countryUrl = "https://colnect.com/en/banknotes/series/country/104-Ireland";
// mainCrawler.queue(countryUrl);

countriesCrawler.queue("https://colnect.com/en/banknotes/countries");

mainCrawler.on('drain', async () => {
    console.log("Terminating crawler for all countries...");
    await crawlerNotifier.notifyDone();
});