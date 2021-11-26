package com.moneycol.indexer.infra.connectivity.gke.data;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(fluent = true)
public class KubeServiceDetails {

    private final String port;
    private final String internalIp;
}
