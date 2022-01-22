package com.moneycol.indexer.indexing;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class ElasticSearchRestClientTest {

    private static final String ELASTICSEARCH_VERSION = "7.10.2";
    private static final DockerImageName ELASTICSEARCH_CONTAINER = DockerImageName
            .parse("docker.elastic.co/elasticsearch/elasticsearch-oss")
            .withTag(ELASTICSEARCH_VERSION);
    private static final ElasticsearchContainer elasticsearchContainer =
            new ElasticsearchContainer(ELASTICSEARCH_CONTAINER);

    private static RestHighLevelClient elasticClient;

    @BeforeAll
    public static void setup() {
        elasticsearchContainer.start();
        String elasticHostAddress = elasticsearchContainer.getHttpHostAddress();
        elasticClient = new RestHighLevelClient(
                RestClient.builder(HttpHost.create(elasticHostAddress)));
    }

    @AfterAll
    public static void tearDown() {
        elasticsearchContainer.close();
    }

    @Test
    public void clusterHealthIsGreenOnStartup() {
        try {
            ClusterHealthRequest request = new ClusterHealthRequest();
            ClusterHealthResponse response = elasticClient.cluster().health(request, RequestOptions.DEFAULT);
            assertThat(response.getStatus()).isEqualTo(ClusterHealthStatus.GREEN);
        } catch (Exception e) {
            fail(e);
        }
    }
}
