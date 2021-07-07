package com.moneycol.datacollector.colnect.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

@Slf4j
public class CountrySeriesListing {

    private final String url;
    private final SelenideElement banknotesForCountryListingLink =
            $("div.navigation_box > div > strong:nth-child(3)");
    private final String countryName;

    @Builder
    public CountrySeriesListing(String url, String countryName) {
        this.url = url;
        this.countryName = countryName;
    }

    public void visit() {
        open(url);
    }

    public CountryBanknotesListing visitAllBanknotesListing() {
        //TODO: antigua & barbuda has no link 'banknotesForCountryListingLink'
        log.info("Visiting listing for country {}", countryName);
        banknotesForCountryListingLink.click();
        return new CountryBanknotesListing(countryName, 1);
    }
}
