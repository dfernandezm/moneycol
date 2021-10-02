package com.moneycol.indexer.indexing;

import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.infra.PubSubClient;
import com.moneycol.indexer.infra.function.FunctionTimeoutChecker;
import com.moneycol.indexer.tracker.DefaultFanOutTracker;
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

    @Inject
    private DefaultFanOutTracker defaultFanOutTracker;

    private static final int MESSAGE_BATCH_SIZE = 50;

    public void execute(Message message, Context context) {

        functionTimeoutChecker.startTimer();
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
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }, () -> functionTimeoutChecker.isCloseToTimeout());

            // if timed out, retrigger the function and exit(0)
            retriggerFunction();
        } catch (Throwable t) {
            //TODO: if not done, retrigger this function
            log.error("Error subscribing in indexer", t);
            functionTimeoutChecker.stopScheduler();
        }
    }

    private void retriggerFunction() {
        defaultFanOutTracker.publishDone("continuation");
    }
}