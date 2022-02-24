package com.moneycol.indexer.indexing;

import com.google.cloud.functions.Context;
import com.google.common.base.Stopwatch;
import com.google.events.cloud.pubsub.v1.Message;
import com.google.pubsub.v1.PubsubMessage;
import com.moneycol.indexer.indexing.index.IndexingHandler;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import com.moneycol.indexer.infra.function.FunctionTimeoutTracker;
import com.moneycol.indexer.infra.pubsub.PubSubClient;
import com.moneycol.indexer.tracker.FanOutProcessStatus;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.TaskListConverter;
import com.moneycol.indexer.tracker.TaskListStatusReport;
import com.moneycol.indexer.worker.BanknotesDataSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Singleton
public class IndexerFunctionExecutor  {

    private final PubSubClient pubSubClient;
    private final IndexingDataReader indexingDataReader;
    private final TaskListConverter taskListConverter;
    private final FunctionTimeoutTracker functionTimeoutChecker;
    private final FanOutTracker fanOutTracker;
    private final FanOutConfigurationProperties fanOutConfigurationProperties;
    private final IndexingHandler indexingHandler;

    private static final int MESSAGE_BATCH_SIZE = 250;

    public void execute(Message message, Context context) {

        functionTimeoutChecker.startTimer();
        indexingDataReader.logTriggeringMessage(message);

        TaskListStatusReport taskListStatusResult = unwrapTaskListFromMessage(message);
        String taskListId = taskListStatusResult.getTaskListId();
        log.info("Received request into consolidation function for taskList {}, " +
                        "status received is {}", taskListId, taskListStatusResult.getStatus());
        log.info("Checking status is not already CONSOLIDATION_COMPLETED due to misfire...");

        if (fanOutTracker.isConsolidationCompleted(taskListId)) {
            log.warn("This function execution is " +
                    "for an already completed taskList {} -- exiting now", taskListId);
            functionTimeoutChecker.stopScheduler();
            return;
        }

        updateStatus(taskListId, FanOutProcessStatus.CONSOLIDATING);

        try {
            log.info("Start pulling messages from sink to process...");
            String sinkSubscriptionId = fanOutConfigurationProperties.getPubSub().getSinkTopicName();
            boolean isDone = pubSubClient.subscribeSync(sinkSubscriptionId, MESSAGE_BATCH_SIZE,
                    (pubsubMessage) -> processMessage(taskListId, pubsubMessage),
                    functionTimeoutChecker::isCloseToTimeout);

            if (functionTimeoutChecker.timedOut() && !isDone) {
                log.info("Function is going to timeout, and it's not done, re-triggering now for taskList {}", taskListId);
                retriggerFunction(taskListId);
            } else {
                log.info("Consolidation completed for taskList {}", taskListStatusResult.getTaskListId());
                indexingHandler.switchIndexAlias();
                updateStatus(taskListId, FanOutProcessStatus.CONSOLIDATION_COMPLETED);
            }

            log.info("Function execution exiting for taskListId {}, stopping scheduler before exit", taskListId);
            functionTimeoutChecker.stopScheduler();
        } catch (Throwable t) {
            log.error("Error subscribing in indexer", t);
            functionTimeoutChecker.stopScheduler();
            log.info("Check if recovery is needed after error");
            if (!fanOutTracker.isConsolidationCompleted(taskListStatusResult.getTaskListId())) {
                retriggerFunction(taskListStatusResult.getTaskListId());
            }
        }
    }



    private void processMessage(String taskListId, PubsubMessage pubsubMessage) {
        log.info("Received message in batch: {}", pubsubMessage);
        BanknotesDataSet banknotesDataSet = indexingDataReader.readBanknotesDataSet(pubsubMessage);
        log.info("Filename in message is: {}", banknotesDataSet.getFilename());
        indexData(banknotesDataSet);
        Integer decrementAmount = banknotesDataSet.getBanknotes().size() == 0 ? 0 :
                -1 * banknotesDataSet.getBanknotes().size();
        log.info("Decrementing processed size of {} from file {}", decrementAmount,
                banknotesDataSet.getFilename());
        fanOutTracker.updatePendingItemsToProcessCount(taskListId, decrementAmount);
    }

    private void indexData(BanknotesDataSet banknotesDataSet) {
        Integer size = banknotesDataSet.getBanknotes() == null ? 0 : banknotesDataSet.getBanknotes().size();
        log.info("Now proceeding to index set {} with {} elements", banknotesDataSet.getCountry(),
                size);
        Stopwatch stopwatch = Stopwatch.createStarted();
        indexingHandler.indexData(banknotesDataSet);
        log.info("Indexing dataset {} of {} elements took {} ms - \n {}",
                banknotesDataSet.getCountry(),
                banknotesDataSet.getBanknotes().size(),
                stopwatch.elapsed(TimeUnit.MILLISECONDS),
                banknotesDataSet);
    }

    private TaskListStatusReport unwrapTaskListFromMessage(Message message) {
        return taskListConverter.readMessageAsTaskListStatus(message);
    }

    private void updateStatus(String taskListId, FanOutProcessStatus status) {
        fanOutTracker.updateTaskListStatus(taskListId, status);
    }

    private void retriggerFunction(String taskListId) {
        // This is the way to indicate that indexing can start
        fanOutTracker.notifyProcessingCompleted(taskListId);
    }
}