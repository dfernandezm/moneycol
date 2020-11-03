package com.moneycol.collections.app;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.request.RequestHeaders;
import net.moneycol.SignUpMutation;
import net.moneycol.type.SignUpUserInput;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class App {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Future<String> res = testGql();
        res.get();

    }

    private static Future<String> testGql() {
        String endpoint = "https://moneycol-graphql-k7qc4frf3q-ew.a.run.app/graphql";
        endpoint = "http://localhost:4000/graphql";

        CompletableFuture<String> call = new CompletableFuture<>();

        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(endpoint)
                .build();

        SignUpUserInput signUpUserInput = SignUpUserInput.builder()
                .username("dafe")
                .firstName("dafe3")
                .lastName("vvv")
                .password("password1")
                .repeatedPassword("password1")
                .email("apolloAndroid3@mailinator.com")
                .build();

        SignUpMutation signUpMutation = SignUpMutation.builder()
                .userInput(signUpUserInput)
                .build();

        setupAuthenticatedMutation(apolloClient, signUpMutation, "")
                .enqueue(new ApolloCall.Callback<SignUpMutation.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<SignUpMutation.Data> response) {
                        System.out.println(Thread.currentThread().getName());
                        try {
                            SignUpMutation.Data data = response.getData();
                            if (data != null) {
                                System.out.println(data.signUpWithEmail().toString());
                                call.complete(response.toString());
                            } else {

                            }


                        } catch(Throwable t ) {
                            call.completeExceptionally(t);
                            return;
                        }

                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        System.out.println(e.getMessage());
                        call.completeExceptionally(e);
                        return;
                    }
                });

        return call;

    }

    private void loginWithEmail(String email, String password) {
       // setupUnAuthenticatedMutation()
    }

    private ApolloClient setupApolloClient(String endpoint, String token) {
       return ApolloClient.builder()
                .serverUrl(endpoint)
                .build();
    }

    private static <D extends Operation.Data,T,V extends Operation.Variables> ApolloMutationCall<T>
        setupAuthenticatedMutation(ApolloClient client, Mutation<D,T,V> mutation, String token) {
            return client
                    .mutate(mutation)
                    .toBuilder()
                    .requestHeaders(
                            RequestHeaders.builder()
                                    .addHeader("Authorization", token)
                                    .build()
                    ).build();
    }

    private static <D extends Operation.Data,T,V extends Operation.Variables> ApolloMutationCall<T>
        setupUnAuthenticatedMutation(ApolloClient client, Mutation<D,T,V> mutation, String token) {
            return client
                    .mutate(mutation)
                    .toBuilder()
                    .build();
    }

    private static <D extends Operation.Data,T,V extends Operation.Variables> ApolloQueryCall<T>  setupQuery(
            ApolloClient client, Query<D,T,V> query, String token) {
            return client
                    .query(query)
                    .toBuilder()
                    .requestHeaders(
                            RequestHeaders.builder()
                                    .addHeader("Authorization", "")
                                    .build()
                    ).build();
    }
}
