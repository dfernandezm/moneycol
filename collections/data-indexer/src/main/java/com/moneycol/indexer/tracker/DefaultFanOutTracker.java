package com.moneycol.indexer.tracker;

import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.infra.JsonWriter;
import com.moneycol.indexer.infra.PubSubClient;
import com.moneycol.indexer.tracker.tasklist.TaskList;
import com.moneycol.indexer.tracker.tasklist.TaskListRepository;
import io.micronaut.context.annotation.Primary;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Singleton
@Primary
public class DefaultFanOutTracker implements FanOutTracker {

    private static final String BATCHES_TOPIC_NAME_TEMPLATE = "%s.moneycol.indexer.batches";
    public static final String DONE_TOPIC_NAME_TEMPLATE = "%s.moneycol.indexer.batching.done";
    private static final String SINK_TOPIC_NAME_TEMPLATE = "%s.moneycol.indexer.sink";
    private static final String SINK_TOPIC_NAME = String.format(SINK_TOPIC_NAME_TEMPLATE, DEFAULT_ENV);

    private final TaskListRepository taskListRepo;
    private final PubSubClient pubSubClient;
    private final JsonWriter jsonWriter;

    public DefaultFanOutTracker(TaskListRepository taskListRepository, PubSubClient pubSubClient,
                                JsonWriter jsonWriter) {
        this.pubSubClient = pubSubClient;
        this.taskListRepo = taskListRepository;
        this.jsonWriter = jsonWriter;
    }

    @Override
    public String createTaskList(TaskList taskList) {
        return taskListRepo.createTaskList(taskList);
    }

    @Override
    public boolean hasCompletedProcessing(String taskListId) {
        TaskList taskList = taskListRepo.findById(taskListId);
        return taskList.hasProcessingCompleted();
    }

    @Override
    public void incrementCompletedCount(String taskListId, Integer quantity) {
        taskListRepo.updateFieldAtomically(taskListId, "completedTasks", 1);
    }

    @Override
    public void completeProcessing(String taskListId) {
        TaskList taskList = taskListRepo.findById(taskListId);
        taskList.completeProcessing();
        taskListRepo.update(taskList);
    }

    @Override
    public void updateProcessingFor(GenericTask<?> genericTask) {
        String taskListId = genericTask.getTaskListId();

        log.info("Incrementing task count completion for taskListId {}", taskListId);
        incrementCompletedCount(taskListId, 1);

        if (hasCompletedProcessing(taskListId)) {
            log.info("Completed FULL set of tasks for taskListId {}", taskListId);
            completeProcessing(taskListId);
            publishProcessingDone(taskListId);
        }
    }

    @Override
    public void publishWorkerTask(GenericTask<?> genericTask) {
        String batchesTopic = String.format(BATCHES_TOPIC_NAME_TEMPLATE, DEFAULT_ENV);
        pubSubClient.publishMessage(batchesTopic, genericTask);
    }

    @Override
    public <T> void publishIntermediateResult(T resultData) {
        pubSubClient.publishMessage(SINK_TOPIC_NAME, resultData);
    }

    @Override
    public GenericTask<?> readMessageAsTask(Message message) {
        String messageString = messageToString(message);
        log.info("De serializing message...");
        return jsonWriter.toGenericTask(messageString);
    }

    @Override
    public void publishProcessingDone(String taskListId) {
        String doneTopicName = String.format(DONE_TOPIC_NAME_TEMPLATE, DEFAULT_ENV);
        TaskListStatusResult taskListDoneResult = TaskListStatusResult.builder()
                .taskListId(taskListId)
                .status(Status.PROCESSING_COMPLETED)
                .build();
        log.info("Publishing PROCESSING_DONE status to PubSub {}", taskListDoneResult);
        pubSubClient.publishMessage(doneTopicName, taskListDoneResult);
    }

    @Override
    public TaskListStatusResult readMessageAsTaskListStatus(Message message) {
        String messageString = messageToString(message);
        log.info("De serializing message...");
        return jsonWriter.toObject(messageString, TaskListStatusResult.class);
    }

    @Override
    public void updateTaskListStatus(String taskListId, Status status) {
        TaskList taskList = taskListRepo.findById(taskListId);
        taskList.setStatus(status);
        taskListRepo.update(taskList);
    }

    @Override
    public boolean hasConsolidationCompleted(String taskListId) {
        TaskList taskList = taskListRepo.findById(taskListId);
        return taskList.getStatus() == Status.CONSOLIDATION_COMPLETED;
    }

    private String messageToString(Message message) {
        return new String(
                Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }
}
