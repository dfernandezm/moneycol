package com.moneycol.indexer.infra.connectivity.gke;

import com.moneycol.indexer.infra.connectivity.ElasticSearchDiscoveryClient;
import com.moneycol.indexer.infra.connectivity.ElasticSearchEndpoint;
import com.moneycol.indexer.infra.connectivity.gke.data.KubeServiceDetails;
import io.micronaut.context.annotation.Primary;

import javax.inject.Singleton;

@Primary
@Singleton
public class InGkeElasticSearchDiscoveryClient implements ElasticSearchDiscoveryClient {

    private final GkeClient gkeClient;
    private final GkeClusterDetails gkeClusterDetails;

    public InGkeElasticSearchDiscoveryClient(GkeClient gkeClient, GkeClusterDetails gkeClusterDetails) {
        this.gkeClient = gkeClient;
        this.gkeClusterDetails = gkeClusterDetails;
    }

    @Override
    public ElasticSearchEndpoint obtainEndpoint() {
        KubeServiceDetails kubeServiceDetails = gkeClient.getServiceDetails(
                gkeClusterDetails.getElasticsearchServiceName(),
                gkeClusterDetails.getElasticsearchServiceNamespace());

        return ElasticSearchEndpoint.builder()
                .scheme("http")
                .host(kubeServiceDetails.internalIp())
                .port(kubeServiceDetails.port())
                .build();
    }
}
