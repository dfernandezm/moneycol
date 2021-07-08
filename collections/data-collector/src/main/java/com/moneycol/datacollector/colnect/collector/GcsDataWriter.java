package com.moneycol.datacollector.colnect.collector;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.moneycol.datacollector.colnect.BanknotesDataSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GcsDataWriter implements DataWriter {

    //https://www.baeldung.com/java-google-cloud-storage
    @Override
    public void writeDataBatch(BanknotesDataSet banknotesDataSet) {

        // will get creds from GOOGLE_APPLICATION_CREDENTIALS
        Storage storage = StorageOptions.getDefaultInstance().getService();
        String bucketName = "moneycol-import";
        Bucket bucket = null;

        try {
            bucket = storage.create(BucketInfo.of(bucketName));
        } catch(StorageException se) {
            log.warn("Couldn't create bucket {} -- probably exists", bucketName, se);
        }

        //TODO: write json to object
    }
}
