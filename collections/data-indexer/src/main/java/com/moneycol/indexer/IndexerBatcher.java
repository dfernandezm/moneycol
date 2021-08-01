package com.moneycol.indexer;

import com.google.api.gax.paging.Page;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.micronaut.gcp.function.GoogleFunctionInitializer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class IndexerBatcher extends GoogleFunctionInitializer
        implements BackgroundFunction<PubSubMessage> {

    @Inject
    LoggingService loggingService;

    @Override
    public void accept(PubSubMessage message, Context context) {
        log.info("Function called with context {}", context);

//        if (message != null && message.getData() != null) {
//            String data = new String(
//                    Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
//                    StandardCharsets.UTF_8);
//            log.info("Data received {}", data);
//        }

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
                int currentBatchSize = i + 1;
                filesBatch.setBatchSize(currentBatchSize);
                inventory.addFileBatch(filesBatch);
                filesBatch = FilesBatch.builder()
                        .batchSize(batchSize)
                        .processed(false)
                        .build();
                // reset counter
                i = 0;
            }

            filesBatch.addFile(blob.getName());
        }

        // add last batch
        inventory.addFileBatch(filesBatch);

        writeToGcs(inventory);
    }

    private void writeToGcs(Inventory inventory) {

    }
}

@Builder
@Setter
class FilesBatch {
    private Integer batchSize;
    private final Boolean processed;
    private final List<String> filenames = new ArrayList<>();

    public void addFile(String filename) {
        filenames.add(filename);
    }
}

@Builder
@Setter
@Getter
class Inventory {

    private final String rootName;
    private final List<FilesBatch> filesBatches = new ArrayList<>();
    public void addFileBatch(FilesBatch filesBatch) {
        filesBatches.add(filesBatch);
    }
}

class PubSubMessage {

    String data;
    Map<String, String> attributes;
    String messageId;
    String publishTime;
}

@Slf4j
@Singleton
class LoggingService {

    void logMessage(PubSubMessage message) {
        log.info("Received message {}", message);
    }
}

//byte[] content = blob.getContent();
//String json = new String(content);

// this may take a lot of time (>9minutes) so need to consider:
// - CloudRun via CloudScheduler/PubSub
// - GKE job via CloudScheduler/Pubsub:
// https://medium.com/@avish1990/kubernetes-cron-job-with-gcp-pub-sub-cloud-sql-and-batch-jobs-4affca71388c
