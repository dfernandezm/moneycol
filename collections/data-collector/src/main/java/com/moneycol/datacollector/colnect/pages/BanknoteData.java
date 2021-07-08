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
    private String name;
    private Integer year;
    private String country;
    private String catalogCode;
}
