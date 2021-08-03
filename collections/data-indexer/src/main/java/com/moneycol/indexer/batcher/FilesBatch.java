package com.moneycol.indexer.batcher;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
public
class FilesBatch {

    private Integer batchSize;
    private final Boolean processed;
    private final List<String> filenames = new ArrayList<>();

    public void addFile(String filename) {
        filenames.add(filename);
    }
}
