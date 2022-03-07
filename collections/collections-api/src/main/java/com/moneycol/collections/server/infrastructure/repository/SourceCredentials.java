package com.moneycol.collections.server.infrastructure.repository;

import javax.inject.Singleton;

@Singleton
public class SourceCredentials {
    private static String GOOGLE_CREDENTIALS_KEY_ENV_VAR_NAME = "GOOGLE_APPLICATION_CREDENTIALS";

    public String getCredentials() {
       return  System.getenv(GOOGLE_CREDENTIALS_KEY_ENV_VAR_NAME);
    }
}
