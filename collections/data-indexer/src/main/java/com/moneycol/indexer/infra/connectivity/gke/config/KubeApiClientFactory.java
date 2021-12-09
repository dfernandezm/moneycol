package com.moneycol.indexer.infra.connectivity.gke.config;

import com.moneycol.indexer.infra.connectivity.gke.GkeAuthenticator;
import com.moneycol.indexer.infra.connectivity.gke.GkeClusterDetails;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.io.FileReader;
import java.io.IOException;

@Slf4j
@Factory
@RequiredArgsConstructor
public class KubeApiClientFactory {

    private final GkeClusterDetails gkeClusterDetails;

    @Bean
    @Singleton
    public ApiClient obtainKubeClientFromKubeConfig() {
        GkeKubeAccessConfigurer gkeKubeAccessConfigurer = new GkeKubeAccessConfigurer();
        GkeKubeConfig kubeConfigDetails = gkeKubeAccessConfigurer.authenticate(gkeClusterDetails);

        // this client lib has an issue here: the kubeconfig file does not have a token
        // and when ran with kubectl the token gets appended to the file so it works,
        // but when run programmatically only, this does not happen and there's a
        // 403 Forbidden error.
        //
        // This can be fixed by using GCP Auth lib to get application
        // default credentials from the underlying service account. This solution
        // has now been implemented in the GkeAuthenticator class, that extends/overrides
        // the existing GCP Authenticator.
        // Another more 'crude' approach is getting the token and add it to the templated
        // kubeconfig.yaml file, populating 'access-token' and 'expiry'
        KubeConfig.registerAuthenticator(new GkeAuthenticator());
        KubeConfig kubeConfig;
        try {
            kubeConfig = KubeConfig.loadKubeConfig(new FileReader(kubeConfigDetails.kubeConfigFilePath()));
            return ClientBuilder
                    .kubeconfig(kubeConfig)
                    .build();
        } catch (IOException ioe) {
            log.error("Error creating Kube API Client", ioe);
            return null;
        }
    }
}
