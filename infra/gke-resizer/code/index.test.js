// Run tests with ./node_modules/jest/bin/jest.js --detectOpenHandles index

let mockNodePoolSize = jest.fn((request) => {

  if (!request.name) {
    throw new Error(`Name must be specified in format 'projects/* /locations/* /clusters/* /nodePools/*'`);
  }

  if (request.nodeCount === undefined || request.nodeCount === null) {
    throw new Error(`nodeCount must be specified`);
  }

  return ["success"]
});

jest.mock('@google-cloud/container', () => {
  return {
      v1: {
        ClusterManagerClient: jest.fn(() => {
            return {
              setNodePoolSize: mockNodePoolSize
            }
        })
    },
  }
});

const indexJs = require('./index');
const expectedClusterResizeDownRequest = {
  name: "projects/moneycol/locations/europe-west1-b/clusters/cluster-dev2/nodePools/indexing-pool",
  nodeCount: 0
}

// data is in expected format:
//{
// "bucketName": "moneycol-import",
// "dataUri": "colnect/13-01-2022"
//}
const PUBSUB_MESSAGE_PAYLOAD = {"@type":"type.googleapis.com/google.pubsub.v1.PubsubMessage",
"attributes":null,
"data":"eyJidWNrZXROYW1lIjoibW9uZXljb2wtaW1wb3J0IiwiZGF0YVVyaSI6ImNvbG5lY3QvMTMtMDItMjAyMiJ9"
};

// data is just {}
const PUBSUB_MESSAGE_PAYLOAD_UNEXPECTED = {"@type":"type.googleapis.com/google.pubsub.v1.PubsubMessage",
"attributes":null,
"data":"e30="
};

const HTTP_REQUEST = { body: {
        projectId:  "moneycol",
        zone:       "europe-west1-b",
        clusterId:  "cluster-dev2",
        nodePoolId: "test",
        nodeCount: 1
}};

beforeEach(() => { jest.clearAllMocks(); });

test('pubsub message invokes node pool resize down for indexing', async () => {
    await indexJs.resizeCluster(PUBSUB_MESSAGE_PAYLOAD, {});
    expect(mockNodePoolSize).toHaveBeenCalledWith(expectedClusterResizeDownRequest);
});

test('pubsub message without expected contents does not trigger resize', async () => {
  await indexJs.resizeCluster(PUBSUB_MESSAGE_PAYLOAD_UNEXPECTED, {});
  expect(mockNodePoolSize).not.toHaveBeenCalled();
});

test('pubsub message is read into json', async () => {
  const mck = jest.spyOn(Buffer, 'from');
  await indexJs.resizeCluster(PUBSUB_MESSAGE_PAYLOAD, {});
  expect(mck).toHaveBeenCalledWith(PUBSUB_MESSAGE_PAYLOAD.data, 'base64');
});

test('http invocation resizes a specific node pool', async () => {
  await indexJs.resizeCluster(HTTP_REQUEST, {});
  const expectedResizeRequestFromHttp = {
    name: `projects/${HTTP_REQUEST.body.projectId}/locations/${HTTP_REQUEST.body.zone}/clusters/${HTTP_REQUEST.body.clusterId}/nodePools/${HTTP_REQUEST.body.nodePoolId}`,
    nodeCount: 1
  }
  expect(mockNodePoolSize).toHaveBeenCalledWith(expectedResizeRequestFromHttp);
});

test('http invocation successful', async () => {
  const res = { status: jest.fn(() => { return { send: jest.fn()}})};
  await indexJs.resizeCluster(HTTP_REQUEST, res);
  expect(res.status).toHaveBeenCalledWith(200);
});