package com.moneycol.datacollector.colnect.collector;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@ToString
@Getter
@Setter
public class CrawlingProcessState {
    private final String seriesUrl;
    private final String countryListingUrl;
    private final Integer pageNumber;
}
