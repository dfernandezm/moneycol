 const { GoogleAuth } = require('google-auth-library');
 const { PubSub } = require('@google-cloud/pubsub');
 const dayjs = require('dayjs')

 const RESIZER_FUNCTION_URL = process.env.RESIZER_FUNCTION_URL || 
 "https://europe-west1-moneycol.cloudfunctions.net/gke-resize"
 
const PROJECT_ID = process.env.PROJECT_ID || "moneycol";
const ZONE = process.env.ZONE || "europe-west1-b";
const CLUSTER_ID = process.env.CLUSTER_ID || "cluster-dev2";
const NODE_POOL_ID =  process.env.NODE_POOL_ID || "indexing-pool";

const GCS_BUCKET =  process.env.GCS_BUCKET || "moneycol-import";
const WEBSITE_NAME = process.env.WEBSITE_NAME || "colnect";
const TOPIC_NAME = process.env.CRAWLER_DONE_TOPIC_NAME || "dev.crawler.events";

 class CrawlerNotifier {

    constructor() {
        this.auth = new GoogleAuth();
        this.pubsubClient = new PubSub(); 
    }

    //This can be done as a trigger of the pubsub dev.crawler.events and not here
    async resizeClusterNodePoolToZero() {
        const endpoint = RESIZER_FUNCTION_URL;
        const targetAudience = RESIZER_FUNCTION_URL  
        console.info(`request ${endpoint} with target audience ${targetAudience} for resizing crawler`);
        const client = await this.auth.getIdTokenClient(targetAudience);
        const body = {
          "projectId": PROJECT_ID,
          "zone": ZONE,
          "clusterId":  CLUSTER_ID,
          "nodePoolId": NODE_POOL_ID,
          "nodeCount":  0
        };
        const res = await client.request({endpoint, body});
        console.info("Request to resize indexing pool sent");
        console.info(res.data);
    }

    async notifyDone() {
        // Publishes the message as a string, e.g. "Hello, world!" or JSON.stringify(someObject)
        const topicName = TOPIC_NAME;
        const data = JSON.stringify(this.buildDoneNotification(GCS_BUCKET));

        //TODO: publishing DONE here should also trigger resizing the node pool back to 0
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