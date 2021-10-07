package com.moneycol.indexer.infra;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.moneycol.indexer.tracker.tasklist.TaskList;
import com.moneycol.indexer.tracker.tasklist.TaskListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.concurrent.ExecutionException;


@Slf4j
//@AllArgsConstructor
//@NoArgsConstructor
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
}

