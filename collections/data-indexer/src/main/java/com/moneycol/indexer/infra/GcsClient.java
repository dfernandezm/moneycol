package com.moneycol.indexer.infra;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import jakarta.inject.Singleton;

@Singleton
public class GcsClient {

    private final JsonWriter jsonWriter = JsonWriter.builder().build();

    /**
     * Reads the entire content of 'objectPath' in 'bucketName' into a String in memory
     *
     * @param bucketName the GCS bucket to read from
     * @param objectPath the path of the object within the bucket
     * @return string containing the entire contents of the object under 'objectPath'
     */
    public String readObjectContents(String bucketName, String objectPath) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(bucketName, objectPath);
        Blob blob = storage.get(blobId);
        return new String(blob.getContent());
    }

    /**
     * Converts 'data' do JSON string and writes it to GCS bucket
     *
     * @param bucketName
     * @param objectName
     * @param data
     * @param <T>
     */
    public <T> void  writeToGcs(String bucketName, String objectName, T data) {
        String inventoryJson = jsonWriter.asJsonString(data);
        writeDataToGcs(bucketName, objectName, inventoryJson);
    }

    private void writeDataToGcs(String bucketName, String objectName, String data) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, data.getBytes());
    }

    public Page<Blob> listBucketBlobs(String bucketName, String objectPath) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        return storage.list(bucketName, Storage.BlobListOption.pageSize(250),
                Storage.BlobListOption.prefix(objectPath));
    }
}
