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
    private static final String KUBECONFIG_TEMPLATE = "apiVersion: v1\n" +
            "kind: Config\n" +
            "current-context: my-cluster\n" +
            "contexts: [{name: my-cluster, context: {cluster: cluster-1, user: user-1}}]\n" +
            //"users: [{name: user-1, user: {auth-provider: {name: gcp, config: {access-token: \"%s\", expiry: \"%s\"}}}}]\n" +
            "users: [{name: user-1, user: {auth-provider: {name: gcp, config: {}}}}]\n" +
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
        try (ClusterManagerClient client = ClusterManagerClient.create()) {

            log.info("Authenticating against GKE using stored credentials");
            // this should use the default credentials depending on where it runs

            String projectId = clusterDetails.getProjectId();
            String zone = clusterDetails.getZone();
            String clusterName = clusterDetails.getClusterName();

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

    private void generateKubeConfigFile(Cluster clusterResponse) throws IOException {
        String endpoint = clusterResponse.getEndpoint();
        String caCertificateBase64 = clusterResponse.getMasterAuth().getClusterCaCertificate();
        String kubeConfigYaml = String.format(KUBECONFIG_TEMPLATE, endpoint, caCertificateBase64);
        Path kubeConfigFilePath = Path.of(KUBECONFIG_PATH);
        Files.write(kubeConfigFilePath, kubeConfigYaml.getBytes(StandardCharsets.UTF_8));
    }
}
