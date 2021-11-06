package com.moneycol.indexer.tracker;

import com.moneycol.indexer.infra.pubsub.PubSubClient;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import com.moneycol.indexer.tracker.tasklist.TaskList;
import com.moneycol.indexer.tracker.tasklist.TaskListRepository;
import io.micronaut.context.annotation.Primary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@RequiredArgsConstructor
@Singleton
@Primary
public class DefaultFanOutTracker implements FanOutTracker {

    private final TaskListRepository taskListRepo;
    private final PubSubClient pubSubClient;
    private final FanOutConfigurationProperties fanOutConfigurationProperties;

    @Override
    public String createTaskList(TaskList taskList) {
        return taskListRepo.createTaskList(taskList);
    }

    @Override
    public boolean areAllTasksCompleted(String taskListId) {
        TaskList taskList = taskListRepo.findById(taskListId);
        return taskList.allSpawnedTasksCompleted();
    }

    @Override
    public void incrementCompletedCount(String taskListId, Integer quantity) {
        // should check if it completed already and don't increment more
        taskListRepo.updateFieldAtomically(taskListId, "completedTasks", 1);
    }

    @Override
    public void completeProcessing(String taskListId) {
        TaskList taskList = taskListRepo.findById(taskListId);
        taskList.completeProcessingStatus();
        taskListRepo.update(taskList);
    }

    @Override
    public void updateOverallTaskProgressAtomically(String taskListId) {
        taskListRepo.executeTaskListUpdateInTransaction(taskListId, this::notifyProcessingCompleted);
    }

    @Override
    public void spawnTask(IntermediateTask<?> genericTask) {
        String batchesTopic = fanOutConfigurationProperties.getPubSub().getTriggerTopicName();
        pubSubClient.publishMessage(batchesTopic, genericTask);
    }

    @Override
    public <T> void publishIntermediateResult(T resultData) {
        String sinkTopicName = fanOutConfigurationProperties.getPubSub().getSinkTopicName();
        pubSubClient.publishMessage(sinkTopicName, resultData);
    }

    @Override
    public void notifyProcessingCompleted(String taskListId) {
        String doneTopicName = fanOutConfigurationProperties.getPubSub().getDoneTopicName();
        TaskListStatusReport taskListDoneResult = TaskListStatusReport.builder()
                .taskListId(taskListId)
                .status(FanOutProcessStatus.PROCESSING_COMPLETED)
                .build();
        log.info("Notifying PROCESSING_COMPLETED status {}", taskListDoneResult);
        pubSubClient.publishMessage(doneTopicName, taskListDoneResult);
    }

    @Override
    public void updateTaskListStatus(String taskListId, FanOutProcessStatus status) {
        TaskList taskList = taskListRepo.findById(taskListId);
        taskList.setStatus(status);
        taskListRepo.update(taskList);
    }

    @Override
    public void updatePendingItemsToProcessCount(String taskListId, Integer count) {
        taskListRepo.updateFieldAtomically(taskListId, "valuesToProcess", count);
    }

    @Override
    public boolean isConsolidationCompleted(String taskListId) {
        TaskList taskList = taskListRepo.findById(taskListId);
        return taskList.getStatus() == FanOutProcessStatus.CONSOLIDATION_COMPLETED;
    }
}
