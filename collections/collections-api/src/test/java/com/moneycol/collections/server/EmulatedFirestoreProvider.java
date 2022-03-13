package com.moneycol.collections.server;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.auth.Credentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.common.collect.ImmutableList;
import com.moneycol.collections.server.infrastructure.repository.FirestoreProvider;
import io.micronaut.context.annotation.Requires;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Singleton
@Requires(env="test")
public class EmulatedFirestoreProvider implements FirestoreProvider {
    private static final String LOCAL_FIRESTORE_EMULATOR_HOST = "127.0.0.1:8080";
    private static final String TEST_PROJECT_ID = "testproject";
    private static Firestore instance;

    private String endpoint;

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    // Important: setHost() is broken, setting a non-environment variable value only works
    // by using the new (quite undocumented) setEmulatorHost() method,
    // see issue https://github.com/googleapis/java-firestore/issues/190
    @Override
    public Firestore getFirestoreInstance() {
        if (instance == null) {
            String hostAndPort = this.endpoint;
            log.info("Setting emulator host as " + hostAndPort);
            FirestoreOptions firestoreOptions = FirestoreOptions.getDefaultInstance()
                    .toBuilder()
                    .setEmulatorHost(hostAndPort)
                    .setChannelProvider(
                            InstantiatingGrpcChannelProvider.newBuilder().setEndpoint(hostAndPort)
                                    .setChannelConfigurator(input -> {
                                        input.usePlaintext();
                                        return input;
                                    }).build()
                    )
                    .setCredentialsProvider(NoCredentialsProvider.create())
                    .setProjectId(TEST_PROJECT_ID)
                    .build();

            instance = firestoreOptions.getService();
        }

        return instance;
    }

    // Used only when using .setCredentialsProvider(FixedCredentialsProvider.create(new FakeCreds()))
    private static class FakeCreds extends Credentials {
        @Override
        public String getAuthenticationType() {
            return null;
        }

        @Override
        public Map<String, List<String>> getRequestMetadata(URI uri) throws IOException {
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("Authorization", ImmutableList.of("Bearer owner"));
            return headers;
        }

        @Override
        public boolean hasRequestMetadata() {
            return true;
        }

        @Override
        public boolean hasRequestMetadataOnly() {
            return true;
        }

        @Override
        public void refresh() throws IOException {

        }
    }
}
