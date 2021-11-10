import * as k8s from "@pulumi/kubernetes";
import * as pulumi from "@pulumi/pulumi";
import * as gcp from "@pulumi/gcp";

// This config should be passed in instead of hardcode
const name = "cluster-dev2";
const nodePoolName = "elasticsearch-pool";
const location = "europe-west1-b";
const projectId = "moneycol";

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

// IAM Policy (may not be required, need to test removing this block in separate project to avoid lock-out)
// How to add to current IAMpolicy
// See: https://cloud.google.com/kubernetes-engine/docs/how-to/hardening-your-cluster
// and TF here: https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/google_service_account_iam#google_service_account_iam_binding  
const currentProjectPolicy = new gcp.projects.IAMPolicy("moneycol_project", {
  policyData: "{\"bindings\":[{\"members\":[\"serviceAccount:461081581931@cloudbuild.gserviceaccount.com\"],\"role\":\"roles/cloudbuild.builds.builder\"},{\"members\":[\"serviceAccount:service-461081581931@gcp-sa-cloudbuild.iam.gserviceaccount.com\"],\"role\":\"roles/cloudbuild.serviceAgent\"},{\"members\":[\"serviceAccount:service-461081581931@gcf-admin-robot.iam.gserviceaccount.com\"],\"role\":\"roles/cloudfunctions.serviceAgent\"},{\"members\":[\"serviceAccount:service-461081581931@gcp-sa-cloudscheduler.iam.gserviceaccount.com\"],\"role\":\"roles/cloudscheduler.serviceAgent\"},{\"members\":[\"serviceAccount:service-461081581931@compute-system.iam.gserviceaccount.com\"],\"role\":\"roles/compute.serviceAgent\"},{\"members\":[\"serviceAccount:461081581931@cloudbuild.gserviceaccount.com\",\"serviceAccount:gke-resizer@moneycol.iam.gserviceaccount.com\"],\"role\":\"roles/container.admin\"},{\"members\":[\"serviceAccount:service-461081581931@container-engine-robot.iam.gserviceaccount.com\"],\"role\":\"roles/container.serviceAgent\"},{\"members\":[\"serviceAccount:collections-api@moneycol.iam.gserviceaccount.com\"],\"role\":\"roles/datastore.user\"},{\"members\":[\"serviceAccount:461081581931-compute@developer.gserviceaccount.com\",\"serviceAccount:461081581931@cloudservices.gserviceaccount.com\",\"serviceAccount:moneycol@appspot.gserviceaccount.com\",\"serviceAccount:service-461081581931@containerregistry.iam.gserviceaccount.com\",\"user:ga.christov@gmail.com\"],\"role\":\"roles/editor\"},{\"members\":[\"serviceAccount:indexer-batcher@moneycol.iam.gserviceaccount.com\",\"serviceAccount:test-account@moneycol.iam.gserviceaccount.com\"],\"role\":\"roles/firebase.admin\"},{\"members\":[\"serviceAccount:firebase-service-account@firebase-sa-management.iam.gserviceaccount.com\"],\"role\":\"roles/firebase.managementServiceAgent\"},{\"members\":[\"serviceAccount:firebase-adminsdk-c8avz@moneycol.iam.gserviceaccount.com\"],\"role\":\"roles/firebase.sdkAdminServiceAgent\"},{\"members\":[\"serviceAccount:collections-api@moneycol.iam.gserviceaccount.com\"],\"role\":\"roles/firebaseauth.admin\"},{\"members\":[\"serviceAccount:collections-api@moneycol.iam.gserviceaccount.com\"],\"role\":\"roles/firebaseauth.viewer\"},{\"members\":[\"serviceAccount:service-461081581931@firebase-rules.iam.gserviceaccount.com\"],\"role\":\"roles/firebaserules.system\"},{\"members\":[\"serviceAccount:service-461081581931@gcp-sa-firestore.iam.gserviceaccount.com\"],\"role\":\"roles/firestore.serviceAgent\"},{\"members\":[\"serviceAccount:collections-api@moneycol.iam.gserviceaccount.com\",\"serviceAccount:firebase-adminsdk-c8avz@moneycol.iam.gserviceaccount.com\"],\"role\":\"roles/iam.serviceAccountTokenCreator\"},{\"members\":[\"serviceAccount:example-moneycol@moneycol.iam.gserviceaccount.com\",\"serviceAccount:moneycol1@moneycol.iam.gserviceaccount.com\",\"user:morenza@gmail.com\"],\"role\":\"roles/owner\"},{\"members\":[\"serviceAccount:gcs-buckets@moneycol.iam.gserviceaccount.com\",\"serviceAccount:indexer-batcher@moneycol.iam.gserviceaccount.com\"],\"role\":\"roles/pubsub.publisher\"},{\"members\":[\"serviceAccount:indexer-batcher@moneycol.iam.gserviceaccount.com\"],\"role\":\"roles/pubsub.subscriber\"},{\"members\":[\"serviceAccount:service-461081581931@cloud-redis.iam.gserviceaccount.com\"],\"role\":\"roles/redis.serviceAgent\"},{\"members\":[\"serviceAccount:service-461081581931@serverless-robot-prod.iam.gserviceaccount.com\"],\"role\":\"roles/run.serviceAgent\"},{\"members\":[\"serviceAccount:461081581931@cloudbuild.gserviceaccount.com\"],\"role\":\"roles/secretmanager.admin\"},{\"members\":[\"serviceAccount:collections-api@moneycol.iam.gserviceaccount.com\",\"serviceAccount:gcs-buckets@moneycol.iam.gserviceaccount.com\",\"serviceAccount:indexer-batcher@moneycol.iam.gserviceaccount.com\"],\"role\":\"roles/storage.admin\"},{\"members\":[\"serviceAccount:collections-api@moneycol.iam.gserviceaccount.com\",\"serviceAccount:gcs-buckets@moneycol.iam.gserviceaccount.com\",\"serviceAccount:indexer-batcher@moneycol.iam.gserviceaccount.com\"],\"role\":\"roles/storage.objectAdmin\"},{\"members\":[\"serviceAccount:gcs-buckets@moneycol.iam.gserviceaccount.com\"],\"role\":\"roles/storage.objectViewer\"}]}",
  project: "moneycol",
}, {
  protect: true,
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