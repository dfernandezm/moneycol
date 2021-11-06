package com.moneycol.indexer.indexing;

import com.moneycol.indexer.TestHelper;
import com.moneycol.indexer.infra.JsonWriter;
import com.moneycol.indexer.worker.BanknoteData;
import com.moneycol.indexer.worker.BanknotesDataSet;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class ElasticSearchRestClientTest {

    private static final TestHelper testHelper = new TestHelper();

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

    // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-document-index.html
    @Test
    public void indexBasicDocumentCorrectly() {
        BanknoteData banknoteData =
                testHelper.readBanknoteDataFromJsonFile("testdata/single-banknote.json");
        Map<String, Object> jsonMap = testHelper.readBanknoteDataToMap("testdata/single-banknote.json");

        // It only works with jsonMap, not directly with object
        IndexRequest indexRequest = new IndexRequest("banknotes-test")
                .type("banknotes")
                .id(banknoteData.getCatalogCode()).source(jsonMap);

        try {
            IndexResponse indexResponse = elasticClient.index(indexRequest, RequestOptions.DEFAULT);

            String index = indexResponse.getIndex();
            String id = indexResponse.getId();

            if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                assertThat(index).isEqualTo("banknotes-test");
                assertThat(id).isEqualTo(banknoteData.getCatalogCode());
            } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                fail("Index was updated instead of created");
            }

        } catch (Exception e) {
            fail(e);
        }
    }

    // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-bulk.html
    @Test
    public void indexBanknotesDatasetCorrectly() {
        BanknotesDataSet banknoteDataSet =
                testHelper.readBanknoteDataSetFromJsonFile("testdata/banknotesDataset.json");

        JsonWriter jsonWriter = JsonWriter.builder().build();
        BulkRequest banknotesDatasetBulk = new BulkRequest();
        String indexName = "banknotes-test";

        List<IndexRequest> indexRequestList = banknoteDataSet.getBanknotes().stream()
                .map(jsonWriter::toMap)
                .map(jsonMap ->
                        new IndexRequest(indexName)
                                .id(jsonMap.get("catalogCode"))
                                // this seems to be required in elasticsearch 6.5
                                .type("banknotes")
                                .source(jsonMap))
                .collect(Collectors.toList());

        banknotesDatasetBulk.requests().addAll(indexRequestList);

        try {

            BulkResponse bulkResponse = elasticClient.bulk(banknotesDatasetBulk, RequestOptions.DEFAULT);

            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                DocWriteResponse itemResponse = bulkItemResponse.getResponse();
                switch (bulkItemResponse.getOpType()) {
                    case INDEX:
                    case CREATE:
                        IndexResponse indexResponse = (IndexResponse) itemResponse;
                        break;
                    case UPDATE:
                        UpdateResponse updateResponse = (UpdateResponse) itemResponse;
                        break;
                    case DELETE:
                        DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
                }
            }

            CountRequest countRequest = new CountRequest("banknotes-test");
            CountResponse countResponse = elasticClient.count(countRequest, RequestOptions.DEFAULT);
            System.out.println("Size: " + countResponse.getCount());
            assertThat(countResponse.getCount()).isEqualTo(banknoteDataSet.getBanknotes().size());

        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void readDataFromFiles() {
        BanknoteData banknoteData =
                testHelper.readBanknoteDataFromJsonFile("testdata/single-banknote.json");
        assertThat(banknoteData.getCountry()).isEqualTo("Albania");
    }

    private BanknoteData readSingleBanknoteData(String file) {
        return testHelper.readBanknoteDataFromJsonFile(file);
    }
}
