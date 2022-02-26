package com.moneycol.indexer.indexing.index;

import com.moneycol.indexer.infra.connectivity.ElasticSearchDiscoveryClient;
import com.moneycol.indexer.infra.connectivity.ElasticSearchEndpoint;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import javax.inject.Singleton;

@Factory
@RequiredArgsConstructor
public class ElasticClientFactory {

    @Bean
    @Singleton
    public ElasticSearchClient elasticSearchClient(ElasticSearchProperties elasticsearchProperties,
    ElasticSearchDiscoveryClient elasticSearchDiscoveryClient) {
        ElasticSearchEndpoint elasticSearchEndpoint = elasticSearchDiscoveryClient.obtainEndpoint();
        RestHighLevelClient elasticClient = new RestHighLevelClient(
                RestClient.builder(HttpHost.create(elasticSearchEndpoint.getEndpoint())));
        return ElasticSearchClient
                .builder()
                .elasticClient(elasticClient)
                .elasticsearchProperties(elasticsearchProperties)
                .build();
    }
}
