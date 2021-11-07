package com.moneycol.indexer.batcher;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the group of file batches ({@link FilesBatch}) that make up all the files to be processed.
 * In this case it's the whole GCS bucket content, described as multiple JSON files.
 *
 * The ({@link Inventory}) itself is written as a single JSON file at the root and used as the reference
 * to create the individual tasks for the fan-out process (each batch in the inventory is a 'task')
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@RequiredArgsConstructor
public class Inventory {

    @Builder.Default
    private final String rootName = "defaultInventory";
    private final List<FilesBatch> filesBatches = new ArrayList<>();

    public void addFileBatch(FilesBatch filesBatch) {
        filesBatches.add(filesBatch);
    }
}
