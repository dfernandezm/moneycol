package com.moneycol.indexer.indexing;

import com.moneycol.indexer.infra.connectivity.ElasticSearchDiscoveryClient;
import com.moneycol.indexer.infra.connectivity.ElasticSearchEndpoint;
import com.moneycol.indexer.infra.connectivity.InGkeElasticSearchDiscoveryClient;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ElasticSearchDiscoveryClientTest {

    @Test
    public void findsInternalIpAndPortForElasticSearchService() {
        ElasticSearchDiscoveryClient elasticSearchDiscoveryClient = new InGkeElasticSearchDiscoveryClient();
        ElasticSearchEndpoint elasticSearchEndpoint = elasticSearchDiscoveryClient.obtainEndpoint();

        assertThat(elasticSearchEndpoint.getEndpoint()).isNotNull();
        assertThat(elasticSearchEndpoint.port()).isNotNull();
        assertThat(elasticSearchEndpoint.getEndpoint())
                .isEqualTo("http://" + elasticSearchEndpoint.host() + ":" + elasticSearchEndpoint.port());
    }
}
