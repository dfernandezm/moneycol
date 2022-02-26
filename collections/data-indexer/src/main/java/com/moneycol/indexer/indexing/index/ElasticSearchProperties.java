package com.moneycol.indexer.indexing.index;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("elasticsearch")
@Getter
@Setter
public class ElasticSearchProperties {

    //TODO: should be indexPrefix as it will have date appended
    private String indexName;
    private String hostAddress;
}
