package com.moneycol.indexer.batcher;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the group of file batches that make up the entire set to be processed.
 * In this case it's the whole GCS bucket content.
 * It is written as a JSON file at the root and used as an 'index' to create the individual
 * tasks of the fan-out process (each batch is a 'task')
 */
@Builder
@Setter
@Getter
public
class Inventory {

    private final String rootName;
    private final List<FilesBatch> filesBatches = new ArrayList<>();

    public void addFileBatch(FilesBatch filesBatch) {
        filesBatches.add(filesBatch);
    }
}
