package com.moneycol.indexer.worker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor // for Jackson
@AllArgsConstructor
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
