package com.moneycol.indexer;

import com.google.api.core.ApiFuture;
import com.google.cloud.NoCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.moneycol.indexer.infra.FirestoreTaskListRepository;
import com.moneycol.indexer.infra.PubSubClient;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import com.moneycol.indexer.tracker.DefaultFanOutTracker;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.GenericTask;
import com.moneycol.indexer.tracker.Status;
import com.moneycol.indexer.tracker.tasklist.TaskList;
import com.moneycol.indexer.tracker.tasklist.TaskListRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testcontainers.containers.FirestoreEmulatorContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FanOutTrackerTest {

    private static FirestoreEmulatorContainer emulator = new FirestoreEmulatorContainer(
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:316.0.0-emulators")
    );

    private static Firestore firestore;

    @BeforeAll
    public static void setup() {
        emulator.start();
        FirestoreOptions options = FirestoreOptions.getDefaultInstance().toBuilder()
                .setHost(emulator.getEmulatorEndpoint())
                .setCredentials(NoCredentials.getInstance())
                .setProjectId("test-project")
                .build();
        firestore = options.getService();
    }

    @AfterAll
    public static void stop() {
        emulator.stop();
    }

    @Test
    public void firestoreBasicQuery() throws ExecutionException, InterruptedException {

        CollectionReference users = firestore.collection("users");
        DocumentReference docRef = users.document("alovelace");
        Map<String, Object> data = new HashMap<>();
        data.put("first", "Ada");
        data.put("last", "Lovelace");
        ApiFuture<WriteResult> result = docRef.set(data);
        result.get();

        ApiFuture<QuerySnapshot> query = users.get();
        QuerySnapshot querySnapshot = query.get();

        QueryDocumentSnapshot firstResult = querySnapshot.getDocuments().get(0);

        assertThat(firstResult.getData()).containsEntry("first", "Ada");
    }

    @Test
    public void testCreatesTaskListWithoutError() {
        PubSubClient pubSubClient = mock(PubSubClient.class);
        FanOutTracker fanOutTracker = prepareFanOutTracker(pubSubClient);
        TaskList taskList = TaskList.create(250);

        String taskListId = fanOutTracker.createTaskList(taskList);

        assertThat(taskListId).isNotNull();
        assertTaskListHasExpectedFields(taskListId);
    }

    @Test
    public void concurrentIncrementsOfTasksReportCorrectValues() throws InterruptedException {
        PubSubClient pubSubClient = Mockito.mock(PubSubClient.class);
        FanOutTracker fanOutTracker = prepareFanOutTracker(pubSubClient);

        Integer numberOfTasks = 600;
        TaskList taskList = TaskList.create(numberOfTasks);
        String taskListId = fanOutTracker.createTaskList(taskList);

        // If this concurrency is not enough, the test may fail as in Firestore emulator
        // the result won't be seen fast enough
        ExecutorService executorService = Executors.newFixedThreadPool(50);

        // 600 iterations
        IntStream.range(0, numberOfTasks).forEach(i -> {
            executorService.submit(() ->
                    fanOutTracker.incrementCompletedCount(taskListId, 1));
        });

        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);

        // add a wait to give Firestore emulator time to settle
        Thread.sleep(2000);
        TaskList found = findTaskList(taskListId);
        assertThat(found.getCompletedTasks()).isEqualTo(numberOfTasks);
    }

    @Test
    public void updatesStatusConcurrentlyForOneTask() throws InterruptedException {

        // Given 20
        Integer totalTasksToComplete = 1;
        PubSubClient pubSubClient = Mockito.mock(PubSubClient.class);
        FanOutTracker fanOutTracker = prepareFanOutTracker(pubSubClient);
        TaskList taskList = TaskList.create(totalTasksToComplete);
        String taskListId = fanOutTracker.createTaskList(taskList);
        GenericTask<?> genericTask = GenericTask.builder()
                .taskListId(taskListId)
                .status(Status.PROCESSING)
                .build();


        fanOutTracker.updateOverallTaskProgressAtomically(genericTask);

        // add a wait to give Firestore emulator time to settle
        Thread.sleep(1000);

        assertThat(fanOutTracker.allTasksCompleted(taskListId)).isTrue();
        assertEquals(findTaskList(taskListId).getStatus(), Status.PROCESSING_COMPLETED);
        // verify mock called

    }

    //TODO: test for decrement value

    @Test
    public void updatesValuesToProcessUpAndDown() {
        PubSubClient pubSubClient = Mockito.mock(PubSubClient.class);
        FanOutTracker fanOutTracker = prepareFanOutTracker(pubSubClient);
        TaskList taskList = TaskList.create(20);
        String taskListId = fanOutTracker.createTaskList(taskList);


        fanOutTracker.updateValuesToProcessCount(taskListId, 10);
        fanOutTracker.updateValuesToProcessCount(taskListId, 10);

        Integer decr = -1 * 10;
        fanOutTracker.updateValuesToProcessCount(taskListId, decr);
        fanOutTracker.updateValuesToProcessCount(taskListId, decr);

        TaskList finalTaskList = findTaskList(taskListId);
        assertEquals(0, finalTaskList.getValuesToProcess());

    }

    // https://firebase.google.com/docs/emulator-suite/connect_firestore
    @Disabled("Transactions do not work on emulator")
    @Test
    public void updatesStatusConcurrentlyOnlyOnceForMultipleTasks() throws InterruptedException {

        // Given 20
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Integer totalTasksToComplete = 2;
        PubSubClient pubSubClient = Mockito.mock(PubSubClient.class);
        FanOutTracker fanOutTracker = prepareFanOutTracker(pubSubClient);
        TaskList taskList = TaskList.create(totalTasksToComplete);
        String taskListId = fanOutTracker.createTaskList(taskList);
        GenericTask<?> genericTask = GenericTask.builder()
                .taskListId(taskListId)
                .status(Status.PROCESSING)
                .build();

        for (int i = 0; i < totalTasksToComplete; i++) {
            executorService.submit(() -> {
                fanOutTracker.updateOverallTaskProgressAtomically(genericTask);
           });
         }

        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);

        // add a wait to give Firestore emulator time to settle
        Thread.sleep(2000);

       // assertThat(fanOutTracker.allTasksCompleted(taskListId)).isTrue();
        assertEquals(Status.PROCESSING_COMPLETED, findTaskList(taskListId).getStatus());
    }


    @Test
    public void isDoneTest() throws InterruptedException, ExecutionException {
        Integer totalTasksToComplete = 150;
        PubSubClient pubSubClient = mock(PubSubClient.class);
        FanOutTracker fanOutTracker = prepareFanOutTracker(pubSubClient);
        TaskList taskList = TaskList.create(totalTasksToComplete);
        String taskListId = fanOutTracker.createTaskList(taskList);

        // concurrently complete tasks, leave last one not completed
        ExecutorService executorService = Executors.newFixedThreadPool(50);

        for (int i = 0; i < totalTasksToComplete - 1; i++) {
            executorService.submit(() -> {
                fanOutTracker.incrementCompletedCount(taskListId, 1);
                sleep(100L);
                try {
                    assertThat(fanOutTracker.allTasksCompleted(taskListId)).isFalse();
                } catch (AssertionError ae) {
                    fail(ae);
                }

            });
        }

        executorService.awaitTermination(3, TimeUnit.SECONDS);

        // once more
        fanOutTracker.incrementCompletedCount(taskListId, 1);
        assertThat(fanOutTracker.allTasksCompleted(taskListId)).isTrue();
    }

    @Test
    public void completesWithCorrectStatus() throws InterruptedException, ExecutionException {
        Integer totalTasksToComplete = 150;

        PubSubClient pubSubClient = mock(PubSubClient.class);
        FanOutTracker fanOutTracker = prepareFanOutTracker(pubSubClient);
        TaskList taskList = TaskList.create(totalTasksToComplete);
        String taskListId = fanOutTracker.createTaskList(taskList);

        // When
        fanOutTracker.completeProcessing(taskListId);

        // Then
        taskList = findTaskList(taskListId);
        assertThat(taskList).isNotNull();
        assertThat(taskList.getStatus()).isEqualTo(Status.PROCESSING_COMPLETED);
    }

    private void sleep(Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void assertTaskListHasExpectedFields(String id) {
        CollectionReference taskLists = firestore.collection("taskLists");
        DocumentReference docRef = taskLists.document(id);

        try {
            DocumentSnapshot ds = docRef.get().get();
            assertThat(ds.exists()).isTrue();
            TaskList taskList = ds.toObject(TaskList.class);
            assert taskList != null;
            assertThat(taskList.getCompletedTasks()).isEqualTo(0);
            assertThat(taskList.getNumberOfTasks()).isEqualTo(250);
        } catch(ExecutionException | InterruptedException e) {
            fail(e);
        }
    }

    private TaskList findTaskList(String taskListId) {
        CollectionReference taskLists = firestore.collection("taskLists");
        DocumentReference docRef = taskLists.document(taskListId);

        try {
            DocumentSnapshot ds = docRef.get().get();
            assertThat(ds.exists()).isTrue();
            return ds.toObject(TaskList.class);
        } catch(ExecutionException | InterruptedException e) {
            fail(e);
            return null;
        }
    }

    private FanOutTracker prepareFanOutTracker(PubSubClient pubSubClientMock) {
        TaskListRepository taskListRepository = new FirestoreTaskListRepository(firestore);
        FanOutConfigurationProperties fanoutConfig = Mockito.mock(FanOutConfigurationProperties.class);
        FanOutConfigurationProperties.PubSubConfigurationProperties pubsubProps =
                mock(FanOutConfigurationProperties.PubSubConfigurationProperties.class);
        when(pubsubProps.getDoneTopicName()).thenReturn("done-test");
        when(fanoutConfig.getPubSub()).thenReturn(pubsubProps);

        return new DefaultFanOutTracker(taskListRepository, pubSubClientMock, fanoutConfig);
    }
}
