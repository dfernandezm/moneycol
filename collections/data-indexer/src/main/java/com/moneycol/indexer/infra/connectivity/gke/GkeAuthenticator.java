package com.moneycol.indexer.infra.connectivity.gke;

import com.google.auth.oauth2.GoogleCredentials;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.authenticators.GCPAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class GkeAuthenticator extends GCPAuthenticator {

    static {
        KubeConfig.registerAuthenticator(new GkeAuthenticator());
    }

    private static final Logger log = LoggerFactory.getLogger(GkeAuthenticator.class);

    @Override
    public String getToken(Map<String, Object> config) {
        String token = (String) config.get("access-token");
        if (token == null || isExpired(config)) {
            log.info("Generating new token and storing it");
            token = generateNewToken();
            config.put("access-token", token);
            config.put("expiry", Instant.now().plusSeconds(3600).toString());
        }
        return token;
    }

    @Override
    public boolean isExpired(Map<String, Object> config) {
        Object expiryObj = config.get("expiry");
        if (expiryObj == null) {
            return true;
        }
        return super.isExpired(config);
    }

    private String generateNewToken() {
        try {
            GoogleCredentials creds = GoogleCredentials.getApplicationDefault();
            creds = creds.createScoped("https://www.googleapis.com/auth/cloud-platform");
            creds.refreshIfExpired();
            return creds.getAccessToken().getTokenValue();
        } catch (IOException ioe) {
            log.warn("Cannot generate token in GCP", ioe);
            return null;
        }
    }

    @Override
    public Map<String, Object> refresh(Map<String, Object> config) {
        log.info("Refreshing token");
        String refreshedToken = generateNewToken();
        config.put("access-token", refreshedToken);
        config.put("expiry", Instant.now().plusSeconds(3600).toString());
        return config;
    }
}
