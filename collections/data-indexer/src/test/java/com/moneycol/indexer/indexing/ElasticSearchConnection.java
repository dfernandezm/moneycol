package com.moneycol.indexer.indexing;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class ElasticSearchConnection {

    private String scheme;
    private final String host;
    private final String port;
}
