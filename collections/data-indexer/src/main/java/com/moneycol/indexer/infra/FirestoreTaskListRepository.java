package com.moneycol.indexer.infra;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.moneycol.indexer.tracker.Status;
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

    // Constructor injection does not seem to work with functions
    //@Inject
    private final Firestore firestore;

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
            log.error("Error creating taskList", e);
            throw new RuntimeException("Error creating taskList", e);
        }
    }

    public void updateTaskListProcessCompletionInTransaction(String taskListId, Consumer<String> doneConsumer) {

        try {
            // idea of the lock: https://medium.com/@codyzus/lock-down-with-cloud-firestore-61a98d1f4647
            firestore.runTransaction(transaction -> {
                try {
                    log.info("Incrementing task count completion in transaction for taskListId {}", taskListId);

                    DocumentReference taskListRef = firestore.collection("taskLists").document(taskListId);
                    TaskList taskList = transaction.get(taskListRef).get().toObject(TaskList.class);

                    taskList.setCompletedTasks(taskList.getCompletedTasks() + 1);
                    log.info("Tasks {}, completed {}", taskList.getNumberOfTasks(), taskList.getCompletedTasks());
                    if (taskList.allSpawnedTasksCompleted() && taskList.getStatus() != Status.PROCESSING_COMPLETED) {
                        log.info("All tasks completed for taskList {} -- updating status", taskListId);
                        transaction.update(taskListRef, "status", "PROCESSING_COMPLETED");
                        transaction.update(taskListRef, "completedTasks", taskList.getCompletedTasks() + 1);

                        //taskList.setStatus(Status.PROCESSING_COMPLETED);
                        //transaction.set(taskListRef, taskList);
                        log.info("TaskList {} completed processing now -- executing follow-up", taskListId);
                        log.info("Completed FULL set of tasks for taskListId {}", taskListId);
                        doneConsumer.accept(taskListId);
                    } else if (taskList.allSpawnedTasksCompleted()) {
                        log.info("TaskList {} has already completed processing before -- ignoring", taskListId);
                        //transaction.set(taskListRef, taskList);
                        transaction.update(taskListRef, "completedTasks", taskList.getCompletedTasks() + 1);

                    } else {
                        log.info("Progress updated for taskList {} to {}", taskListId, taskList.getCompletedTasks() + 1);
                        //transaction.set(taskListRef, taskList);
                        transaction.update(taskListRef, "completedTasks", taskList.getCompletedTasks() + 1);
                    }

                } catch (Throwable t) {
                    log.error("Error updating taskList {}, it will retry", taskListId, t);
                }

                return true;
            }).get();
        } catch (Throwable t) {
            log.warn("Error running transaction to update taskListId {}", taskListId, t);
        }
    }
}

