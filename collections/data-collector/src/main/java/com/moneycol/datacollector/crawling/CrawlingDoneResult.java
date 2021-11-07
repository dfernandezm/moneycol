package com.moneycol.datacollector.crawling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlingDoneResult {

    private String doneMessage;

    /**
     * This is the path within the bucket where data is stored for this crawler run
     * (i.e. colnect/01-11-2021) see BatcherFunctionTest
     */
    private String dataUri;
}
