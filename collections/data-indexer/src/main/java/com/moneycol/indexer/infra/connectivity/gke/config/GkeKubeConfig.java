package com.moneycol.indexer.infra.connectivity;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Builder
public class GkeKubeConfig {

    private final String kubeConfigFilePath;
}
