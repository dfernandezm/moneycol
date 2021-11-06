package com.moneycol.indexer.indexing;

import com.moneycol.indexer.TestHelper;
import com.moneycol.indexer.indexing.index.ElasticSearchClient;
import com.moneycol.indexer.worker.BanknotesDataSet;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Map;

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


    private static String hostAddress;

    @Test
    public void indexDataset() {
        BanknotesDataSet banknoteDataSet =
                testHelper.readBanknoteDataSetFromJsonFile("testdata/banknotesDataset.json");
        elasticsearchClient.index(banknoteDataSet);
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
