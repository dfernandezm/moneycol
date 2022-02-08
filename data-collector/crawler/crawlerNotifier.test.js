const CrawlerNotifier = require('./crawlerNotifier');
import { BanknotesWriter } from './banknotesWriter';

test('builds message for notification', () => {
   const crawlerNotifier = new CrawlerNotifier();
   const result = crawlerNotifier.buildDoneNotification("moneycol-import")
   //TODO: the date will be changing with the day
   expect(result).toEqual({
    "bucketName": "moneycol-import",
    "dataUri": "colnect/19-01-2022"
   })
});