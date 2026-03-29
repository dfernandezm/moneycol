// For more information, see https://crawlee.dev/
import { CheerioCrawler, ProxyConfiguration } from 'crawlee';

// To avoid having to add .js extension, need to add Node 18 experimental flag
// https://stackoverflow.com/questions/73449628/how-to-force-typescript-in-node-to-not-require-js-extension-when-importing-es6
import { router } from './colnect';

const startUrls = ['https://colnect.com/en/banknotes'];

const crawler = new CheerioCrawler({
    // proxyConfiguration: new ProxyConfiguration({ proxyUrls: ['...'] }),
    requestHandler: router,
    maxConcurrency: 3,
    minConcurrency: 1,
    maxRequestsPerCrawl: 450,
    maxRequestsPerMinute: 30,
    maxRequestRetries: 15,

});

await crawler.run(startUrls);
