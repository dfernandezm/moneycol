package com.moneycol.indexer.tracker;

import com.moneycol.indexer.infra.PubSubClient;
import com.moneycol.indexer.tracker.tasklist.TaskList;
import com.moneycol.indexer.tracker.tasklist.TaskListRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Singleton
public class DefaultFanOutTracker implements FanOutTracker {

    private static final String BATCHES_TOPIC_NAME_TEMPLATE = "%s.moneycol.indexer.batches";
    public static final String DONE_TOPIC_NAME_TEMPLATE = "%s.moneycol.indexer.batching.done";

    // Constructor injection does not seem to work with functions
    @Inject
    private TaskListRepository taskListRepo;

    @Inject
    private PubSubClient pubSubClient;

    @Override
    public String createTaskList(TaskList taskList) {
        return taskListRepo.createTaskList(taskList);
    }

    @Override
    public boolean hasCompleted(String taskListId) {
        TaskList taskList = taskListRepo.findById(taskListId);
        return taskList.hasCompleted();
    }

    @Override
    public void incrementCompletedCount(String taskListId, Integer quantity) {
        taskListRepo.updateFieldAtomically(taskListId, "completedTasks", 1);
    }

    @Override
    public void complete(String taskListId) {
        TaskList taskList = taskListRepo.findById(taskListId);
        taskList.complete();
        taskListRepo.update(taskList);
    }

    @Override
    public void updateTracking(GenericTask<?> genericTask) {
        String taskListId = genericTask.getTaskListId();

        log.info("Incrementing task count completion for taskListId {}", taskListId);
        incrementCompletedCount(taskListId, 1);

        if (hasCompleted(taskListId)) {
            log.info("Completed FULL set of tasks for taskListId {}", taskListId);
            complete(taskListId);
            publishDone(taskListId);
        }
    }

    @Override
    public void publishTask(GenericTask<?> genericTask) {
        String batchesTopic = String.format(BATCHES_TOPIC_NAME_TEMPLATE, DEFAULT_ENV);
        pubSubClient.publishMessage(batchesTopic, genericTask);
    }

    @Override
    public void publishIntermediateResult() {

    }

    private void publishDone(String taskListId) {
        String doneTopicName = String.format(DONE_TOPIC_NAME_TEMPLATE, DEFAULT_ENV);
        TaskListDoneResult taskListDoneResult = TaskListDoneResult.builder()
                .taskListId(taskListId)
                .status(Status.PROCESSING_COMPLETED)
                .build();
        log.info("Publishing DONE status to pubsub {}", taskListDoneResult);
        pubSubClient.publishMessage(doneTopicName, taskListDoneResult);
    }

}
