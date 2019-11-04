let Crawler = require("crawler");
let userAgentString = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5"
let total = 0;
let fs = require('fs');


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
    userAgent: userAgentString,
    // This will be called for each crawled page
    callback : function (error, res, done) {
        if(error){
            console.log(error);
        }else{
            var $ = res.$;
            let tablesWithCoins = $("table.coin")
            tablesWithCoins.each(function(i, elem) {
                let cellsInTable = $('td img',this);
                let imgUri1 = cellsInTable.eq(0).attr('src');
                let imgUri2 = cellsInTable.eq(1).attr('src');
                
                if (imgUri1.indexOf('noimage') == -1 ){
                    console.log("A: "+(i+1),imgUri1);
                    let imgFilename1 = filenameFromUri("a",imgUri1)
                    console.log("File: " + imgFilename1);

                    imgDownloadCrawler.queue({
                        uri: imgUri1,
                        filename: imgFilename1
                     });
                    total++;
                }

                if (imgUri2.indexOf('noimage') == -1) {
                    console.log("R: "+(i+1),imgUri2);
                    let imgFilename2 = filenameFromUri("r",imgUri2)
                    console.log("File: " + imgFilename2);

                    imgDownloadCrawler.queue({
                        uri: imgUri2,
                        filename: imgFilename2
                     });
                    total++;
                }
                
                
            });
            
            console.log("Number: " + total);
            //console.log($("table.coin tr.coin-img td img").attr('src'));
            //console.log("<<<<<<<<<<<<<<<<<<<<<<<")
        }
        done();
    }
});

aCrawler.queue("<url here>");

const filenameFromUri = (aOrR, uri) => {
    return aOrR + "-" + uri.substring(uri.lastIndexOf('/')+1,uri.length);
}