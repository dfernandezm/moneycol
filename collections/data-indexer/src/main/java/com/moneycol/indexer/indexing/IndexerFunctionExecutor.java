package com.moneycol.indexer.indexing;

import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.infra.PubSubClient;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import com.moneycol.indexer.infra.function.FunctionTimeoutTracker;
import com.moneycol.indexer.tracker.DefaultFanOutTracker;
import com.moneycol.indexer.tracker.Status;
import com.moneycol.indexer.tracker.TaskListStatusResult;
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
    private FunctionTimeoutTracker functionTimeoutChecker;

    @Inject
    private DefaultFanOutTracker defaultFanOutTracker;

    @Inject
    private FanOutConfigurationProperties fanOutConfigurationProperties;

    private static final int MESSAGE_BATCH_SIZE = 50;

    private static final String DATA_SINK_SUBSCRIPTION_NAME = "{env}.moneycol.indexer.sink";

    public void execute(Message message, Context context) {

        functionTimeoutChecker.startTimer();
        indexingDataReader.logTriggeringMessage(message);

        TaskListStatusResult taskListStatusResult = unwrapTaskListFromMessage(message);
        String taskListId = taskListStatusResult.getTaskListId();
        log.info("Received request into consolidation function for taskList {}, status is {}", taskListId,
                taskListStatusResult.getStatus());
        updateStatus(taskListId, Status.CONSOLIDATING);

        try {
            log.info("Start pulling messages from sink to process...");
            String subscriptionId = DATA_SINK_SUBSCRIPTION_NAME.replace("{env}", "dev");
            pubSubClient.subscribeSync(subscriptionId, MESSAGE_BATCH_SIZE,
                    (pubsubMessage) -> {
                        log.info("Received message in batch of 50: {}", pubsubMessage);
                        BanknotesDataSet banknotesDataSet = indexingDataReader.readBanknotesDataSet(pubsubMessage);
                        log.info("Read BanknotesDataSet: {}", banknotesDataSet);
                        indexData(banknotesDataSet);
                    }, () -> functionTimeoutChecker.isCloseToTimeout());

            if (functionTimeoutChecker.timedOut()) {
                log.info("Function is going to timeout, re-triggering now for taskList {}", taskListId);
                retriggerFunction(taskListId);
            } else {
                log.info("Consolidation completed for taskList {}", taskListStatusResult.getTaskListId());
                updateStatus(taskListId, Status.CONSOLIDATION_COMPLETED);
            }

            log.info("Function execution exiting for taskListId {}", taskListId);
        } catch (Throwable t) {

            log.error("Error subscribing in indexer", t);
            functionTimeoutChecker.stopScheduler();
            log.info("Check if recovery is needed after error");
            if (!defaultFanOutTracker.hasConsolidationCompleted(taskListStatusResult.getTaskListId())) {
                retriggerFunction(taskListStatusResult.getTaskListId());
            }
        }
    }

    private void indexData(BanknotesDataSet banknotesDataSet) {
        try {
            log.info("Now proceeding to index set {}", banknotesDataSet.getCountry());
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private TaskListStatusResult unwrapTaskListFromMessage(Message message) {
        return defaultFanOutTracker.readMessageAsTaskListStatus(message);
    }

    private void updateStatus(String taskListId, Status status) {
        defaultFanOutTracker.updateTaskListStatus(taskListId, status);
    }

    private void retriggerFunction(String taskListId) {
        defaultFanOutTracker.publishProcessingDone(taskListId);
    }
}