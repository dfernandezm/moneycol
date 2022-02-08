 const { GoogleAuth } = require('google-auth-library');
 const { PubSub } = require('@google-cloud/pubsub');
 const dayjs = require('dayjs')

const GCS_BUCKET =  process.env.GCS_BUCKET || "moneycol-import";
const WEBSITE_NAME = process.env.WEBSITE_NAME || "colnect";
const TOPIC_NAME = process.env.CRAWLER_DONE_TOPIC_NAME || "dev.crawler.events";

 class CrawlerNotifier {

    constructor() {
        this.auth = new GoogleAuth();
        this.pubsubClient = new PubSub(); 
    }

    async notifyDone() {
        // Publishes the message as a string, e.g. "Hello, world!" or JSON.stringify(someObject)
        const topicName = TOPIC_NAME;
        const data = JSON.stringify(this.buildDoneNotification(GCS_BUCKET));

        // publishing DONE also triggers resizing the node pool back to 0
        // using the pubsub topic as trigger
        const dataBuffer = Buffer.from(data);

        try {
            const messageId = await this.pubsubClient.topic(topicName).publish(dataBuffer);
            console.log(`Message ${messageId} published.`);
        } catch (error) {
            console.error(`Received error while publishing: ${error.message}`);
            process.exitCode = 1;
        }
    }    

     buildDoneNotification(bucketName) {
         const now = dayjs();
         const dateOfToday = now.format("DD-MM-YYYY");
         console.info(`Date of today is ${dateOfToday}`);

         const data = {
             bucketName: bucketName,
             dataUri: WEBSITE_NAME + "/" + dateOfToday,
         };
         return data;
     }
}

module.exports = CrawlerNotifier;