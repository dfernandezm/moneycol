package com.moneycol.indexer;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.Status;
import com.moneycol.indexer.tracker.tasklist.TaskList;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutionException;

// Replace with FanoutTracker
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Singleton
public class FirestoreTracker implements FanOutTracker {

    // Constructor injection does not seem to work with functions
    @Inject
    private Firestore firestore;

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
    public boolean hasCompleted(String taskListId) {
        DocumentReference taskList = firestore.collection("taskLists").document(taskListId);
        try {
            DocumentSnapshot doc = taskList.get().get();
            TaskList storedTaskList = doc.toObject(TaskList.class);
            if  (storedTaskList != null) {
                return storedTaskList.hasCompleted();
            } else {
                throw new RuntimeException("TaskList not found " + taskListId);
            }
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error creating taskList", e);
            throw new RuntimeException("Error creating taskList", e);
        }
    }

    @Override
    public void incrementCompletedCount(String taskListId, Integer quantity) {
        DocumentReference taskList = firestore.collection("taskLists").document(taskListId);
        try {
            taskList.update("completedTasks", FieldValue.increment(quantity)).get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error creating taskList", e);
            throw new RuntimeException("Error creating taskList", e);
        }
    }

    @Override
    public void complete(String taskListId) {
        DocumentReference taskListDoc = firestore.collection("taskLists").document(taskListId);
        try {
            TaskList storedTaskList = taskListDoc.get().get().toObject(TaskList.class);
            if (storedTaskList != null) {
                storedTaskList.setStatus(Status.COMPLETED);
                WriteResult writeResult = taskListDoc.set(storedTaskList).get();
                log.info("TaskList with ID {} completed: {}", storedTaskList.getId(),
                        writeResult.getUpdateTime());

            }
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error creating taskList", e);
            throw new RuntimeException("Error creating taskList", e);
        }
    }
}
