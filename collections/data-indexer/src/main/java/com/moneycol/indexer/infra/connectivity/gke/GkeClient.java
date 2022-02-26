package com.moneycol.indexer.infra.connectivity.gke;

import com.moneycol.indexer.infra.connectivity.gke.data.IpAddressType;
import com.moneycol.indexer.infra.connectivity.gke.data.KubeServiceDetails;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeAddress;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.List;

@Slf4j
@Singleton
public class GkeClient {

    private final ApiClient apiClient;

    public GkeClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Obtain basic details from a Service in K8s, knowing its name and namespace.
     *
     *  Solution is based off: https://ahmet.im/blog/authenticating-to-gke-without-gcloud/
     * @param serviceName
     * @param namespace
     * @return
     */
    public KubeServiceDetails getServiceDetails(String serviceName, String namespace) {
        try {
            log.info("Getting service details for service {} in namespace {}", serviceName, namespace);

            CoreV1Api api = new CoreV1Api(apiClient);
            V1Service v1Service = api.readNamespacedService(serviceName, namespace,
                    null, true, false);

            // find the port from NodePort
            String nodePort = readNodePort(v1Service);

            // get the internal IP address of any node
            String internalIp = getNodeIpByType(api, IpAddressType.INTERNAL_IP);

            log.info("Found internal IP {} and port {}", internalIp, nodePort);
            return KubeServiceDetails.builder()
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
}
