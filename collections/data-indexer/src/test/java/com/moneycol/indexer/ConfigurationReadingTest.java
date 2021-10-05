package com.moneycol.indexer;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import com.google.cloud.firestore.Firestore;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
public class ConfigurationReadingTest {

    @Test
    public void testEnvironmentVariable() throws Exception {
        String testProjectId = "testProjectId";
        String envvarName = "GCP_PROJECT_ID";

        // it will be picked up if the property is called gcp-project-id
        SystemLambda.withEnvironmentVariable(envvarName, testProjectId).execute(() -> {
            ApplicationContext applicationContext = ApplicationContext.run();
            Firestore firestore = applicationContext.getBean(Firestore.class);
            assertThat(firestore.getOptions().getProjectId()).isEqualTo("testProjectId");
        });
    }


}
