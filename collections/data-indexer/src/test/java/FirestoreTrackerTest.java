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
import com.moneycol.indexer.FirestoreTracker;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.TaskList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.fail;

public class FirestoreTrackerTest {

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
    public void testSimple() throws ExecutionException, InterruptedException {

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
        FanOutTracker fanOutTracker = new FirestoreTracker(firestore);
        TaskList taskList = TaskList.create(250);

        String taskListId = fanOutTracker.createTaskList(taskList);

        assertThat(taskListId).isNotNull();
        assertTaskListHasExpectedFields(taskListId);
    }

    @Test
    public void concurrentIncrementsOfTasksReportCorrectValues() throws InterruptedException {
        FanOutTracker fanOutTracker = new FirestoreTracker(firestore);
        TaskList taskList = TaskList.create(250);
        String taskListId = fanOutTracker.createTaskList(taskList);

        ExecutorService executorService = Executors.newFixedThreadPool(20);

        // 600 iterations
        IntStream.range(0, 600).forEach(i -> {
            executorService.submit(() ->
                    fanOutTracker.incrementCompletedCount(taskListId, 1));
        });

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        TaskList found = findTaskList(taskListId);
        assertThat(found.getCompletedTasks()).isEqualTo(600);
    }

    @Test
    public void isDoneTest() throws InterruptedException {
        Integer completedTasks = 50;

        FanOutTracker fanOutTracker = new FirestoreTracker(firestore);
        TaskList taskList = TaskList.create(completedTasks);
        String taskListId = fanOutTracker.createTaskList(taskList);

        // concurrently complete tasks, leave last one not completed
        ExecutorService executorService = Executors.newFixedThreadPool(50);

        IntStream.range(0, completedTasks - 1).peek(i ->  {
            executorService.submit(() -> {
                fanOutTracker.incrementCompletedCount(taskListId, 1);
                sleep(100L);
                assertThat(fanOutTracker.isDone(taskListId)).isFalse();
            });
        });

        executorService.awaitTermination(3, TimeUnit.SECONDS);

        // once more
        fanOutTracker.incrementCompletedCount(taskListId, 1);
        assertThat(fanOutTracker.isDone(taskListId)).isTrue();

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
}
