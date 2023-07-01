// For more information, see https://crawlee.dev/
import { CheerioCrawler, ProxyConfiguration } from 'crawlee';

// To avoid having to add .js extension, need to add Node 18 experimental flag
// https://stackoverflow.com/questions/73449628/how-to-force-typescript-in-node-to-not-require-js-extension-when-importing-es6
import { router } from './routes';

const startUrls = ['https://crawlee.dev'];

const crawler = new CheerioCrawler({
    // proxyConfiguration: new ProxyConfiguration({ proxyUrls: ['...'] }),
    requestHandler: router,
});

await crawler.run(startUrls);
