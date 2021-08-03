package com.moneycol.indexer.batcher;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Setter
@Getter
class Inventory {

    private final String rootName;
    private final List<FilesBatch> filesBatches = new ArrayList<>();
    public void addFileBatch(FilesBatch filesBatch) {
        filesBatches.add(filesBatch);
    }
}
