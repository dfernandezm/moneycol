package com.moneycol.indexer.batcher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DataUri {

    private String bucketName;

    /**
     * Location inside the bucket where a file with crawled data is written/read from
     */
    private String dataUri;
}
