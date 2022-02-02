/**
 * Responds to any HTTP request.
 * 
 * @param {!express:Request} req HTTP request context.
 * @param {!express:Response} res HTTP response context.
 */
//TODO: automate deployment
 const container = require("@google-cloud/container");
 const client = new container.v1.ClusterManagerClient();
 
 exports.resizeCluster = async (req, res) => {

   // If the function is triggered by pubsub
   // req = message, res = context
   // ---
   // If the function is triggered via HTTP
   // req is the request and JSON is at req.body
   // res is regular response with status() and other functions
   let payload = req.body ? req.body : req;
   
   console.log(`Request Body`, JSON.stringify(payload));
   
   const request = {
     projectId:  payload.projectId,
     zone:       payload.zone,
     clusterId:  payload.clusterId,
     nodePoolId: payload.nodePoolId,
     nodeCount:  payload.nodeCount,
   };
 
   const result = await client.setNodePoolSize(request);
   const operation = result[0];
 
   console.log(operation);

   if (res.status) {
      res.status(200).send('Success');
   }
   
 };
 