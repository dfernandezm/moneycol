package com.moneycol.indexer.indexing.index;

import com.moneycol.indexer.worker.BanknotesDataSet;
import lombok.RequiredArgsConstructor;

import javax.inject.Singleton;

/**
 * Given a dataset, build a document from it to index in ES
 */
@Singleton
@RequiredArgsConstructor
public class IndexingHandler {

    private final ElasticSearchClient elasticsearchClient;

    public void indexData(BanknotesDataSet banknotesDataSet) {
        elasticsearchClient.index(banknotesDataSet);
    }

    public void switchIndexAlias() {
        elasticsearchClient.updateIndexAlias();
    }
}
