import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.batcher.BatcherFunction;
import com.moneycol.indexer.batcher.FileBatcher;
import com.moneycol.indexer.batcher.FilesBatch;
import com.moneycol.indexer.batcher.Inventory;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.GenericTask;
import com.moneycol.indexer.tracker.tasklist.TaskList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for main.java.com.example.functions.helloworld.HelloPubSub.
 */
public class BatcherFunctionTest {

    private BatcherFunction batcherFunction;
    private static final Logger logger = Logger.getLogger(BatcherFunctionTest.class.getName());

    FanOutTracker fanOutTracker;
    FileBatcher fileBatcher;

    @BeforeEach
    public void setUp() {
        fanOutTracker = Mockito.mock(FanOutTracker.class);
        fileBatcher = Mockito.mock(FileBatcher.class);
        batcherFunction = new BatcherFunction(fanOutTracker, fileBatcher);
    }

    @AfterEach
    public void clear() {
        Mockito.reset(fanOutTracker, fileBatcher);
    }

    @Test
    public void processInventoryCorrectly() {

        // Given
        when(fileBatcher.buildAndStoreInventory()).thenReturn(fakeInventory());
        Message message = new Message();
        message.setData(Base64.getEncoder().encodeToString(
                "John".getBytes(StandardCharsets.UTF_8)));

        // When
        batcherFunction.accept(message, null);

        // Then
        verify(fileBatcher, only()).buildAndStoreInventory();

        String taskListId = assertSingleTaskListCreated();

        assertCorrectWorkerTaskPublishedFor(taskListId);
    }

    private void assertCorrectWorkerTaskPublishedFor(String taskListId) {
        ArgumentCaptor<GenericTask<FilesBatch>> genericTaskArgumentCaptor = ArgumentCaptor.forClass(GenericTask.class);
        verify(fanOutTracker, times(1)).publishWorkerTask(genericTaskArgumentCaptor.capture());

        assertThat(genericTaskArgumentCaptor.getValue().getTaskListId()).isEqualTo(taskListId);
        assertThat(genericTaskArgumentCaptor
                .getValue()
                .getContent()
                .getFilenames()).containsOnly("filename-test.json");
    }

    private String assertSingleTaskListCreated() {
        ArgumentCaptor<TaskList> taskListArgumentCaptor = ArgumentCaptor.forClass(TaskList.class);
        verify(fanOutTracker, times(1)).createTaskList(taskListArgumentCaptor.capture());

        String taskListId = taskListArgumentCaptor.getValue().getId();
        assertThat(taskListId).isNotNull();
        assertThat(taskListArgumentCaptor.getValue().getNumberOfTasks()).isEqualTo(1);
        return taskListId;
    }

    private Inventory fakeInventory() {
        Inventory inventory = Inventory.builder().rootName("inventory-test.json").build();
        FilesBatch filesBatch = FilesBatch.builder()
                .processed(false)
                .batchSize(30)
                .build();
        filesBatch.addFile("filename-test.json");
        inventory.addFileBatch(filesBatch);
        return inventory;
    }
}
