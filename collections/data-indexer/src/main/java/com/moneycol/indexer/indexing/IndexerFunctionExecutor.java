package com.moneycol.indexer.indexing;

import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.infra.function.FunctionTimeoutChecker;
import com.moneycol.indexer.infra.PubSubClient;
import com.moneycol.indexer.worker.BanknotesDataSet;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class IndexerFunctionExecutor  {

    @Inject
    private PubSubClient pubSubClient;

    @Inject
    private IndexingDataReader indexingDataReader;

    @Inject
    private FunctionTimeoutChecker functionTimeoutChecker;

    private static final int MESSAGE_BATCH_SIZE = 50;

    public void execute(Message message, Context context) {

        indexingDataReader.logTriggeringMessage(message);

        //TODO: update taskList status to INDEXING and to COMPLETED when done
        // delegate to dedicated service
        //TODO: extract constant/default

        try {
            String subscriptionId = PubSubClient.DATA_SINK_SUBSCRIPTION_NAME.replace("{env}", "dev");
            pubSubClient.subscribeSync(subscriptionId, MESSAGE_BATCH_SIZE,
                    (pubsubMessage) -> {
                log.info("Received message in batch of 50: {}", pubsubMessage);
                BanknotesDataSet banknotesDataSet = indexingDataReader.readBanknotesDataSet(pubsubMessage);
                log.info("Read BanknotesDataSet: {}", banknotesDataSet);
                log.info("Now proceed to index set");
            }, () -> functionTimeoutChecker.isCloseToTimeout());

            // if timed out, retrigger the function and exit(0)
        } catch (Exception e) {
            //TODO: if not done, retrigger this function
            log.error("Error subscribing in indexer", e);
        }
    }
}