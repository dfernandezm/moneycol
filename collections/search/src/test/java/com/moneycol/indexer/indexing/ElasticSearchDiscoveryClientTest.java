package com.moneycol.indexer.indexing;

import com.moneycol.search.infra.connectivity.ElasticSearchDiscoveryClient;
import com.moneycol.search.infra.connectivity.ElasticSearchEndpoint;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ElasticSearchDiscoveryClientTest implements TestPropertyProvider {

    @Inject
    private ElasticSearchDiscoveryClient elasticSearchDiscoveryClient;

    @Test
    public void findsInternalIpAndPortForElasticSearchService() {
        ElasticSearchEndpoint elasticSearchEndpoint = elasticSearchDiscoveryClient.obtainEndpoint();

        assertThat(elasticSearchEndpoint.getEndpoint()).isNotNull();
        assertThat(elasticSearchEndpoint.port()).isNotNull();
        assertThat(elasticSearchEndpoint.getEndpoint())
                .isEqualTo("http://" + elasticSearchEndpoint.host() + ":" + elasticSearchEndpoint.port());
    }

    // The @ConfigurationProperties (GkeClusterDetails in this case)
    // seem not to be possible to fill in tests
    //
    // See: https://github.com/micronaut-projects/micronaut-test/issues/32
    //
    // So we use the notation of the propertySources to fill a map with the equivalent
    // properties (see: https://docs.micronaut.io/1.3.0.M1/guide/index.html#_property_value_binding)
    //
    // The test requires GOOGLE_APPLICATION_CREDENTIALS pointing to a SA with GKE access for
    // the cluster details indicated here
    @Nonnull
    @Override
    public Map<String, String> getProperties() {
        return Map.of(
                "gke.project-id", "moneycol",
                "gke.zone", "europe-west1-b",
                "gke.cluster-name", "cluster-dev2",
                "gke.elasticsearch-service-name", "elasticsearch-nodeport",
                "gke.elasticsearch-service-namespace", "default");
    }
}
