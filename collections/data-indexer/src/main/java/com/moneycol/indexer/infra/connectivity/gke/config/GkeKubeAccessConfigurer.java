package com.moneycol.indexer.infra.connectivity.gke.config;

import com.google.cloud.container.v1.ClusterManagerClient;
import com.google.container.v1.Cluster;
import com.moneycol.indexer.infra.connectivity.gke.GkeClusterDetails;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class GkeKubeAccessConfigurer {

    private static final String KUBECONFIG_PATH = "/tmp/kubeconfig.yaml";

    /**
     * This is the templated kubeconfig.yaml usually created by GKE so 'kubectl'
     * can access the cluster. For that to work it requires:
     *
     * - clusterEndpoint
     * - CA Certificate
     * - Access Token (kubectl adds this)
     * - Expiry (kubectl adds this)
     *
     * The block with user data is:
     * "users: [{name: user-1, user: {auth-provider: {name: gcp, config: {access-token: \"%s\", expiry: \"%s\"}}}}]\n" +
     *
     */
    private static final String KUBECONFIG_TEMPLATE = "apiVersion: v1\n" +
            "kind: Config\n" +
            "current-context: my-cluster\n" +
            "contexts: [{name: my-cluster, context: {cluster: cluster-1, user: user-1}}]\n" +
            "users: [{name: user-1, user: {auth-provider: {name: gcp, config: {}}}}]\n" +
            "clusters:\n" +
            "- name: cluster-1\n" +
            "  cluster:\n" +
            "    server: \"https://%s\"\n" +
            "    certificate-authority-data: \"%s\"";



    public GkeKubeConfig authenticate(GkeClusterDetails clusterDetails) {
        try (ClusterManagerClient client = ClusterManagerClient.create()) {

            log.info("Authenticating against GKE using stored credentials");
            String projectId = clusterDetails.getProjectId();
            String zone = clusterDetails.getZone();
            String clusterName = clusterDetails.getClusterName();

            log.info("Reading properties projectId {}, cluster {}, zone {}", projectId, clusterName, zone);
            Cluster clusterResponse = client.getCluster(projectId, zone, clusterName);
            String endpoint = clusterResponse.getEndpoint();
            String caCertificate = clusterResponse.getMasterAuth().getClusterCaCertificate();

            writeKubeConfigFile(endpoint, caCertificate);

            log.info("Kubeconfig file generated");
            return GkeKubeConfig
                    .builder()
                    .kubeConfigFilePath(KUBECONFIG_PATH)
                    .build();


        } catch(Throwable t) {
            throw new RuntimeException("Error authenticating against GKE", t);
        }
    }

    private void writeKubeConfigFile(String endpoint, String caCertificate) throws IOException {
        String kubeConfigYaml = String.format(KUBECONFIG_TEMPLATE, endpoint, caCertificate);
        Path kubeConfigFilePath = Path.of(KUBECONFIG_PATH);
        Files.write(kubeConfigFilePath, kubeConfigYaml.getBytes(StandardCharsets.UTF_8));
    }
}
