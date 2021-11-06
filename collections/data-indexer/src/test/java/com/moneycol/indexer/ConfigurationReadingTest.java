package com.moneycol.indexer;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import com.google.cloud.firestore.Firestore;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationReadingTest {

    @Test
    public void testEnvironmentVariable() throws Exception {

        SystemLambda
                .withEnvironmentVariable("FANOUT_GCP_PROJECT_ID", "testProjectId")
                .and("FANOUT_SOURCE_BUCKET_NAME", "moneycol-import")
                .and("FANOUT_PUB_SUB_TRIGGER_TOPIC_NAME", "test.moneycol.indexer.batches")
                .and("FANOUT_PUB_SUB_DONE_TOPIC_NAME", "test.moneycol.indexer.batching.done")
                .and("FANOUT_PUB_SUB_SINK_TOPIC_NAME", "test.moneycol.indexer.sink")
                .and("FANOUT_CONSOLIDATION_PROCESS_TIMEOUT_SECONDS", "540")
                .and("FANOUT_CONSOLIDATION_PROCESS_TIMEOUT_THRESHOLD_SECONDS", "10")
                .execute(() -> {

                    ApplicationContext applicationContext = ApplicationContext.run();
                    Firestore firestore = applicationContext.getBean(Firestore.class);
                    Environment env = applicationContext.getEnvironment();
                    FanOutConfigurationProperties fanoutConfig =
                            applicationContext.getBean(FanOutConfigurationProperties.class);

                    assertThat(env.getProperty("fanout.gcp-project-id", String.class).get())
                            .isEqualTo("testProjectId");
                    assertThat(firestore.getOptions().getProjectId()).isEqualTo("testProjectId");


                    assertPropertyHasValue(env, fanoutConfig.getSourceBucketName(),
                            "fanout.source-bucket-name",
                            "moneycol-import");

                    assertPropertyHasValue(env, fanoutConfig.getPubSub().getSinkTopicName(),
                            "fanout.pub-sub.sink-topic-name",
                            "test.moneycol.indexer.sink");

                    assertPropertyHasValue(env, fanoutConfig.getPubSub().getDoneTopicName(),
                            "fanout.pub-sub.done-topic-name",
                            "test.moneycol.indexer.batching.done");

                    assertThat(env.getProperty("fanout.consolidation-process-timeout-seconds", String.class).get())
                            .isEqualTo("540");

                    assertThat(env.getProperty("fanout.consolidation-process-timeout-threshold-seconds", String.class).get())
                            .isEqualTo("10");
        });
    }

    private void assertPropertyHasValue(Environment env, String propertyValue,
                                        String propertyName, String expectedValue) {
        assertThat(env.getProperty(propertyName, String.class).get())
                .isEqualTo(expectedValue);
        assertThat(propertyValue)
                .isEqualTo(expectedValue);
    }
}
