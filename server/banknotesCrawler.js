let Crawler = require("crawler");
let userAgentStringOld1 = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5"
let userAgentString = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5"
let googleUserAgent = "APIs-Google (+https://developers.google.com/webmasters/APIs-Google.html)"

//"Mozilla/5.0 (Windows NT 5.1; rv:7.0.1) Gecko/20100101 Firefox/7.0.1";
let total = 0;
let fs = require('fs');

const baseUrl = "https://www.realbanknotes.com";
const colnectUrl = "https://colnect.com";

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
    // This will be called for each crawled page
    callback : function (error, res, done) {
        if(error){
            console.log(error);
        } else {
            var $ = res.$;

            let links = $("div.pl_list a");
            let bankNoteDetails = $("div.pl-it")
            // let names = $("h2.item_header a");

            // if (names.length > 0) {
            //     names.each(function(i, elem) {
            //         let valueNames = $(elem).text()
            //         console.log("Text: " + valueNames)
            //     });
            // }

            if (bankNoteDetails.length > 0) {
                bankNoteDetails.each(function(i, elem) {
                            let valueName = $("h2.item_header a",$(elem)).text()
                            let issueYearLinks = $("div.i_d dl dd a",$(elem))
                            let bankNoteData = $("div.i_d dl dd",$(elem))
                            bankNoteData.each(function(i, el) {
                                let vals = $(el).html()
                                //console.log("<<<<" + vals + " " + i + ">>>>>" + "\n");
                            });

                            let catalogCode = bankNoteData.eq(0).text()
                            let desc = bankNoteData.eq(6).text()

                            //console.log("banknotedata: " + catalogCode)
                            let year = "";
                            if (issueYearLinks.length > 0) {
                                issueYearLinks.each(function(i, el) {
                                    let href = $(el).attr('href');
                                    if (href.indexOf("/year/") !== -1) {
                                        year = $(el).text();
                                    }
                                });
                            }

                            console.log(valueName + " - " + year + " - " + catalogCode + " - " + desc) ;
                            
                        });
            }


            //console.log("Links: " + links.html());
            links.each(function(i, elem) {
                let linkUrl = $(elem).attr('href')
                console.log("Href: " + linkUrl)
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
                //aCrawler.queue(baseUrl + linkUrl);
            }   
        }   
    });
}
let albaniaUrl = "https://colnect.com/en/banknotes/series/country/3954-Albania"
let afgUrl = "https://colnect.com/en/banknotes/series/country/3953-Afghanistan"
aCrawler.queue(albaniaUrl)