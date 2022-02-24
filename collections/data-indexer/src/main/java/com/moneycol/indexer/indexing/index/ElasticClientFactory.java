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

    //TODO: these may need to be passed in to the method not constructor
    private final ElasticSearchProperties elasticsearchProperties;
    private final ElasticSearchDiscoveryClient elasticSearchDiscoveryClient;

    @Bean
    @Singleton
    public ElasticSearchClient elasticSearchClient() {
        ElasticSearchEndpoint elasticSearchEndpoint = elasticSearchDiscoveryClient.obtainEndpoint();
        RestHighLevelClient elasticClient = new RestHighLevelClient(
                RestClient.builder(HttpHost.create(elasticSearchEndpoint.getEndpoint())));
        return ElasticSearchClient
                .builder()
                .elasticClient(elasticClient)
                .elasticsearchProperties(elasticsearchProperties)
                .build();

        //elasticsearchProperties, elasticClient);
    }
}
