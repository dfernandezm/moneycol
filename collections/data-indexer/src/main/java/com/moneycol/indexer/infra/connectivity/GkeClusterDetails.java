package com.moneycol.indexer.infra.connectivity;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Builder
public class GkeClusterDetails {

    private final String projectId;
    private final String zone;
    private final String clusterName;
}
