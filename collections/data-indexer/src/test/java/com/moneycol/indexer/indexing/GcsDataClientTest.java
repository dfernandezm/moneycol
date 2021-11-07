package com.moneycol.indexer.indexing;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.moneycol.indexer.infra.GcsClient;
import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflyCore;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@HoverflyCore(config = @HoverflyConfig(destination = {"oauth2.googleapis.com", "storage.googleapis.com"}))
@ExtendWith(HoverflyExtension.class)
@Slf4j
public class GcsDataClientTest {

    @Test
    public void testListingObjectsContainFullPath(Hoverfly hoverfly) {

        // given
        GcsClient gcsClient = new GcsClient();
        hoverfly.simulate(SimulationSource.defaultPath("gcs-subdir-with-full-path.json"));

        // when
        Page<Blob> blobPage = gcsClient.listBucketBlobs("moneycol-import", "colnect/04-11-2021");

        // then
        assertThat(blobPage.getValues()).isNotNull();
        assertThat(blobPage.getValues().iterator().hasNext()).isTrue();
        blobPage.getValues().forEach(blob -> {
            // We should get all the values in the subPath like:
            // colnect/04-11-2021/inventory.json
            // colnect/04-11-2021/en-Spain-p-1.json
            assertThat(blob.getName()).startsWith("colnect/04-11-2021");
            assertThat(blob.getName()).endsWith(".json");
        });
    }

    @Test
    public void testListingEmptyInSubdirectory(Hoverfly hoverfly) {

        // given
        GcsClient gcsClient = new GcsClient();
        hoverfly.simulate(SimulationSource.defaultPath("gcs-list-empty-path.json"));

        // when
        Page<Blob> blobPage = gcsClient.listBucketBlobs("moneycol-import", "colnect/02-11-2021");

        // then
        assertThat(blobPage.getValues()).isNotNull();
        assertThat(blobPage.getValues().iterator().hasNext()).isFalse();
    }
}
