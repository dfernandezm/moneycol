package com.moneycol.indexer.indexing;

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
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static io.kubernetes.client.extended.kubectl.Kubectl.get;
import static org.assertj.core.api.Assertions.assertThat;

//@HoverflyCore(config = @HoverflyConfig(destination = {"oauth2.googleapis.com", "storage.googleapis.com"}))
//@HoverflyCapture(filename = "kubernetes-get-pods.json")
//@ExtendWith(HoverflyExtension.class)
public class ElasticSearchConnectTest {

    @Test
    public void listServicesInCluster() throws IOException, KubectlException {
        String kubeConfigPath = System.getenv("HOME") + "/.kube/config";

        // loading the out-of-cluster config, a kubeconfig from file-system
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        String ns = "default";
        String name = "elasticsearch-nodeport";
        V1Service service =
                get(V1Service.class)
                        .name(name)
                        .apiClient(client)
                        .namespace(ns)
                        .execute();
        assertThat(service.getKind()).isEqualTo("NodePort");
    }

    @Test
    public void listPodsAllNamespaces() throws ApiException, IOException {
        String kubeConfigPath = System.getenv("HOME") + "/.kube/config";

        // loading the out-of-cluster config, a kubeconfig from file-system
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        // invokes the CoreV1Api client
        V1PodList list =
                api.listPodForAllNamespaces(false, null, null, null, null, null, null, null, false);
        for (V1Pod item : list.getItems()) {
            System.out.println(item.getMetadata().getName());
        }
    }


    // Obtain the kubeconfig / configuration via the GKE client lib
    // https://github.com/googleapis/java-container

    @Test
    public void authenticateGkeFromCode() {

    }

    /**
     * Given kubeconfig has been created for a GKE cluster
     * When requesting the nodePort of a known nodeport Service
     * And the External/Internal IP of any node in the cluster
     * Then elasticsearch can be accessed via ip:nodePort
     *
     * @throws ApiException
     * @throws IOException
     */
    @Test
    public void getsInternalIpAddressFromNodePort() throws ApiException, IOException {

        String kubeConfigPath = System.getenv("HOME") + "/.kube/config";

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

        // get the internal ip address of any node
        String ip = getNodeIpByType(api, "ExternalIP");

        assertThat(ip).isNotNull();
        System.out.println("IP: " + ip);

        // Then access elastic
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet("http://" + ip + ":" + nodePort);
        HttpResponse response = httpClient.execute(httpGet);

        System.out.println(EntityUtils.toString(response.getEntity()));
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
}
