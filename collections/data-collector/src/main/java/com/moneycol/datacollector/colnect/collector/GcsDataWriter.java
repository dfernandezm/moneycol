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
    private static final String BUCKET_NAME = "moneycol-import";
    private final String OBJECT_NAME_FORMAT = "colnect/%s-%s-p-%s.json";
    private final String STATE_FILE_NAME = "state.json";

    @Override
    public void writeDataBatch(BanknotesDataSet banknotesDataSet) {
        log.info("Writing batch of data into GCS for {}", banknotesDataSet.getCountry());
        log.info("Value of GCP Creds {}", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));

        String jsonData = jsonWriter.asJsonString(banknotesDataSet);

        log.info("About to write banknote batch:\n {}", jsonWriter.prettyPrint(banknotesDataSet));

        String objectName = String.format(OBJECT_NAME_FORMAT,
                banknotesDataSet.getLanguage(),
                banknotesDataSet.getCountry(),
                banknotesDataSet.getPageNumber());
        writeDataToGcs(objectName, jsonData);
    }

    @Override
    public void saveState(CrawlingProcessState crawlingProcessState) {
        log.info("Saving state {}", crawlingProcessState);
        String jsonData = jsonWriter.asJsonString(crawlingProcessState);
        writeDataToGcs(STATE_FILE_NAME, jsonData);
        log.info("Crawling state saved {}", crawlingProcessState);
    }

    @Override
    public CrawlingProcessState findState() {
        //TODO: return null if it does not exist
        String stateJson = readDataFromGcs(STATE_FILE_NAME);
        return jsonWriter.toObject(stateJson);
    }

    public String readDataFromGcs(String objectName) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(BUCKET_NAME, objectName);
        byte[] bytes = storage.readAllBytes(blobId);
        return new String(bytes);
    }

    private void writeDataToGcs(String objectName, String data) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        createBucketIfNeeded(storage);
        BlobId blobId = BlobId.of(BUCKET_NAME, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        // use GCS event to fire cloud function
        storage.create(blobInfo, data.getBytes());
    }

    private void createBucketIfNeeded(Storage storage) {
        try {
            storage.create(BucketInfo.of(BUCKET_NAME));
        } catch (StorageException se) {
            log.debug("Couldn't create bucket {} -- probably it exists already", BUCKET_NAME);
        } catch (Exception e) {
            log.error("Couldn't create bucket", e);
        }
    }
}
