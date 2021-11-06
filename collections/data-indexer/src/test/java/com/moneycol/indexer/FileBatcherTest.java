package com.moneycol.indexer;

import com.moneycol.indexer.infra.JsonWriter;
import com.moneycol.indexer.batcher.FilesBatch;
import com.moneycol.indexer.tracker.IntermediateTask;
import com.moneycol.indexer.tracker.FanOutProcessStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FileBatcherTest {

    @Test
    public void serializesDeserializesToJsonFromGenericType() {
        FilesBatch filesBatch = FilesBatch.builder()
                .batchSize(50)
                .processed(false)
                .build();
        filesBatch.addFile("example1.json");
        filesBatch.addFile("example2.json");
        IntermediateTask<FilesBatch> intermediateTask = IntermediateTask.<FilesBatch>builder()
                                            .taskListId("aTaskId")
                                            .status(FanOutProcessStatus.PENDING)
                                            .content(filesBatch)
                                            .build();

        JsonWriter jsonWriter = JsonWriter.builder().build();

        String genericTaskJson = jsonWriter.asJsonString(intermediateTask);
        IntermediateTask<FilesBatch> deserializedTask = jsonWriter.toGenericTask(genericTaskJson);

        assertThat(deserializedTask).isNotNull();
        assertThat(deserializedTask).hasFieldOrPropertyWithValue("status", FanOutProcessStatus.PENDING);
        assertThat(deserializedTask.getContent()).isNotNull();
        assertThat(deserializedTask.getContent().getFilenames()).contains("example1.json", "example2.json");
    }
}
