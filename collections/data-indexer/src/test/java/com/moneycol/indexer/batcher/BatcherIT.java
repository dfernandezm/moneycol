package com.moneycol.indexer.batcher;

import com.moneycol.indexer.infra.GcsClient;
import com.moneycol.indexer.infra.JsonWriter;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflyCore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@HoverflyCore(config = @HoverflyConfig(destination = {"www.googleapis.com","oauth2.googleapis.com"}))
@ExtendWith(HoverflyExtension.class)
@MicronautTest
public class BatcherIT {

    @Inject
    private GcsClient gcsClient;

    @Test
    public void readsInventoryFromDataPath(Hoverfly hoverfly) {

        // Given
        hoverfly.simulate(SimulationSource.defaultPath("gcs-simulation-success.json"));

        // When
        String json = gcsClient.readObjectContents("moneycol-import","colnect/04-11-2021/inventory.json");
        JsonWriter jsonWriter = new JsonWriter();
        Inventory inventory = jsonWriter.toObject(json, Inventory.class);

        // Then
        assertThat(inventory.getRootName()).isEqualTo("inventory");
    }
}
