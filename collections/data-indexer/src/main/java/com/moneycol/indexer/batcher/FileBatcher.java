package com.moneycol.indexer.batcher;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.moneycol.indexer.infra.GcsClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Singleton
@Slf4j
public class FileBatcher {

    private final GcsClient gcsClient;
    private final String BUCKET_NAME = "moneycol-import";
    private final String INVENTORY_OBJECT_NAME = "inventory.json";
    private final String PROJECT_ID = "moneycol";

    public FileBatcher(GcsClient gcsClient) {
        this.gcsClient = gcsClient;
    }

    public Inventory buildAndStoreInventory() {
        Inventory inventory = buildInventory(PROJECT_ID, BUCKET_NAME);
        gcsClient.writeToGcs(BUCKET_NAME, INVENTORY_OBJECT_NAME, inventory);
        return inventory;
    }

    private Inventory buildInventory(String projectId, String bucketName) {
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

        log.info("Building batches...");

        for (Blob blob : blobs.iterateAll()) {
            log.info("Object: {}", blob.getName());
            if (i < batchSize) {
                log.info("Adding to existing batch: {}", blob.getName());
                i++;
            } else {
                log.info("Batch is finished, setting size and restarting");

                // last batch
                int currentBatchSize = i;
                filesBatch.setBatchSize(currentBatchSize);
                inventory.addFileBatch(filesBatch);

                // restart
                i = 0;
                filesBatch = FilesBatch.builder()
                        .batchSize(batchSize)
                        .processed(false)
                        .build();
            }

            filesBatch.addFile(blob.getName());
        }

        // add last batch
        inventory.addFileBatch(filesBatch);
        return inventory;
    }
}
