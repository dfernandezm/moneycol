package com.moneycol.collections.server.infrastructure.repository;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import io.micronaut.context.annotation.Requires;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@Requires(notEnv="test")
public class CloudFirebaseProvider implements FirebaseProvider {
    private static String PROJECT_ID = "moneycol";
    private SourceCredentials sourceCredentials;

    @Inject
    public CloudFirebaseProvider(SourceCredentials sourceCredentials) {
        this.sourceCredentials = sourceCredentials;
    }

    @Override
    public Firestore getFirestoreInstance() {
        try {

            log.info("Reading credentials key for Firestore from environment: {}", sourceCredentials.getCredentials());
            //TODO: WARNING: Your application has authenticated using end user credentials from Google Cloud SDK. We recommend that most server applications use service accounts instead. If your application continues to use end user credentials from Cloud SDK, you might receive a "quota exceeded" or "API not enabled" error. For more information about service accounts, see https://cloud.google.com/docs/authentication/.

            FirestoreOptions opt  = FirestoreOptions.newBuilder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setProjectId(PROJECT_ID)
                    .build();


            Firestore firestore =  opt.getService();
            return firestore;
        } catch (Exception e) {
            log.error("Error setting up Google credentials", e);
            throw new RuntimeException(e);
        }
    }
}
