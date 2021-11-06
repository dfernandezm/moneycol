package com.moneycol.indexer.batcher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of files to be processed together, making up an individual 'task', constituent
 * of the initial fan-out process and delegated to workers.
 *
 * In this concrete case this is a group of JSON files to be processed.
 *
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor // for Jackson
@AllArgsConstructor
public class FilesBatch {

    private Integer batchSize;
    private Boolean processed;
    private final List<String> filenames = new ArrayList<>();

    public void addFile(String filename) {
        filenames.add(filename);
    }
}
