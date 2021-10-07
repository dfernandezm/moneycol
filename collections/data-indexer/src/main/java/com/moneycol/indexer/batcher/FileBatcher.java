package com.moneycol.indexer.batcher;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.moneycol.indexer.infra.GcsClient;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Singleton
@Slf4j
public class FileBatcher {

    private final GcsClient gcsClient;
    private final FanOutConfigurationProperties fanoutConfig;
    private final String INVENTORY_OBJECT_NAME = "inventory.json";
    private final Integer FILES_BATCH_SIZE = 30;

    public FileBatcher(GcsClient gcsClient, FanOutConfigurationProperties fanoutConfig) {
        this.gcsClient = gcsClient;
        this.fanoutConfig = fanoutConfig;
    }

    public Inventory buildAndStoreInventory() {
        Inventory inventory = buildInventory();
        gcsClient.writeToGcs(fanoutConfig.getSourceBucketName(), INVENTORY_OBJECT_NAME, inventory);
        return inventory;
    }

    // Ideally, we shouldn't need to import Blob / PageBlob here
    private Inventory buildInventory() {

        Page<Blob> blobs = gcsClient.listBucketBlobs(fanoutConfig.getSourceBucketName());

        int i = 0;
        int batchSize = FILES_BATCH_SIZE;

        Inventory inventory = Inventory.builder()
                .rootName("inventory")
                .build();

        FilesBatch filesBatch = FilesBatch.builder()
                .batchSize(batchSize)
                .processed(false)
                .build();

        log.info("Building batches...");
        int batchNumber = 1;

        for (Blob blob : blobs.iterateAll()) {
            log.info("Object: {}", blob.getName());
            if (i < batchSize) {
                log.info("Adding to existing batch (#{}) : {}", batchNumber, blob.getName());
                i++;
            } else {
                log.info("Batch {} is finished, setting size and restarting", batchNumber);

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
                batchNumber++;
            }
            filesBatch.addFile(blob.getName());
        }

        // add last batch
        inventory.addFileBatch(filesBatch);
        return inventory;
    }
}
