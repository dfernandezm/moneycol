package com.moneycol.indexer.infra.connectivity.gke;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("gke")
@Getter
@Setter
//Important: cannot use this Lombok annotation or properties cannot be populated in test
//@Accessors(fluent = true)
public class GkeClusterDetails {

    private String projectId;
    private String zone;
    private String clusterName;
    private String elasticsearchServiceName;
    private String elasticsearchServiceNamespace;
}
