package com.moneycol.datacollector.colnect.pages;

import lombok.Builder;
import lombok.ToString;

@ToString
@Builder
public class BanknoteData {
    private String name;
    private Integer year;
    private String country;
}
