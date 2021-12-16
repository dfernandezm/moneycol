/**
 * Responds to any HTTP request.
 *
 * @param {!express:Request} req HTTP request context.
 * @param {!express:Response} res HTTP response context.
 */

 const container = require("@google-cloud/container");
 const client = new container.v1.ClusterManagerClient();
 
 exports.resizeCluster = async (req, res) => {
   console.log(`Request Body`, JSON.stringify(req.body));
   
   const request = {
     projectId:  req.body.projectId,
     zone:       req.body.zone,
     clusterId:  req.body.clusterId,
     nodePoolId: req.body.nodePoolId,
     nodeCount:  req.body.nodeCount,
   };
 
   const result = await client.setNodePoolSize(request);
   const operation = result[0];
 
   console.log(operation);
   res.status(200).send('Success');
 };
 