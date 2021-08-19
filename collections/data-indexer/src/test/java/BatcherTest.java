import com.moneycol.indexer.JsonWriter;
import com.moneycol.indexer.batcher.FilesBatch;
import com.moneycol.indexer.tracker.GenericTask;
import com.moneycol.indexer.tracker.Status;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BatcherTest {

    public void testIterateWritesJsonMoreThanOneBatch() {
    }

    @Test
    public void serializesDeserializesToJsonFromGenericType() {
        FilesBatch filesBatch = FilesBatch.builder()
                .batchSize(50)
                .processed(false)
                .build();
        filesBatch.addFile("example1.json");
        filesBatch.addFile("example2.json");
        GenericTask<FilesBatch> genericTask = GenericTask.<FilesBatch>builder()
                                            .taskListId("aTaskId")
                                            .status(Status.PENDING)
                                            .content(filesBatch)
                                            .build();

        JsonWriter jsonWriter = new JsonWriter();

        String genericTaskJson = jsonWriter.asJsonString(genericTask);
        GenericTask<FilesBatch> deserializedTask = jsonWriter.toGenericTask(genericTaskJson);

        assertThat(deserializedTask).isNotNull();
        assertThat(deserializedTask).hasFieldOrPropertyWithValue("status", Status.PENDING);
        assertThat(deserializedTask.getContent()).isNotNull();
        assertThat(deserializedTask.getContent().getFilenames()).contains("example1.json", "example2.json");
    }
}
