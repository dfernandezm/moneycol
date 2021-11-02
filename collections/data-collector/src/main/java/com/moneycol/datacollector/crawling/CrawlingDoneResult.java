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
    private String dataUri;
}
