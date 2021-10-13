package com.moneycol.indexer.indexing.index;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties("elasticsearch")
@RequiredArgsConstructor
@Data
public class ElasticsearchProperties {

    private final String indexName;
    private final String hostAddress;
}
