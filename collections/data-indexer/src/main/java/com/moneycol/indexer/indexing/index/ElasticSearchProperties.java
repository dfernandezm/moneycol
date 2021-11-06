package com.moneycol.indexer.indexing.index;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("elasticsearch")
@Getter
@Setter
public class ElasticSearchProperties {

    private String indexName;
    private String hostAddress;
}
