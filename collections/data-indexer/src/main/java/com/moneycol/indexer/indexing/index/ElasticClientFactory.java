package com.moneycol.indexer.indexing.index;

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

    private final ElasticSearchProperties elasticsearchProperties;

    @Bean
    @Singleton
    public ElasticSearchClient elasticSearchClient() {
        RestHighLevelClient elasticClient = new RestHighLevelClient(
                RestClient.builder(HttpHost.create(elasticsearchProperties.getHostAddress())));
        return new ElasticSearchClient(elasticsearchProperties, elasticClient);
    }
}
