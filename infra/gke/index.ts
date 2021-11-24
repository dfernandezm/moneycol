import * as k8s from "@pulumi/kubernetes";
import * as pulumi from "@pulumi/pulumi";
import * as gcp from "@pulumi/gcp";

// This config should be passed in instead of hardcode
const name = "cluster-dev2";
const nodePoolName = "elasticsearch-pool";
const location = "europe-west1-b";
const projectId = "moneycol";

const serverlessVpcCidr = "10.21.0.0/28";
const serverlessVpcNetwork = "default";
const serverlessVpcConnectorMachineType = "f1-micro";

// Create a GKE cluster
const engineVersion = gcp.container.getEngineVersions({project: projectId, location: location}).then(v => v.latestMasterVersion);
const cluster = new gcp.container.Cluster(name, {
    name: name,
    project: projectId,
    location: location,
    initialNodeCount: 1,
    minMasterVersion: engineVersion,
    removeDefaultNodePool: true,
    workloadIdentityConfig: { identityNamespace: `${projectId}.svc.id.goog` },
    addonsConfig: {
      configConnectorConfig: { enabled: true }
    },
});

// GKE Custom IAM role with desired permissions for the nodes in the cluster
const gkeNodesCustomRoleName = "gke_nodes_role";
const gkeNodesRole = new gcp.projects.IAMCustomRole(gkeNodesCustomRoleName, {
  project: projectId,
  description: "The IAM Role for the GKE nodes Service Account",
  permissions: [
    // Recommended roles instead of scopes: 
    // https://cloud.google.com/kubernetes-engine/docs/how-to/access-scopes
    // recommended role, 
    //Monitoring Metric Writer
    "monitoring.metricDescriptors.create",
    "monitoring.metricDescriptors.get",
    "monitoring.metricDescriptors.list",
    "monitoring.monitoredResourceDescriptors.get",
    "monitoring.monitoredResourceDescriptors.list",
    "monitoring.timeSeries.create",

    // Monitoring Viewer
    "cloudnotifications.activities.list",
    "monitoring.alertPolicies.get",
    "monitoring.alertPolicies.list",
    "monitoring.dashboards.get",
    "monitoring.dashboards.list",
    "monitoring.groups.get",
    "monitoring.groups.list",
    "monitoring.metricDescriptors.get",
    "monitoring.metricDescriptors.list",
    "monitoring.monitoredResourceDescriptors.get",
    "monitoring.monitoredResourceDescriptors.list",
    "monitoring.notificationChannelDescriptors.get",
    "monitoring.notificationChannelDescriptors.list",
    "monitoring.notificationChannels.get",
    "monitoring.notificationChannels.list",
    "monitoring.publicWidgets.get",
    "monitoring.publicWidgets.list",
    "monitoring.services.get",
    "monitoring.services.list",
    "monitoring.slos.get",
    "monitoring.slos.list",
    "monitoring.timeSeries.list",
    "monitoring.uptimeCheckConfigs.get",
    "monitoring.uptimeCheckConfigs.list",
    "opsconfigmonitoring.resourceMetadata.list",
    "stackdriver.projects.get",

    // Logging writer
    "logging.logEntries.create",

    // Logging viewer
    "logging.buckets.get",
    "logging.buckets.list",
    "logging.exclusions.get",
    "logging.exclusions.list",
    "logging.locations.get",
    "logging.locations.list",
    "logging.logEntries.list",
    "logging.logMetrics.get",
    "logging.logMetrics.list",
    "logging.logServiceIndexes.list",
    "logging.logServices.list",
    "logging.logs.list",
    "logging.operations.get",
    "logging.operations.list",
    "logging.queries.create",
    "logging.queries.delete",
    "logging.queries.get",
    "logging.queries.list",
    "logging.queries.listShared",
    "logging.queries.update",
    "logging.sinks.get",
    "logging.sinks.list",
    "logging.usage.get",
    "logging.views.get",
    "logging.views.list",

    // Object viewer for Container Registry
    "resourcemanager.projects.get",
    "storage.objects.get",
    "storage.objects.list"
  ],
  roleId: gkeNodesCustomRoleName,
  title: "GKE Nodes",
});

// GKE - Service Account for Nodes
// Imported from: 
// pulumi import gcp:serviceAccount/account:Account gke-nodes-moneycol projects/moneycol/serviceAccounts/gke-nodes-moneycol@moneycol.iam.gserviceaccount.com
const gkeNodesServiceAccountName = "gke-nodes-moneycol";
const gkeNodesServiceAccount = new gcp.serviceaccount.Account(gkeNodesServiceAccountName, {
  project: projectId,
  accountId: gkeNodesServiceAccountName,
  disabled: false,
  displayName: "Service Account for GKE cluster nodes",
}, {
  protect: true,
  dependsOn: gkeNodesRole
});

// IAMBinding to update the PROJECT one, it is IMPORTANT to use `new gcp.projects.IAMBinding` and not
// `new gcp.serviceAccount.IAMBinding` or the overall policy for the project won't be updated
const gkeNodesBinding = "project";
const gkeNodesIamBinding = new gcp.projects.IAMBinding(gkeNodesBinding, {
  project: projectId,
  role: gkeNodesRole.name,
  members: [pulumi.interpolate`serviceAccount:${gkeNodesServiceAccount.email}`]
});

// GKE - Node Pool for 
const preemptibleNodes = new gcp.container.NodePool(nodePoolName, {
  name: nodePoolName,
  project: projectId,
  location: location,
  cluster: cluster.name,
  nodeCount: 2,
  nodeConfig: {
      preemptible: true,
      serviceAccount: gkeNodesServiceAccount.email,
      machineType: "n1-standard-1",
      oauthScopes: [
        // Majority of permissions are granted via the service account and not scopes anymore
        "https://www.googleapis.com/auth/cloud-platform"
    ],
  },
}, {dependsOn: [gkeNodesServiceAccount, gkeNodesIamBinding]});

// Export the Cluster name
export const clusterName = cluster.name;

// KubeConfig from the created GKE cluster - only required if deployments are done at creation time
//
// Manufacture a GKE-style kubeconfig. Note that this is slightly "different"
// because of the way GKE requires gcloud to be in the picture for cluster
// authentication (rather than using the client cert/key directly).
export const kubeconfig = pulumi.
    all([ cluster.name, cluster.endpoint, cluster.masterAuth ]).
    apply(([ name, endpoint, masterAuth ]) => {
        const context = `${gcp.config.project}_${gcp.config.zone}_${name}`;
        return `apiVersion: v1
clusters:
- cluster:
    certificate-authority-data: ${masterAuth.clusterCaCertificate}
    server: https://${endpoint}
  name: ${context}
contexts:
- context:
    cluster: ${context}
    user: ${context}
  name: ${context}
current-context: ${context}
kind: Config
preferences: {}
users:
- name: ${context}
  user:
    auth-provider:
      config:
        cmd-args: config config-helper --format=json
        cmd-path: gcloud
        expiry-key: '{.credential.token_expiry}'
        token-key: '{.credential.access_token}'
      name: gcp
`;
    });

// This provider can be used to deploy/query resources in the GKE created cluster
//   
// Create a Kubernetes provider instance that uses our cluster from above.
const clusterProvider = new k8s.Provider(name, {
    kubeconfig: kubeconfig,
});

// This is the Serverless VPC Access to connector to be able to make
// comms between CloudRun / CloudFunctions and GKE in the default VPC
const vpcConnectorName = "moneycolvpcconnectordev";
const serverlessVpcConnector = new gcp.vpcaccess.Connector(vpcConnectorName, {
  project: projectId,
  name: vpcConnectorName,
  region: "europe-west1",
  ipCidrRange: serverlessVpcCidr,
  network: serverlessVpcNetwork,
  machineType: serverlessVpcConnectorMachineType,
  minInstances: 2,
  maxInstances: 3
});


