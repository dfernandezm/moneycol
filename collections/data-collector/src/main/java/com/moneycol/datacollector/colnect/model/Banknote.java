package com.moneycol.datacollector.colnect.model;

import lombok.Builder;

@Builder
public class Banknote {
    private final BanknoteId banknoteId;
    private final CatalogCode catalogCode;
    private final Country country;
    private final BanknoteName name;
    private final Year year;
}
