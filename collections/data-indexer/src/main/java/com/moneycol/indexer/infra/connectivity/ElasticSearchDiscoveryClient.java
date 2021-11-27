package com.moneycol.indexer.infra.connectivity;

public interface ElasticSearchDiscoveryClient {

    ElasticSearchEndpoint obtainEndpoint();
}
