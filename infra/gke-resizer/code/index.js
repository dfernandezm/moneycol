/**
 * Responds to any HTTP request.
 * 
 * @param {!express:Request} req HTTP request context.
 * @param {!express:Response} res HTTP response context.
 */

 const container = require("@google-cloud/container");
 const client = new container.v1.ClusterManagerClient();

const indexingNodePoolFullName = "projects/moneycol/locations/europe-west1-b/clusters/cluster-dev2/nodePools/indexing-pool";
const resizeDownDetails = {
   name: indexingNodePoolFullName,
   nodeCount: 0
 }

 const readPubSubMessage = (req) => {
    const message = req.data;
    const messageContent = Buffer.from(message, 'base64').toString();
    return JSON.parse(messageContent);
 }
 
 exports.resizeCluster = async (req, res) => {

   // If the function is triggered by pubsub
   // req = message, res = context
   // ---
   // If the function is triggered via HTTP
   // req is the request and JSON is at req.body
   // res is regular response with status() and other functions
   
   let payload = {};
   let request;
   if (req.body) {
     // http
     console.log("HTTP function");
     payload = req.body;
     console.log(`HTTP function request body`, JSON.stringify(payload));
     request = {
      name: `projects/${payload.projectId}/locations/${payload.zone}/clusters/${payload.clusterId}/nodePools/${payload.nodePoolId}`,
      nodeCount:  payload.nodeCount,
    };
    res = await processRequest(request, res);
   } else {

    console.log("Pubsub message");
    console.log("Original request", JSON.stringify(req));
     // PubSub
     payload = req;
     payload = readPubSubMessage(req);
     
     // message contents are:
     //{
       //"bucketName": "moneycol-import",
       //"dataUri": "colnect/13-01-2022"
      //}
     if (payload.bucketName) {
      console.log(`Reacting to pubsub message, resizing indexing pool to ${resizeDownDetails.nodeCount}`); 
      console.log(`Request Body`, JSON.stringify(payload));
      request = resizeDownDetails;
      res = await processRequest(request, res);
     } else {
       console.error("Unexpected PubSub message -- ignoring and exiting");
     }
   }
 };

async function processRequest(request, res) {
  const result = await client.setNodePoolSize(request);
  const operation = result[0];

  console.log(operation);

  if (res.status) {
    res.status(200).send('Success');
  } else {
    res = operation;
  }

  console.log("End of execution");
  return res;
}
 