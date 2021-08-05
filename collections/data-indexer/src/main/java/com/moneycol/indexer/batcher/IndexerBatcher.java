package com.moneycol.indexer.batcher;

import com.google.api.gax.paging.Page;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.moneycol.indexer.JsonWriter;
import io.micronaut.gcp.function.GoogleFunctionInitializer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

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
    private LoggingService loggingService;
    private final JsonWriter jsonWriter = new JsonWriter();
    private Publisher publisher;

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

                publishBatch(filesBatch);

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

    // https://stackoverflow.com/questions/17374743/how-can-i-get-the-memory-that-my-java-program-uses-via-javas-runtime-api
    private void publishBatch(FilesBatch filesBatch) {
        String filesBatchJson = jsonWriter.asJsonString(filesBatch);

        // Create the PubsubMessage object
        ByteString byteStr = ByteString.copyFrom(filesBatchJson, StandardCharsets.UTF_8);
        PubsubMessage pubsubApiMessage = PubsubMessage
                                        .newBuilder()
                                        .setData(byteStr)
                                        .build();
        try {
            if (publisher == null) {
                publisher = Publisher
                        .newBuilder(ProjectTopicName.of("moneycol",
                                "dev.moneycol.indexer.batches"))
                        .build();
            }

            publisher.publish(pubsubApiMessage).get();
        } catch (InterruptedException | ExecutionException | IOException e) {
            log.error("Error publishing Pub/Sub message: " + e.getMessage(), e);
        }
    }
}

//byte[] content = blob.getContent();
//String json = new String(content);

// this may take a lot of time (>9minutes) so need to consider:
// - CloudRun via CloudScheduler/PubSub
// - GKE job via CloudScheduler/Pubsub:
// https://medium.com/@avish1990/kubernetes-cron-job-with-gcp-pub-sub-cloud-sql-and-batch-jobs-4affca71388c
