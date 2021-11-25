package com.moneycol.indexer.infra.connectivity;

import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.authenticators.GCPAuthenticator;

public class InGkeElasticSearchDiscoveryClient implements ElasticSearchDiscoveryClient {

    //TODO: use DI

    @Override
    public ElasticSearchEndpoint obtainEndpoint() {
        GkeClusterDetails gkeClusterDetails = GkeClusterDetails.builder()
                        .clusterName("cluster-dev2")
                        .projectId("moneycol")
                        .zone("europe-west1-b")
                        .build();

        KubeConfig.registerAuthenticator(new GCPAuthenticator());
        GkeClient gkeClient = GkeClient.builder().build();
        GkeKubeConfig kubeConfig = gkeClient.authenticate(gkeClusterDetails);

        GkeServiceDetails gkeServiceDetails = gkeClient.getServiceDetails(kubeConfig.kubeConfigFilePath(),
                "elasticsearch-nodeport",
                "default");

        return ElasticSearchEndpoint.builder()
                .scheme("http")
                .host(gkeServiceDetails.internalIp())
                .port(gkeServiceDetails.port())
                .build();
    }
}
