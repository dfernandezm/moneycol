package com.moneycol.indexer.indexing;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.infra.PubSubClient;
import com.moneycol.indexer.worker.BanknotesDataSet;
import io.micronaut.gcp.function.GoogleFunctionInitializer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;


/**
 *
 * This function is reponsible for the indexing of data published in the Sink Topic
 *
 * - Reads data from sink topic via Synchronous Pull (Pull Request) in groups of 50-100
 * - This generates a bulk insert into ElasticSearch
 * - Need to ensure that ES is not overrun by the operation so batch should be adjusted
 * - Function can take more than 9 minutes in this operation so time should be tracked, ensure
 * messages are ACK before the timeout, and another event is published to trigger same function
 * - The original trigger of this function can be an Scheduler or, as part of the fan-out, detect
 *   - First X messages
 *   - when all are done pushing to sink
 *   - others?
 * - The 'ventilator' needs to keep track of all the spawned tasks in fan-out so that this function (acting
 * as fan-in with sink topic) knows when it's 'done'.
 *    - It can be that it pulls from topic until 'done'
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
    private PubSubClient pubSubClient;

    @Inject
    private IndexingDataReader indexingDataReader;

    private static final int MESSAGE_BATCH_SIZE = 50;

    @Override
    public void accept(Message payload, Context context) throws Exception {

        indexingDataReader.logTriggeringMessage(payload);

        //TODO: update taskList status to INDEXING and to COMPLETED when done
        // delegate to dedicated service
        //TODO: extract constant/default
        String subscriptionId = PubSubClient.DATA_SINK_SUBSCRIPTION_NAME.replace("{env}", "dev");
        pubSubClient.subscribeSync(subscriptionId, MESSAGE_BATCH_SIZE, (pubsubMessage) -> {
            log.info("Received message in batch of 50: {}", pubsubMessage);
            BanknotesDataSet banknotesDataSet = indexingDataReader.readBanknotesDataSet(pubsubMessage);
            log.info("Read BanknotesDataSet: {}", banknotesDataSet);
            log.info("Now proceed to index set");
        });
    }


}