package com.moneycol.indexer;

import com.google.api.gax.paging.Page;
import com.google.cloud.PageImpl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.batcher.BatcherFunctionExecutor;
import com.moneycol.indexer.batcher.FileBatcher;
import com.moneycol.indexer.batcher.FilesBatch;
import com.moneycol.indexer.batcher.Inventory;
import com.moneycol.indexer.infra.GcsClient;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.IntermediateTask;
import com.moneycol.indexer.tracker.tasklist.TaskList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BatcherFunctionTest {

    private BatcherFunctionExecutor batcherFunction;

    FanOutTracker fanOutTracker;
    FileBatcher fileBatcher;

    private static final String SINGLE_FILENAME = "filename-test.json";
    private static final String INVENTORY_FILENAME = "inventory-test.json";

    @BeforeEach
    public void setUp() {
        fanOutTracker = mock(FanOutTracker.class);
        fileBatcher = mock(FileBatcher.class);
        batcherFunction = new BatcherFunctionExecutor(fanOutTracker, fileBatcher);
    }

    @AfterEach
    public void clear() {
        Mockito.reset(fanOutTracker, fileBatcher);
    }

    @Test
    public void processInventoryWithOneFileCorrectly() {

        // Given
        when(fileBatcher.buildAndStoreInventory(any())).thenReturn(fakeSingleFileBatchWithSingleFileInventory());
        Message message = anyJsonMessage();

        // When
        batcherFunction.execute(message, null);

        // Then
        verify(fileBatcher, only()).buildAndStoreInventory(any());
        String taskListId = assertSingleTaskListCreated();
        assertCorrectWorkerTaskPublishedFor(taskListId, 1, List.of(SINGLE_FILENAME));
    }

    @Test
    public void shouldUseConfiguredBucketToReadData() {
        // Given
        FanOutConfigurationProperties fanOutConfigurationProperties =
                mockBucketNameConfigWith("moneycol-import");
        Message m = anyJsonMessage();

        Page<Blob> pages = new PageImpl<>(null, "", new ArrayList<>());
        GcsClient mockGcsClient = mock(GcsClient.class);
        when(mockGcsClient.listBucketBlobs(any(), any())).thenReturn(pages);

        FileBatcher fileBatcher = new FileBatcher(mockGcsClient, fanOutConfigurationProperties);
        batcherFunction = new BatcherFunctionExecutor(fanOutTracker, fileBatcher);
        batcherFunction.execute(m, null);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockGcsClient).listBucketBlobs(captor.capture(), any());
        assertThat(captor.getValue()).isEqualTo("moneycol-import");
    }

    private FanOutConfigurationProperties mockBucketNameConfigWith(String bucketName) {
        FanOutConfigurationProperties fanOutConfigurationProperties = mock(FanOutConfigurationProperties.class);
        when(fanOutConfigurationProperties.getSourceBucketName()).thenReturn(bucketName);
        return fanOutConfigurationProperties;
    }

    /**
     * Given a config with bucketName
     * And a triggering message with dataUri path
     * When executing the batcher function
     * Then the inventory json file is written in the path `bucket/dataUri/inventory.json`
     */
    @Test
    public void inventoryFileIsWrittenInDataUri() {
        // Given
        String bucketName = "moneycol-import";
        FanOutConfigurationProperties fanOutConfigurationProperties =
                mockBucketNameConfigWith(bucketName);
        String dataUri = "colnect/31-10-2021";
        Message messageWithDataUri =
                aMessage(String.format("{\"doneMessage\":\"crawling-done\",\"dataUri\":\"%s\"}", dataUri));

        GcsClient mockGcsClient = mock(GcsClient.class);
        mockBlobsResult(mockGcsClient);
        FileBatcher fileBatcher = new FileBatcher(mockGcsClient, fanOutConfigurationProperties);

        // When
        batcherFunction = new BatcherFunctionExecutor(fanOutTracker, fileBatcher);
        batcherFunction.execute(messageWithDataUri, null);

        // Then
        ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> objectPathCaptor = ArgumentCaptor.forClass(String.class);

        String expectedInventoryJsonLocation = "colnect/31-10-2021/inventory.json";
        verify(mockGcsClient).writeToGcs(bucketCaptor.capture(), objectPathCaptor.capture(), any());
        assertThat(bucketCaptor.getValue()).isEqualTo(bucketName);
        assertThat(objectPathCaptor.getValue()).isEqualTo(expectedInventoryJsonLocation);
    }

    /**
     * Given config has a bucket name
     * And dataUri is present in triggering message
     * And there's blob/s present in the bucket path 'bucketName/dataUri'
     * When executing the batcher function
     * Then the filenames in the batch are from the URI 'bucketName/dataUri/filename'
     */
    @Test
    public void filenamesInInventoryContainDataUri() {
        // Given
        String bucketName = "moneycol-import";
        String dataUri = "colnect/31-10-2021";
        String dataFilename = "en-Spain-p-1.json";
        FanOutConfigurationProperties fanOutConfigurationProperties =
                mockBucketNameConfigWith(bucketName);
        Message messageWithDataUri =
                aMessage(String.format("{\"doneMessage\":\"crawling-done\",\"dataUri\":\"%s\"}", dataUri));

        GcsClient mockGcsClient = mock(GcsClient.class);
        mockBlobsResultWithValues(mockGcsClient, bucketName,
                dataUri, dataFilename);

        // When
        FileBatcher fileBatcher = new FileBatcher(mockGcsClient, fanOutConfigurationProperties);
        batcherFunction = new BatcherFunctionExecutor(fanOutTracker, fileBatcher);
        batcherFunction.execute(messageWithDataUri, null);

        // Then
        ArgumentCaptor<IntermediateTask<?>> taskArgumentCaptor = ArgumentCaptor.forClass(IntermediateTask.class);
        verify(fanOutTracker).spawnTask(taskArgumentCaptor.capture());

        String expectedFilenamePath = "colnect/31-10-2021/en-Spain-p-1.json";
        FilesBatch batch = (FilesBatch) taskArgumentCaptor.getValue().getContent();
        assertThat(batch.getFilenames()).hasSize(1);
        assertThat(batch.getFilenames()).contains(expectedFilenamePath);
    }

    private void mockBlobsResult(GcsClient mockGcsClient) {
        Page<Blob> pages = new PageImpl<>(null, "", new ArrayList<>());
        when(mockGcsClient.listBucketBlobs(any(), any())).thenReturn(pages);
    }

    private void mockBlobsResultWithValues(GcsClient mockGcsClient, String bucketName,
                                           String dataUri, String filename) {
        BlobInfo blobInfo =
                BlobInfo.newBuilder(BlobId.of(bucketName, dataUri + "/" + filename)).build();

        Blob blob = mock(Blob.class);
        when(blob.getBlobId()).thenReturn(BlobId.of(bucketName, dataUri + "/" + filename));
        when(blob.getName()).thenReturn(blobInfo.getName());

        List<Blob> blobs = List.of(blob);
        Page<Blob> pages = new PageImpl<>(null, "", blobs);
        when(mockGcsClient.listBucketBlobs(any(), any())).thenReturn(pages);
    }

    private Message anyJsonMessage() {
        Message message = new Message();
        message.setData(Base64.getEncoder().encodeToString(
                "{ \"test\":\"done\" }".getBytes(StandardCharsets.UTF_8)));
        return message;
    }

    private Message aMessage(String messageData) {
        Message message = new Message();
        message.setData(Base64.getEncoder().encodeToString(
                messageData.getBytes(StandardCharsets.UTF_8)));
        return message;
    }

    private void assertCorrectWorkerTaskPublishedFor(String taskListId, int numberOfWorkerTasks,
                                                     List<String> filenamesInBatch) {
        ArgumentCaptor<IntermediateTask<FilesBatch>> genericTaskArgumentCaptor = ArgumentCaptor.forClass(IntermediateTask.class);
        verify(fanOutTracker, times(numberOfWorkerTasks)).spawnTask(genericTaskArgumentCaptor.capture());

        assertThat(genericTaskArgumentCaptor.getValue().getTaskListId()).isEqualTo(taskListId);
        assertThat(genericTaskArgumentCaptor
                .getValue()
                .getContent()
                .getFilenames()).containsAll(filenamesInBatch);
    }

    private String assertSingleTaskListCreated() {
        ArgumentCaptor<TaskList> taskListArgumentCaptor = ArgumentCaptor.forClass(TaskList.class);
        verify(fanOutTracker, times(1)).createTaskList(taskListArgumentCaptor.capture());

        String taskListId = taskListArgumentCaptor.getValue().getId();
        assertThat(taskListId).isNotNull();
        assertThat(taskListArgumentCaptor.getValue().getNumberOfTasks()).isEqualTo(1);
        return taskListId;
    }

    private Inventory fakeSingleFileBatchWithSingleFileInventory() {
        Inventory inventory = Inventory.builder()
                .rootName(INVENTORY_FILENAME)
                .build();
        FilesBatch filesBatch = FilesBatch.builder()
                .processed(false)
                .batchSize(30)
                .build();
        filesBatch.addFile(SINGLE_FILENAME);
        inventory.addFileBatch(filesBatch);
        return inventory;
    }
}
