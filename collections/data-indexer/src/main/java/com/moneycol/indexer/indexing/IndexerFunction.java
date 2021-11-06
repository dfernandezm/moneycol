package com.moneycol.indexer.indexing;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import io.micronaut.gcp.function.GoogleFunctionInitializer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;


/**
 *
 * This function is responsible for the processing of data published in the Sink Topic.
 * This data cannot be streamed directly to the destination due to the limitations
 * of the infra (1 Elasticsearch node, underpowered).
 *
 * - Reads data from sink topic via Synchronous Pull (Pull Request) in groups of 50-100
 * - This generates a bulk insert into ElasticSearch
 * - Need to ensure that ES is not overrun by the operation so batch should be adjusted
 * - Function can take more than 9 minutes in this operation so time should be tracked, ensure
 * messages are ACK before the timeout, and another trigger is published to carry one with same
 * function
 * - The 'batcher' function keeps track via Firestore of all the spawned tasks in fan-out
 * so that this function (acting as fan-in with sink topic) knows when it's 'done'
 *
 * - Elastic connectivity is required (it's in GKE)
 *  - Proper way is via Serverless VPC connector - it requires GKE with VPC-native (this is not currently
 *  possible because GKE cluster has been built without it)
 *  - It could work with kubectl proxy, but not clear how to do it programmatically
 *  - Pragmatic approach is making the cluster public via LB / Nodeport
 *  - Ideally this would be temporary, while GKE is not rebuilt properly
 *
 */
@Slf4j
public class IndexerFunction extends GoogleFunctionInitializer
        implements BackgroundFunction<Message> {

    @Inject
    private IndexerFunctionExecutor indexerFunctionExecutor;

    @Override
    public void accept(Message payload, Context context) {
        indexerFunctionExecutor.execute(payload, context);
    }
}