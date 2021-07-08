package com.moneycol.datacollector.colnect.collector;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.moneycol.datacollector.colnect.BanknotesDataSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GcsDataWriter implements DataWriter {

    private final JsonWriter jsonWriter = new JsonWriter();

    //https://www.baeldung.com/java-google-cloud-storage
    @Override
    public void writeDataBatch(BanknotesDataSet banknotesDataSet) {
        log.info("Writing batch of data into GCS for {}", banknotesDataSet.getCountry());

        // will get creds from GOOGLE_APPLICATION_CREDENTIALS
        log.info("Value of GCP Creds {}", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
        Storage storage = StorageOptions.getDefaultInstance().getService();
        String bucketName = "moneycol-import";

        try {
            storage.create(BucketInfo.of(bucketName));
        } catch (StorageException se) {
            log.warn("Couldn't create bucket {} -- probably it exists already", bucketName, se);
        } catch (Exception e) {
            log.error("Couldn't create bucket", e);
        }

        String data = jsonWriter.asJsonString(banknotesDataSet);
        String objectName = "colnect-" + banknotesDataSet.getCountry() + "-p-" + banknotesDataSet.getPageNumber() + ".json";
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, data.getBytes());
    }
}
