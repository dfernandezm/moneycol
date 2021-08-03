package com.moneycol.indexer.batcher;

import com.google.api.gax.paging.Page;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.moneycol.indexer.JsonWriter;
import io.micronaut.gcp.function.GoogleFunctionInitializer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * This function acts as a batcher/ventilator in a fan-out / fan-in
 * workflow.
 *
 * - It lists the target bucket with all the files
 * - Creates batches of 30 files to process
 * - Publishes the batches for workers to pick up in moneycol.indexer.banknotes.batches
 *
 */
@Slf4j
public class IndexerBatcher extends GoogleFunctionInitializer
        implements BackgroundFunction<PubSubMessage> {

    @Inject
    LoggingService loggingService;

    @Override
    public void accept(PubSubMessage message, Context context) {
        log.info("Function called with context {}", context);
        loggingService.logMessage(message);
        listObjects("moneycol", "moneycol-import");
    }

    public void listObjects(String projectId, String bucketName) {

        Storage storage = StorageOptions
                .newBuilder()
                .setProjectId(projectId)
                .build()
                .getService();

        Storage.BlobListOption blobListOption = Storage.BlobListOption.pageSize(250);
        Page<Blob> blobs = storage.list(bucketName, blobListOption);

        int i = 0;
        int batchSize = 30;

        Inventory inventory = Inventory.builder()
                                .rootName("inventory")
                                .build();

        FilesBatch filesBatch = FilesBatch.builder()
                                .batchSize(batchSize)
                                .processed(false)
                                .build();

        for (Blob blob : blobs.iterateAll()) {
            log.info("Found object: {}", blob.getName());
            if (i < batchSize) {
                log.info("Adding to existing batch: {}", blob.getName());
                i++;
            } else {
                log.info("Batch is finished, setting size and restarting");

                int currentBatchSize = i;
                i = 0;
                filesBatch.setBatchSize(currentBatchSize);
                inventory.addFileBatch(filesBatch);

                // restart
                filesBatch = FilesBatch.builder()
                        .batchSize(batchSize)
                        .processed(false)
                        .build();

            }

            filesBatch.addFile(blob.getName());
        }

        // add last batch
        inventory.addFileBatch(filesBatch);

        writeToGcs(inventory);
    }

    private void writeToGcs(Inventory inventory) {
        JsonWriter jsonWriter = new JsonWriter();
        String inventoryJson = jsonWriter.asJsonString(inventory);
        writeDataToGcs("inventory.json", inventoryJson);
    }

    private void writeDataToGcs(String objectName, String data) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of("moneycol-import", objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        // use GCS event to fire cloud function
        storage.create(blobInfo, data.getBytes());
    }

    // write to pubsub https://cloud.google.com/functions/docs/calling/pubsub
    // PubSub
    // 'dev.colnectBanknotes.batches'
    private void createTopicIfItDoesNotExist() {}

    // PubSub publish batch
    // Subscriber function picks up
    private void publishBatch() {}
}

//byte[] content = blob.getContent();
//String json = new String(content);

// this may take a lot of time (>9minutes) so need to consider:
// - CloudRun via CloudScheduler/PubSub
// - GKE job via CloudScheduler/Pubsub:
// https://medium.com/@avish1990/kubernetes-cron-job-with-gcp-pub-sub-cloud-sql-and-batch-jobs-4affca71388c
