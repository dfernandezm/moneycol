package com.moneycol.search.infra.connectivity;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Builder
public class ElasticSearchEndpoint {

    @Builder.Default
    private final String scheme = "http";
    private final String host;
    private final String port;

    public String getEndpoint() {
        return scheme + "://" + host + ":" + port;
    }
}
