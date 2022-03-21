package com.moneycol.indexer.indexing;

import com.moneycol.indexer.TestHelper;
import com.moneycol.indexer.indexing.index.DateUtil;
import com.moneycol.indexer.indexing.index.ElasticClientFactory;
import com.moneycol.indexer.indexing.index.ElasticSearchClient;
import com.moneycol.indexer.indexing.index.ElasticSearchProperties;
import com.moneycol.indexer.worker.BanknotesDataSet;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ElasticSearchClientTest implements TestPropertyProvider  {

    private static final TestHelper testHelper = new TestHelper();

    private static final String ELASTICSEARCH_VERSION = "7.10.2";
    private static final DockerImageName ELASTICSEARCH_CONTAINER = DockerImageName
            .parse("docker.elastic.co/elasticsearch/elasticsearch-oss")
            .withTag(ELASTICSEARCH_VERSION);

    @Rule
    private static final ElasticsearchContainer elasticsearchContainer =
            new ElasticsearchContainer(ELASTICSEARCH_CONTAINER);

    @Inject
    private ElasticSearchClient elasticsearchClient;

    public static DateUtil dateUtilSpy() {
        DateUtil dateUtilSpy = Mockito.spy(DateUtil.builder().build());
        LocalDateTime fixedDate = LocalDateTime.of(2022, 2, 22, 0, 0);
        Mockito.doReturn(fixedDate).when(dateUtilSpy).getTodayDate();
        return dateUtilSpy;
    }

    // https://stackoverflow.com/questions/53132087/overriding-a-dependency-in-a-micronaut-test
    // https://stackoverflow.com/questions/55634575/micronaut-mock-factory-created-beans-in-spock
    @Factory
    public static class ElasticTestClientFactory {

        @Replaces(bean = ElasticSearchClient.class, factory = ElasticClientFactory.class)
        @Singleton
        public ElasticSearchClient elasticSearchClient() {
            RestHighLevelClient elasticClient = getRestHighLevelClient();
            ElasticSearchProperties elasticsearchProperties = new ElasticSearchProperties();
            elasticsearchProperties.setHostAddress(elasticsearchContainer.getHttpHostAddress());
            elasticsearchProperties.setIndexName("banknotes-test");

            DateUtil dateUtilSpy = dateUtilSpy();
            return
                    ElasticSearchClient.builder()
                            .elasticClient(elasticClient)
                            .dateUtil(dateUtilSpy)
                            .elasticsearchProperties(elasticsearchProperties)
                            .build();
        }
    }


    @Test
    public void indexDataset() {
        BanknotesDataSet banknoteDataSet =
                testHelper.readBanknoteDataSetFromJsonFile("testdata/banknotesDataset.json");
        ElasticSearchClient elasticsearchClientSpy = Mockito.spy(elasticsearchClient);

       // LocalDateTime fixedDate = LocalDateTime.of(2022, 2, 22, 0, 0);
       // Mockito.doReturn(fixedDate).when(elasticsearchClientSpy).

        String expectedIndexName = "banknotes-test-22-02-2022";
        elasticsearchClientSpy.index(banknoteDataSet);

        expectIndexToBe(expectedIndexName);
    }

    @Test
    public void aliasIndexTest() {
        BanknotesDataSet banknoteDataSet =
                testHelper.readBanknoteDataSetFromJsonFile("testdata/banknotesDataset.json");
        ElasticSearchClient elasticsearchClientSpy = Mockito.spy(elasticsearchClient);

        //LocalDateTime fixedDate = LocalDateTime.of(2022, 2, 22, 0, 0);
        String expectedIndexName = "banknotes-test-22-02-2022";
        String expectedAlias = "banknotes-test";
        //Mockito.doReturn(fixedDate).when(elasticsearchClientSpy).getTodayDate();

        elasticsearchClientSpy.index(banknoteDataSet);
        elasticsearchClientSpy.updateIndexAlias();

        expectAliasToBe(expectedIndexName, expectedAlias);
    }

    private void expectIndexToBe(String expectedIndexName) {
        try {
            GetIndexRequest request = new GetIndexRequest(expectedIndexName);
            RestHighLevelClient client = getRestHighLevelClient();
            GetIndexResponse getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);
            assertThat(getIndexResponse.getIndices().length).isEqualTo(1);
            assertThat(Arrays.stream(getIndexResponse.getIndices()).findFirst().get()).isEqualTo(expectedIndexName);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void expectAliasToBe(String indexName, String  expectedAlias) {
        Map<String, Set<AliasMetadata>> aliasResponse = getAlias(expectedAlias);
        Assertions.assertNotNull(aliasResponse);
        assertEquals(aliasResponse.get(indexName).size(), 1);
        AliasMetadata am = AliasMetadata.builder(expectedAlias).build();
        assertTrue(aliasResponse.get(indexName).contains(am));
    }

    private Map<String, Set<AliasMetadata>> getAlias(String alias) {
        GetAliasesRequest request = new GetAliasesRequest(alias);
        RestHighLevelClient client = getRestHighLevelClient();
        try {
            GetAliasesResponse response = client.indices().getAlias(request, RequestOptions.DEFAULT);
            System.out.print("aliases " +  response.getAliases());
            return response.getAliases();
        } catch (Exception e) {
            fail("Error obtaining alias", e);
            return null;
        }
    }

    private static RestHighLevelClient getRestHighLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(HttpHost.create(elasticsearchContainer.getHttpHostAddress())));
    }

    // See: https://github.com/micronaut-projects/micronaut-test/issues/32
    @Nonnull
    @Override
    public Map<String, String> getProperties() {
        elasticsearchContainer.start();
        return Map.of("elasticsearch.index-name", "banknotes-test",
                "elasticsearch.host-address", elasticsearchContainer.getHttpHostAddress());
    }
}
