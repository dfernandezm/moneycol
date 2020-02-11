package com.moneycol.collections.server.infrastructure.repository;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.auth.Credentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.common.collect.ImmutableList;
import io.micronaut.context.annotation.Requires;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
@Requires(env="test")
public class EmulatedFirebaseProvider implements FirebaseProvider {
    private static final String LOCAL_FIRESTORE_EMULATOR_HOST = "127.0.0.1:8080";
    private static final String TEST_PROJECT_ID = "testproject";

    @Override
    public Firestore getFirestoreInstance() {
        FirestoreOptions foptions = FirestoreOptions.getDefaultInstance().toBuilder()
                .setHost(LOCAL_FIRESTORE_EMULATOR_HOST)
                .setChannelProvider(
                        InstantiatingGrpcChannelProvider.newBuilder().setEndpoint("localhost:8080")
                                .setChannelConfigurator(input -> {
                                                        input.usePlaintext();
                                                        return input;
                                                        }).build()
                )
                .setCredentialsProvider(FixedCredentialsProvider.create(new FakeCreds()))
                .setProjectId(TEST_PROJECT_ID)
                .build();

        return foptions.getService();
    }

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
