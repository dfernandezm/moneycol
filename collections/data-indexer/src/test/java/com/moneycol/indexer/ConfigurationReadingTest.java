package com.moneycol.indexer;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import com.google.cloud.firestore.Firestore;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

//@MicronautTest
public class ConfigurationReadingTest {

    @Test
    public void testEnvironmentVariable() throws Exception {
        String testProjectId = "testProjectId";
        String envvarName = "GCP_PROJECT_ID";

        // it will be picked up if the property is called gcp-project-id
        SystemLambda
                .withEnvironmentVariable("FANOUT_GCP_PROJECT_ID", "testProjectId")
                .and("FANOUT_SOURCE_BUCKET_NAME", "moneycol-import")
                .and("FANOUT_PUBSUB_TRIGGER_TOPIC_NAME", "test.moneycol.indexer.batches")
                .and("FANOUT_PUBSUB_DONE_TOPIC_NAME", "test.moneycol.indexer.batching.done")
                .and("FANOUT_PUBSUB_SINK_TOPIC_NAME", "test.moneycol.indexer.sink")
                .execute(() -> {
            ApplicationContext applicationContext = ApplicationContext.run();
            Firestore firestore = applicationContext.getBean(Firestore.class);
            Environment env = applicationContext.getEnvironment();

            assertThat(firestore.getOptions().getProjectId()).isEqualTo("testProjectId");
        });
    }


}
