package com.moneycol.indexer.indexing.index;

import com.moneycol.indexer.infra.JsonWriter;
import com.moneycol.indexer.worker.BanknotesDataSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// - how to connect to ES in GKE? - via proxy? - VPC Serverless connector only works on VPC-native GKE
// - fan -in the results: if counters are incremented can push event on last one for indexing
// https://github.com/elastic/elasticsearch-java
// High level rest client,
// https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-supported-apis.html

@Slf4j
@RequiredArgsConstructor
public class ElasticSearchClient {

    private final ElasticSearchProperties elasticsearchProperties;
    private final RestHighLevelClient elasticClient;
    private final String BANKNOTES_TYPE = "banknotes";

    public void index(BanknotesDataSet banknotesDataSet) {

        JsonWriter jsonWriter = JsonWriter.builder().build();
        BulkRequest banknotesDatasetBulk = new BulkRequest();
        String indexName = elasticsearchProperties.getIndexName();

        if (banknotesDataSet.getBanknotes() != null && banknotesDataSet.getBanknotes().size() > 0) {
            List<IndexRequest> indexRequestList = banknotesDataSet.getBanknotes().stream()
                    .map(jsonWriter::toMap)
                    .map(jsonMap -> {
                            String id = jsonMap.get("catalogCode") == null ?
                                    UUID.randomUUID().toString() :
                                    jsonMap.get("catalogCode") + "-" + UUID.randomUUID();
                            return new IndexRequest(indexName)
                                    .id(id)
                                    .setPipeline("add-current-time")
                                    // this seems to be required in elasticsearch 6.5
                                    .type(BANKNOTES_TYPE)
                                    .source(jsonMap);
                    })
                    .collect(Collectors.toList());

            log.info("Index requests list of size {} for dataset {}", indexRequestList.size(), banknotesDataSet.getCountry());
            banknotesDatasetBulk.requests().addAll(indexRequestList);

            try {

                BulkResponse bulkResponse = elasticClient.bulk(banknotesDatasetBulk, RequestOptions.DEFAULT);

                if (bulkResponse.hasFailures()) {
                    log.error("There's failures in the bulk operation: {}", bulkResponse.buildFailureMessage());
                    //Arrays.stream(bulkResponse.getItems()).forEach(item -> item.getResponse().getResult().toString());
                } else {
                    log.info("Successful bulk index for dataset {} original size {}, indexed {}",
                            banknotesDataSet.getCountry(),
                            banknotesDataSet.getBanknotes().size(),
                            bulkResponse.getItems().length);
                }

            } catch (Throwable t) {
                log.error("Error occurred in bulk insert -- continue with rest", t);
            }
        } else {
            log.warn("Banknotes dataset is empty -- won't be indexed {}", banknotesDataSet);
        }

            // check for errors in each request?

    }
}
