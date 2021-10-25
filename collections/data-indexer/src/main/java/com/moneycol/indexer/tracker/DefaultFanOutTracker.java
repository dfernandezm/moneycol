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
    public boolean allTasksCompleted(String taskListId) {
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
    public void updateOverallTaskProgressAtomically(GenericTask<?> genericTask) {
        String taskListId = genericTask.getTaskListId();
        taskListRepo.updateTaskListProcessCompletionInTransaction(taskListId, this::notifyProcessingDone);
    }

    @Override
    public void spawnTask(GenericTask<?> genericTask) {
        String batchesTopic = fanOutConfigurationProperties.getPubSub().getTriggerTopicName();
        pubSubClient.publishMessage(batchesTopic, genericTask);
    }

    @Override
    public <T> void publishIntermediateResult(T resultData) {
        String sinkTopicName = fanOutConfigurationProperties.getPubSub().getSinkTopicName();
        pubSubClient.publishMessage(sinkTopicName, resultData);
    }

    @Override
    public void notifyProcessingDone(String taskListId) {
        String doneTopicName = fanOutConfigurationProperties.getPubSub().getDoneTopicName();
        TaskListStatusResult taskListDoneResult = TaskListStatusResult.builder()
                .taskListId(taskListId)
                .status(Status.PROCESSING_COMPLETED)
                .build();
        log.info("Publishing PROCESSING_COMPLETED status to PubSub {}", taskListDoneResult);
        pubSubClient.publishMessage(doneTopicName, taskListDoneResult);
    }

    @Override
    public void updateTaskListStatus(String taskListId, Status status) {
        TaskList taskList = taskListRepo.findById(taskListId);
        taskList.setStatus(status);
        taskListRepo.update(taskList);
    }

    @Override
    public void updateValuesToProcessCount(String taskListId, Integer count) {
        taskListRepo.updateFieldAtomically(taskListId, "valuesToProcess", count);
    }

    @Override
    public boolean hasConsolidationCompleted(String taskListId) {
        TaskList taskList = taskListRepo.findById(taskListId);
        return taskList.getStatus() == Status.CONSOLIDATION_COMPLETED;
    }
}
