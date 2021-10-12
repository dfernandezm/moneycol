package com.moneycol.indexer.indexing;

import com.moneycol.indexer.worker.BanknotesDataSet;

import javax.inject.Singleton;

// - how to connect to ES in GKE? - via proxy? - VPC Serverless connector only works on VPC-native GKE
// - fan -in the results: if counters are incremented can push event on last one for indexing
// https://github.com/elastic/elasticsearch-java
// High level rest client,
// https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.10/java-rest-high-supported-apis.html

@Singleton
public class ElasticSearchClient {


    public void bulkIndex(BanknotesDataSet banknotesDataSet) {





    }

}
