package com.moneycol.indexer;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class GcsClient {

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
}
