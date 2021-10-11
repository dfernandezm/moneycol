package com.moneycol.indexer.indexing;

import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.infra.PubSubClient;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import com.moneycol.indexer.infra.function.FunctionTimeoutTracker;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.Status;
import com.moneycol.indexer.tracker.TaskListStatusResult;
import com.moneycol.indexer.worker.BanknotesDataSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@RequiredArgsConstructor
@Singleton
public class IndexerFunctionExecutor  {

    private final PubSubClient pubSubClient;
    private final IndexingDataReader indexingDataReader;
    private final FunctionTimeoutTracker functionTimeoutChecker;
    private final FanOutTracker fanOutTracker;
    private final FanOutConfigurationProperties fanOutConfigurationProperties;

    private static final int MESSAGE_BATCH_SIZE = 250;


    public void execute(Message message, Context context) {

        functionTimeoutChecker.startTimer();
        indexingDataReader.logTriggeringMessage(message);

        TaskListStatusResult taskListStatusResult = unwrapTaskListFromMessage(message);
        String taskListId = taskListStatusResult.getTaskListId();
        log.info("Received request into consolidation function for taskList {}, " +
                        "status received is {}", taskListId, taskListStatusResult.getStatus());

        log.info("Checking status is not already CONSOLIDATION_COMPLETED due to misfire...");

        if (fanOutTracker.hasConsolidationCompleted(taskListId)) {
            log.warn("This function execution is " +
                    "for an already completed taskList {} -- exiting now", taskListId);
            functionTimeoutChecker.stopScheduler();
            return;
        }

        updateStatus(taskListId, Status.CONSOLIDATING);

        try {
            log.info("Start pulling messages from sink to process...");
            String sinkSubscriptionId = fanOutConfigurationProperties.getPubSub().getSinkTopicName();
            boolean isDone = pubSubClient.subscribeSync(sinkSubscriptionId, MESSAGE_BATCH_SIZE,
                    (pubsubMessage) -> {
                        log.info("Received message in batch of 250: {}", pubsubMessage);
                        BanknotesDataSet banknotesDataSet = indexingDataReader.readBanknotesDataSet(pubsubMessage);
                        log.info("Read BanknotesDataSet: {}", banknotesDataSet);
                        indexData(banknotesDataSet);
                    }, functionTimeoutChecker::isCloseToTimeout);

            if (functionTimeoutChecker.timedOut() && !isDone) {
                log.info("Function is going to timeout, and it's not done, re-triggering now for taskList {}", taskListId);
                retriggerFunction(taskListId);
            } else {
                log.info("Consolidation completed for taskList {}", taskListStatusResult.getTaskListId());
                updateStatus(taskListId, Status.CONSOLIDATION_COMPLETED);
            }

            log.info("Function execution exiting for taskListId {}, stopping scheduler before exit", taskListId);
            functionTimeoutChecker.stopScheduler();
        } catch (Throwable t) {
            log.error("Error subscribing in indexer", t);
            functionTimeoutChecker.stopScheduler();
            log.info("Check if recovery is needed after error");
            if (!fanOutTracker.hasConsolidationCompleted(taskListStatusResult.getTaskListId())) {
                retriggerFunction(taskListStatusResult.getTaskListId());
            }
        }
    }

    // Simulate index data - change for real indexing
    private void indexData(BanknotesDataSet banknotesDataSet) {
        try {
            log.info("Now proceeding to index set {} with {} elements", banknotesDataSet.getCountry(),
                    banknotesDataSet.getBanknotes().size());
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private TaskListStatusResult unwrapTaskListFromMessage(Message message) {
        return fanOutTracker.readMessageAsTaskListStatus(message);
    }

    private void updateStatus(String taskListId, Status status) {
        fanOutTracker.updateTaskListStatus(taskListId, status);
    }

    private void retriggerFunction(String taskListId) {
        fanOutTracker.notifyProcessingDone(taskListId);
    }
}