package com.moneycol.indexer;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.TaskList;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.concurrent.ExecutionException;

// keep track of all the batches
// https://stackoverflow.com/questions/55222414/increment-existing-value-in-firebase-firestore

@Singleton
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class FirestoreTracker implements FanOutTracker {

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
        } catch (ExecutionException | InterruptedException e) {
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
            Long totalTasks = doc.getLong("totalTasks");
            assert completedTasks != null;
            assert totalTasks != null;
            return completedTasks < totalTasks;
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
}
