package com.moneycol.indexer;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.Status;
import com.moneycol.indexer.tracker.TaskList;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Singleton
public class FirestoreTracker implements FanOutTracker {

    // Constructor injection does not seem to work with functions
    @Inject
    private Firestore firestore;

    public void createTaskList() {
        TaskList taskList = TaskList.create(250);
        try {
            DocumentReference cr = firestore.collection("taskLists").document(taskList.getId());
            cr.create(taskList).get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error creating collection", e);
        }
    }

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
    public boolean isDone(String taskListId) {
        DocumentReference taskList = firestore.collection("taskLists").document(taskListId);
        try {
            DocumentSnapshot doc = taskList.get().get();
            Long completedTasks = doc.getLong("completedTasks");
            Long numberOfTasks = doc.getLong("numberOfTasks");
            assert completedTasks != null;
            assert numberOfTasks != null;
            return completedTasks.equals(numberOfTasks);
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
        DocumentReference taskList = firestore.collection("taskLists").document(taskListId);
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", Status.COMPLETED);
            taskList.update(updates).get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error creating taskList", e);
            throw new RuntimeException("Error creating taskList", e);
        }
    }
}
