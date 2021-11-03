package com.moneycol.indexer.indexing;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.moneycol.indexer.infra.GcsClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class GcsDataClientTest {

    @Disabled("This test connects directly to GCS - enable to check")
    @Test
    public void testListing() {
        GcsClient gcsClient = new GcsClient();

        Page<Blob> blobPage = gcsClient.listBucketBlobs("moneycol-import", "colnect/02-11-2021");

        assertThat(blobPage.getValues()).isNotNull();
        assertThat(blobPage.getValues().iterator().hasNext()).isTrue();
        blobPage.getValues().forEach(blob -> {
            // We should get all the values in the subPath like:
            // colnect/02-11-2021/inventory.json
            // colnect/02-11-2021/en-Spain-p-1.json
            assertThat(blob.getName()).startsWith("colnect/02-11-2021");
            assertThat(blob.getName()).endsWith(".json");
        });
    }
}
