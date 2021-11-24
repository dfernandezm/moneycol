package com.moneycol.indexer.indexing;

import com.google.cloud.container.v1.ClusterManagerClient;
import com.google.container.v1.Cluster;
import com.google.container.v1.ListNodePoolsResponse;
import io.kubernetes.client.extended.kubectl.exception.KubectlException;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeAddress;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("These tests only work locally as they require GKE and Kubectl")
@Tag("system-gke")
public class GkeConnectTest {

    @Test
    public void listServicesInCluster() throws IOException, KubectlException, ApiException {
        // Note: this file needs to be generated previously using one of the other tests:
        // getClusterInformationToAuthenticateWithoutGcloud
        String kubeConfigPath = "/tmp/kubeconfig.yaml";
        // String kubeConfigPath = System.getenv("HOME") + "/.kube/config";

        // loading the out-of-cluster config, a kubeconfig from file-system
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        String ns = "default";
        String name = "elasticsearch-nodeport";

        CoreV1Api api = new CoreV1Api();
        V1Service service = api.readNamespacedService(name, ns, null, true, false);

        assertThat(service.getMetadata()).isNotNull();
        assertThat(service.getSpec()).isNotNull();
        assertThat(service.getMetadata().getName()).isEqualTo(name);
        assertThat(service.getSpec().getType()).isEqualTo("NodePort");
    }

    @Test
    public void listPodsAllNamespaces() throws ApiException, IOException {

        // Note: this file needs to be generated previously using one of the other tests
        String kubeConfigPath = "/tmp/kubeconfig.yaml";

        // loading the out-of-cluster config, a kubeconfig from file-system
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        // invokes the CoreV1Api client
        V1PodList list =
                api.listPodForAllNamespaces(false, null, null,
                        null, null, null, null, null, false);
        for (V1Pod item : list.getItems()) {
            System.out.println(item.getMetadata().getName());
        }

        assertThat(list.getItems()).isNotEmpty();
    }

    /**
     * Given GKE permission exists in the runner of this test
     * And data for the cluster is known: project, zone, clusterName
     * And the cluster exists
     * When listing node pools of this cluster
     * Then non-empty value is returned
     *
     * @throws IOException
     */
    @Test
    public void listNodePoolsInGkeCluster() throws IOException {
        ClusterManagerClient client = ClusterManagerClient.create();
        String projectId = "moneycol";
        String zone = "europe-west1-b";
        String clusterId = "cluster-dev2";
        ListNodePoolsResponse response =
                client.listNodePools(projectId, zone, clusterId);
        assertThat(response.getNodePoolsList()).isNotEmpty();
    }

    /**
     * Given the environment has permission to connect to GKE Cluster control plane (i.e. via Service Account)
     * And the projectId, zone and clusterName are known
     * When getting the cluster Endpoint and CA Certificate
     * Then they're returned correctly
     * And are used to create a temporary kubeconfig.yaml
     * And this kubeconfig.yaml file works to authenticate kubectl commands
     *
     * @throws IOException
     */
    @Test
    public void getClusterInformationToAuthenticateWithoutGcloud() throws IOException {
        ClusterManagerClient client = ClusterManagerClient.create();
        String projectId = "moneycol";
        String zone = "europe-west1-b";
        String clusterId = "cluster-dev2";

        Cluster clusterResponse = client.getCluster(projectId, zone, clusterId);
        String endpoint = clusterResponse.getEndpoint();
        String caCertificateBase64 = clusterResponse.getMasterAuth().getClusterCaCertificate();

        String kubeConfigTemplate = "apiVersion: v1\n" +
                "kind: Config\n" +
                "current-context: my-cluster\n" +
                "contexts: [{name: my-cluster, context: {cluster: cluster-1, user: user-1}}]\n" +
                "users: [{name: user-1, user: {auth-provider: {name: gcp}}}]\n" +
                "clusters:\n" +
                "- name: cluster-1\n" +
                "  cluster:\n" +
                "    server: \"https://%s\"\n" +
                "    certificate-authority-data: \"%s\"";

        String kubeConfigYaml = String.format(kubeConfigTemplate, endpoint, caCertificateBase64);

        Path kubeConfigFilePath = Path.of("/tmp/kubeconfig.yaml");
        Files.write(kubeConfigFilePath, kubeConfigYaml.getBytes(StandardCharsets.UTF_8));

        assertThat(Files.exists(kubeConfigFilePath)).isTrue();
        assertKubectlWorks(kubeConfigFilePath.toAbsolutePath().toString());
    }

    /**
     * Given kubeconfig has been created for a GKE cluster
     * When requesting the nodePort of a known nodeport Service
     * And the External/Internal IP of any node in the cluster is returned
     * Then elasticsearch can be accessed via that ip:nodePort
     *
     * @throws ApiException
     * @throws IOException
     */
    @Test
    public void getsInternalIpAddressFromNodePort() throws ApiException, IOException {

        // String kubeConfigPath = System.getenv("HOME") + "/.kube/config";
        String kubeConfigPath = "/tmp/kubeconfig.yaml";

        // loading the out-of-cluster config, a kubeconfig from file-system
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        // invokes the CoreV1Api client
        V1Service v1Service = api.readNamespacedService("elasticsearch-nodeport", "default", null, true, false);
        assertThat(v1Service.getSpec().getType()).isEqualTo("NodePort");

        // get nodes
        // get one node internal ip
        // api.listNode()
        List<V1ServicePort> ports = v1Service.getSpec().getPorts();

        String nodePort =
                ports.stream()
                        .filter(port -> port.getNodePort() != null)
                        .map(p -> p.getNodePort().toString())
                        .findFirst()
                        .orElse(null);

        assertThat(nodePort).isEqualTo("30200");

        // get the IP address of any node - it should be internal, but for the test we pick the external one
        String ip = getNodeIpByType(api, "ExternalIP");

        assertThat(ip).isNotNull();
        System.out.println("IP: " + ip);

        // Then access elastic
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet("http://" + ip + ":" + nodePort);
        HttpResponse response = httpClient.execute(httpGet);
        String responseValue = EntityUtils.toString(response.getEntity());
        System.out.println(responseValue);

        assertThat(responseValue).contains("\"cluster_name\" : \"moneycol-elasticsearch\",");
    }

    private String getNodeIpByType(CoreV1Api api, String addressType)  {
        try {
            V1NodeList nodes = api.listNode(null, false,
                    null, null, null,
                    null, null, null , false);

            V1Node firstNode = nodes.getItems().stream().findFirst().orElse(null);

            if (firstNode != null) {
                V1NodeAddress internalIp = firstNode.getStatus().getAddresses().stream()
                        .filter(address -> address.getType().equals(addressType))
                        .findFirst()
                        .orElse(null);

                if (internalIp != null) {
                    return internalIp.getAddress();
                }
            }

            throw new RuntimeException("Cannot find any internal address in cluster");
        } catch (ApiException e) {
            throw new RuntimeException("Error accessing Kube API", e);
        }
    }

    private void assertKubectlWorks(String kubeconfigPath) {
        try {
            String cmdLine = "kubectl get pods --kubeconfig " + kubeconfigPath;
            ProcessBuilder pb = new ProcessBuilder(cmdLine.split(" "));
            Process p = pb.start();
            String output = new String(p.getInputStream().readAllBytes());
            System.out.println(output);
        } catch  (IOException ioe) {
            Assertions.fail(ioe);
        }
    }
}
