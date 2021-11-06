package com.moneycol.indexer.infra;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.TransactionOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.common.base.Preconditions;
import com.moneycol.indexer.tracker.FanOutProcessStatus;
import com.moneycol.indexer.tracker.tasklist.TaskList;
import com.moneycol.indexer.tracker.tasklist.TaskListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Singleton
public class FirestoreTaskListRepository implements TaskListRepository {

    private final Firestore firestore;

    private static final int MAX_NUMBER_OF_TRANSACTION_RETRIES = 30;

    @Override
    public String createTaskList(TaskList taskList) {
        try {
            DocumentReference cr = firestore.collection("taskLists").document(taskList.getId());
            WriteResult result = cr.create(taskList).get();
            log.info("Created taskList at {} with ID: {}", result.getUpdateTime(), taskList.getId());
            return taskList.getId();
        } catch (Exception e) {
            log.error("Error creating taskList", e);
            throw new RuntimeException("Error creating taskList", e);
        }
    }

    @Override
    public TaskList findById(String taskListId) {
        DocumentReference doc = firestore.collection("taskLists").document(taskListId);
        try {
            return doc.get().get().toObject(TaskList.class);
        } catch (Exception e) {
            log.error("Error taskList", e);
            throw new RuntimeException("Error taskList", e);
        }
    }

    @Override
    public void update(TaskList toUpdate) {
        if (toUpdate.getId() != null) {
            DocumentReference doc = firestore.collection("taskLists").document(toUpdate.getId());
            try {
                doc.set(toUpdate).get();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            throw new IllegalArgumentException("TaskList does not have an ID");
        }
    }

    @Override
    public void updateFieldAtomically(String taskListId, String fieldName, Integer quantity) {
        DocumentReference taskList = firestore.collection("taskLists").document(taskListId);
        try {
            taskList.update(fieldName, FieldValue.increment(quantity)).get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error in taskList", e);
            throw new RuntimeException("Error in taskList", e);
        }
    }

    public void executeTaskListUpdateInTransaction(String taskListId, Consumer<String> doneConsumer) {
        TransactionOptions transactionOptions = TransactionOptions.createReadWriteOptionsBuilder()
                .setNumberOfAttempts(MAX_NUMBER_OF_TRANSACTION_RETRIES)
                .build();
        try {
            // idea of the lock: https://medium.com/@codyzus/lock-down-with-cloud-firestore-61a98d1f4647
            firestore.runTransaction(transaction -> {
                try {
                    log.info("Incrementing task count completion in transaction for taskListId {}", taskListId);

                    DocumentReference taskListRef = firestore.collection("taskLists").document(taskListId);
                    TaskList taskList = transaction.get(taskListRef).get().toObject(TaskList.class);
                    Preconditions.checkNotNull(taskList);
                    taskList.setCompletedTasks(taskList.getCompletedTasks() + 1);

                    if (taskList.allSpawnedTasksCompleted() && taskList.getStatus() != FanOutProcessStatus.PROCESSING_COMPLETED) {
                        log.info("All tasks completed for taskList {} -- updating status", taskListId);
                        taskList.setStatus(FanOutProcessStatus.PROCESSING_COMPLETED);
                        log.info("TaskList {} completed processing now -- executing follow-up", taskListId);
                        log.info("Completed FULL set of tasks for taskListId {}", taskListId);
                        transaction.set(taskListRef, taskList);

                        // may not execute this here to not hold the transaction and use a lock to execute after the transaction
                        // given that, a that point, only 1 worker will execute this code
                        // and others will be ignored
                        doneConsumer.accept(taskListId);
                    } else if (taskList.getCompletedTasks() >= taskList.getNumberOfTasks()) {
                        log.info("TaskList {} has already completed processing -- ignoring",
                                taskListId);
                    } else {
                        log.info("Updating progress for taskList {} to {} from a total of {}",
                                taskListId,
                                taskList.getCompletedTasks() + 1,
                                taskList.getNumberOfTasks());
                        transaction.set(taskListRef, taskList);
                    }

                } catch (Throwable t) {
                    log.error("Error updating taskList {}, it will retry", taskListId, t);
                }

                return 0;
            }, transactionOptions).get();
        } catch (Throwable t) {
            log.error("Error running transaction to update taskListId {}", taskListId, t);
        }
    }
}

