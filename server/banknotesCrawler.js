let Crawler = require("crawler");
const csvWriter = require("./csvWriter")

let userAgentStringOld1 = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5"
let userAgentString = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5"
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

let aCrawler = new Crawler({
   // maxConnections : 10
    rateLimit: 1000,
    userAgent: googleUserAgent,
    // This will be called for each country link
    callback : function (error, res, done) {
        if(error){
            console.log(error);
        } else {
            const $ = res.$;

            let links = $("div.pl_list a");
            let bankNoteDetails = $("div.pl-it")
            let countryName = extractCountryName($);

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
                            let imageLinkFront = "https:" + imageLinkEl.eq(0).attr("data-src");
                            let imageLinkBack = "https:" + imageLinkEl.eq(1).attr("data-src");
                            
                            // replacing thumbnails (/t/) with big imgs (/b/)
                            imageLinkFront = imageLinkFront.replace(/\/t\//g,'/b/'); 
                            imageLinkBack = imageLinkBack.replace(/\/t\//g,'/b/');

                            let year = "";
                            if (issueYearLinks.length > 0) {
                                issueYearLinks.each(function(i, el) {
                                    let href = $(el).attr('href');
                                    if (href.indexOf("/year/") !== -1) {
                                        year = $(el).text();
                                    }
                                });
                            }
                            let banknote = {};
                            banknote.banknoteName = valueName;
                            banknote.country = countryName;
                            banknote.year = year;
                            banknote.catalogCode = catalogCode;
                            banknote.desc = desc;
                            banknote.link = banknoteLink;
                            banknote.imageLinkFront = imageLinkFront;
                            banknote.imageLinkBack = imageLinkBack;
                            banknotesList.push(banknote);
                            console.log(valueName + " - " + year + " - " + catalogCode + " - " + banknoteLink + " - " + desc) ;
                        });

                csvWriter.writeCsvRecords(banknotesList);

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
                                //console.log("New page within list: " + url);
                                visitedUrls.push(url);
                                aCrawler.queue(url);
                            }   
                        }
                    });
                }
            }

            links.each(function(i, elem) {
                let linkUrl = $(elem).attr('href')
                aCrawler.queue(colnectUrl + linkUrl)
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
            var $ = res.$;
            console.log("Crawling main list");
            let countriesLinks = $("div.pl_list a");
            countriesLinks.each(function(i, el) {
                let href = $(el).attr('href');
                let countryUrl = colnectUrl + href;
                console.log("Sending for process: " + countryUrl);
                aCrawler.queue(countryUrl);
            });
        }
     }
});

const extractCountryName = ($) => {
    let countryNameHtml = $("div.filter_one a");
    let countryName = "";
    if (countryNameHtml.length > 0) {
        countryName = countryNameHtml.eq(1).text();
        console.log("Countryname: " + countryName);
    }
    return countryName;
}

const moreThanOnePage = ($) => {
    return $("div.navigation_box div a.pager_page").length > 0
}

let albaniaUrl = "https://colnect.com/en/banknotes/series/country/3954-Albania";
let usaUrl = "https://colnect.com/en/banknotes/series/country/3985-United_States_of_America";
let usaUrl2 = "https://colnect.com/en/banknotes/list/country/3985-United_States_of_America/series/103988-Specialized_Issues_-_Continental_Congress";
let afgUrl = "https://colnect.com/en/banknotes/series/country/3953-Afghanistan";
aCrawler.queue(usaUrl2);

//let mainCountriesUrl = "https://colnect.com/es/banknotes/countries";
//countriesCrawler.queue(mainCountriesUrl);