package com.moneycol.indexer.infra.connectivity;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.container.v1.ClusterManagerClient;
import com.google.container.v1.Cluster;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeAddress;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Builder
public class GkeClient {

    //TODO: use DI

    private static final String KUBECONFIG_PATH = "/tmp/kubeconfig.yaml";
    private static final String KUBECONFIG_TEMPLATE = "apiVersion: v1\n" +
            "kind: Config\n" +
            "current-context: my-cluster\n" +
            "contexts: [{name: my-cluster, context: {cluster: cluster-1, user: user-1}}]\n" +
            "users: [{name: user-1, user: {auth-provider: {name: gcp}}}]\n" +
            "clusters:\n" +
            "- name: cluster-1\n" +
            "  cluster:\n" +
            "    server: \"https://%s\"\n" +
            "    certificate-authority-data: \"%s\"";

    /**
     *  String projectId = "moneycol";
     *  String zone = "europe-west1-b";
     *  String clusterId = "cluster-dev2";
     *
     * @param clusterDetails
     */
    public GkeKubeConfig authenticate(GkeClusterDetails clusterDetails) {
        try {

            log.info("Authenticating against GKE using stored credentials");
            // this should use the default credentials depending on where it runs
            ClusterManagerClient client = ClusterManagerClient.create();

            GoogleCredentials googleCredentials = GoogleCredentials.getApplicationDefault();
            log.info("Token: {}", googleCredentials.getAccessToken());

            String projectId = clusterDetails.projectId();
            String zone = clusterDetails.zone();
            String clusterName = clusterDetails.clusterName();

            Cluster clusterResponse = client.getCluster(projectId, zone, clusterName);
            generateKubeConfigFile(clusterResponse);

            log.info("Kubeconfig file generated");
            return GkeKubeConfig
                    .builder()
                    .kubeConfigFilePath(KUBECONFIG_PATH)
                    .build();

        } catch(Throwable t) {
            throw new RuntimeException("Error authenticating against GKE", t);
        }
    }

    /**
     * Obtain basic details from a Service in K8s, knowing its name and namespace.
     *
     * The `kubeConfigPath` points to the location of a valid kubeconfig, already populated to talk
     * to Kube API.
     *
     *  Solution is based off: https://ahmet.im/blog/authenticating-to-gke-without-gcloud/
     * @param kubeConfigPath
     * @param serviceName
     * @param namespace
     * @return
     */
    public GkeServiceDetails getServiceDetails(String kubeConfigPath, String serviceName, String namespace) {
        try {
            log.info("Getting service details from {} for service {} in namespace {}",
                    kubeConfigPath, serviceName, namespace);

            //TODO: the problem here is the kubeconfig file does not have a token
            // when ran with kubectl the token gets appended to the file so it works
            // but when run programmatically only, this does not happen
            // loading the out-of-cluster config, a kubeconfig from file-system

            ApiClient client =
                    ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
            Configuration.setDefaultApiClient(client);

            CoreV1Api api = new CoreV1Api();
            V1Service v1Service = api.readNamespacedService(serviceName, namespace, null, true, false);

            // find the port from NodePort
            String nodePort = readNodePort(v1Service);

            // get the IP address of any node - it should be internal, but for the test we pick the external one
            String internalIp = getNodeIpByType(api, IpAddressType.INTERNAL_IP);

            log.info("Found internal IP {} and port {}", internalIp, nodePort);
            return GkeServiceDetails.builder()
                    .port(nodePort)
                    .internalIp(internalIp)
                    .build();
        } catch(Throwable t) {
            throw new RuntimeException("Error authenticating against GKE", t);
        }
    }

    /**
     * Get a node internal or external IP. If this is for a NodePort service invocation, it does not
     * matter which node is selected.
     *
     * @param api
     * @param ipAddressType
     *
     * @return the IP address of one of the cluster nodes
     */
    private String getNodeIpByType(CoreV1Api api, IpAddressType ipAddressType) {
        try {
            V1NodeList nodes = listNodes(api);
            V1Node firstNode = nodes.getItems().stream().findFirst().orElse(null);

            if (firstNode != null) {
                if (firstNode.getStatus() != null) {
                    V1NodeAddress internalIp = firstNode.getStatus().getAddresses().stream()
                            .filter(address -> address.getType().equals(ipAddressType.toString()))
                            .findFirst()
                            .orElse(null);

                    if (internalIp != null) {
                        return internalIp.getAddress();
                    }
                }
            }

            throw new RuntimeException("Cannot find any internal address in cluster");
        } catch (ApiException e) {
            throw new RuntimeException("Error accessing Kube API", e);
        }
    }

    private V1NodeList listNodes(CoreV1Api api) throws ApiException {
        return api.listNode(null, false,
                null, null, null,
                null, null, null , false);
    }

    private String readNodePort(V1Service v1Service) {
        List<V1ServicePort> ports = v1Service.getSpec().getPorts();
        String nodePort =
                ports.stream()
                        .filter(port -> port.getNodePort() != null)
                        .map(p -> p.getNodePort().toString())
                        .findFirst()
                        .orElse(null);
        return nodePort;
    }

    private void generateKubeConfigFile(Cluster clusterResponse) throws IOException {
        String endpoint = clusterResponse.getEndpoint();
        String caCertificateBase64 = clusterResponse.getMasterAuth().getClusterCaCertificate();

        String kubeConfigYaml = String.format(KUBECONFIG_TEMPLATE, endpoint, caCertificateBase64);
        Path kubeConfigFilePath = Path.of(KUBECONFIG_PATH);
        Files.write(kubeConfigFilePath, kubeConfigYaml.getBytes(StandardCharsets.UTF_8));
    }
}
