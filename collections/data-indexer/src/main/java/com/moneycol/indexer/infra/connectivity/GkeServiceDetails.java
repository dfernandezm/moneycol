package com.moneycol.indexer.infra.connectivity;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(fluent = true)
public class GkeServiceDetails {

    private final String port;
    private final String internalIp;
}
