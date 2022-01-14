const Crawler = require("crawler");

const Banknote = require("./banknote")
const BanknoteDataset = require("./banknoteDataset")
const BanknotesWriter = require("./banknotesWriter");
let fs = require('fs');

const banknotesWriter = new BanknotesWriter();
let googleUserAgent = "APIs-Google (+https://developers.google.com/webmasters/APIs-Google.html)"
let total = 0;
let totalBanknotes = 0;
const colnectUrl = "https://colnect.com";

// Regex of the country as named on the banknote detail section
const countryRegex = /<\/strong>\:(.*)<a.*/gm;
let visitedUrls = []

let imgDownloadCrawler = new Crawler({
    rateLimit: 1000,
    encoding:null,
    jQuery:false, // set false to suppress warning message.
    callback:function(err, res, done){
        if(err){
            console.error(err.stack);
        }else{
            console.log(`About to download for filename ${res.options.filename}`)
            fs.createWriteStream(res.options.filename).write(res.body);
        }
        done();
    }
});

let mainCrawler = new Crawler({
    // maxConnections : 50,
    rateLimit: 1000,
    userAgent: googleUserAgent,

    // This will be called for each country link
    callback : function (error, res, done) {
        if(error){
            console.log("Error occurred");
            console.log(error);
        } else {
            const $ = res.$;

            // series links
            let links = $("div.pl_list a");

            // banknote detail (list with pages)
            let bankNoteDetails = $("#plist_items div.pl-it")


            // banknote found
            if (bankNoteDetails.length > 0) {

                let banknotesList = [];
                let countryName = extractFromFilter($,"_flt-country");
                let seriesName = extractFromFilter($,"_flt-series");

                bankNoteDetails.each(function(i, elem) {

                    const map = new Map();
                    let group;

                    $('div.i_d dl',$(elem)).children().each((i, dlElem) => {
                        switch (dlElem.name.toLowerCase()) {
                            case "dt":
                            // start a list for this <dt>
                            //console.log($(dlElem).html());
                            map.set($(dlElem).text(), group = []);
                            break;

                            case "dd":
                            // add <dd> to the list for the current <dt>; if there is one.
                            group?.push($(dlElem).text());
                            group = [];
                            break;

                            default:
                            // just in case; 
                            // anyways, where would <dd>s belong that are after something else than a <dt>?
                            // ignore them
                            group = null;
                        }
                    });

                    let hasVariants = false;
                    if (map.get("Variants:")) {
                        hasVariants = true;
                    }

                    let faceValue = "";
                    let banknoteLink = colnectUrl + $("h2.item_header a",$(elem)).attr('href')
                    let year = "";
                    let composition = "";
                    let size = "";
                    let distribution = "";
                    let themes = "";
                    let catalogCode = "";
                    let desc = "";

                    map.forEach((value, key) => {
                        console.log(key + " -> " + value);

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
                    })
                    
                    let valueName = $("h2.item_header a",$(elem)).text()

                    // Images
                    let imageLinkEl = $("div.item_thumb a img",$(elem))
                    let imageLinkFront = imageLinkEl.length > 0 ? "https:" + imageLinkEl.eq(0).attr("data-src") : "No-front-img";
                    let imageLinkBack = imageLinkEl.length > 1 ? "https:" + imageLinkEl.eq(1).attr("data-src") : "No-back-img";
                    imageLinkFront = imageLinkFront.replace(/\/t\//g,'/b/'); // replacing thumbnails with big imgs
                    imageLinkBack = imageLinkBack.replace(/\/t\//g,'/b/');

                    const banknote = new Banknote();
                    banknote.country = countryName;
                    banknote.series = seriesName;
                    banknote.name = valueName;
                    banknote.year = year;

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
                    
                    banknotesList.push(banknote);

                    console.log(`Parsed banknote, total is ${++totalBanknotes}`);
                    console.log(`${JSON.stringify(banknote)}`)
                });

                total++;
                const banknoteDataset = new BanknoteDataset(countryName, total, "en", banknotesList);
                banknotesWriter.writeToGcs(banknoteDataset);
                console.log(">>>Banknotes batch written to json<<<<");

                // navigate page if required
                if (moreThanOnePage($)) {
                    $("div.navigation_box div a.pager_page").each(function(i, el) {
                        let href = $(el).attr('href');
                        // control pagelink are the > and >> to go one page more or to the end
                        let notControl = $('.pager_control',$(el)).length == 0
                        let url = colnectUrl + href;
                        let notFirstPage = !url.endsWith('/page/1');

                        if (!visitedUrls.includes(url)) {
                            if (notControl && notFirstPage) {
                                console.log("New page within list: " + url);
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

const extractLinks = (banknotesLinks) => {
    banknotesLinks.each(function(i, elem) {
        let linkUrl = $(elem).attr('href')
        if (linkUrl) {
            if (linkUrl.indexOf("/banknote/") != -1) {
                console.log("Link: " + linkUrl);
            }   
        }   
    });
}

const countriesCrawler = new Crawler({
    // maxConnections : 10
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
            console.log("countries " + countriesLinks.length)
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
    //console.log("countrynamehtml " + countryNameHtml);

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

const readYear = (issueYearLinks, $) => {
    let year = "";
    if (issueYearLinks.length > 0) {
        issueYearLinks.each(function (i, el) {
            let href = $(el).attr('href');
            if (href.indexOf("/year/") !== -1) {
                year = $(el).text();
            }
        });
    }
    return year;
}

let mainCountriesUrl = "https://colnect.com/en/banknotes/countries";
countriesCrawler.queue(mainCountriesUrl);

// let countryUrl = "https://colnect.com/en/banknotes/series/country/104-Ireland";
// mainCrawler.queue(countryUrl);

//TODO: write some tests for:
// Queue some HTML code directly without grabbing (mostly for tests)
// c.queue([{
//    html: '<p>This is a <strong>test</strong></p>'
// }]);

// Individual countries or series
// let albaniaUrl = "https://colnect.com/en/banknotes/series/country/3954-Albania";
// let usaUrl = "https://colnect.com/en/banknotes/series/country/3985-United_States_of_America";
// let usaUrl2 = "https://colnect.com/en/banknotes/list/country/3985-United_States_of_America/series/103988-Specialized_Issues_-_Continental_Congress";



// let afgUrl = "https://colnect.com/en/banknotes/series/country/1-Afghanistan";
// mainCrawler.queue(afgUrl);