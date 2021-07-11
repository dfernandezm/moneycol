package com.moneycol.datacollector.colnect.pages;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Builder
@Getter
@Setter
public class BanknoteData {
    private String catalogCode;
    private String series;
    private String name;
    private String year;
    private String country;
    private String faceValue;
    private String score;
    private String description;
    private Boolean hasVariants;
    private String composition;
    private String size;
    private String distribution;
    private String themes;
    private String originalLink;
}
