let Crawler = require("crawler");
const csvWriter = require("./csvWriter")

const Banknote = require("./banknote")
const BanknoteDataset = require("./banknoteDataset")
const BanknotesWriter = require("./banknotesWriter");
const banknotesWriter = new BanknotesWriter();

let googleUserAgent = "APIs-Google (+https://developers.google.com/webmasters/APIs-Google.html)"

let total = 0;
let fs = require('fs');



const colnectUrl = "https://colnect.com";

// Regex of the country as named on the banknote detail section
const countryRegex = /<\/strong>\:(.*)<a.*/gm;

let visitedUrls = []

let imgDownloadCrawler = new Crawler({
    rateLimit: 1000,
    encoding:null,
    jQuery:false,// set false to suppress warning message.
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
   // maxConnections : 10
    rateLimit: 1000,
    userAgent: googleUserAgent,

    // This will be called for each country link
    callback : function (error, res, done) {
        if(error){
            console.log("Error occurred");
            console.log(error);
        } else {
            const $ = res.$;

            let links = $("div.pl_list a");
            let bankNoteDetails = $("div.pl-it")
            let countryName = extractCountryName($);

            // banknote found
            if (bankNoteDetails.length > 0) {
                let banknotesList = [];
                bankNoteDetails.each(function(i, elem) {

                            let valueName = $("h2.item_header a",$(elem)).text()
                            let banknoteLink = colnectUrl + $("h2.item_header a",$(elem)).attr('href')
                            let issueYearLinks = $("div.i_d dl dd a",$(elem))
                            let bankNoteData = $("div.i_d dl dd",$(elem))

                            let catalogCode = bankNoteData.eq(0).text()
                            let desc = bankNoteData.eq(6).text()

                            // Images
                            let imageLinkEl = $("div.item_thumb a img",$(elem))
                            let imageLinkFront = imageLinkEl.length > 0 ? "https:" + imageLinkEl.eq(0).attr("data-src") : "No-front-img";
                            let imageLinkBack = imageLinkEl.length > 1 ? "https:" + imageLinkEl.eq(1).attr("data-src") : "No-back-img";
                            imageLinkFront = imageLinkFront.replace(/\/t\//g,'/b/'); // replacing thumbnails with big imgs
                            imageLinkBack = imageLinkBack.replace(/\/t\//g,'/b/');

                            // year
                            let year = readYear(issueYearLinks, $);

                            const banknote = new Banknote();
                            banknote.name = valueName;
                            banknote.country = countryName;
                            banknote.year = year;

                            //TODO: catalogCode is wrong for link
                            // https://colnect.com/en/banknotes/banknote/79878-1_Pound-Specialized_Issues-Antigua_and_Barbuda
                            banknote.catalogCode = catalogCode;
                            banknote.description = desc;
                            banknote.originalLink = banknoteLink;
                            banknote.imageLinkFront = imageLinkFront;
                            banknote.imageLinkBack = imageLinkBack;
                            
                            banknotesList.push(banknote);

                            console.log("Parsed banknote");
                            console.log(`${JSON.stringify(banknote)}`)
                        });

                total++;
                const banknoteDataset = new BanknoteDataset(countryName, total, "en", banknotesList);
                banknotesWriter.writeJson(banknoteDataset);

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
                let linkUrl = $(elem).attr('href')
                mainCrawler.queue(colnectUrl + linkUrl)
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
            //let countriesLinks = $("div.pl_list a");
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

const extractCountryName = ($) => {
    let countryNameHtml = $("div.filter_one a");
    let countryName = "";
    if (countryNameHtml.length > 0) {
        countryName = countryNameHtml.eq(1).text();
        console.log("Countryname: " + countryName); //TODO: returns x
    }
    return countryName;
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

//TODO: write some tests for:
// Queue some HTML code directly without grabbing (mostly for tests)
// c.queue([{
//    html: '<p>This is a <strong>test</strong></p>'
// }]);

// Individual countries or series
// let albaniaUrl = "https://colnect.com/en/banknotes/series/country/3954-Albania";
// let usaUrl = "https://colnect.com/en/banknotes/series/country/3985-United_States_of_America";
// let usaUrl2 = "https://colnect.com/en/banknotes/list/country/3985-United_States_of_America/series/103988-Specialized_Issues_-_Continental_Congress";