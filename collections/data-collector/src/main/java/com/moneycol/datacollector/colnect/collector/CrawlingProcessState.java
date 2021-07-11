package com.moneycol.datacollector.colnect.collector;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

//TODO: adapt lombok to Jackson read/write ways

@ToString
@Getter
@Setter
public class CrawlingProcessState {

    public CrawlingProcessState() {}

    @Builder
    public CrawlingProcessState(String seriesUrl, String currentUrl, Integer pageNumber) {
        this.seriesUrl = seriesUrl;
        this.currentUrl = currentUrl;
        this.pageNumber = pageNumber;
    }

    private String seriesUrl;
    private String currentUrl;
    private Integer pageNumber;
}
